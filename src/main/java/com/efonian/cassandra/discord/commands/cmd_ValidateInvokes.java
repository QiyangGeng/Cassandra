package com.efonian.cassandra.discord.commands;

import com.efonian.cassandra.discord.commands.annotation.DeclareCommandAccessLevel;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@DeclareCommandAccessLevel(accessLevel = CommandAccessLevel.ADMIN)
public class cmd_ValidateInvokes extends Command {
    @Override
    void execute(CommandContainer cc) {
        Map<List<String>, Command> commands = cmdManager.getRegisteredCommands();
        
        List<String> invokes = commands.keySet().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        
        validateDuplicates(cc, commands, invokes);
        validateCharacters(cc, commands, invokes);
    }
    
    private void validateDuplicates(CommandContainer cc, Map<List<String>, Command> commands, List<String> invokes) {
        List<String> dupes = findDuplicates(invokes);
        dupes = concInvokesAndCmds(dupes, commands);
    
        if(dupes.size() == 1)
            cc.event.getChannel().sendMessage("Found duplicate invoke: " + dupes.get(0)).queue();
        else if(dupes.size() > 1) {
            String last = dupes.remove(dupes.size() -1);
            cc.event.getChannel().sendMessage("Found duplicate invokes: " + grammaticallyJoin(dupes)).queue();
        }
        else
            cc.event.getChannel().sendMessage("No duplicate invokes were found").queue();
    }
    
    private List<String> concInvokesAndCmds(List<String> invokes, Map<List<String>, Command> commands) {
        return invokes.stream().map(s -> s + " (" + findAllCmdsConc(commands, s) + ")").collect(Collectors.toList());
    }
    
    private String findAllCmdsConc(Map<List<String>, Command> commands, String invoke) {
        List<String> cmdsConc = new ArrayList<>();
        for(List<String> invokes : commands.keySet()) {
            if(invokes.stream().anyMatch(s -> s.equalsIgnoreCase(invoke)))
                cmdsConc.add(commands.get(invokes).getClass().getName());
        }
        return String.join(", ", cmdsConc);
    }
    
    // Based on Holger's: https://stackoverflow.com/a/48165787
    private void validateCharacters(CommandContainer cc, Map<List<String>, Command> commands, List<String> invokes) {
        Pattern illegals = Pattern.compile("[^0-9A-Za-z]");
        List<String> invalidInvokes = invokes.stream()
                .map(illegals::matcher)
                .filter(Matcher::matches)
                .map(Matcher::group)
                .collect(Collectors.toList());
        
        invalidInvokes = concInvokesAndCmds(invalidInvokes, commands);
    
        if(invalidInvokes.size() == 1)
            cc.event.getChannel().sendMessage(
                    "Found invoke with illegal characters (non alphanumeric): " + invalidInvokes.get(0))
                    .queue();
        else if(invalidInvokes.size() > 1) {
            String last = invalidInvokes.remove(invalidInvokes.size() -1);
            cc.event.getChannel().sendMessage(
                    "Found invokes with illegal characters (non alphanumeric): " + grammaticallyJoin(invalidInvokes))
                    .queue();
        }
        else
            cc.event.getChannel().sendMessage("No duplicate invokes were found").queue();
    }
    
    private String grammaticallyJoin(List<String> strings) {
        String last = strings.remove(strings.size() - 1);
        return String.join(", ", strings) + ", and " + last;
    }
    
    @Override
    List<String> invokes() {
        return List.of("validateinvokes", "validate_invokes", "vi");
    }
    
    @Override
    String description() {
        return "Validates invokes, namely, this command ensures that there are no duplicate invokes, " +
                "and does not contain weird characters.";
    }
    
    // Slightly modified from Sebastian: https://stackoverflow.com/a/31641116
    private <T> List<T> findDuplicates(Collection<T> collection) {
        Set<T> uniques = new HashSet<>();
        return collection.stream()
                .filter(e -> !uniques.add(e))
                .collect(Collectors.toList());
    }
}
