package net.brickcraftdream.rainworldmc_biomes.data;

import net.brickcraftdream.rainworldmc_biomes.Rainworld_MC_Biomes;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
/*
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

 */

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class RainworldBiomeTagProvider extends FabricTagProvider<Biome> {
    /**
     * Constructs a new {@link FabricTagProvider} with the default computed path.
     *
     * <p>Common implementations of this class are provided.
     *
     * @param output           the {@link FabricDataOutput} instance
     * @param registriesFuture the backing registry for the tag type
     */


    ///VERSION SPECIFIC: 1.21.1
    public static final TagKey<Biome> SHELTER = TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Rainworld_MC_Biomes.MOD_ID, "shelter"));
    ///VERSION SPECIFIC: 1.20.1
    ///public static final TagKey<Biome> SHELTER = TagKey.create(Registries.BIOME, new ResourceLocation(Rainworld_MC_Biomes.MOD_ID, "shelter"));

    public RainworldBiomeTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, Registries.BIOME, registriesFuture);
    }

    private static List<ResourceKey<Biome>> keyList = new ArrayList<>();
    private static Map<String, List<ResourceKey<Biome>>> threatList = new HashMap<>();

    public static void addThreatTag(String region, ResourceKey<Biome> key) {
        List<ResourceKey<Biome>> list = threatList.get(region);
        list.add(key);
        threatList.put(region, list);
    }

    public static void addTag(ResourceKey<Biome> key) {
        keyList.add(key);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        for(ResourceKey<Biome> biome : keyList) {
            getOrCreateTagBuilder(SHELTER)
                    .add(biome);
        }
        for(String s : threatList.keySet()) {
            ///VERSION SPECIFIC: 1.21.1
            TagKey<Biome> REGION = TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Rainworld_MC_Biomes.MOD_ID, s));
            ///VERSION SPECIFIC: 1.20.1
            ///TagKey<Biome> REGION = TagKey.create(Registries.BIOME, new ResourceLocation(Rainworld_MC_Biomes.MOD_ID, s));
            for(ResourceKey<Biome> biome : threatList.get(s)) {
                getOrCreateTagBuilder(REGION)
                        .add(biome);
            }
        }
    }
}
