package com.efonian.cassandra.discord.command;

import com.efonian.cassandra.discord.command.annotation.DeclareCommandAccessLevel;
import com.efonian.cassandra.discord.command.annotation.Disabled;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Disabled
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@DeclareCommandAccessLevel(accessLevel = CommandAccessLevel.FULL)
public class cmd_Disabled extends Command {
    @Override
    boolean execute(CommandContainer cc) {
        cc.event.getChannel().sendMessage("what").queue();
        return false;
    }
    
    @Override
    List<String> invokes() {
        return null;
    }
    
    @Override
    public String description() {
        return "you should not see this";
    }
}
