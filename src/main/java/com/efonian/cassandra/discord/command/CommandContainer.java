package com.efonian.cassandra.discord.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A container which stores parsed information from a received message in a form useful to commands
 */
public class CommandContainer {
    private static final Pattern argPattern = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");
    
    MessageReceivedEvent event;
    String invoke;
    List<String> args;
    
    /**
     * Simple constructor which stores the parameters within this object
     * @param invoke    The invoke used to call the command
     * @param args      The arguments to the command call
     * @param event     The event itself
     */
    private CommandContainer(String invoke, List<String> args, MessageReceivedEvent event) {
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
        List<String> split = new ArrayList<>();
        Matcher m = argPattern.matcher(event.getMessage().getContentRaw().replaceFirst(prefix, "").trim()
                .replaceAll("\\*", ""));
        while(m.find())
            split.add(m.group(1).replace("\"", ""));
        String invoke = split.get(0);
        List<String> args;
        if(split.size() > 1)
            args = split.subList(1, split.size());
        else
            args = new ArrayList<>();
        return new CommandContainer(invoke, args, event);
    }
}
