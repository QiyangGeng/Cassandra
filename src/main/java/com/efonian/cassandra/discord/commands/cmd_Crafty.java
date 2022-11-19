package com.efonian.cassandra.discord.commands;

import com.efonian.cassandra.discord.commands.annotation.DeclareCommandAccessLevel;
import com.efonian.cassandra.discord.commands.annotation.Disabled;
import org.springframework.stereotype.Component;

import java.util.List;

// TODO: non-full AL has access to info, o/w everything
@Disabled
@Component
@DeclareCommandAccessLevel(accessLevel = CommandAccessLevel.ADMIN)
class cmd_Crafty extends Command {
    @Override
    void execute(CommandContainer cc) {
    
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
