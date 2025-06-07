package net.brickcraftdream.rainworldmc_biomes.image;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DynamicAssets {
    private static final ResourceLocation DYNAMIC_TEXTURE_ID = ResourceLocation.fromNamespaceAndPath("rainworld", "textures/dynamic/shader_data.png");

    public static void loadOrUpdateTexture() {
        File textureFile;
        if(System.getProperty("os.name").toLowerCase().contains("win")) {
            textureFile = new File(Minecraft.getInstance().gameDirectory, "config\\customtexture\\texture.png");
        }
        else {
            textureFile = new File(Minecraft.getInstance().gameDirectory, "config/customtexture/texture.png");
        }


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

        } catch (IOException e) {
            System.err.println("Failed to load texture: " + e.getMessage());
        }
    }
}
