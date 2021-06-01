package com.efonian.cassandra.discord.commands;

import com.efonian.cassandra.discord.commands.annotation.DeclareCommandAccessLevel;
import com.efonian.cassandra.util.UtilRuntime;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;

@Component
@DeclareCommandAccessLevel(accessLevel = CommandAccessLevel.ADMIN)
class cmd_Info extends Command {
    @Override
    void execute(CommandContainer cc) {
        cc.event.getChannel().sendMessage(simpleEmbedBuilder("Info")
                .addField("Start time", DateTimeFormatter
                        .ofLocalizedDate(FormatStyle.FULL)
                        .withLocale(Locale.CANADA)
                        .withZone(ZoneId.systemDefault())
                        .format(UtilRuntime.getStartTime()), false)
                .addField("Hmm", "Yeah I'm not tracking anything else right now", false)
                .build()).queue();
    }
    
    @Override
    List<String> invokes() {
        return List.of("info");
    }
    
    @Override
    String description() {
        return "displays information on the bot";
    }
}
