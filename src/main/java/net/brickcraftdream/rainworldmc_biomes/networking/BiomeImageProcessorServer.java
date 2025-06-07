package net.brickcraftdream.rainworldmc_biomes.networking;

import net.brickcraftdream.rainworldmc_biomes.biome.ExtendedBiome;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;

public class BiomeImageProcessorServer {
    // Server side handling, separate class for client
    public static float getNextFreeTempBiome(ServerLevel level) {
        RegistryAccess registryAccess = level.registryAccess();
        String namespace = "rainworld";

        // Get the biome registry
        Registry<Biome> biomeRegistry = registryAccess.registryOrThrow(Registries.BIOME);

        // Loop through all biomes in the registry
        for (ResourceLocation biomeId : biomeRegistry.keySet()) {
            // Check if the biome belongs to the specified namespace
            if (biomeId.getNamespace().equals(namespace)) {
                // Get the biome using the ResourceLocation
                ResourceKey<Biome> biomeKey = ResourceKey.create(Registries.BIOME, biomeId);
                Biome biome = biomeRegistry.get(biomeKey);
                assert biome != null;
                float downfall = ((ExtendedBiome) (Object) biome).getDownfall();
                if(downfall == 0.5f) {
                    return biome.getBaseTemperature();
                }
            }
        }
        return 69420f;

    }

    public static Biome getBiomeFromTemp(ServerLevel level, float temperature) {
        RegistryAccess registryAccess = level.registryAccess();
        String namespace = "rainworld";

        // Get the biome registry
        Registry<Biome> biomeRegistry = registryAccess.registryOrThrow(Registries.BIOME);

        // Loop through all biomes in the registry
        for (ResourceLocation biomeId : biomeRegistry.keySet()) {
            // Check if the biome belongs to the specified namespace
            if (biomeId.getNamespace().equals(namespace)) {
                // Get the biome using the ResourceLocation
                ResourceKey<Biome> biomeKey = ResourceKey.create(Registries.BIOME, biomeId);
                Biome biome = biomeRegistry.get(biomeKey);
                assert biome != null;
                float temp = biome.getBaseTemperature();
                if (temp == temperature) {
                    return biome;
                }
            }
        }
        return null;
    }

    public static ResourceLocation getBiomeResourceLocationFromTemp(ServerLevel level, float temperature) {
        RegistryAccess registryAccess = level.registryAccess();
        String namespace = "rainworld";

        // Get the biome registry
        Registry<Biome> biomeRegistry = registryAccess.registryOrThrow(Registries.BIOME);

        // Loop through all biomes in the registry
        for (ResourceLocation biomeId : biomeRegistry.keySet()) {
            // Check if the biome belongs to the specified namespace
            if (biomeId.getNamespace().equals(namespace)) {
                // Get the biome using the ResourceLocation
                ResourceKey<Biome> biomeKey = ResourceKey.create(Registries.BIOME, biomeId);
                Biome biome = biomeRegistry.get(biomeKey);
                assert biome != null;
                float temp = biome.getBaseTemperature();
                if (temp == temperature) {
                    return biomeId;
                }
            }
        }
        return null;
    }
}
