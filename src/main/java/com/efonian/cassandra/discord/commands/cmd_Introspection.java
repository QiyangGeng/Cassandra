package com.efonian.cassandra.discord.commands;

import com.efonian.cassandra.discord.commands.annotation.DeclareCommandAccessLevel;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
@DeclareCommandAccessLevel(accessLevel = CommandAccessLevel.FULL)
public class cmd_Introspection extends Command {
    @Override
    void execute(CommandContainer cc) {
        if(cc.args.size() == 0)
            throw new IllegalArgumentException();
        
        // TODO: check for non-exact matches as well
        // https://stackoverflow.com/questions/5694385/getting-the-filenames-of-all-files-in-a-folder
        
        String cmdName = cc.args.get(0).toLowerCase();
        Command cmd = cmdManager.findCommandForUser(cc.event.isFromGuild(), cc.event.getAuthor(), cmdName);
        
        if(cmd != null) {
            cmdName = cmd.getClass().getSimpleName();
        } else if(cmdName.startsWith("cmd_")) {
            cmdName = cmdName.substring(0, 4)
                    + cmdName.substring(4, 5).toUpperCase()
                    + cmdName.substring(5);
        } else {
            cmdName = cmdName.substring(0, 1).toUpperCase() + cmdName.substring(1);
            cmdName = "cmd_" + cmdName;
        }
        
        Path directory = Paths.get("./src/main/java/com/efonian/cassandra/discord/commands/" + cmdName + ".java");
        
        if(!Files.exists(directory)) {
            cc.event.getChannel().sendMessage("Cannot find command " + cmdName).queue();
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
