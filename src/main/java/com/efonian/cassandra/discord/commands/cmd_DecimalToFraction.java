package com.efonian.cassandra.discord.commands;

import com.efonian.cassandra.util.UtilMathLong;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class cmd_DecimalToFraction extends Command {
    @Override
    void execute(CommandContainer cc) {
        cc.event.getChannel().sendMessage(convert(cc.args.get(0))).queue();
    }
    
    private String convert(String input) {
        String[] splitInput = input.split("\\|");
        
        int decimalIndex = splitInput[0].indexOf(".");
        if(decimalIndex == -1) {
            throw new IllegalArgumentException("Not a decimal.");
        }
        
        // a.b|c, where c repeats (i.e. a.bcccccc...)
        String strA = splitInput[0].substring(0, decimalIndex);
        String strB = splitInput[0].substring(decimalIndex + 1);
        String strC = splitInput.length == 1 ? "" : splitInput[1];
        
        long baseB = (long) Math.pow(10, strB.length());
        long baseC = (long) Math.pow(10, strC.length());
        
        long a = strA.equals("") ? 0 : Long.parseLong(strA);
        long b = strB.equals("") ? 0 : Long.parseLong(strB);
        long c = strC.equals("") ? 0 : Long.parseLong(strC);
        
        if(c == 0) {
            return simplify(a * baseB + b, baseB);
        }
        
        return simplify(baseB*((a*baseB + b)*(baseC - 1) + c), baseB*baseB*(baseC - 1));
        
//        long nom1 = a*baseB + b;
//        long den1 = baseB;
//
//        long nom2 = c;
//        long den2 = baseB*(baseC - 1);
//
//        long nom = nom1*den2 + nom2*den1;
//        long den = den1*den2;
    
    }
    
    private String simplify(long nom, long den) {
        long gcd = UtilMathLong.gcd(nom, den);
        return nom/gcd + "/" + den/gcd;
    }
    
    @Override
    List<String> invokes() {
        return List.of("decimaltofraction", "d2f", "dtf");
    }
    
    @Override
    String description() {
        return "";
    }
}
