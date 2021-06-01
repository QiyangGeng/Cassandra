package com.efonian.cassandra.discord.misc;

import com.efonian.cassandra.discord.DiscordBot;
import com.efonian.cassandra.discord.event.EventListenerManager;
import com.efonian.cassandra.util.UtilQRCode;
import com.google.zxing.NotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class QRCodeAutoDecoder {
    private EventListenerManager eventManager;
    
    @PostConstruct
    public void init() {
        eventManager.registerOperator(MessageReceivedEvent.class, event -> {
            if(event.getAuthor().equals(event.getJDA().getSelfUser()))
                return;

            List<Message.Attachment> attachedImages = event.getMessage().getAttachments()
                    .stream().filter(Message.Attachment::isImage).collect(Collectors.toList());

            for(Message.Attachment att : attachedImages) {
                String textOutput;
                try {
                    String urlString = att.getUrl();
                    textOutput = UtilQRCode.decodeQRCodeImage(ImageIO.read(new URL(urlString)));
                    event.getChannel().sendMessage(new EmbedBuilder() {{
                        setTitle("QR Code Auto Decoder");
                        setThumbnail(urlString);
                        addField("Decoded Text: ", textOutput, false);
                        setAuthor(DiscordBot.getSelfName());
                        setTimestamp(Instant.now());
                    }}.build()).queue();
                } catch(NotFoundException | IOException ignored) {}
            }
        });
    }
    
    @Autowired
    private void setEventManager(EventListenerManager eventManager) {
        this.eventManager = eventManager;
    }
}
