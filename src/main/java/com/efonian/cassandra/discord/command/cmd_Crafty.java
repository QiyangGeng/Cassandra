package com.efonian.cassandra.discord.command;

import com.efonian.cassandra.discord.command.annotation.DeclareCommandAccessLevel;

import java.util.List;

// TODO: non-full AL has access to info, o/w everything
@DeclareCommandAccessLevel(accessLevel = CommandAccessLevel.DYNAMIC)
public class cmd_Crafty extends Command {
    @Override
    boolean execute(CommandContainer cc) {
        return false;
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
