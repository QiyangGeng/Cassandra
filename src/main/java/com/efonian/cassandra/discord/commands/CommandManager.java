package com.efonian.cassandra.discord.commands;

import com.efonian.cassandra.discord.commands.annotation.Cooldown;
import com.efonian.cassandra.discord.event.EventListenerManager;
import com.efonian.cassandra.misc.DaemonThreadFactory;
import com.efonian.cassandra.util.UtilRuntime;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * The {@code CommandManager} is responsible for processing, and validating incoming command calls, and starting
 * the task to execute the commands
 */
@Component
public final class CommandManager {
    private static final Logger logger = LoggerFactory.getLogger(CommandManager.class);
    private static final ThreadFactory DAEMON_THREAD_FACTORY = new DaemonThreadFactory();
    
    private static final String PREFIX = "!";
    
    @Value("${cassandra.discord.commands.default.lifetime}")
    private long defaultCommandLifetime;
    private final TimeUnit defaultCommandTimeUnit = TimeUnit.SECONDS;
    
    private final Map<List<String>, Command> registeredCommands = new ConcurrentHashMap<>();
    private final ExecutorService executors = Executors.newFixedThreadPool(4, DAEMON_THREAD_FACTORY);
    
    private EventListenerManager eventListenerManager;
    
    private final ScheduledExecutorService cooldownRemovalService =
            Executors.newSingleThreadScheduledExecutor(DAEMON_THREAD_FACTORY);
    private final ConcurrentHashMap<Long, ScheduledFuture<?>> usersOnCooldown = new ConcurrentHashMap<>();
    
    
    /**
     * Initialization method that is automatically called. Completes the following:
     *      - registers message listener
     */
    @PostConstruct
    private void init() {
        registerMessageListener();
    }
    
    /**
     * Registers a message listener and direct received messages to {@code handleMessageInput}
     */
    private void registerMessageListener() {
        eventListenerManager.registerOperator(MessageReceivedEvent.class, this::handleMessageInput);
    }
    
    /**
     * Handles a received message. Completes the following:
     *      - logs the message
     *      - Validate the message
     *      - Validate the request
     *      - If the request is valid, queue the request
     * @param event     The {@code MessageReceivedEvent}
     */
    private void handleMessageInput(MessageReceivedEvent event) {
        if(event.getAuthor().getIdLong() != event.getJDA().getSelfUser().getIdLong())
            logMessage(event);
        
        // Command prefix check
        if(!event.getMessage().getContentRaw().startsWith(PREFIX))
            return;
        
        // Looks for the wanted command and start a new task
        CommandContainer cc = CommandContainer.parse(PREFIX, event);
        Command requestedCommand = findCommand(cc.invoke);
        
        // the startTask message trusts validation by this block
        if(validateRequest(cc, requestedCommand))
            startTask(requestedCommand, cc);
    }
    
    /**
     * Logs information about a received message, including
     *      - Author of the message
     *      - Channel and Guild name if the message was sent from guild
     *      - Raw content of the message
     *      - The number of attachments, if the message contains any
     * @param event     The {@code MessageReceivedEvent}
     */
    private static void logMessage(MessageReceivedEvent event) {
        logger.info(String.format(
                "Received message from (%s) in %s%s: \"%s\"",
                event.getAuthor().getName(),
                event.getChannelType().isGuild() ?
                        "guild (" + event.getGuild().getName() + "), channel (" + event.getChannel().getName() + ")" :
                        "private channel",
                event.getMessage().getAttachments().isEmpty() ?
                        "" :
                        " with " + event.getMessage().getAttachments().size() + "attachments",
                event.getMessage().getContentRaw()));
    }
    
    /**
     * Validates whether the command request should be granted by the manager, also handles printing
     * @return  true    if the request should be granted, false otherwise
     */
    private boolean validateRequest(CommandContainer cc, Command command) {
        // Null check
        if(command == null)
            return false;
        
        // Permission check
        return CommandAccessManager.hasPermission(cc, command).getOrElseGet((msg) -> {
            cc.event.getChannel().sendMessage(
                    cc.event.getAuthor().getAsMention() + " Unable to request command: " + msg).queue();
            return false;
        });
    }
    
    /**
     * Finds a command given a potential invoke
     * @param call  The potential invoke
     * @return      The command, if the invoke matches with any, else {@code null}
     */
    private Command findCommand(String call) {
        for(List<String> validInvokes : registeredCommands.keySet()) {
            if(validInvokes.stream().anyMatch(invoke -> invoke.equalsIgnoreCase(call)))
                return registeredCommands.get(validInvokes);
        }
        return null;
    }
    
    private void startTask(Command cmd, CommandContainer cc) {
        logger.info("Accepted command of class " + cmd.getClass().getSimpleName());
    
        CompletableFuture
                .runAsync(() -> cmd.execute(cc), executors)
                .orTimeout(defaultCommandLifetime, defaultCommandTimeUnit)
                .thenAcceptAsync(result -> handleCommandFirstExecutionComplete(cmd, cc))
                .exceptionallyAsync(throwable -> handleCommandException(throwable.getCause(), cmd, cc));
        
        // TODO: test cooldown system
        putUserOnCooldown(cc.event.getAuthor().getIdLong(), cmd);
    }
    
    private void putUserOnCooldown(long user, Command cmd) {
        Class<? extends Command> cla = cmd.getClass();
        
        if(!cla.isAnnotationPresent(Cooldown.class))
            return;
        
        if(CommandAccessManager.checkUserAccessLevel(user, CommandAccessLevel.MODERATOR))
            return;
        
        int cooldown = cla.getAnnotation(Cooldown.class).cooldown();
        if(cooldown > 0)
            usersOnCooldown.put(user,
                    cooldownRemovalService.schedule(() -> usersOnCooldown.remove(user), cooldown, TimeUnit.SECONDS));
    }
    
    private void putUserOffCooldown(long user) {
        ScheduledFuture<?> task = usersOnCooldown.remove(user);
        if(task != null)
            task.cancel(false);
    }
    
    private void handleCommandFirstExecutionComplete(Command cmd, CommandContainer cc) {
        logger.info(String.format("Completed command %s for (%s)",
                cmd.getClass().getSimpleName(), cc.event.getAuthor().getName()));
    }
    
    private void handleTimeout(Command cmd, CommandContainer cc) {
        logger.info(String.format("Timed out completing command %s for (%s)",
                cmd.getClass().getSimpleName(),
                cc.event.getAuthor().getName()
        ));
        cc.event.getChannel().sendMessage(String.format("%s timed out on task",
                cc.event.getAuthor().getAsMention())).queue();
    }
    
    private Void handleCommandException(Throwable throwable, Command cmd, CommandContainer cc) {
        putUserOffCooldown(cc.event.getAuthor().getIdLong());
        
        if(throwable.getClass().equals(RuntimeException.class)) {
            if(throwable.getCause() != null)
                throwable = throwable.getCause();
        }
        
        if(throwable instanceof TimeoutException) {
            handleTimeout(cmd, cc);
            return null;
        }
        
        if(throwable instanceof IllegalArgumentException) {
            Command.sendHelpMessage(cc.event.getChannel(), cmd);
            logger.info(String.format("Illegal arguments for command %s from (%s)",
                    cmd.getClass().getSimpleName(),
                    cc.event.getAuthor().getName()
            ));
            throwable.printStackTrace();
            return null;
        }
        
        // General exceptions
        cc.event.getChannel().sendMessage(String.format("%s Unable to complete task",
                cc.event.getAuthor().getAsMention())).queue();
        logger.info(String.format("Failed completing command %s for (%s) with exception %s",
                cmd.getClass().getSimpleName(),
                cc.event.getAuthor().getName(),
                throwable.getClass().getName()
        ));
        logger.info("Exception message: " + throwable.getMessage());
        throwable.printStackTrace();
        return null;
    }
    
    /**
     * Register a command, should only be called by the {@code Command.java} superclass.
     * @param invokes   A list of invokes the command will accept that are <em>not</em> case sensitive
     * @param command   The command object to be registered
     */
    void registerCommand(List<String> invokes, Command command) {
        registeredCommands.put(invokes, command);
    }
    
    /**
     * Termination method which is automatically called. Completes the following:
     *      - shuts down the commands executor service
     */
    @PreDestroy
    private void shutdown() {
        UtilRuntime.shutDownExecutorService(executors);
        UtilRuntime.shutDownExecutorService(cooldownRemovalService);
    }
    
    /**
     * Simple autowired setter method which sets the EventListenerManager
     * @param eventListenerManager  The event listener manager bean
     */
    @Autowired
    private void setEventListenerManager(EventListenerManager eventListenerManager) {
        this.eventListenerManager = eventListenerManager;
    }
}
