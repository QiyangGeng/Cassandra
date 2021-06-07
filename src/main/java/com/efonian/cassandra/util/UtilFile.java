package com.efonian.cassandra.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class UtilFile {
    private static final Logger logger = LoggerFactory.getLogger(UtilFile.class);
    
    @Nullable
    public static String readAll(Path path) {
        String targetFile = null;
        try {
            targetFile = new String(Files.readAllBytes(path));
        } catch(IOException e) {
            logger.warn("Caught IOException when trying to read file with path: " + path);
            e.printStackTrace();
        }
        return targetFile;
    }
    
    @Nullable
    public static String readAll(String path) {
        return readAll(Paths.get(path));
    }
    
    @Nullable
    public static String readAll(File file) {
        return readAll(file.toPath());
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
