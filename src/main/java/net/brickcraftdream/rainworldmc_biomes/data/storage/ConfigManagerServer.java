package net.brickcraftdream.rainworldmc_biomes.data.storage;

import com.google.gson.*;
import net.brickcraftdream.rainworldmc_biomes.Rainworld_MC_Biomes;
import net.brickcraftdream.rainworldmc_biomes.image.ImageGenerator;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerLevel;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.brickcraftdream.rainworldmc_biomes.Rainworld_MC_Biomes.MOD_ID;

public class ConfigManagerServer {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();


    // For mod configuration data
    public void saveDataToConfigFolder(String fileName, byte[] data) {
        try {
            // Get the config directory for your mod
            Path configDir = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);

            // Create the directory if it doesn't exist
            Files.createDirectories(configDir);

            // Create the file path
            Path filePath = configDir.resolve(fileName);

            // Write the data to the file
            Files.write(filePath, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Reading config data
    public byte[] readDataFromConfigFolder(String fileName) {
        try {
            Path configDir = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
            Path filePath = configDir.resolve(fileName);

            if (Files.exists(filePath)) {
                return Files.readAllBytes(filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null; // Or default data
    }

    public static void saveBufferedImageToConfigFolder(BufferedImage bufferedImage, String fileName) {
        try {
            // Get the config directory for your mod
            Path configDir = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);

            // Create the directory if it doesn't exist
            Files.createDirectories(configDir);

            // Create the file path
            Path filePath = configDir.resolve(fileName);

            // Write the BufferedImage to the file
            ImageGenerator.saveImageToFile(bufferedImage, "png", filePath.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the path to a config file in the mod's config directory
     * @param fileName Name of the config file (without path)
     * @return Path to the config file
     */
    public static Path getConfigPath(String fileName) {
        return FabricLoader.getInstance()
                .getConfigDir()
                .resolve(Rainworld_MC_Biomes.MOD_ID)
                .resolve(fileName);
    }

    /**
     * Creates a default config file if it doesn't exist
     * @param fileName Name of the config file
     * @param defaultConfig Default JSON object to write
     * @return true if the default config was created, false if the file already existed
     */
    public static boolean createDefaultConfigIfNotExists(String fileName, JsonObject defaultConfig) {
        Path configPath = getConfigPath(fileName);

        try {
            if (Files.notExists(configPath)) {
                Files.createDirectories(configPath.getParent());
                String jsonStr = GSON.toJson(defaultConfig);
                Files.writeString(configPath, jsonStr);
                return true;
            }
        } catch (IOException e) {
            System.err.println("Failed to create default config file: " + fileName);
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Reads a JSON config file
     * @param fileName Name of the config file
     * @return JsonObject containing the config data, or null if the file couldn't be read
     */
    public static JsonObject readConfig(String fileName) {
        Path configPath = getConfigPath(fileName);

        try {
            if (Files.exists(configPath)) {
                String jsonStr = Files.readString(configPath);
                return JsonParser.parseString(jsonStr).getAsJsonObject();
            }
        } catch (IOException | JsonSyntaxException e) {
            System.err.println("Failed to read config file: " + fileName);
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Writes a JSON object to a config file
     * @param fileName Name of the config file
     * @param config JsonObject to write
     * @return true if the write was successful, false otherwise
     */
    public static boolean writeConfig(String fileName, JsonObject config) {
        Path configPath = getConfigPath(fileName);

        try {
            Files.createDirectories(configPath.getParent());
            String jsonStr = GSON.toJson(config);
            Files.writeString(configPath, jsonStr);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to write config file: " + fileName);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Helper method to get a string value from a JsonObject with a default
     * @param json JsonObject to read from
     * @param key Key to look up
     * @param defaultValue Default value if key doesn't exist
     * @return The string value or default
     */
    public static String getString(JsonObject json, String key, String defaultValue) {
        if (json != null && json.has(key) && json.get(key).isJsonPrimitive()) {
            return json.get(key).getAsString();
        }
        return defaultValue;
    }

    /**
     * Helper method to get an int value from a JsonObject with a default
     * @param json JsonObject to read from
     * @param key Key to look up
     * @param defaultValue Default value if key doesn't exist
     * @return The int value or default
     */
    public static int getInt(JsonObject json, String key, int defaultValue) {
        if (json != null && json.has(key) && json.get(key).isJsonPrimitive()) {
            try {
                return json.get(key).getAsInt();
            } catch (NumberFormatException e) {
                // Return default if the value isn't a valid int
            }
        }
        return defaultValue;
    }

    /**
     * Helper method to get a boolean value from a JsonObject with a default
     * @param json JsonObject to read from
     * @param key Key to look up
     * @param defaultValue Default value if key doesn't exist
     * @return The boolean value or default
     */
    public static boolean getBoolean(JsonObject json, String key, boolean defaultValue) {
        if (json != null && json.has(key) && json.get(key).isJsonPrimitive()) {
            return json.get(key).getAsBoolean();
        }
        return defaultValue;
    }

    /**
     * Helper method to get a JsonObject from within another JsonObject
     * @param json JsonObject to read from
     * @param key Key to look up
     * @return The nested JsonObject or null if not found
     */
    public static JsonObject getObject(JsonObject json, String key) {
        if (json != null && json.has(key) && json.get(key).isJsonObject()) {
            return json.get(key).getAsJsonObject();
        }
        return null;
    }

    /**
     * Helper method to get a JsonArray from within a JsonObject
     * @param json JsonObject to read from
     * @param key Key to look up
     * @return The JsonArray or null if not found
     */
    public static JsonArray getArray(JsonObject json, String key) {
        if (json != null && json.has(key) && json.get(key).isJsonArray()) {
            return json.get(key).getAsJsonArray();
        }
        return null;
    }

}
