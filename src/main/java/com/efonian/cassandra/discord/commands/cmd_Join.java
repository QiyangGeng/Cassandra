package com.efonian.cassandra.discord.commands;

import com.efonian.cassandra.discord.DiscordBot;
import com.efonian.cassandra.discord.commands.annotation.Cooldown;
import com.efonian.cassandra.discord.commands.annotation.DeclareCommandAccessLevel;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Cooldown(cooldown = 5)
@DeclareCommandAccessLevel
public class cmd_Join extends Command {
    // TODO: rework
    @Override
    void execute(CommandContainer cc) {
        if(!cc.event.isFromGuild())
            return;
        
        TextChannel textChannel = cc.event.getTextChannel();
        AudioManager audioManager = cc.event.getGuild().getAudioManager();
        
        if(audioManager.isConnected()) {
            textChannel.sendMessageFormat(
                    "%s is already connected to the channel \"%s\".", DiscordBot.getSelfName(), audioManager.getConnectedChannel().getName()
            ).complete().delete().queueAfter(30, TimeUnit.SECONDS);
            return;
        }
        
        GuildVoiceState memberVoiceState = cc.event.getMember().getVoiceState();
        
        
        if(!memberVoiceState.inVoiceChannel()) {
            textChannel.sendMessageFormat(
                    "%s is unable to find any voice channel with %s.", DiscordBot.getSelfName(), cc.event.getAuthor().getName()
            ).complete().delete().queueAfter(30, TimeUnit.SECONDS);
            return;
        }
        
        
        VoiceChannel voiceChannel = memberVoiceState.getChannel();
        Member selfMember = cc.event.getGuild().getSelfMember();
        
        if(!selfMember.hasPermission(voiceChannel, Permission.VOICE_CONNECT)) {
            textChannel.sendMessageFormat(
                    "%s lacks relevant permissions to join the voice channel \"%s\".", DiscordBot.getSelfName(), voiceChannel.getName()
            ).complete().delete().queueAfter(30, TimeUnit.SECONDS);
            return;
        }
        audioManager.openAudioConnection(voiceChannel);
        
        List<Member> voiceChannelMembers = new ArrayList<Member>() {{
            addAll(voiceChannel.getMembers());
            remove(cc.event.getMember());
            remove(cc.event.getGuild().getMember(cc.event.getJDA().getSelfUser()));
        }};
        String names = " alongside " + grammaticallyEnumerate(voiceChannelMembers);
        
        textChannel.sendMessageFormat(
                "Per invitation from %s, %s is joining the voice channel \"%s\"%s.", cc.event.getAuthor().getName(), DiscordBot.getSelfName(), voiceChannel.getName(), names
        ).queue();
    }
    
    private static String grammaticallyEnumerate(List<Member> l) {
        if(l.size() <= 0) return "";
        if(l.size() == 1) return l.get(0).getUser().getName();
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < l.size() - 1; i++)
            stringBuilder.append(String.format("%s, ", l.get(i).getUser().getName()));
        stringBuilder.append(String.format("and %s", l.get(l.size() -1)));
        return stringBuilder.toString();
    }
    
    @Override
    List<String> invokes() {
        return List.of("join");
    }
    
    @Override
    String description() {
        return "";
    }
}
