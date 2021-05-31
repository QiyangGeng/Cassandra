package com.efonian.cassandra.discord.commands;

import com.efonian.cassandra.discord.commands.annotation.DeclareCommandAccessLevel;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(value = "cassandra.discord.init", havingValue = "true")
@DeclareCommandAccessLevel
public class cmd_Ping extends Command {
    @Override
    boolean execute(CommandContainer cc) {
        cc.event.getChannel().sendMessage("_ _").queue(message ->
                cc.event.getChannel().editMessageById(message.getIdLong(), new EmbedBuilder() {{
            setTitle("Pong!");
            setDescription(":stopwatch: " + (message.getTimeCreated().toInstant().toEpochMilli()
                    - cc.event.getMessage().getTimeCreated().toInstant().toEpochMilli()) + "ms");
            setColor(0xFF40E0D0);
        }}.build()).queue());
        return false;
    }
    
    @Override
    List<String> invokes() {
        return List.of("ping");
    }
    
    @Override
    public String description() {
        return "pong (shows the ping)";
    }
}
