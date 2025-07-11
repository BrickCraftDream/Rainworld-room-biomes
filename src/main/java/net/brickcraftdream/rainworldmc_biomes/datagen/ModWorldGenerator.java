package net.brickcraftdream.rainworldmc_biomes.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;

import java.util.concurrent.CompletableFuture;

public class ModWorldGenerator extends FabricDynamicRegistryProvider {
    public ModWorldGenerator(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    //@Override
    //protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries) {
    //    entries.addAll(registries.getWrapperOrThrow(RegistryKeys.BIOME));
    //}

    @Override
    public String getName() {
        return "World Gen";
    }

    @Override
    protected void configure(HolderLookup.Provider registries, Entries entries) {
        entries.addAll(registries.lookupOrThrow(Registries.BIOME));
    }
}
