package com.efonian.cassandra.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.soywiz.kaifu2x.Kaifu2xCli;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


public final class UtilImage {
    public static final Logger logger = LoggerFactory.getLogger(UtilImage.class);
    
    @Nullable
    public static BufferedImage readImage(@Nonnull String url) {
        try {
            return ImageIO.read(new URL(url));
        } catch(IOException e) {
            logger.warn(String.format("IOException when reading image from URL (%s): %s", url, e.getMessage()));
            return null;
        }
    }
    
    @Nullable
    public static BufferedImage readImage(@Nonnull URL url) {
        try {
            return ImageIO.read(url);
        } catch(IOException e) {
            logger.warn(String.format("IOException when reading image from URL (%s): %s", url, e.getMessage()));
            return null;
        }
    }
    
    @Nullable
    public static BufferedImage readImage(@Nonnull File file) {
        try {
            return ImageIO.read(file);
        } catch(IOException e) {
            logger.warn(String.format("IOException when reading image from file (%s): %s", file.getPath(), e.getMessage()));
            return null;
        }
    }
    
    // Source: https://code.google.com/archive/p/game-engine-for-java/source#31
    public static BufferedImage toBufferedImage(@Nonnull Image img) {
        if(img instanceof BufferedImage)
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
    
    @Nullable public static Color averageColor(@Nonnull String url) {
        try {
            return averageColor(new URL(url));
        } catch(MalformedURLException e) {
            logger.warn("Received malformed URL: " + e.getMessage());
            return null;
        }
    }
    
    public static Color averageColor(@Nonnull URL url) {
        return urlImageAverageColorMemo.getUnchecked(url);
    }
    
    private static Color averageColorOfImage(@Nonnull URL url) {
        BufferedImage image = readImage(url);
        if(image == null)
            throw new NullPointerException();
        return averageColorOfImage(image);
    }
    
    private static final LoadingCache<File, Color> fileImageAverageColorMemo = CacheBuilder.newBuilder()
            .maximumSize(10)
            .build(CacheLoader.from(UtilImage::averageColorOfImage));
    
    public static Color averageColor(@Nonnull File file) {
        return fileImageAverageColorMemo.getUnchecked(file);
    }
    
    private static Color averageColorOfImage(@Nonnull File file) {
        BufferedImage image = readImage(file);
        if(image == null)
            throw new NullPointerException();
        return averageColorOfImage(image);
    }
    
    private static Color averageColorOfImage(@Nonnull BufferedImage image) {
        final int w = image.getWidth(), h = image.getHeight();
        long r = 0, g = 0, b = 0;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                final Color pixel = new Color(image.getRGB(x, y));
                r += pixel.getRed();
                g += pixel.getGreen();
                b += pixel.getBlue();
            }
        }
        final float mul = 1.0f / (255 * w * h);
        return new Color(r * mul, g * mul, b * mul);
    }
    
    public static BufferedImage rotateImage(BufferedImage image, double theta) {
        if(theta != 0) {
            // A one-pixel border is added to help with AA at the edges
            image = addBorder(image, 1);
            
            // We calculate the width and height of the new image after rotation
            final double rads = Math.toRadians(theta);
            final double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));
            final int w = image.getWidth(), h = image.getHeight();
            final int newW = (int) Math.floor(w*cos + h*sin), newH = (int) Math.floor(h*cos + w*sin);
            
            // We rotate the image around its centre using the Graphics2D API
            BufferedImage rotatedIcon = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = rotatedIcon.createGraphics();
            g.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
            g.addRenderingHints(new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR));
            g.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
            g.translate((newW - w)/2, (newH - h)/2);
            g.rotate(rads, w >> 1, h >> 1);
            g.drawRenderedImage(image, null);
            g.dispose();
            
            return rotatedIcon;
        }
        return image;
    }
    
    public static BufferedImage addBorder(BufferedImage image, int borderWidth) {
        BufferedImage result = new BufferedImage(image.getWidth() + borderWidth * 2, image.getHeight() + borderWidth * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        g.drawImage(image, borderWidth, borderWidth, null);
        g.dispose();
        return result;
    }
    
    public static BufferedImage stackImages(int width, int height, BufferedImage... images) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB) {{
            Graphics g = createGraphics();
            for(BufferedImage i: images)
                if(i != null)
                    g.drawImage(i, 0, 0, null);
            g.dispose();
        }};
    }
    
    public enum ResizeTool {
        NEAREST_NEIGHBOR {
            @Override
            public BufferedImage resize(BufferedImage source, int width, int height) {
                return commonResize(source, width, height, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            }
        },
        BILINEAR {
            @Override
            public BufferedImage resize(BufferedImage source, int width, int height) {
                return commonResize(source, width, height, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            }
        },
        BICUBIC {
            @Override
            public BufferedImage resize(BufferedImage source, int width, int height) {
                return commonResize(source, width, height, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            }
        },
        PROGRESSIVE_BILINEAR {
            @Override
            public BufferedImage resize(BufferedImage source, int width, int height) {
                return progressiveResize(source, width, height, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            }
        },
        PROGRESSIVE_BICUBIC {
            @Override
            public BufferedImage resize(BufferedImage source, int width, int height) {
                return progressiveResize(source, width, height, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            }
        },
        AVERAGE {
            @Override
            public BufferedImage resize(BufferedImage source, int width, int height) {
                Image img2 = source.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
                BufferedImage img = new BufferedImage(width, height, source.getType());
                Graphics2D g = img.createGraphics();
                try {
                    g.drawImage(img2, 0, 0, width, height, null);
                } finally {
                    g.dispose();
                }
                return img;
            }
        },
        KAIFU2X {
            @Override
            public BufferedImage resize(BufferedImage source, int width, int height) {
                synchronized(this) {
                    try {
                        while(source.getWidth() < width) {
                            File directory = new File("./ProgramData/images/");
                            File input = File.createTempFile("input_", ".png", directory);
                            File output = File.createTempFile("output_", ".png", directory);
                            input.deleteOnExit();
                            output.deleteOnExit();
                            ImageIO.write(source, "png", input);
                            Kaifu2xCli.main(new String[] {"-n0", "-s2", input.getPath(), output.getPath()});
                            source = ImageIO.read(output);
                        }
                        
                        if(source.getWidth() > width)
                            source = NEAREST_NEIGHBOR.resize(source, width, height);
                        
                        RescaleOp rescaleOp = new RescaleOp(1.005f, 5, null);
                        rescaleOp.filter(source, source);
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                    
                    return source;
                }
            }
        },
        ISR {
            @Override
            public BufferedImage resize(BufferedImage source, int width, int height) {
                // TODO
                // https://github.com/idealo/image-super-resolution
                return source;
            }
        };
        
        public abstract BufferedImage resize(BufferedImage source, int width, int height);
        
        private static BufferedImage progressiveResize(BufferedImage source, int width, int height, Object hint) {
            int w = Math.max(source.getWidth() / 2, width);
            int h = Math.max(source.getHeight() / 2, height);
            BufferedImage img = commonResize(source, w, h, hint);
            while(w != width || h != height) {
                BufferedImage prev = img;
                w = Math.max(w / 2, width);
                h = Math.max(h / 2, height);
                img = commonResize(prev, w, h, hint);
                prev.flush();
            }
            return img;
        }
        
        private static BufferedImage commonResize(BufferedImage source, int width, int height, Object hint) {
            BufferedImage img = new BufferedImage(width, height, source.getType());
            Graphics2D g = img.createGraphics();
            try {
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
                g.drawImage(source, 0, 0, width, height, null);
            } finally {
                g.dispose();
            }
            return img;
        }
    }
}
