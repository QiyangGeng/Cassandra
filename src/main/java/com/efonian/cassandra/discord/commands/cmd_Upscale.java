package com.efonian.cassandra.discord.commands;

import com.efonian.cassandra.discord.commands.annotation.Cooldown;
import com.efonian.cassandra.discord.commands.annotation.DeclareCommandAccessLevel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Cooldown(cooldown = 30)
@DeclareCommandAccessLevel(accessLevel = CommandAccessLevel.TRUSTED)
public class cmd_Upscale extends Command {
    
    @Override
    void execute(CommandContainer cc) {
    
    }
    
    @Override
    List<String> invokes() {
        return List.of("upscale");
    }
    
    @Override
    String description() {
        return null;
    }
}
