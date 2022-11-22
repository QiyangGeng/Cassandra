package com.efonian.cassandra.discord.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * A class for copying commands classes over to the resources folder while running from file. Used for the introspection
 * command.
 */
public class CloneCommandsToResource {
    private static final String CMD_DIR_ORI = "./src/main/java/com/efonian/cassandra/discord/commands/";
    private static final String CMD_DIR_DST = "./src/main/resources/com/efonian/cassandra/discord/commands/";
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Input 'c' for clone, and 'd' for delete.");
        String input = scanner.nextLine();
        
        if(input.equals("c") || input.equals("clone"))
            cloneAllCommandsToResource();
        else if(input.equals("d") || input.equals("delete"))
            deleteAllCommandsInResources();
    }
    
    /**
     * Clones all the commands into the resource folder, will also delete commands that no longer exist;
     * only run from file
     */
    private static void cloneAllCommandsToResource() {
        List<Path> commandsOriginal = Arrays
                .stream(new File(CMD_DIR_ORI).listFiles())
                .filter(file -> file.getName().startsWith("cmd_"))
                .map(File::toPath)
                .collect(Collectors.toList());
        
        deleteAllCommandsInResources();
        
        commandsOriginal.forEach(path -> {
            try {
                File cmdDest = new File(CMD_DIR_DST + path.getName(path.getNameCount() - 1));
                Files.copy(path, cmdDest.toPath());
            } catch(IOException exception) {
                exception.printStackTrace();
            }
        });
    }
    
    private static void deleteAllCommandsInResources() {
        Arrays.stream(new File(CMD_DIR_DST).listFiles())
                .filter(file -> file.getName().startsWith("cmd_"))
                .forEach(File::delete);
    }
}
