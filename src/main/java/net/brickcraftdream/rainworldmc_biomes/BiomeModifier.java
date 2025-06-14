package net.brickcraftdream.rainworldmc_biomes;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;

import java.util.List;

public class BiomeModifier {
    public static void modifyBiome(String namespace, String path) {
        // Modify existing biomes
        ResourceKey<Biome> biomeResourceKey = ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(namespace, path));
        BiomeModifications.create(ResourceLocation.fromNamespaceAndPath(Rainworld_MC_Biomes.MOD_ID, "biome_modifications")).add(ModificationPhase.ADDITIONS,
                        BiomeSelectors.includeByKey(biomeResourceKey),
                        context -> {
                            context.getEffects().setGrassColor(1);
                        });
    }

    /**
     * Creates and registers a new biome at runtime
     * @param server The MinecraftServer instance
     * @param biomeId The identifier for the new biome (e.g., "yourmodid:custom_biome")
     * @return The RegistryKey for the newly created biome, or null if registration failed
     */
    public static ResourceKey<Biome> createAndRegisterBiome(MinecraftServer server, String biomeId) {
        // Get the dynamic registry manager from the server
        RegistryAccess registryManager = server.registryAccess();

        // Get the biome registry
        Registry<Biome> biomeRegistry = registryManager.registryOrThrow(Registries.BIOME);

        // Create a registry key for our new biome
        ResourceLocation biomeIdentifier = ResourceLocation.fromNamespaceAndPath("rainworld", biomeId);
        ResourceKey<Biome> biomeKey = ResourceKey.create(Registries.BIOME, biomeIdentifier);

        // Check if the biome already exists
        if (biomeRegistry.containsKey(biomeKey)) {
            System.out.println("Biome " + biomeId + " already exists!");
            return biomeKey;
        }

        // Create a new biome
        Biome newBiome = createCustomBiome();

        // Try to register the biome
        if (biomeRegistry instanceof WritableRegistry<Biome> mutableRegistry) {
            mutableRegistry.register(biomeKey, newBiome, RegistrationInfo.BUILT_IN);
            System.out.println("Successfully registered new biome: " + biomeId);

            // Force chunk regeneration in affected areas if needed
            // This is complex and would need custom implementation

            return biomeKey;
        } else {
            System.err.println("Failed to register biome: Registry is not writable");
            return null;
        }
    }

    /**
     * Creates a custom biome with specific properties
     * Customize this method to create the biome with your desired characteristics
     */
    private static Biome createCustomBiome() {
        return new Biome.BiomeBuilder()
                .generationSettings(BiomeGenerationSettings.EMPTY)
                .mobSpawnSettings(MobSpawnSettings.EMPTY)
                .hasPrecipitation(false)
                .temperature(3)
                .downfall(1)
                .specialEffects((new BiomeSpecialEffects.Builder())
                        .skyColor(12312)
                        .fogColor(124435)
                        .waterColor(35122)
                        .waterFogColor(3251)
                        .build())
                .build();
    }

}

