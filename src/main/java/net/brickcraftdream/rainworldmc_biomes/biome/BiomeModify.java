package net.brickcraftdream.rainworldmc_biomes.biome;

import net.brickcraftdream.rainworldmc_biomes.Rainworld_MC_Biomes;
import net.brickcraftdream.rainworldmc_biomes.networking.NetworkManager;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.biome.Biome;

import static net.brickcraftdream.rainworldmc_biomes.EverythingProvider.*;
import static net.brickcraftdream.rainworldmc_biomes.EverythingProvider.waterfog;

public class BiomeModify {
    public static void modifyBiome(String namespace, String path, int skyColor, int fogColor, int waterColor, int waterFogColor) {
        // Modify existing biomes
        ResourceKey<Biome> biomeResourceKey = ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(namespace, path));
        BiomeModifications.create(ResourceLocation.fromNamespaceAndPath(Rainworld_MC_Biomes.MOD_ID, "biome_modifications")).add(ModificationPhase.ADDITIONS,
                BiomeSelectors.includeByKey(biomeResourceKey),
                context -> {
                    context.getEffects().setSkyColor(skyColor);
                    context.getEffects().setFogColor(fogColor);
                    context.getEffects().setWaterColor(waterColor);
                    context.getEffects().setWaterFogColor(waterFogColor);
                    context.getWeather().setDownfall(1);
                });

    }

    public static void modifyBiome2(String namespace, String path, int skyColor, int fogColor, int waterColor, int waterFogColor, MinecraftServer server) {
        // Modify existing biomes
        ResourceKey<Biome> biomeResourceKey = ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(namespace, path));

        // Apply the biome modification using the standard API
        BiomeModifications.create(ResourceLocation.fromNamespaceAndPath(Rainworld_MC_Biomes.MOD_ID, "biome_modifications_" + path))
                .add(ModificationPhase.ADDITIONS,
                        BiomeSelectors.includeByKey(biomeResourceKey),
                        context -> {
                            context.getEffects().setSkyColor(skyColor);
                            context.getEffects().setFogColor(fogColor);
                            context.getEffects().setWaterColor(waterColor);
                            context.getEffects().setWaterFogColor(waterFogColor);
                            context.getWeather().setDownfall(1);
                        });

        // Force client-side visual updates
        //updateClientBiomeVisuals(biomeResourceKey, skyColor, fogColor, waterColor, waterFogColor);
        PlayerLookup.all(server).forEach(p -> ServerPlayNetworking.send(p, new NetworkManager.BiomeCacheUpdatePacket(biomeResourceKey)));
    }

    public static void updateClientBiomeVisuals(ResourceKey<Biome> biomeKey, int skyColor, int fogColor, int waterColor, int waterFogColor) {
        // This should be called on the client side only
        if (Minecraft.getInstance().level != null) {
            // Get the biome from the registry
            Minecraft.getInstance().level.registryAccess()
                    .registryOrThrow(Registries.BIOME)
                    .getHolder(biomeKey)
                    .ifPresent(biomeHolder -> {
                        // Force update any client-side cache or rendering
                        Minecraft.getInstance().levelRenderer.allChanged();
                    });
        }
    }

}
