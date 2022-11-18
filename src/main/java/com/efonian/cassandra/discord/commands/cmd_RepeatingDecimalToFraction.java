package com.efonian.cassandra.discord.commands;

import java.util.List;

public class cmd_RepeatingDecimalToFraction extends Command {
    @Override
    void execute(CommandContainer cc) {
    
    }
    
    void convert(String input) {
        String[] splitInput = input.split("\\|");
        
        int decimalIndex = splitInput[0].indexOf("\\.");
//        if(decimalIndex == -1)
        
        
        String strA = splitInput[0].substring(0, decimalIndex);
        String strB = splitInput[0].substring(decimalIndex);
        String strC = splitInput[1];
        
        long baseB = (long) Math.pow(10, strB.length());
        long baseC = (long) Math.pow(10, strC.length());
        
        long a = Long.parseLong(strA);
        long b = Long.parseLong(strB);
        long c = Long.parseLong(strC);
        
        long nom1 = a*baseB + b;
        long den1 = baseB;
        
        long nom2 = c;
        long den2 = baseB*(baseC - 1);
        
        // Simplify here maybe
        
        long nom = nom1*den2 + nom2*den1;
        long den = den1*den2;
        
        // Simplify
    }
    
    @Override
    List<String> invokes() {
        return null;
    }
    
    @Override
    String description() {
        return null;
    }
}
