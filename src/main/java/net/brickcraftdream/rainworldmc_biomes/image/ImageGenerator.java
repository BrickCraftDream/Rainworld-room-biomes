package net.brickcraftdream.rainworldmc_biomes.image;

import net.minecraft.util.ColorRGBA;

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

    // New method:
    public static byte[] splitPaletteIntoBytes(int palette) {
        byte hi = (byte) ((palette >> 8) & 0xFF);
        byte lo = (byte) ((palette >> 0) & 0xFF);
        return new byte[]{hi, lo};
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
