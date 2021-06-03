package com.efonian.cassandra.discord.commands;

import com.efonian.cassandra.discord.commands.annotation.DeclareCommandAccessLevel;
import com.efonian.cassandra.util.UtilImage;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@DeclareCommandAccessLevel
public class cmd_Hug extends Command {
    private static final Map<String, Hug> availableHugger = new ConcurrentHashMap<>(Arrays
            .stream(Hug.values()).collect(Collectors.toMap(Enum::name, Function.identity())));
    
    @Override
    void execute(CommandContainer cc) {
        User target = cc.event.getAuthor();
        Hug hugger = cc.args.isEmpty() ? Hug.AHR : availableHugger.getOrDefault(cc.args.get(0).toUpperCase(), Hug.AHR);
        
        if(cc.event.getMessage().getMentionedUsers().size() > 0)
            target = cc.event.getMessage().getMentionedUsers().get(0);
        else {
            for(String arg: cc.args) {
                try {
                    target = cc.event.getJDA().retrieveUserById(arg).complete();
                    break;
                } catch(Exception ignored) {}
            }
        }
        
        cc.event.getChannel().sendMessage("Slowly approaching " + target.getName() + "...").queue();
        cc.event.getChannel().sendFile(hugger.hug(target), hugger.name() + "_hugging_" + target.getName() + ".png").queue();
    }
    
    @Override
    CommandAccessLevel dynamicallyAssignAccessLevel(CommandContainer cc) {
        // later, different al to different hugger
        return super.dynamicallyAssignAccessLevel(cc);
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
        AHR("com/efonian/cassandra/discord/commands/hug/AHR/0.png",
                "com/efonian/cassandra/discord/commands/hug/AHR/1.png",
                12, 230, 160, 314),
//        AKG(null, null, 0, 0, 0, 0),
//        ATH(null, null, 0, 0, 0, 0),
        HLN("com/efonian/cassandra/discord/commands/hug/HLN/0.png", null,
                14.7D, 556, 508, 918),
//        SNA(null, null, 0, 0, 0, 0),
//        SRK(null, null, 0, 0, 0, 0),
        ;
        
        private final String pathTop, pathBase;
        private final double theta;
        private final int targetSize;
        private final int xOffset, yOffset;
        
        // These are lazy loaded; top must not be null, base can be null
        private BufferedImage top;
        private BufferedImage base;
        
        private byte[] hug(User target) {
            try {
                // Loading assets
                BufferedImage top = getTop();
                BufferedImage base = getBase();
                BufferedImage avatar = ImageIO.read(new URL(target.getEffectiveAvatarUrl()));
                
                // Generating product, we write to a bytes array which can be sent through JDA as file
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                BufferedImage icon = new BufferedImage(top.getWidth(), top.getHeight(), BufferedImage.TYPE_INT_ARGB);
                icon.createGraphics().drawImage(rotate(resize(avatar, targetSize), theta), xOffset, yOffset, null);
                ImageIO.write(UtilImage.stackImages(icon.getWidth(), icon.getHeight(), base, icon, top), "png", bytes);
                return bytes.toByteArray();
            } catch(IOException exception) {
                throw new RuntimeException(exception);
            }
        }
        
        private static BufferedImage rotate(BufferedImage image, double theta) {
            return UtilImage.rotateImage(image, theta);
        }
        
        private static BufferedImage resize(BufferedImage image, int targetSize) {
            if (image.getWidth() > targetSize)
                return UtilImage.ResizeTool.NEAREST_NEIGHBOR.resize(image, targetSize, targetSize);
            else if(image.getWidth() < targetSize)
                return UtilImage.ResizeTool.KAIFU2X.resize(image, targetSize, targetSize);
            return image;
        }
    
        @NotNull
        private BufferedImage getTop() throws IOException {
            URL url = cmd_Hug.class.getClassLoader().getResource(pathTop);
            if(url == null)
                throw new IOException("Cannot find top image for hug " + this.name());
            return top == null ? (top = ImageIO.read(url)) : top;
        }
        
        @Nullable
        private BufferedImage getBase() throws IOException {
            if(pathBase == null)
                return null;
            URL url = cmd_Hug.class.getClassLoader().getResource(pathBase);
            if(url == null)
                throw new IOException("Cannot find base image for hug " + this.name());
            return base == null ? (base = ImageIO.read(url)) : base;
        }
        
        Hug(String pathTop, String pathBase, double theta, int targetSize, int xOffset, int yOffset) {
            this.pathTop = pathTop;
            this.pathBase = pathBase;
            this.theta = theta;
            this.targetSize = targetSize;
            this.xOffset = xOffset;
            this.yOffset = yOffset;
        }
    }
}
