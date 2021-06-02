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
    
    public static BufferedImage stackImages(@Nonnull BufferedImage... images) {
        return new BufferedImage(images[0].getWidth(), images[0].getHeight(), BufferedImage.TYPE_INT_ARGB) {{
            Graphics g = createGraphics();
            for(BufferedImage i: images)
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
                            File input = new File("./src/ProgramData/images/input.png");
                            ImageIO.write(source, "png", input);
                            File output = new File("./src/ProgramData/images/output.png");
                            Kaifu2xCli.main(new String[]{"-n0", "-s2", input.getPath(), output.getPath()});
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
