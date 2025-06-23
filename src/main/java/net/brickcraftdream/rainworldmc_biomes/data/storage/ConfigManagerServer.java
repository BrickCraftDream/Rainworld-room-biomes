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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static net.brickcraftdream.rainworldmc_biomes.Rainworld_MC_Biomes.MOD_ID;

public class ConfigManagerServer {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static class WarpData {
        public int x;
        public int y;
        public int z;
        public boolean markAsUnfinished;

        public WarpData(int x, int y, int z, boolean markAsUnfinished) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.markAsUnfinished = markAsUnfinished;
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("x", x);
            json.addProperty("y", y);
            json.addProperty("z", z);
            json.addProperty("markAsUnfinished", markAsUnfinished);
            return json;
        }

        public static WarpData fromJson(JsonObject json) {
            int x = ConfigManagerServer.getInt(json, "x", 0);
            int y = ConfigManagerServer.getInt(json, "y", 0);
            int z = ConfigManagerServer.getInt(json, "z", 0);
            boolean mark = ConfigManagerServer.getBoolean(json, "markAsUnfinished", false);
            return new WarpData(x, y, z, mark);
        }
    }

    public static boolean addOrUpdateWarp(String fileName, String warpName, WarpData data) {
        JsonObject config = readConfig(fileName);
        if (config == null) config = new JsonObject();

        config.add(warpName, data.toJson());
        return writeConfig(fileName, config);
    }

    public static List<String> getAllWarpNames(String fileName) {
        JsonObject config = readConfig(fileName);
        if (config == null) return Collections.emptyList();

        List<String> names = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : config.entrySet()) {
            if (entry.getValue().isJsonObject()) {
                names.add(entry.getKey());
            }
        }
        return names;
    }

    public static WarpData getWarpData(String fileName, String warpName) {
        JsonObject config = readConfig(fileName);
        if (config != null && config.has(warpName)) {
            JsonObject warpJson = config.getAsJsonObject(warpName);
            return WarpData.fromJson(warpJson);
        }
        return null;
    }

    // For mod configuration data
    public static void saveDataToConfigFolder(String fileName, byte[] data) {
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
    public static byte[] readDataFromConfigFolder(String fileName) {
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
            if(bufferedImage != null) {
                ImageGenerator.saveImageToFile(bufferedImage, "png", filePath.toString());
            }
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
