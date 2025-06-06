package net.brickcraftdream.rainworldmc_biomes;

import org.joml.Vector3f;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ColorPicker {
    public static String getColor(String imagePath, String xCoord, String yCoord) {
        int x = Integer.parseInt(xCoord);
        int y = Integer.parseInt(yCoord);

        if(imagePath.endsWith("59.png")) {
            imagePath = "/home/deck/IdeaProjects/Rainworld-MC_Biomes mojamap/rooms/palettes/palette58.png";
        }

        try {
            BufferedImage image = ImageIO.read(new File(imagePath));
            int rgb = image.getRGB(x, y);
            String hexColor = String.format("%06x", rgb).substring(2);
            return "#" + hexColor;
        } catch (IOException e) {
            return "Error reading image " + imagePath + ": " + e.getMessage();
        }
    }

    public static int combineColors(String imagePath1, String xCoord1, String yCoord1, String imagePath2, String xCoord2, String yCoord2, double opacity) {
        long colorValue1 = Long.parseLong(getColor(imagePath1, xCoord1, yCoord1).substring(1), 16);
        long colorValue2 = Long.parseLong(getColor(imagePath2, xCoord2, yCoord2).substring(1), 16);

        int r1 = (int) ((colorValue1 >> 16) & 0xFF);
        int g1 = (int) ((colorValue1 >> 8) & 0xFF);
        int b1 = (int) (colorValue1 & 0xFF);

        int r2 = (int) ((colorValue2 >> 16) & 0xFF);
        int g2 = (int) ((colorValue2 >> 8) & 0xFF);
        int b2 = (int) (colorValue2 & 0xFF);

        int r = (int) (r1 * (1 - opacity) + r2 * opacity);
        int g = (int) (g1 * (1 - opacity) + g2 * opacity);
        int b = (int) (b1 * (1 - opacity) + b2 * opacity);

        int combinedColor = (r << 16) | (g << 8) | b;
        return combinedColor;
    }

    public static Vector3f combineColorsVec3(String imagePath1, String xCoord1, String yCoord1, String imagePath2, String xCoord2, String yCoord2, double opacity) {
        long colorValue1 = Long.parseLong(getColor(imagePath1, xCoord1, yCoord1).substring(1), 16);
        long colorValue2 = Long.parseLong(getColor(imagePath2, xCoord2, yCoord2).substring(1), 16);

        int r1 = (int) ((colorValue1 >> 16) & 0xFF);
        int g1 = (int) ((colorValue1 >> 8) & 0xFF);
        int b1 = (int) (colorValue1 & 0xFF);

        int r2 = (int) ((colorValue2 >> 16) & 0xFF);
        int g2 = (int) ((colorValue2 >> 8) & 0xFF);
        int b2 = (int) (colorValue2 & 0xFF);

        int r = (int) (r1 * (1 - opacity) + r2 * opacity);
        int g = (int) (g1 * (1 - opacity) + g2 * opacity);
        int b = (int) (b1 * (1 - opacity) + b2 * opacity);

        return new Vector3f(r, g, b);
    }
}