package com.efonian.cassandra.discord.commands;

import com.efonian.cassandra.discord.commands.annotation.DeclareCommandAccessLevel;
import com.efonian.cassandra.util.UtilImage;
import net.dv8tion.jda.api.entities.User;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

@Component
@DeclareCommandAccessLevel
public class cmd_Hug extends Command {
    @Override
    void execute(CommandContainer cc) {
        User target = cc.event.getAuthor();
        
        if(!cc.args.isEmpty()) {
            if(cc.args.get(0).chars().allMatch(Character::isDigit)) {
                User potentialTarget = cc.event.getJDA().retrieveUserById(cc.args.get(0)).complete();
                target = potentialTarget == null? target : potentialTarget;
            } else if(cc.event.getMessage().getMentionedUsers().size() > 0) {
                target = cc.event.getMessage().getMentionedUsers().get(0);
            }
        }
        
        cc.event.getChannel().sendMessage("Slowly approaching " + target.getName() + "...").queue();
        cc.event.getChannel().sendFile(Hug.A.hug(target).toByteArray(), "hugging " + target.getName() + ".png").queue();
    }
    
    @Override
    List<String> invokes() {
        return List.of("hug");
    }
    
    @Override
    String description() {
        return "have a hug!";
    }
    
    private enum Hug {
        A {
            private static final String TOP = "com/efonian/cassandra/discord/commands/hug/A/0.png";
            private static final String BASE = "com/efonian/cassandra/discord/commands/hug/A/1.png";
            
            @Override
            ByteArrayOutputStream hug(User target) {
                try {
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    BufferedImage top = ImageIO.read(new FileInputStream(cmd_Hug.class.getClassLoader().getResource(TOP).getPath()));
                    BufferedImage base = ImageIO.read(new FileInputStream(cmd_Hug.class.getClassLoader().getResource(BASE).getPath()));
                    BufferedImage icon = new BufferedImage(top.getWidth(), top.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    icon.createGraphics().drawImage(formatIcon(UtilImage.readImage(new URL(target.getEffectiveAvatarUrl())), 230, 12), 160, 314, null);
                    ImageIO.write(UtilImage.stackImages(base, icon, top), "png", bytes);
                    bytes.close();
                    return bytes;
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                }
            }
            
            private BufferedImage formatIcon(BufferedImage icon, int targetSize, int theta) {
                if (icon.getWidth() > targetSize)
                    icon = UtilImage.ResizeTool.NEAREST_NEIGHBOR.resize(icon, targetSize, targetSize);
                else if(icon.getWidth() < targetSize)
                    icon = UtilImage.ResizeTool.KAIFU2X.resize(icon, targetSize, targetSize);
                
                return UtilImage.rotateImage(icon, theta);
            }
        },
        S {
            @Override
            ByteArrayOutputStream hug(User target) {
                // TODO
                return null;
            }
        };
        
        abstract ByteArrayOutputStream hug(User target);
    }
}
