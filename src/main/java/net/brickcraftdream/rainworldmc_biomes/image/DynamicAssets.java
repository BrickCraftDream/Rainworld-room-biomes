package net.brickcraftdream.rainworldmc_biomes.image;

import com.mojang.blaze3d.platform.NativeImage;
import net.brickcraftdream.rainworldmc_biomes.data.storage.ConfigManagerServer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

import static net.brickcraftdream.rainworldmc_biomes.Rainworld_MC_Biomes.MOD_ID;

public class DynamicAssets {
    private static final ResourceLocation DYNAMIC_TEXTURE_ID = ResourceLocation.fromNamespaceAndPath("rainworld", "textures/dynamic/shader_data.png");

    public static void loadOrUpdateTexture() {
        File textureFile;
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
        Path filePath = configDir.resolve("shader_data.png");
        textureFile = filePath.toFile();

        if (!textureFile.exists()) {
            System.err.println("Texture file does not exist: " + textureFile.getAbsolutePath());
            return;
        }

        try (FileInputStream fis = new FileInputStream(textureFile)) {
            NativeImage image = NativeImage.read(fis);
            DynamicTexture texture = new DynamicTexture(image);

            TextureManager textureManager = Minecraft.getInstance().getTextureManager();
            textureManager.register(DYNAMIC_TEXTURE_ID, texture);
            System.out.println("Registered dynamic texture at " + DYNAMIC_TEXTURE_ID);

        } catch (Exception e) {
            System.err.println("Failed to load texture: " + e.getMessage());
        }
    }

    public static void loadOrUpdateTexture_2() {
        File textureFile;
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
        Path filePath = configDir.resolve("shader_data.png");
        textureFile = filePath.toFile();

        if (!textureFile.exists()) {
            System.err.println("Texture file does not exist: " + textureFile.getAbsolutePath());
            return;
        }

        try (FileInputStream fis = new FileInputStream(textureFile)) {
            TextureManager textureManager = Minecraft.getInstance().getTextureManager();

            // Check if the texture already exists
            DynamicTexture existingTexture = null;
            try {
                existingTexture = (DynamicTexture) textureManager.getTexture(DYNAMIC_TEXTURE_ID);
            } catch (Exception e) {
                // Texture doesn't exist or isn't a DynamicTexture, will create new one
            }

            NativeImage newImage = NativeImage.read(fis);

            if (existingTexture != null) {
                // Update existing texture
                try {
                    // Close the old pixels to prevent memory leaks
                    existingTexture.getPixels().close();

                    // Replace with new pixels
                    existingTexture.setPixels(newImage);
                    existingTexture.upload();
                    System.out.println("Updated existing dynamic texture at " + DYNAMIC_TEXTURE_ID);
                } catch (Exception e) {
                    // If updating fails, fall back to creating a new texture
                    System.err.println("Failed to update existing texture, creating new one: " + e.getMessage());
                    existingTexture = null;
                }
            }

            // If there was no existing texture or updating failed, create a new one
            if (existingTexture == null) {
                DynamicTexture texture = new DynamicTexture(newImage);
                textureManager.register(DYNAMIC_TEXTURE_ID, texture);
                System.out.println("Registered new dynamic texture at " + DYNAMIC_TEXTURE_ID);
            }
        } catch (IOException e) {
            System.err.println("Failed to load texture: " + e.getMessage());
        }
    }

}
