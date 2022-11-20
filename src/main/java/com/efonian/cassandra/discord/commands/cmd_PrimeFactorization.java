package com.efonian.cassandra.discord.commands;

import com.efonian.cassandra.discord.commands.annotation.DeclareCommandAccessLevel;
import com.efonian.cassandra.util.UtilMathLong;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@DeclareCommandAccessLevel(accessLevel = CommandAccessLevel.TRUSTED)
public class cmd_PrimeFactorization extends Command {
    @Override
    void execute(CommandContainer cc) {
        if(cc.args.isEmpty())
            throw new IllegalArgumentException();
        
        long num = Long.parseLong(cc.args.get(0));
        List<String> factors = UtilMathLong.primeFactorizationPollardRho(num)
                .stream().map(Object::toString).collect(Collectors.toList());
        
        cc.event.getChannel().sendMessage(String.join(", ", factors)).queue();
    }
    
    @Override
    List<String> invokes() {
        return List.of("primefactorization", "pf");
    }
    
    @Override
    String description() {
        return "Prints the prime factors of a given positive number (64 bit signed) using the Pollard Rho Algorithm.";
    }
}
