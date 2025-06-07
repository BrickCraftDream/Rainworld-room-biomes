package net.brickcraftdream.rainworldmc_biomes.client;

import net.brickcraftdream.rainworldmc_biomes.image.DynamicAssets;
import net.brickcraftdream.rainworldmc_biomes.image.ImageGenerator;
import net.brickcraftdream.rainworldmc_biomes.networking.BiomeImageProcessorClient;
import net.brickcraftdream.rainworldmc_biomes.networking.NetworkManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

import java.awt.image.BufferedImage;
import java.io.IOException;

@Environment(EnvType.CLIENT)
public class Rainworld_MC_BiomesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(NetworkManager.BiomeUpdatePacket.ID, (payload, context) -> {
            context.client().execute(() -> {
                // Client receives the biome data back plus the new byte array
                BufferedImage image = BiomeImageProcessorClient.byteArrayToBufferedImage(payload.imageData());
                try {
                    if(System.getProperty("os.name").toLowerCase().contains("win")) {
                        ImageGenerator.saveImageToFile(image, "png", Minecraft.getInstance().gameDirectory.getPath() + "\\config\\customtexture\\texture.png");
                    }
                    else {
                        ImageGenerator.saveImageToFile(image, "png", Minecraft.getInstance().gameDirectory.getPath() + "/config/customtexture/texture.png");
                    }
                    DynamicAssets.loadOrUpdateTexture();
                } catch (IOException e) {
                    System.out.println("Failed to save image to file: " + e.getMessage());
                }
            });
        });
    }

}

