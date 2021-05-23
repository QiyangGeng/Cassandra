package com.efonian.cassandra.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


public final class UtilImage {
    public static final Logger logger = LoggerFactory.getLogger(UtilImage.class);
    
    @Nullable
    public static BufferedImage readImage(String url) {
        try {
            return ImageIO.read(new URL(url));
        } catch(IOException e) {
            logger.warn(String.format("IOException when reading image from URL (%s): %s", url, e.getMessage()));
            return null;
        }
    }
    
    @Nullable
    public static BufferedImage readImage(URL url) {
        try {
            return ImageIO.read(url);
        } catch(IOException e) {
            logger.warn(String.format("IOException when reading image from URL (%s): %s", url, e.getMessage()));
            return null;
        }
    }
    
    @Nullable
    public static BufferedImage readImage(File file) {
        try {
            return ImageIO.read(file);
        } catch(IOException e) {
            logger.warn(String.format("IOException when reading image from file (%s): %s", file.getPath(), e.getMessage()));
            return null;
        }
    }
    
    // Source: https://code.google.com/archive/p/game-engine-for-java/source#31
    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage)
            return (BufferedImage) img;
        
        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        
        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        
        // Return the buffered image
        return bimage;
    }
    
    private static final LoadingCache<URL, Color> urlImageAverageColorMemo = CacheBuilder.newBuilder()
            .maximumSize(10)
            .build(CacheLoader.from(UtilImage::averageColorOfImage));
    
    @Nullable public static Color averageColor(String url) {
        try {
            return averageColor(new URL(url));
        } catch(MalformedURLException e) {
            logger.warn("Received malformed URL: " + e.getMessage());
            return null;
        }
    }
    
    public static Color averageColor(URL url) {
        return urlImageAverageColorMemo.getUnchecked(url);
    }
    
    private static Color averageColorOfImage(URL url) {
        BufferedImage image = readImage(url);
        if(image == null)
            throw new NullPointerException();
        return averageColorOfImage(image);
    }
    
    private static final LoadingCache<File, Color> fileImageAverageColorMemo = CacheBuilder.newBuilder()
            .maximumSize(10)
            .build(CacheLoader.from(UtilImage::averageColorOfImage));
    
    public static Color averageColor(File file) {
        return fileImageAverageColorMemo.getUnchecked(file);
    }
    
    private static Color averageColorOfImage(File file) {
        BufferedImage image = readImage(file);
        if(image == null)
            throw new NullPointerException();
        return averageColorOfImage(image);
    }
    
    private static Color averageColorOfImage(BufferedImage image) {
        final int w = image.getWidth(), h = image.getHeight();
        long r = 0, g = 0, b = 0, a = 0;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                final Color pixel = new Color(image.getRGB(x, y));
                r += pixel.getRed();
                g += pixel.getGreen();
                b += pixel.getBlue();
                a += pixel.getAlpha();
            }
        }
        final float mul = 1.0f / (255 * w * h);
        return new Color(r * mul, g * mul, b * mul);
    }
}
