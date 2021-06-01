package com.efonian.cassandra.discord.commands;

import com.efonian.cassandra.discord.commands.annotation.DeclareCommandAccessLevel;
import com.efonian.cassandra.discord.commands.annotation.Disabled;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

// TODO: non-full AL has access to info, o/w everything
@Disabled
@Component
@ConditionalOnProperty(value = "cassandra.discord.init", havingValue = "true")
@DeclareCommandAccessLevel(accessLevel = CommandAccessLevel.DYNAMIC)
public class cmd_Crafty extends Command {
    @Override
    void execute(CommandContainer cc) {
    
    }
    
    @Override
    CommandAccessLevel dynamicallyAssignAccessLevel(CommandContainer cc) {
        return super.dynamicallyAssignAccessLevel(cc);
    }
    
    @Override
    List<String> invokes() {
        return null;
    }
    
    @Override
    String description() {
        return null;
    }
}
