package com.efonian.cassandra.crafty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

//import org.graalvm.polyglot.*;
//import org.graalvm.polyglot.proxy.*;

// make a ProcessBuilder Util thingamajiggy
public final class CraftyClientJWrapper {
    private static final Logger logger = LoggerFactory.getLogger(CraftyClientJWrapper.class);
    
    public static List<String> craftyClient(String... args) {
        // python on coding machine is python 3.9
        final String[] processArguments = List.of("python", "CraftyClient.py", args).toArray(String[]::new);
        ProcessBuilder processBuilder = new ProcessBuilder(processArguments);
        processBuilder.redirectErrorStream(true);
        
        try {
            Process process = processBuilder.start();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            List<String> output = new ArrayList<>();
            String line;
            while((line = reader.readLine()) != null)
                output.add(line);
            
            reader.close();
            process.waitFor();
            process.destroy();
            return output;
        } catch(IOException e) {
            logger.warn("CraftyClient.py not found (maybe): " + e.getMessage());
            return null;
        } catch(InterruptedException e) {
            logger.warn(e.getMessage());
            return null;
        }
    }
    
//    public static List<String> craftyClient(String... args) {
//        try(Context context = Context.create()) {
//            context.eval("python", "import json\nimport sys\nfrom crafty_client import CraftyWeb");
//            Value file = context.eval("python", "open(\"crafty.json\", 'r')");
//            System.out.println(file);
//        }
//        return null;
//    }
}
