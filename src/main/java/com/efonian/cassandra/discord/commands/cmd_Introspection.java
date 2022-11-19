package com.efonian.cassandra.discord.commands;

import com.efonian.cassandra.discord.commands.annotation.DeclareCommandAccessLevel;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
@DeclareCommandAccessLevel(accessLevel = CommandAccessLevel.REGULAR)
public class cmd_Introspection extends Command {
    @Override
    void execute(CommandContainer cc) {
        if(cc.args.size() == 0)
            throw new IllegalArgumentException();
        
        // TODO: check for non-exact matches as well
        // https://stackoverflow.com/questions/5694385/getting-the-filenames-of-all-files-in-a-folder
        
        String potentialCommandName = cc.args.get(0).toLowerCase();
        if(potentialCommandName.startsWith("cmd_")) {
            potentialCommandName = potentialCommandName.substring(0, 4)
                    + potentialCommandName.substring(4, 5).toUpperCase()
                    + potentialCommandName.substring(5);
        } else {
            potentialCommandName = potentialCommandName.substring(0, 1).toUpperCase() + potentialCommandName.substring(1);
            potentialCommandName = "cmd_" + potentialCommandName;
        }
        
        Path directory = Paths.get("./src/main/java/com/efonian/cassandra/discord/commands/" + potentialCommandName + ".java");
        
        if(!Files.exists(directory)) {
            cc.event.getChannel().sendMessage("Cannot find command " + potentialCommandName).queue();
            return;
        }
        
        cc.event.getChannel().sendFile(directory.toFile()).queue();
    }
    
    @Override
    List<String> invokes() {
        return List.of("introspect", "introspection", "quine");
    }
    
    @Override
    String description() {
        return "[command name]: look at commands";
    }
}
