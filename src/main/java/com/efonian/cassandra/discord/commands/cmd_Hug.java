package com.efonian.cassandra.discord.commands;

import com.efonian.cassandra.discord.commands.annotation.DeclareCommandAccessLevel;
import com.efonian.cassandra.util.UtilImage;
import net.dv8tion.jda.api.entities.User;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

@Component
@DeclareCommandAccessLevel
public class cmd_Hug extends Command {
    @Override
    void execute(CommandContainer cc) {
        User target = cc.event.getAuthor();
    
        if(cc.event.getMessage().getMentionedUsers().size() > 0)
            target = cc.event.getMessage().getMentionedUsers().get(0);
        else if(!cc.args.isEmpty()) {
            User potentialTarget = cc.event.getJDA().retrieveUserById(cc.args.get(0)).complete();
            target = potentialTarget == null ? target : potentialTarget;
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
            private static final int TARGET_SIZE = 230;
            private static final int THETA = 12;
            private static final int X_OFFSET = 160;
            private static final int Y_OFFSET = 314;
            
            private BufferedImage top;
            private BufferedImage base;
            
            @Override
            protected ByteArrayOutputStream hug(User target) {
                try {
                    // Loading assets
                    BufferedImage top = getTop();
                    BufferedImage base = getBase();
                    BufferedImage avatar = ImageIO.read(new URL(target.getEffectiveAvatarUrl()));
                    
                    // Generating product
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    BufferedImage icon = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    icon.createGraphics().drawImage(formatIcon(avatar), X_OFFSET, Y_OFFSET, null);
                    ImageIO.write(UtilImage.stackImages(base, icon, top), "png", bytes);
                    bytes.close();
                    return bytes;
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                }
            }
            
            private BufferedImage formatIcon(BufferedImage icon) {
                if (icon.getWidth() > TARGET_SIZE)
                    icon = UtilImage.ResizeTool.NEAREST_NEIGHBOR.resize(icon, TARGET_SIZE, TARGET_SIZE);
                else if(icon.getWidth() < TARGET_SIZE)
                    icon = UtilImage.ResizeTool.KAIFU2X.resize(icon, TARGET_SIZE, TARGET_SIZE);
                
                return UtilImage.rotateImage(icon, THETA);
            }
            
            private BufferedImage getTop() throws IOException {
                URL url = cmd_Hug.class.getClassLoader().getResource(TOP);
                if(url == null)
                    throw new IOException("Cannot find top image for hug A");
                return top == null ? (top = ImageIO.read(url)) : top;
            }
    
            private BufferedImage getBase() throws IOException {
                URL url = cmd_Hug.class.getClassLoader().getResource(BASE);
                if(url == null)
                    throw new IOException("Cannot find base image for hug A");
                return base == null ? (base = ImageIO.read(url)) : base;
            }
        },
        S {
            @Override
            protected ByteArrayOutputStream hug(User target) {
                // TODO
                return null;
            }
        };
        
        protected abstract ByteArrayOutputStream hug(User target);
    }
}
