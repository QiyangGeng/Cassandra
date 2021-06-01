package com.efonian.cassandra.discord.commands;

import com.efonian.cassandra.discord.DiscordBot;
import com.efonian.cassandra.discord.commands.annotation.DeclareCommandAccessLevel;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;

@Component
@DeclareCommandAccessLevel(accessLevel = CommandAccessLevel.LIMITED)
class cmd_Help extends Command {
    @Override
    void execute(CommandContainer cc) {
        CommandAccessLevel userAL = CommandAccessManager.findUserAccessLevel(cc.event.getAuthor().getIdLong());
        List<Command> availableCommands = CommandAccessManager.getAvailableCommands(cc);
        
        // We sort the commands alphabetically
        availableCommands.sort((cmd1, cmd2) -> {
            String name1 = cmd1.getClass().getSimpleName();
            String name2 = cmd2.getClass().getSimpleName();
            return name1.compareTo(name2);
        });
        
        cc.event.getChannel().sendMessage(new EmbedBuilder() {{
            setTitle("Help Message");
            setThumbnail(DiscordBot.getSelfAvatar());
            setDescription("Your access level: " + StringUtils.capitalize(userAL.name().toLowerCase()));
            for(Command cmd: availableCommands)
                addField(cmd.getClass().getSimpleName().substring(4), codeBlockWrap(cmd.description()), false);
            setAuthor(getSelfName());
            setTimestamp(Instant.now());
        }}.build()).queue();
    }
    
    @Override
    List<String> invokes() {
        return List.of("help");
    }
    
    @Override
    String description() {
        return "display help message";
    }
}
