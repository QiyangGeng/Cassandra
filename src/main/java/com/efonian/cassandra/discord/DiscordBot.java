package com.efonian.cassandra.discord;

import com.efonian.cassandra.discord.event.EventListener;
import com.efonian.cassandra.discord.event.EventListenerManager;
import com.efonian.cassandra.util.UtilImage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.DisconnectEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.self.SelfUpdateAvatarEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.security.auth.login.LoginException;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;

@Component
@ConditionalOnProperty(value = "cassandra.discord.init", havingValue = "true")
public final class DiscordBot {
    private static final Logger logger = LoggerFactory.getLogger(DiscordBot.class);
    
    @Value("${cassandra.discord.ownerId}")
    private long ownerId;
    
    @Value("${cassandra.discord.debug.guild.id}")
    private long debugGuildId;
    
    @Value("${cassandra.discord.token}")
    private String token;
    
    private EventListener eventListener;
    private EventListenerManager eventListenerManager;
    
    private static JDA jda;
    
    @PostConstruct
    protected void start() {
        logger.info("Building JDA for Discord bot");
        try {
            jda = JDABuilder.createDefault(token)
                    .setAutoReconnect(true)
                    .setStatus(OnlineStatus.ONLINE)
                    .setActivity(Activity.playing("Tests"))
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .addEventListeners(eventListener)
                    .build();
        } catch(LoginException e) { e.printStackTrace(); }
        logger.info("Built JDA for Discord bot");
    
        createAuxiliaryListeners();
    }
    
    // Note to self: remember to update when persistent operators are added
    private void createAuxiliaryListeners() {
        eventListenerManager
                .registerOperator(ReadyEvent.class, event -> {
                    readyPreparations();
                    logger.info("Discord bot ready");
                })
                .registerOperator(DisconnectEvent.class, event -> logger.info("Discord bot disconnected"))
                .registerOperator(ReconnectedEvent.class, event -> logger.info("Discord bot reconnected"))
                .registerOperator(ShutdownEvent.class, event -> logger.info("Discord bot shutdown"))
                .registerOperator(GuildJoinEvent.class,
                        event -> logger.info("Discord bot joined guild: " + event.getGuild().getName()))
                .registerOperator(GuildLeaveEvent.class,
                        event -> logger.info("Discord bot left guild: " + event.getGuild().getName()))
                .registerOperator(SelfUpdateAvatarEvent.class,
                        event -> avatarAverageColor = UtilImage.averageColor(event.getNewAvatarUrl()));
    }
    
    private void readyPreparations() {
        try {
            avatarAverageColor = UtilImage.averageColor(new URL(getSelfAvatar()));
        } catch(MalformedURLException e) {
            logger.warn("Got bad url from JDA");
        }
    }
    
    @PreDestroy
    protected void stop() {
        if (jda != null) {
            logger.info("Shutting down JDA for Discord bot");
            jda.shutdown();
        }
    }
    
    @Autowired
    private void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }
    
    @Autowired
    private void setEventListenerManager(EventListenerManager eventListenerManager) {
        this.eventListenerManager = eventListenerManager;
    }
    
    // Misc Getters
    private static Color avatarAverageColor;
    
    public static JDA getJda() {
        return jda;
    }
    
    public static long getSelfId() {
        return DiscordBot.getSelfUser().getIdLong();
    }
    
    public static User getSelfUser() {
        return DiscordBot.getJda().getSelfUser();
    }
    
    public static String getSelfName() {
        return DiscordBot.getSelfUser().getName();
    }
    
    public static String getSelfAvatar() {
        return DiscordBot.getJda().getSelfUser().getEffectiveAvatarUrl();
    }
    
    public static Color getSelfAvatarAverageColour() {
        return DiscordBot.avatarAverageColor;
    }
    
    public long getOwnerId() { return ownerId; }
    
    public long getDebugGuildId() {
        return debugGuildId;
    }
}
