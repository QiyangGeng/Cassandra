package com.efonian.cassandra.discord.command;

import com.efonian.cassandra.discord.command.annotation.DeclareCommandAccessLevel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(value = "cassandra.discord.init", havingValue = "true")
@DeclareCommandAccessLevel(accessLevel=CommandAccessLevel.FULL)
public class cmd_Exception extends Command {
    @Override
    boolean execute(CommandContainer cc) {
        throw new RuntimeException("Exception thrown from exception command");
    }
    
    @Override
    List<String> invokes() {
        return List.of("exception");
    }
    
    @Override
    public String description() {
        return "throws a RunTimeException";
    }
}
