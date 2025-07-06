package net.brickcraftdream.rainworldmc_biomes.mixins.sodium;

import net.caffeinemc.mods.sodium.client.world.biome.LevelBiomeSlice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelBiomeSlice.class, remap = false)
public class LevelBiomeSliceMixin {

    @Shadow
    private final LevelBiomeSlice.BiasMap bias = new LevelBiomeSlice.BiasMap();;

    @Inject(method = "calculateBias(IIIIJ)V", at = @At("TAIL"))
    private void calculateBias(int cellIndex, int cellX, int cellY, int cellZ, long seed, CallbackInfo ci) {
        this.bias.set(cellIndex, 10, 0, 4);
    }
}
