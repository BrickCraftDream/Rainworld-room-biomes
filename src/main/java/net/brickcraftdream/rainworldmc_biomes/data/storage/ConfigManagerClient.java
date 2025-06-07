package net.brickcraftdream.rainworldmc_biomes.data.storage;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.brickcraftdream.rainworldmc_biomes.Rainworld_MC_Biomes.MOD_ID;

public class ConfigManagerClient {
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

}
