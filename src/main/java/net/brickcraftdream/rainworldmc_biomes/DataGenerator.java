package net.brickcraftdream.rainworldmc_biomes;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;


public class DataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(net.brickcraftdream.rainworldmc_biomes.datagen.ModWorldGenerator::new);
		pack.addProvider(net.brickcraftdream.rainworldmc_biomes.data.RainworldBiomeTagProvider::new);
	}

	@Override
	public void buildRegistry(RegistrySetBuilder registryBuilder) {
		System.out.println("BUILD REGISTRY");
		registryBuilder.add(Registries.BIOME, BiomeBootstrap::bootstrap);
	}
}
