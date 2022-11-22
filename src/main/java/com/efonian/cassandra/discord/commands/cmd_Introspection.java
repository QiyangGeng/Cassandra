package com.efonian.cassandra.discord.commands;

import com.efonian.cassandra.discord.commands.annotation.DeclareCommandAccessLevel;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;
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
        // This means that this command also accepts cmd_Invoke, which is not completely intended, but I think it's
        // alright
        if(cmdName.startsWith("cmd_"))
            cmdName = cmdName.substring(4);
        
        // returns null if no such command exist, or user does not have access
        Command cmd = cmdManager.findCommandForUser(cc.event.isFromGuild(), cc.event.getAuthor(), cmdName);
        
        if(cmd == null) {
            cc.event.getChannel().sendMessage("Cannot find command " + cmdName).queue();
            return;
        }
        
        cmdName = cmd.getClass().getSimpleName();
        Path directory;
        try {
            directory = Paths.get(Paths.get(this.getClass()
                    .getResource("/com/efonian/cassandra/discord/commands/").toURI()).toString(),
                    cmdName + ".java");
        } catch(URISyntaxException e) {
            throw new RuntimeException(e);
        }
        
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
