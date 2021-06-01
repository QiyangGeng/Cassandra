package com.efonian.cassandra.discord.commands;

import com.efonian.cassandra.discord.commands.annotation.DeclareCommandAccessLevel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(value = "cassandra.discord.init", havingValue = "true")
@DeclareCommandAccessLevel(accessLevel=CommandAccessLevel.FULL)
class cmd_Exception extends Command {
    @Override
    void execute(CommandContainer cc) {
        throw new RuntimeException("Exception thrown from exception command");
    }
    
    @Override
    List<String> invokes() {
        return List.of("exception");
    }
    
    @Override
    String description() {
        return "throws a RunTimeException";
    }
}
