package com.efonian.cassandra.discord.command;

import com.efonian.cassandra.discord.event.EventListenerManager;
import com.efonian.cassandra.misc.DaemonThreadFactory;
import com.efonian.cassandra.util.UtilRuntime;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * The {@code CommandManager} is responsible for processing, and validating incoming command calls, and starting
 * the task to execute the commands
 */
@Component
@ConditionalOnProperty(value = "cassandra.discord.init", havingValue = "true")
public final class CommandManager {
    private static final Logger logger = LoggerFactory.getLogger(CommandManager.class);
    
    private static final String PREFIX = "!";
    
    @Value("${cassandra.discord.command.default.lifetime}")
    private long defaultCommandLifetime;
    private TimeUnit defaultCommandTimeUnit = TimeUnit.SECONDS;
    
    private Map<List<String>, Command> registeredCommands = new ConcurrentHashMap<>();
    private EventListenerManager eventListenerManager;
    
    private ExecutorService executors = Executors.newFixedThreadPool(4, new DaemonThreadFactory());
    private Map<Long, Command> commandsQueue = new ConcurrentHashMap<>();
    
    
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
        eventListenerManager.registerOperator(MessageReceivedEvent.class, event -> {
            handleMessageInput(event);
            return false;
        });
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
        // remember to remove if too much
        if(event.getAuthor().getIdLong() != event.getJDA().getSelfUser().getIdLong())
            logMessage(event);
        
        // Prefix check
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
                event.getChannelType().isGuild() ? "guild (" + event.getGuild().getName() + "), channel (" + event.getChannel().getName() + ")" : "private channel",
                event.getMessage().getAttachments().isEmpty() ? "" : " with " + event.getMessage().getAttachments().size() + "attachments",
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
        if(!CommandAccessManager.hasPermission(cc, command).getOrElseGet((msg) -> {
            cc.event.getChannel().sendMessage(
                    cc.event.getAuthor().getAsMention() + " Unable to request command: " + msg).queue();
            return false;
        })) return false;
        
        // Multiple command requests check
        if(commandsQueue.containsKey(cc.event.getAuthor().getIdLong()) && !CommandAccessManager.canRequestMulti(cc.event.getAuthor().getIdLong())) {
            cc.event.getChannel().sendMessage(cc.event.getAuthor().getAsMention() + " Busy working on command " +
                    commandsQueue.get(cc.event.getAuthor().getIdLong()).invokes().get(0)).queue();
            return false;
        }
        
        return true;
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
    
    /**
     * This method only starts the execution of the command, but does not check whether the command should be executed;
     * please check for user/member permission and previous usage before call.
     *
     * This method will use reflection to create a copy of the command, which is a bit slow, but really cool, so eh.
     * A new CompletableFuture will be created which will execute the command asynchronously, and the execution method
     * will return {@code true} if the command is no longer active or finished, in which case it will be removed from a
     * list of active commands and garbage collected. The command may also return {@code false} in which case it will be
     * kept around until later, for cases such as commands with multiple stages requiring user input. Before the execute
     * returns {@code true} or {@code false}, it may timeout or complete exceptionally, in which case it will be logged
     * and removed to be garbage collected. The timeout is linked with the thread which executes it; if the command
     * requires more time, simply start another thread and return false. Do not unnecessary holt the thread.
     *
     * IMPORTANT: Do not await for response from external components in an command executor thread
     *
     * @param cmd   The command which is to be executed
     * @param cc    The details of the command parameters and such in a packaged container
     */
    private void startTask(Command cmd, CommandContainer cc) {
        try {
            long client = cc.event.getAuthor().getIdLong();
            Command cmdCopy = cmd.getClass().getConstructor().newInstance();
            logger.info("Accepted command of class " + cmd.getClass().getSimpleName());
            
            CompletableFuture
                    .supplyAsync(() -> cmdCopy.execute(cc), executors)
                    // commented out since the default value for the below method is precomputed, uncomment if the API ever gives a lazy option
//                    .completeOnTimeout(handleTimeout(cmdCopy, cc, client), defaultCommandLifetime, defaultCommandTimeUnit)
                    .orTimeout(defaultCommandLifetime, defaultCommandTimeUnit)
                    .thenAcceptAsync(result -> handleCommandFirstExecutionComplete(result, cmdCopy, cc, client))
                    .exceptionallyAsync(throwable -> handleCommandException(throwable.getCause(), cmdCopy, cc, client));
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            logger.warn("An exception occurred when creating a new instance of " + cmd.getClass().getName());
            e.printStackTrace();
        }
    }
    
    private void handleCommandFirstExecutionComplete(boolean result, Command cmd, CommandContainer cc, long client) {
        if (!result) {
            logger.info(String.format("Completed command %s for (%s)", cmd.getClass().getSimpleName(), cc.event.getAuthor().getName()));
        } else {
            logger.info(String.format("Completed command %s for (%s)", cmd.getClass().getSimpleName(), cc.event.getAuthor().getName()));
            logger.info("Commands left in progress: " + commandsQueue.size());
            commandsQueue.put(client, cmd);
        }
    }
    
    // see comment in the startTask method
//    private boolean handleTimeout(Command cmd, CommandContainer cc, long client) {
//        logger.info(String.format("Timed out completing command %s for (%s)",
//                cmd.getClass().getSimpleName(),
//                cc.event.getAuthor().getName()
//        ));
//        cc.event.getChannel().sendMessage(String.format("%s Timed out on task", cc.event.getAuthor().getAsMention())).queue();
//        commandsQueue.remove(client);
//        return true;
//    }
    
    private void handleTimeout(Command cmd, CommandContainer cc, long client) {
        logger.info(String.format("Timed out completing command %s for (%s)",
                cmd.getClass().getSimpleName(),
                cc.event.getAuthor().getName()
        ));
        cc.event.getChannel().sendMessage(String.format("%s Timed out on task", cc.event.getAuthor().getAsMention())).queue();
        commandsQueue.remove(client);
    }
    
    private Void handleCommandException(Throwable throwable, Command cmd, CommandContainer cc, long client) {
        if(throwable.getClass().equals(RuntimeException.class))
            throwable = throwable.getCause();
        
        // see comment in the startTask method
        if(throwable instanceof TimeoutException) {
            handleTimeout(cmd, cc, client);
            return null;
        } else if(throwable instanceof IllegalArgumentException) {
            Command.sendHelpMessage(cc.event.getChannel(), cmd);
            logger.info(String.format("Illegal arguments for command %s from (%s)",
                    cmd.getClass().getSimpleName(),
                    cc.event.getAuthor().getName()
            ));
            commandsQueue.remove(client);
            return null;
        }
    
        cc.event.getChannel().sendMessage(String.format("%s Unable to complete task", cc.event.getAuthor().getAsMention())).queue();
        logger.info(String.format("Failed completing command %s for (%s) with exception %s",
                cmd.getClass().getSimpleName(),
                cc.event.getAuthor().getName(),
                throwable.getClass().getName()
        ));
        logger.info(throwable.getMessage());
//        throwable.printStackTrace();
        commandsQueue.remove(client);
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
        commandsQueue.values().forEach(Command::shutdown);
        UtilRuntime.shutDownExecutorService(executors);
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
