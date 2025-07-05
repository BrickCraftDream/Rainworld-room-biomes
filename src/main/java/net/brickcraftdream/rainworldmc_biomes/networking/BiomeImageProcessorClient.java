package net.brickcraftdream.rainworldmc_biomes.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.*;
import java.util.Arrays;

public class BiomeImageProcessorClient {

    public static float[] paletteColorOld(BufferedImage palette, BufferedImage paletteFade, float fade) {
        float fadeFactor = fade / 100.0f;
        float invFadeFactor = 1.0f - fadeFactor;

        int texWidth = palette.getWidth();
        int texHeight = palette.getHeight();

        float[] colors = new float[36 * 3];

        Raster palRaster = palette.getRaster();
        Raster fadeRaster = paletteFade.getRaster();

        for (int i = 0; i < 36; i++) {
            float u = ((i / 3) % 6) * 5.0f / 32.0f;
            float v = (i < 18)
                    ? (4 - (i % 3)) / 16.0f
                    : (7 - (i % 3)) / 16.0f;

            int x = Math.round(u * texWidth);
            int y = Math.round(v * texHeight);

            float[] palColor = new float[4];
            float[] fadeColor = new float[4];

            palRaster.getPixel(x, y, palColor);
            fadeRaster.getPixel(x, y, fadeColor);

            for (int j = 0; j < 3; j++) {
                float blended = (palColor[j] / 255.0f) * invFadeFactor + (fadeColor[j] / 255.0f) * fadeFactor;
                colors[i * 3 + j] = blended;
            }
        }
        return colors;
    }

    public static float[] paletteColor_previous(BufferedImage palette, BufferedImage paletteFade, float fade) {
        int texWidth = palette.getWidth();
        int texHeight = palette.getHeight();

        float fadeFactor = fade / 100.0f;
        float invFadeFactor = 1.0f - fadeFactor;

        float[] colors = new float[36 * 3];

        for (int i = 0; i < 36; i++) {
            float u = ((i / 3) % 6) * 5.0f / 32.0f;
            float v = (i < 18)
                    ? (4 - (i % 3)) / 16.0f
                    : (7 - (i % 3)) / 16.0f;

            int x = Math.round(u * texWidth);
            int y = Math.round(v * texHeight);

            Color color1 = new Color(palette.getRGB(x, y));
            Color color2 = new Color(paletteFade.getRGB(x, y));

            for (int j = 0; j < 3; j++) {
                float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;
                float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;
                colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;
            }
        }
        return colors;
    }

    public static float[] blendImagesAndSaveReturnFloat(BufferedImage imageA, BufferedImage imageB, float fadeAmountPercent, int startX, int startY, int endX, int endY, String outputFilePath) {
        // Clamp fade
        fadeAmountPercent = Math.max(0, Math.min(fadeAmountPercent, 100));
        float fade = fadeAmountPercent / 100f;

        // Clamp coordinates to image bounds
        int maxWidth = Math.min(imageA.getWidth(), imageB.getWidth());
        int maxHeight = Math.min(imageA.getHeight(), imageB.getHeight());

        startX = Math.max(0, Math.min(startX, maxWidth - 1));
        endX = Math.max(0, Math.min(endX, maxWidth - 1));
        startY = Math.max(0, Math.min(startY, maxHeight - 1));
        endY = Math.max(0, Math.min(endY, maxHeight - 1));

        int width = endX - startX + 1;
        int height = endY - startY + 1;

        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        float[] blendedRGB = new float[width * height * 3];

        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                int rgbA = imageA.getRGB(x, y);
                int rgbB = imageB.getRGB(x, y);

                int rA = (rgbA >> 16) & 0xFF;
                int gA = (rgbA >> 8) & 0xFF;
                int bA = rgbA & 0xFF;

                int rB = (rgbB >> 16) & 0xFF;
                int gB = (rgbB >> 8) & 0xFF;
                int bB = rgbB & 0xFF;

                // Blend (lerp)
                float rF = ((1 - fade) * rA + fade * rB);
                float gF = ((1 - fade) * gA + fade * gB);
                float bF = ((1 - fade) * bA + fade * bB);

                // Compute flipped Y coordinate
                int localX = x - startX;
                int localY = y - startY;
                int flippedY = height - 1 - localY;

                // Store in flipped order in output image
                int r = Math.round(rF);
                int g = Math.round(gF);
                int b = Math.round(bF);
                int rgb = (r << 16) | (g << 8) | b;
                outputImage.setRGB(localX, flippedY, rgb);

                // Store in flipped order in float array
                int flippedIndex = (flippedY * width + localX) * 3;
                blendedRGB[flippedIndex] = rF / 255f;
                blendedRGB[flippedIndex + 1] = gF / 255f;
                blendedRGB[flippedIndex + 2] = bF / 255f;
            }
        }

        // Save output image
        //try {
        //    File outputFile = new File(outputFilePath);
        //    ImageIO.write(outputImage, "png", outputFile);
        //    System.out.println("Blended image saved to: " + outputFilePath);
        //} catch (Exception e) {
        //    e.printStackTrace();
        //}

        return blendedRGB;
    }

    public static float[] paletteColor(BufferedImage palette, BufferedImage paletteFade, float fade) {
        int texWidth = palette.getWidth();
        int texHeight = palette.getHeight();

        float fadeFactor = fade / 100.0f;
        float invFadeFactor = 1.0f - fadeFactor;

        float[] colors = new float[36 * 3];

        //for (int i = 0; i < 36; i++) {
        //    float u = ((i / 3) % 6) * 5.0f / 32.0f;
        //    float v = (i < 18)
        //            ? (4 - (i % 3)) / 16.0f
        //            : (7 - (i % 3)) / 16.0f;
//
        //    int x = Math.round(u * texWidth);
        //    int y = Math.round(v * texHeight);
//
        //    Color color1 = new Color(palette.getRGB(x, y));
        //    Color color2 = new Color(paletteFade.getRGB(x, y));
//
        //    for (int j = 0; j < 3; j++) {
        //        float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;
        //        float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;
        //        colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;
        //    }
        //}
        // The ugly way
        Color color1;
        Color color2;
        int x = 0;
        int y = 0;
        int i = 0;

        x = 0; y = 4;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;

        x = 0; y = 3;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;

        x = 0; y = 2;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;


        x = 5; y = 4;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;

        x = 5; y = 3;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;

        x = 5; y = 2;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;


        x = 10; y = 4;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;

        x = 10; y = 3;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;

        x = 10; y = 2;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;


        x = 15; y = 4;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;

        x = 15; y = 3;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;

        x = 15; y = 2;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;


        x = 20; y = 4;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;

        x = 20; y = 3;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;

        x = 20; y = 2;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;


        x = 25; y = 4;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;

        x = 25; y = 3;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;

        x = 25; y = 2;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;


        int offset = 3;

        x = 0; y = 4 + offset;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;

        x = 0; y = 3 + offset;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;

        x = 0; y = 2 + offset;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;


        x = 5; y = 4 + offset;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;

        x = 5; y = 3 + offset;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;

        x = 5; y = 2 + offset;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;


        x = 10; y = 4 + offset;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;

        x = 10; y = 3 + offset;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;

        x = 10; y = 2 + offset;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;


        x = 15; y = 4 + offset;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;

        x = 15; y = 3 + offset;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;

        x = 15; y = 2 + offset;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;


        x = 20; y = 4 + offset;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;

        x = 20; y = 3 + offset;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;

        x = 20; y = 2 + offset;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;


        x = 25; y = 4 + offset;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;

        x = 25; y = 3 + offset;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;

        x = 25; y = 2 + offset;
        color1 =     new Color(palette.getRGB(x, y));
        color2 = new Color(paletteFade.getRGB(x, y));
        for (int j = 0; j < 3; j++) {float c1 = new float[] { color1.getRed(), color1.getGreen(), color1.getBlue() }[j] / 255.0f;float c2 = new float[] { color2.getRed(), color2.getGreen(), color2.getBlue() }[j] / 255.0f;colors[i * 3 + j] = c1 * invFadeFactor + c2 * fadeFactor;}
        i++;


        return colors;
    }

    public static BufferedImage colorsToImage(float[] colors) {
        if (colors.length != 108) {
            throw new IllegalArgumentException("Input must contain exactly 108 float values (36 RGB colors).");
        }

        int pixelCount = colors.length / 3;
        int width = 6;  // e.g. 6 × 6 grid
        int height = (int)Math.ceil(pixelCount / (double)width);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int i = 0; i < pixelCount; i++) {
            int r = (int)(Math.max(0, Math.min(1, colors[i * 3    ])) * 255);
            int g = (int)(Math.max(0, Math.min(1, colors[i * 3 + 1])) * 255);
            int b = (int)(Math.max(0, Math.min(1, colors[i * 3 + 2])) * 255);

            int x = i % width;
            int y = i / width;

            int argb = new Color(r, g, b, 255).getRGB();
            image.setRGB(x, y, argb);
        }

        return image;
    }

    public static BufferedImage highlightReadPixelsWithAqua(BufferedImage palette) {
        int texWidth = palette.getWidth();
        int texHeight = palette.getHeight();

        BufferedImage copy = new BufferedImage(texWidth, texHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = copy.createGraphics();
        g.drawImage(palette, 0, 0, null);
        g.dispose();

        for (int i = 0; i < 36; i++) {
            float u = ((i / 3) % 6) * 5.0f / 32.0f;
            float v = (i < 18)
                    ? (4 - (i % 3)) / 16.0f
                    : (7 - (i % 3)) / 16.0f;

            int x = Math.round(u * texWidth);
            int y = Math.round(v * texHeight);

            // Set to aqua (cyan) with full opacity
            copy.setRGB(x, y, new Color(0, 255, 255, 255).getRGB());
        }

        return copy;
    }

    public static byte[] resourceLocationToByteArray(ResourceLocation resourceLocation) {
        try {
            // Get the resource as an input stream
            InputStream inputStream = Minecraft.getInstance().getResourceManager()
                    .getResource(resourceLocation)
                    .orElseThrow(() -> new IOException("Resource not found: " + resourceLocation))
                    .open();

            // Read the input stream into a ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            // Close streams
            inputStream.close();
            outputStream.close();

            // Return the byte array
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] bufferedImageToByteArray(BufferedImage bufferedImage, String format) {
        if (bufferedImage == null) {
            return null;
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, format, outputStream);
            outputStream.flush();
            byte[] imageData = outputStream.toByteArray();
            outputStream.close();
            return imageData;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static BufferedImage resourceLocationToBufferedImage(ResourceLocation resourceLocation) {
        try {
            // Get the resource as an input stream
            InputStream inputStream = Minecraft.getInstance().getResourceManager()
                    .getResource(resourceLocation)
                    .orElseThrow(() -> new IOException("Resource not found: " + resourceLocation))
                    .open();

            BufferedImage bufferedImage = ImageIO.read(inputStream);
            inputStream.close();
            return bufferedImage;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BufferedImage byteArrayToBufferedImage(byte[] imageData) {
        if (imageData == null || imageData.length == 0) {
            return null;
        }

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            inputStream.close();
            return bufferedImage;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
