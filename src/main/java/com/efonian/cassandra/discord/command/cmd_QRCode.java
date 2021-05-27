package com.efonian.cassandra.discord.command;

import com.efonian.cassandra.discord.command.annotation.DeclareCommandAccessLevel;
import com.efonian.cassandra.util.UtilImage;
import com.efonian.cassandra.util.UtilQRCode;
import com.efonian.cassandra.util.UtilWeb;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import net.dv8tion.jda.api.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(value = "cassandra.discord.init", havingValue = "true")
@DeclareCommandAccessLevel(accessLevel = CommandAccessLevel.TRUSTED)
public class cmd_QRCode extends Command {
    private static final Logger logger = LoggerFactory.getLogger(cmd_QRCode.class);
    
    private static final int MESSAGE_HISTORY_LIMIT = 5;
    
    @Override
    boolean execute(CommandContainer cc) {
        if(cc.args.length == 0)
            throw new IllegalArgumentException();
        
        switch(cc.args[0]) {
            case "decode":
                decode(cc);
                break;
                
            case "encode":
                encode(cc);
                break;
                
            default:
                throw new IllegalArgumentException();
        }
        return false;
    }
    
    private void decode(CommandContainer cc) {
        List<Message.Attachment> attachedImages = cc.event.getMessage().getAttachments()
                .stream().filter(Message.Attachment::isImage).collect(Collectors.toList());
    
        if(!attachedImages.isEmpty()) {
            BufferedImage image = UtilImage.readImage(UtilWeb.getURL(attachedImages.get(0).getUrl()));
            try {
                decodeAndSendResult(cc, image);
            } catch(NotFoundException e) {
                sendImage(cc.event.getChannel(), "Did not find QR code in this image", image, "potential_qr_code");
            }
        } else {
            cc.event.getChannel().getHistoryBefore(cc.event.getMessageIdLong(), MESSAGE_HISTORY_LIMIT).queue(history -> {
                for(Message msg : history.getRetrievedHistory()) {
                    List<Message.Attachment> attached = msg.getAttachments()
                            .stream().filter(Message.Attachment::isImage).collect(Collectors.toList());
                    
                    if(attached.isEmpty())
                        continue;
    
                    try {
                        decodeAndSendResult(cc, UtilImage.readImage(UtilWeb.getURL(attached.get(0).getUrl())));
                    } catch(NotFoundException e) { continue; }
                    return;
                }
            });
            cc.event.getChannel().sendMessage("Could not find valid image").queue();
        }
    }
    
    private void decodeAndSendResult(CommandContainer cc, BufferedImage image) throws NotFoundException {
        String result = UtilQRCode.decodeQRCodeImage(image);
        cc.event.getChannel().sendMessage(result).queue();
    }
    
    private void encode(CommandContainer cc) {
        if(cc.args.length < 2)
            throw new IllegalArgumentException();
        
        String targetText = String.join(" ", Arrays.copyOfRange(cc.args, 1, cc.args.length));
        
        try {
            sendImage(cc.event.getChannel(), UtilQRCode.generateQRCodeImage(targetText), "qr_code");
        } catch(WriterException e) {
            logger.warn("WriterException trying to encode QR Code: " + e.getMessage());
            cc.event.getChannel().sendMessage("Failed to encode message: " + targetText).queue();
        }
    }
    
    @Override
    List<String> invokes() {
        return List.of("qrc", "qrcode");
    }
    
    @Override
    public String description() {
        return "decode: decodes the QR code attached to the command message if there are any, or in the most recent " +
                MESSAGE_HISTORY_LIMIT + " messages immediately before the command message\n" +
                "encode [text]: encodes text into QR code";
    }
}
