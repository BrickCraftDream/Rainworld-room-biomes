package net.brickcraftdream.rainworldmc_biomes.image;

import net.minecraft.util.ColorRGBA;
import net.minecraft.world.phys.Vec2;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageGenerator {
    public static final int IMAGE_RESOLUTION_SHIFT = 8;
    public static final int IMAGE_RESOLUTION_XY = 1 << IMAGE_RESOLUTION_SHIFT;
    public static final int IMAGE_RESOLUTION_MASK = IMAGE_RESOLUTION_XY - 1;

    public static int[] getCoordsFromLinear(int index) {
        int x = index & IMAGE_RESOLUTION_MASK;
        int y = (index >>> IMAGE_RESOLUTION_SHIFT) & IMAGE_RESOLUTION_MASK;
        return new int[]{x, y};
    }

    public static int getLinearFromCoords(int x, int y) {
        return (y << IMAGE_RESOLUTION_SHIFT) | (x & IMAGE_RESOLUTION_MASK);
    }

    // New method:
    public static byte[] splitPaletteIntoBytes(int palette) {
        byte hi = (byte) ((palette >> 8) & 0xFF);
        byte lo = (byte) ((palette >> 0) & 0xFF);
        return new byte[]{hi, lo};
    }


    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;

    public static Vec2 indexToUV(float temperature) {
        int index = (int) temperature;
        int x = index % TEXTURE_WIDTH;
        int y = index / TEXTURE_WIDTH;

        float u = (x + 0.5f) / TEXTURE_WIDTH;
        float v = (y + 0.5f) / TEXTURE_HEIGHT;

        return new Vec2(u, v);
    }

    public static int roomToImage(BufferedImage image, int palette, int fadePalette, double fadeStrength, float grime, int effectColorA, int effectColorB, int dangerType, int index, String currRoom) {

        int[] coordsA = getCoordsFromLinear(index++);
        int xA = coordsA[0];
        int yA = coordsA[1];

        int[] coordsB = getCoordsFromLinear(index++);
        int xB = coordsB[0];
        int yB = coordsB[1];

        int[] coordsC = getCoordsFromLinear(index++);
        int xC = coordsC[0];
        int yC = coordsC[1];

        if(effectColorA == -10) {
            effectColorA = 0;
        }
        if(effectColorB == -10) {
            effectColorB = 0;
        }

        // New: Split palettes into two bytes instead of one.

        byte[] mainPalBytes = splitPaletteIntoBytes(palette & 0xFFFF);
        byte[] fadePalBytes = splitPaletteIntoBytes(fadePalette & 0xFFFF);

        Color colorPalettes = new Color(
                mainPalBytes[0] & 0xFF,
                mainPalBytes[1] & 0xFF,
                fadePalBytes[0] & 0xFF,
                fadePalBytes[1] & 0xFF
        );

        Color fx = new Color(
                (byte)effectColorA & 0xFF,
                (byte)effectColorB & 0xFF,
                (byte)Math.round(fadeStrength * 255.0f) & 0xFF,
                (byte)Math.round(grime * 255.0f) & 0xFF
        );

        Color roomFlags = new Color(
                (byte)dangerType & 0x03, // There's only 2 bits of data: None, Rain, Flood, RainAndFlood (0, 1, 2, 3)
                0, // Free to use later
                0, // Free to use later
                0 // Free to use later
        );

        image.setRGB(xA, yA, colorPalettes.getRGB());
        image.setRGB(xB, yB, fx.getRGB());
        image.setRGB(xC, yC, roomFlags.getRGB());

        return index;
    }

    public static Object[] imageToRoom(BufferedImage image, int index) {
        int[] coordsA = getCoordsFromLinear(index + 1);
        int xA = coordsA[0];
        int yA = coordsA[1];
        //System.out.println("X: " + xA + " Y: " + yA);

        int[] coordsB = getCoordsFromLinear(index + 2);
        int xB = coordsB[0];
        int yB = coordsB[1];

        int[] coordsC = getCoordsFromLinear(index + 3);
        int xC = coordsC[0];
        int yC = coordsC[1];

        // Extract color data from the image
        Color colorPalettes = new Color(image.getRGB(xA, yA));
        Color fx = new Color(image.getRGB(xB, yB));
        Color roomFlags = new Color(image.getRGB(xC, yC));

        // Extract palette and fade palette from colorPalettes
        int[] colors = extractARGB(image.getRGB(xA, yA));
        int palette = (colors[1] << 8) | (colors[2]);
        //System.out.println("Palette: " + palette + " raw data: " + colorPalettes.getRed() + " " + colorPalettes.getGreen() + " " + colors[1] + " " + colors[2] + "  " + image.getRGB(xA, yA));
        int fadePalette = (colors[3] << 8) | (colors[0]);
        //System.out.println("Fade Palette: " + fadePalette + " raw data: " + ((image.getRGB(xA, yA) >> 0) & 0xFF) + " " + ((image.getRGB(xA, yA) >> 24) & 0xFF) + " " + colors[3] + " " + colors[0] + "  " + image.getRGB(xA, yA));

        // Extract effect colors and other data from fx
        int effectColorA = fx.getRed() & 0xFF;
        //System.out.println("Effect Color A: " + effectColorA);
        int effectColorB = fx.getGreen() & 0xFF;
        //System.out.println("Effect Color B: " + effectColorB);
        float fadeStrength = (fx.getBlue() & 0xFF) / 255.0f;
        //System.out.println("Fade Strength: " + fadeStrength);
        float grime = (fx.getAlpha() & 0xFF) / 255.0f;
        //System.out.println("Grime: " + grime);

        // Extract danger type from roomFlags
        int dangerType = roomFlags.getRed() & 0x03;
        //System.out.println("Danger Type: " + dangerType);

        // Return all extracted data
        return new Object[]{palette, fadePalette, fadeStrength, grime, effectColorA, effectColorB, dangerType, index};
    }

    public static int[] extractARGB(int argb) {
        int alpha = (argb >> 24) & 0xFF; // Extract alpha
        int red = (argb >> 16) & 0xFF;   // Extract red
        int green = (argb >> 8) & 0xFF;  // Extract green
        int blue = argb & 0xFF;          // Extract blue
        return new int[]{alpha, red, green, blue};
    }

    public static void saveImageToFile(BufferedImage image, String format, String outputPath) throws IOException {
        File outputFile = new File(outputPath);

        // Create parent directories if they don't exist
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        ImageIO.write(image, format, outputFile);
    }

}
