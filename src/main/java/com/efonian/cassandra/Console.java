package com.efonian.cassandra;

import com.efonian.cassandra.util.UtilRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Component
@Scope(value= ConfigurableBeanFactory.SCOPE_SINGLETON)
public class Console {
    private static final Logger logger = LoggerFactory.getLogger(Console.class);
    
    private final Scanner scanner = new Scanner(System.in);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    
    @PostConstruct
    private void init() {
        executor.execute(() -> {
            String nextLine;
            while(true) {
                nextLine = scanner.nextLine();
                logger.debug("Received input: " + nextLine);
                if(nextLine.equalsIgnoreCase("forceQuit"))
                    System.exit(5);
                handleInput(nextLine);
            }
        });
    }
    
    private synchronized void handleInput(String input) {
        String[] splitInput = input.split(" ");
        
        // Temporary, console is yet to be implemented, just here to make sure things exit correctly
        if(splitInput[0].equals("exit"))
            UtilRuntime.shutdown();
    }
    
    @PreDestroy
    private void shutdown() {
        logger.info("Shutting down console");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.SECONDS))
                executor.shutdownNow();
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
