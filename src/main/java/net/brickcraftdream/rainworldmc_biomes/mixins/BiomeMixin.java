package net.brickcraftdream.rainworldmc_biomes.mixins;

import net.brickcraftdream.rainworldmc_biomes.biome.ExtendedBiome;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Biome.class)
public class BiomeMixin implements ExtendedBiome {
    @Shadow
    @Final
    public Biome.ClimateSettings climateSettings;
    private int biomeCategory = -1;

    @Override
    public int getBiomeCategory() {
        return biomeCategory;
    }

    @Override
    public void setBiomeCategory(int biomeCategory) {
        this.biomeCategory = biomeCategory;
    }

    @Override
    public float getDownfall() {
        return this.climateSettings.downfall();
    }
}
