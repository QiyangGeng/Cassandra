package com.efonian.cassandra.discord.commands;

import com.efonian.cassandra.discord.commands.annotation.DeclareCommandAccessLevel;
import com.efonian.cassandra.util.UtilRuntime;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.stereotype.Component;

import java.time.Instant;
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
        cc.event.getChannel().sendMessage(new EmbedBuilder() {{
            setTitle(getSelfName());
            setThumbnail(getSelfAvatar());
            
            addField("Start time", DateTimeFormatter
                    .ofLocalizedDate(FormatStyle.FULL)
                    .withLocale(Locale.CANADA)
                    .withZone(ZoneId.systemDefault())
                    .format(UtilRuntime.getStartTime()), false);
            addField("Hmm", "Yeah I'm not tracking anything else right now", false);
            
            setTimestamp(Instant.now());
        }}.build()).queue();
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
