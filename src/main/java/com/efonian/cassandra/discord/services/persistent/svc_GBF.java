package com.efonian.cassandra.discord.services.persistent;

import com.efonian.cassandra.discord.event.EventListenerManager;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class svc_GBF {
    @Value("cassandra.discord.services.gbf.echo.channel.id")
    private long ECHO_CHANNEL_ID;
    
    @Value("cassandra.discord.services.gbf.echo.amount")
    private short DEFAULT_ECHO_AMOUNT;
    
    @Autowired
    private void init(EventListenerManager listenerManager) {
        listenerManager.registerOperator(GuildMessageReceivedEvent.class, event -> {
            if(event.getChannel().getIdLong() != this.ECHO_CHANNEL_ID)
                return;
            
            for(int i = 0; i < DEFAULT_ECHO_AMOUNT; i++) {
            
            }
        });
    }
}
