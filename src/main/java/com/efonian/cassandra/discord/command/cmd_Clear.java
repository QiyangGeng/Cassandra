package com.efonian.cassandra.discord.command;

import com.efonian.cassandra.discord.command.annotation.DeclareCommandAccessLevel;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@ConditionalOnProperty(value = "cassandra.discord.init", havingValue = "true")
@DeclareCommandAccessLevel(guildAccessLevel = CommandAccessLevel.MODERATOR)
public class cmd_Clear extends Command {
    private static final int CAPACITY = 10000;
    
    
    /**
     * Deletes messages in the channel the command was issued. The number of numbers can be passed in as a parameter,
     * the default value is stored in <c>CAPACITY</c>.
     * @param cc    The container with command information
     * @return      Whether the command has finished execution
     */
    @Override
    boolean execute(CommandContainer cc) {
        AtomicInteger numCollect = new AtomicInteger(CAPACITY);
        if(cc.args.length > 0 && cc.args[0].chars().allMatch(Character::isDigit) && Integer.parseInt(cc.args[0]) <= CAPACITY)
            numCollect.set(Integer.parseInt(cc.args[0]));
    
        long messageId = cc.event.getChannel().sendMessage("Collecting " + (numCollect.get() == CAPACITY? "":numCollect) + " messages...").complete().getIdLong();
        List<String> messages = Collections.synchronizedList(new ArrayList<>());
        long t = Instant.now().getEpochSecond();
        cc.event.getChannel().getIterableHistory().cache(false).forEachAsync((m) -> {
            if (t - m.getTimeCreated().toEpochSecond() < 1209590) {
                if (cc.event.isFromGuild() || m.getAuthor().equals(cc.event.getJDA().getSelfUser()))
                    messages.add(m.getId());
                if (messages.size() > 0 && messages.size() % 100 == 0)
                    cc.event.getChannel().editMessageById(messageId, "Collecting " + (numCollect.get() == CAPACITY ? "" : numCollect) + " messages...\nCollected: " + messages.size()).queue();
            }
            return messages.size() < CAPACITY;
        }).thenRun(() -> cc.event.getChannel().purgeMessagesById(messages));
        return false;
    }
    
    @Override
    List<String> invokes() {
        return List.of("clear");
    }
    
    @Override
    public String description() {
        return "clear the current channel";
    }
}
