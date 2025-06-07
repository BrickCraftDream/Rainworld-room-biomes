package net.brickcraftdream.rainworldmc_biomes.biome;

import net.brickcraftdream.rainworldmc_biomes.Rainworld_MC_Biomes;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
}
