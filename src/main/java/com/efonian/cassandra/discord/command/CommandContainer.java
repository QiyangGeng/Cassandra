package com.efonian.cassandra.discord.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;

/**
 * A container which stores parsed information from a received message in a form useful to commands
 */
public class CommandContainer {
    MessageReceivedEvent event;
    String invoke;
    String[] args;
    
    /**
     * Simple constructor which stores the parameters within this object
     * @param invoke    The invoke used to call the command
     * @param args      The arguments to the command call
     * @param event     The event itself
     */
    private CommandContainer(String invoke, String[] args, MessageReceivedEvent event) {
        this.invoke = invoke;
        this.args = args;
        this.event = event;
    }
    
    // TODO: parser should group strings in quotes unless escaped
    /**
     * Parses a received message into a form useful for command execution
     * @param prefix    The command prefix of the environment from which the message was sent
     * @param event     The {@code MessageReceivedEvent}
     * @return          A {@code CommandContainer} with parsed information from the message
     */
    static CommandContainer parse(String prefix, MessageReceivedEvent event) {
        String[] split = event.getMessage().getContentRaw().replaceFirst(prefix, "").trim()
                .replaceAll("\\*", "").split(" ");
        String invoke = split[0];
        String[] args;
        if(split.length > 1)
            args = Arrays.copyOfRange(split, 1, split.length);
        else
            args = new String[0];
        return new CommandContainer(invoke, args, event);
    }
}
