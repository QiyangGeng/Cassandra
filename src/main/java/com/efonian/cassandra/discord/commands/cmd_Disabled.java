package com.efonian.cassandra.discord.commands;

import com.efonian.cassandra.discord.commands.annotation.DeclareCommandAccessLevel;
import com.efonian.cassandra.discord.commands.annotation.Disabled;
import org.springframework.stereotype.Component;

import java.util.List;

@Disabled
@Component
@DeclareCommandAccessLevel(accessLevel = CommandAccessLevel.FULL)
class cmd_Disabled extends Command {
    @Override
    void execute(CommandContainer cc) {
        cc.event.getChannel().sendMessage("what").queue();
    }
    
    @Override
    List<String> invokes() {
        return null;
    }
    
    @Override
    String description() {
        return "you should not see this";
    }
}
