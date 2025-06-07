package net.brickcraftdream.rainworldmc_biomes.TESTING;

import net.brickcraftdream.rainworldmc_biomes.networking.BiomeImageProcessorClient;
import net.minecraft.resources.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ByteArrayToTextFile {

    /**
     * Converts a byte array to text using UTF-8 encoding and saves it to a file
     *
     * @param bytes The byte array to convert to text
     * @param filePath The path where the text file should be saved
     * @return true if the operation was successful, false otherwise
     */
    public static boolean saveByteArrayAsTextFile(byte[] bytes, String filePath) {
        try {
            // Convert byte array to text using UTF-8 encoding
            String text = new String(bytes, StandardCharsets.UTF_8);

            // Create file
            Path path = Paths.get(filePath);
            Files.writeString(path, text);

            System.out.println("File successfully saved at: " + filePath);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Alternative implementation using FileOutputStream
     */
    public static boolean saveByteArrayAsTextFileAlternative(byte[] bytes, String filePath) {
        try (FileOutputStream fos = new FileOutputStream(new File(filePath))) {
            // Convert byte array to text using UTF-8 encoding
            String text = new String(bytes, StandardCharsets.UTF_8);

            // Write the text to the file
            fos.write(text.getBytes(StandardCharsets.UTF_8));

            System.out.println("File successfully saved at: " + filePath);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Converts an image file to a byte array
     *
     * @param imagePath The path to the image file
     * @return byte array containing the image data or null if conversion failed
     */
    public static byte[] imageToByteArray(String imagePath) {
        try {
            // Read the image file
            BufferedImage bufferedImage = ImageIO.read(new File(imagePath));

            // Determine the image format (extension)
            String imageFormat = imagePath.substring(imagePath.lastIndexOf('.') + 1);

            // Convert BufferedImage to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, imageFormat, baos);
            byte[] imageBytes = baos.toByteArray();

            System.out.println("Image successfully converted to byte array, size: " + imageBytes.length + " bytes");
            return imageBytes;

        } catch (IOException e) {
            System.err.println("Error converting image to byte array: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Alternative method to convert an image file to a byte array using NIO
     *
     * @param imagePath The path to the image file
     * @return byte array containing the image data or null if conversion failed
     */
    public static byte[] imageToByteArrayNIO(String imagePath) {
        try {
            Path path = Paths.get(imagePath);
            byte[] imageBytes = Files.readAllBytes(path);

            System.out.println("Image successfully converted to byte array, size: " + imageBytes.length + " bytes");
            return imageBytes;

        } catch (IOException e) {
            System.err.println("Error converting image to byte array: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Example usage of the class
     */
    public static void main(String[] args) {
        // Example byte array (in this case, "Hello, World!" in UTF-8)
        byte[] exampleBytes = imageToByteArray("/home/deck/IdeaProjects/Rainworld-MC_Biomes mojamap 1.21.1/src/main/resources/assets/rainworld/textures/dynamic/shader_data.png");

        // Save to a file
        saveByteArrayAsTextFile(exampleBytes, "/home/deck/IdeaProjects/Rainworld-MC_Biomes mojamap 1.21.1/build/datagen/shader_data_as_text.txt");
    }
}
