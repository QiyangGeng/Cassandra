package com.efonian.cassandra.discord.command;

import com.efonian.cassandra.discord.DiscordBot;
import com.efonian.cassandra.discord.command.annotation.Disabled;
import com.efonian.cassandra.util.UtilImage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.List;


/**
 * A command executes a task with given parameters
 */
public abstract class Command {
    private static final Logger logger = LoggerFactory.getLogger(Command.class);
    
    private CommandManager manager;
    
    /**
     * Sets up each command and register it with the Command manager, an alternative to autowiring such as done in the
     * {@code CommandAccessManager}
     */
    @PostConstruct
    private void init() {
        if(this.getClass().isAnnotationPresent(Disabled.class))     return;
        
        setup();
        manager.registerCommand(invokes(), this);
    }
    
    /**
     * Simple setup method, commands can overwrite to initialize/setup without using another
     * <c>@PostConstruct</c> method. Override if needed to set up stuff, such as opening files, etc. While Spring
     * seems to support multiples of such methods, it is probably best not to rely on that.
     */
    protected void setup() {
    
    }
    
    /**
     * A method to mirror the <c>setup</c> method for shutting down, override if needed
     */
    @PreDestroy
    protected void shutdown() {}
    
    // TODO: invert the return to be true to persist
    /**
     * Abstract execution function of a command
     * Note: due to the way the CommandManager is implemented, commands can just throw an {@code IllegalArgumentException}
     * and its help message will be displayed to the user
     * @param cc    The container with command information
     * @return      Whether the command has finished execution, return false only if the command is awaiting further
     *              input from the user. When false is returned, the command will be kept in memory; otherwise, it
     *              will be left to the garbage collector. The next call to the command will likely be on a different
     *              thread.
     */
    abstract boolean execute(CommandContainer cc);
    
    /**
     * Use List.of() to create an immutable list of invokes.
     * @return an immutable list of accepted invokes for this command.
     */
    abstract List<String> invokes();
    
    /**
     * For consistency, use lowercase first letter and no full-stop at the end.
     * @return a description of what this command does
     */
    abstract String description();
    
    /**
     * Allows the command to dynamically assign itself an access level based on the context of the event.
     */
    CommandAccessLevel dynamicallyAssignAccessLevel(CommandContainer cc) {
        throw new RuntimeException("Called dynamic Assign method of Command class");
    }
    
    public static void sendHelpMessage(MessageChannel channel, Command command) {
        channel.sendMessage(new EmbedBuilder() {{
            setTitle(command.getClass().getSimpleName().substring(4));
            setDescription(command.description());
            setThumbnail(getSelfAvatar());
            setTimestamp(Instant.now());
            setAuthor(getSelfName());
        }}.build()).queue();
    }
    
    public static void sendImage(MessageChannel channel, String message, BufferedImage image, String name, String formatName) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, formatName, bytes);
            bytes.close();
            channel.sendMessage(message).addFile(bytes.toByteArray(), name + "." + formatName).queue();
        } catch(IOException e) {
            logger.warn("IOException when trying to write BufferedImage to ByteArrayOutputStream, or closing stream: " + e.getMessage());
        }
    }
    
    protected static void sendImage(MessageChannel channel, String message, BufferedImage image, String name) {
        sendImage(channel, message, image, name, "png");
    }
    
    protected static void sendImage(MessageChannel channel, BufferedImage image, String name, String formatName) {
        sendImage(channel, "_ _", image, name, formatName);
    }
    
    protected static void sendImage(MessageChannel channel, BufferedImage image, String name) {
        sendImage(channel, image, name, "png");
    }
    
    protected static Color userAvatarAverageColour(User user) {
        if(user.equals(getSelfUser()))
            return getSelfAvatarAverageColour();
        
        try {
            return UtilImage.averageColor(new URL(user.getEffectiveAvatarUrl()));
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    // For the lazy...
    protected static long getSelfId() {
        return DiscordBot.getSelfId();
    }
    
    protected static String getSelfName() {
        return DiscordBot.getSelfName();
    }
    
    protected static User getSelfUser() {
        return DiscordBot.getSelfUser();
    }
    
    protected static String getSelfAvatar() {
        return DiscordBot.getSelfAvatar();
    }
    
    protected static Color getSelfAvatarAverageColour() {
        return DiscordBot.getSelfAvatarAverageColour();
    }
    
    @Autowired
    private void setCommandManager(CommandManager manager) {
        this.manager = manager;
    }
}
