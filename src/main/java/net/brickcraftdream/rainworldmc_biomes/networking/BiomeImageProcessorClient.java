package net.brickcraftdream.rainworldmc_biomes.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BiomeImageProcessorClient {

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
