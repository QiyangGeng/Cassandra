package com.efonian.cassandra.discord.commands;

import com.efonian.cassandra.discord.commands.annotation.DeclareCommandAccessLevel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(value = "cassandra.discord.init", havingValue = "true")
@DeclareCommandAccessLevel
class cmd_Ping extends Command {
    @Override
    void execute(CommandContainer cc) {
        cc.event.getChannel().sendMessage("_ _").queue(message ->
                cc.event.getChannel().editMessageById(message.getIdLong(), simpleEmbedBuilder("Pong!")
                        .setDescription(":stopwatch: " + (message.getTimeCreated().toInstant().toEpochMilli()
                                - cc.event.getMessage().getTimeCreated().toInstant().toEpochMilli()) + "ms")
                        .build()).queue());
    }
    
    @Override
    List<String> invokes() {
        return List.of("ping");
    }
    
    @Override
    String description() {
        return "pong (shows the ping)";
    }
}
