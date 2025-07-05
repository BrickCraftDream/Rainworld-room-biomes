package net.brickcraftdream.rainworldmc_biomes.TESTING;

import net.brickcraftdream.rainworldmc_biomes.image.ImageGenerator;
import net.brickcraftdream.rainworldmc_biomes.networking.BiomeImageProcessorClient;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

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
import java.util.Arrays;

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
        for(int i = 0; i < 185; i++) {
            System.out.println(i + " -> ");
        }
        // Example byte array (in this case, "Hello, World!" in UTF-8)
        //byte[] exampleBytes = imageToByteArray("/home/deck/IdeaProjects/Rainworld-MC_Biomes mojamap 1.21.1/src/main/resources/assets/rainworld/textures/dynamic/shader_data.png");

        // Save to a file
        //saveByteArrayAsTextFile(exampleBytes, "/home/deck/IdeaProjects/Rainworld-MC_Biomes mojamap 1.21.1/build/datagen/shader_data_as_text.txt");
        BufferedImage palette1;
        BufferedImage palette2;
        try {
            palette1 = ImageIO.read(new File("/home/deck/IdeaProjects/Rainworld-MC_Biomes mojamap 1.21.1/src/main/resources/assets/rainworld/textures/palettes/palette0.png"));
            palette2 = ImageIO.read(new File("/home/deck/IdeaProjects/Rainworld-MC_Biomes mojamap 1.21.1/src/main/resources/assets/rainworld/textures/palettes/palette0.png"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (palette1 == null || palette2 == null) {
            System.out.println("Failed to load palette images for paletteA: " + 5 + " and fadePaletteA: " + 0);
            return;
        }

        System.out.println("Palette images loaded successfully.");

        float[] rawDataForNormalGuis = BiomeImageProcessorClient.paletteColor(palette1, palette2, 0f);
        float[] fullRawData = BiomeImageProcessorClient.blendImagesAndSaveReturnFloat(palette1, palette2, 0f, 0, 2, 29, 7, "/home/deck/IdeaProjects/Rainworld-MC_Biomes mojamap 1.21.1/build/datagen/shader_data.png");
        System.out.println("Raw data updated: " + Arrays.toString(rawDataForNormalGuis));
        int n = 0;
        while (n <= 8) {
            //System.out.println("Updating uniform 'colors" + n + "' with raw data.");
            //System.out.println("{ \"name\": \"colors" + (n + 1) + "\",       \"type\": \"matrix4x4\", \"count\": 16, \"values\": [ " +
            //                 rawDataForNormalGuis[0 + (n * 12)] + ", " + rawDataForNormalGuis[1 + (n * 12)] + ", " + rawDataForNormalGuis[2 + (n * 12)] + ", " + 1
            //        + ", " + rawDataForNormalGuis[3 + (n * 12)] + ", " + rawDataForNormalGuis[4 + (n * 12)] + ", " + rawDataForNormalGuis[5 + (n * 12)] + ", " + 1
            //        + ", " + rawDataForNormalGuis[6 + (n * 12)] + ", " + rawDataForNormalGuis[7 + (n * 12)] + ", " + rawDataForNormalGuis[8 + (n * 12)] + ", " + 1
            //        + ", " + rawDataForNormalGuis[9 + (n * 12)] + ", " + rawDataForNormalGuis[10 + (n * 12)] + ", " + rawDataForNormalGuis[11 + (n * 12)] + ", " + 1 + " ] },");
            //System.out.println(new Matrix4f(
            //        rawDataForNormalGuis[0 * n], rawDataForNormalGuis[1 * n], rawDataForNormalGuis[2 * n], 1,
            //        rawDataForNormalGuis[3 * n], rawDataForNormalGuis[4 * n], rawDataForNormalGuis[5 * n], 1,
            //        rawDataForNormalGuis[6 * n], rawDataForNormalGuis[7 * n], rawDataForNormalGuis[8 * n], 1,
            //        rawDataForNormalGuis[9 * n], rawDataForNormalGuis[10 * n], rawDataForNormalGuis[11 * n], 1
            //));
            //System.out.println("Uniform 'colors" + n + "' updated.");
            n++;
        }
        /*
        n = 0;
        while (n <= 33) {
            try {
                System.out.println("{ \"name\": \"data" + (n + 1) + "\",       \"type\": \"matrix4x4\", \"count\": 16, \"values\": [ " +
                                 fullRawData[0  + (n * 16)] + ", " + fullRawData[1  + (n * 16)]  + ", " + fullRawData[2  + (n * 16)] + ", " + fullRawData[3  + (n * 16)]
                        + ", " + fullRawData[4  + (n * 16)] + ", " + fullRawData[5  + (n * 16)]  + ", " + fullRawData[6  + (n * 16)] + ", " + fullRawData[7  + (n * 16)]
                        + ", " + fullRawData[8  + (n * 16)] + ", " + fullRawData[9  + (n * 16)]  + ", " + fullRawData[10 + (n * 16)] + ", " + fullRawData[11 + (n * 16)]
                        + ", " + fullRawData[12 + (n * 16)] + ", " + fullRawData[13 + (n * 16)]  + ", " + fullRawData[14 + (n * 16)] + ", " + fullRawData[15 + (n * 16)]

                        + " ] },");
                n++;
            }
            catch (Exception e) {
                try {
                    System.out.println("{ \"name\": \"data" + (n + 1) + "\",       \"type\": \"matrix4x4\", \"count\": 16, \"values\": [ " +
                            fullRawData[0  + (n * 16)] + ", " + fullRawData[1  + (n * 16)]  + ", " + fullRawData[2  + (n * 16)] + ", " + fullRawData[3  + (n * 16)]
                            + ", " + fullRawData[4  + (n * 16)] + ", " + fullRawData[5  + (n * 16)]  + ", " + fullRawData[6  + (n * 16)] + ", " + fullRawData[7  + (n * 16)]
                            + ", " + fullRawData[8  + (n * 16)] + ", " + fullRawData[9  + (n * 16)]  + ", " + fullRawData[10 + (n * 16)] + ", " + fullRawData[11 + (n * 16)]
                            + ", " + fullRawData[12 + (n * 16)] + ", " + fullRawData[13 + (n * 16)]  + ", " + fullRawData[14 + (n * 16)] + ", " + 0

                            + " ] },");
                    n++;
                }
                catch (Exception f) {
                    try {
                        System.out.println("{ \"name\": \"data" + (n + 1) + "\",       \"type\": \"matrix4x4\", \"count\": 16, \"values\": [ " +
                                fullRawData[0  + (n * 16)] + ", " + fullRawData[1  + (n * 16)]  + ", " + fullRawData[2  + (n * 16)] + ", " + fullRawData[3  + (n * 16)]
                                + ", " + fullRawData[4  + (n * 16)] + ", " + fullRawData[5  + (n * 16)]  + ", " + fullRawData[6  + (n * 16)] + ", " + fullRawData[7  + (n * 16)]
                                + ", " + fullRawData[8  + (n * 16)] + ", " + fullRawData[9  + (n * 16)]  + ", " + fullRawData[10 + (n * 16)] + ", " + fullRawData[11 + (n * 16)]
                                + ", " + fullRawData[12 + (n * 16)] + ", " + fullRawData[13 + (n * 16)]  + ", " + 0 + ", " + 0

                                + " ] },");
                        n++;
                    }
                    catch (Exception g) {
                        try {
                            System.out.println("{ \"name\": \"data" + (n + 1) + "\",       \"type\": \"matrix4x4\", \"count\": 16, \"values\": [ " +
                                    fullRawData[0  + (n * 16)] + ", " + fullRawData[1  + (n * 16)]  + ", " + fullRawData[2  + (n * 16)] + ", " + fullRawData[3  + (n * 16)]
                                    + ", " + fullRawData[4  + (n * 16)] + ", " + fullRawData[5  + (n * 16)]  + ", " + fullRawData[6  + (n * 16)] + ", " + fullRawData[7  + (n * 16)]
                                    + ", " + fullRawData[8  + (n * 16)] + ", " + fullRawData[9  + (n * 16)]  + ", " + fullRawData[10 + (n * 16)] + ", " + fullRawData[11 + (n * 16)]
                                    + ", " + fullRawData[12 + (n * 16)] + ", " + 0  + ", " + 0 + ", " + 0

                                    + " ] },");
                            n++;
                        }
                        catch (Exception h) {
                            try {
                                System.out.println("{ \"name\": \"data" + (n + 1) + "\",       \"type\": \"matrix4x4\", \"count\": 16, \"values\": [ " +
                                        fullRawData[0  + (n * 16)] + ", " + fullRawData[1  + (n * 16)]  + ", " + fullRawData[2  + (n * 16)] + ", " + fullRawData[3  + (n * 16)]
                                        + ", " + fullRawData[4  + (n * 16)] + ", " + fullRawData[5  + (n * 16)]  + ", " + fullRawData[6  + (n * 16)] + ", " + fullRawData[7  + (n * 16)]
                                        + ", " + fullRawData[8  + (n * 16)] + ", " + fullRawData[9  + (n * 16)]  + ", " + fullRawData[10 + (n * 16)] + ", " + fullRawData[11 + (n * 16)]
                                        + ", " + fullRawData[12 + (n * 16)] + ", " + 0  + ", " + 0 + ", " + 0

                                        + " ] },");
                                n++;
                            }
                            catch (Exception i) {
                                try {
                                    System.out.println("{ \"name\": \"data" + (n + 1) + "\",       \"type\": \"matrix4x4\", \"count\": 16, \"values\": [ " +
                                            fullRawData[0  + (n * 16)] + ", " + fullRawData[1  + (n * 16)]  + ", " + fullRawData[2  + (n * 16)] + ", " + fullRawData[3  + (n * 16)]
                                            + ", " + fullRawData[4  + (n * 16)] + ", " + fullRawData[5  + (n * 16)]  + ", " + fullRawData[6  + (n * 16)] + ", " + fullRawData[7  + (n * 16)]
                                            + ", " + fullRawData[8  + (n * 16)] + ", " + fullRawData[9  + (n * 16)]  + ", " + fullRawData[10 + (n * 16)] + ", " + fullRawData[11 + (n * 16)]
                                            + ", " + 0 + ", " + 0  + ", " + 0 + ", " + 0

                                            + " ] },");
                                    n++;
                                }
                                catch (Exception j) {
                                    j.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }

         */
        n = 0;
        while (n <= 46) {
            try {
                System.out.println("{ \"name\": \"data" + (n + 1) + "\",       \"type\": \"matrix4x4\", \"count\": 16, \"values\": [ " +
                        fullRawData[0 + (n * 12)] + ", " + fullRawData[1 + (n * 12)] + ", " + fullRawData[2 + (n * 12)] + ", " + 1
                        + ", " + fullRawData[3 + (n * 12)] + ", " + fullRawData[4 + (n * 12)] + ", " + fullRawData[5 + (n * 12)] + ", " + 1
                        + ", " + fullRawData[6 + (n * 12)] + ", " + fullRawData[7 + (n * 12)] + ", " + fullRawData[8 + (n * 12)] + ", " + 1
                        + ", " + fullRawData[9 + (n * 12)] + ", " + fullRawData[10 + (n * 12)] + ", " + fullRawData[11 + (n * 12)] + ", " + 1 + " ] },");
                n++;
            }
            catch (Exception e) {
                e.printStackTrace();
                //System.out.println(fullRawData[0 + (n * 12)]);
                //System.out.println(fullRawData[1 + (n * 12)]);
                //System.out.println(fullRawData[2 + (n * 12)]);
//
                //System.out.println(fullRawData[3 + (n * 12)]);
                //System.out.println(fullRawData[4 + (n * 12)]);
                //System.out.println(fullRawData[5 + (n * 12)]);
//
                //System.out.println(fullRawData[6 + (n * 12)]);
                //System.out.println(fullRawData[7 + (n * 12)]);
                //System.out.println(fullRawData[8 + (n * 12)]);
//
                //System.out.println(fullRawData[9 + (n * 12)]);
                //System.out.println(fullRawData[10 + (n * 12)]);
                //System.out.println(fullRawData[11 + (n * 12)]);
                n = 99;
            }
        }

        try {
            ImageGenerator.saveImageToFile(BiomeImageProcessorClient.colorsToImage(BiomeImageProcessorClient.paletteColor(palette1, palette2, 0f)), "png", "/home/deck/IdeaProjects/Rainworld-MC_Biomes mojamap 1.21.1/build/datagen/palette_colors.png");
            ImageGenerator.saveImageToFile(BiomeImageProcessorClient.highlightReadPixelsWithAqua(palette1), "png", "/home/deck/IdeaProjects/Rainworld-MC_Biomes mojamap 1.21.1/build/datagen/highlighted_palette.png");
        }
        catch (Exception e) {
            System.err.println("Error saving highlighted palette image: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
