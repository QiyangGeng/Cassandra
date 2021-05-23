package com.efonian.cassandra.discord.command;

import com.efonian.cassandra.discord.DiscordBot;
import com.efonian.cassandra.discord.command.annotation.DeclareCommandAccessLevel;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@ConditionalOnProperty(value = "cassandra.discord.init", havingValue = "true")
@DeclareCommandAccessLevel(accessLevel = CommandAccessLevel.LIMITED)
public class cmd_Help extends Command {
    @Override
    boolean execute(CommandContainer cc) {
        CommandAccessLevel userAL = CommandAccessManager.findUserAccessLevel(cc.event.getAuthor().getIdLong());
        List<Command> availableCommands = CommandAccessManager.getAvailableCommands(cc);
        availableCommands.sort((cmd1, cmd2) -> {
            String name1 = cmd1.getClass().getSimpleName().substring(4);
            String name2 = cmd2.getClass().getSimpleName().substring(4);
            return name1.compareTo(name2);
        });
        
        cc.event.getChannel().sendMessage(new EmbedBuilder() {{
            setTitle("Help Message");
            setThumbnail(DiscordBot.getSelfAvatar());
            setDescription("Your access level: " + StringUtils.capitalize(userAL.name().toLowerCase()));
            for(Command cmd: availableCommands) {
                addField(cmd.getClass().getSimpleName().substring(4), cmd.description(), false);
            }
            setAuthor(DiscordBot.getSelfName());
            setTimestamp(Instant.now());
        }}.build()).queue();
        return false;
    }
    
    @Override
    List<String> invokes() {
        return List.of("help");
    }
    
    @Override
    public String description() {
        return "display help message";
    }
}
