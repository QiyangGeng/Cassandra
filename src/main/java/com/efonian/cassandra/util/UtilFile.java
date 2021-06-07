package com.efonian.cassandra.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

// do read, write, give options to flush, etc.; executor service for files which needs constant writing?
// rolling writing periods
public final class UtilFile {
    private static final Logger logger = LoggerFactory.getLogger(UtilFile.class);
    
    
    /**
     * Should only really be used if too lazy to catch exceptions, reads all bytes of a file as string, no checks
     */
    @Nullable
    public static String readAll(String path) {
        String targetFile = null;
        try {
            targetFile = new String(Files.readAllBytes(Paths.get(path)));
        } catch(IOException e) {
            logger.warn("Caught IOException when trying to read file with path: " + path);
            e.printStackTrace();
        }
        return targetFile;
    }
    
    /**
     * Should only really be used if too lazy to catch exceptions, reads all bytes of a file as string, no checks
     */
    @Nullable
    public static String readAll(File file) {
        String targetFile = null;
        try {
            targetFile = new String(Files.readAllBytes(file.toPath()));
        } catch(IOException e) {
            logger.warn("Caught IOException when trying to read file with path: " + file.getPath());
            e.printStackTrace();
        }
        return targetFile;
    }
    
    public static void write(String path, String content) {
        try {
            PrintWriter writer = new PrintWriter(path);
            writer.write(content);
            writer.close();
        } catch(IOException e) {
            logger.warn("Caught IOException when trying to write file with path: " + path);
        }
    }
}
