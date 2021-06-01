package com.efonian.cassandra.discord.commands;

import com.efonian.cassandra.discord.commands.annotation.DeclareCommandAccessLevel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(value = "cassandra.discord.init", havingValue = "true")
@DeclareCommandAccessLevel(accessLevel=CommandAccessLevel.FULL)
class cmd_Timeout extends Command {
    @Override
    void execute(CommandContainer cc) {
        Thread currentThread = Thread.currentThread();
        
        if(!currentThread.isDaemon())
            return;
        
        // a while true loop could be added here to be more evil, but that is besides the point in this case
        try {
            currentThread.join();
        } catch(InterruptedException ignored) {}
    }
    
    @Override
    List<String> invokes() {
        return List.of("timeout");
    }
    
    @Override
    String description() {
        return "indefinitely hangs the thread executing the command if said thread is daemon; o/w does nothing";
    }
}
