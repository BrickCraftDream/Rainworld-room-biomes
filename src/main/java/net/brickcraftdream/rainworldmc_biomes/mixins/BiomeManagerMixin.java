package net.brickcraftdream.rainworldmc_biomes.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
        value = {BiomeManager.class},
        priority = 900
)
public class BiomeManagerMixin {

    @Shadow @Final private BiomeManager.NoiseBiomeSource noiseBiomeSource;

    /// public Holder<Biome> getBiome(BlockPos blockPos) {
    ///         int closestPointIndex;
    @ModifyVariable(method = "getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;", at = @At("STORE"), ordinal = 0)
    private int closestPointIndex(int closestPointIndex) {
        return closestPointIndex;
    }
    ///         int offsetX = blockPos.getX() - 2;
    @ModifyVariable(method = "getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;", at = @At("STORE"), ordinal = 1)
    private int offsetX(int offsetX) {
        return offsetX;
    }
    ///         int offsetY = blockPos.getY() - 2;
    @ModifyVariable(method = "getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;", at = @At("STORE"), ordinal = 2)
    private int offsetY(int offsetY) {
        return offsetY;
    }
    ///         int offsetZ = blockPos.getZ() - 2;
    @ModifyVariable(method = "getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;", at = @At("STORE"), ordinal = 3)
    private int offsetZ(int offsetZ) {
        return offsetZ;
    }
    ///         int baseX = offsetX >> 2;
    @ModifyVariable(method = "getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;", at = @At("STORE"), ordinal = 4)
    private int baseX(int baseX) {
        return baseX;
    }
    ///         int baseY = offsetY >> 2;
    @ModifyVariable(method = "getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;", at = @At("STORE"), ordinal = 5)
    private int baseY(int baseY) {
        return baseY;
    }
    ///         int baseZ = offsetZ >> 2;
    @ModifyVariable(method = "getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;", at = @At("STORE"), ordinal = 6)
    private int baseZ(int baseZ) {
        return baseZ;
    }
    ///         double fractionalX = (double) (offsetX & 3) / 4.0;
    ///         double fractionalY = (double) (offsetY & 3) / 4.0;
    ///         double fractionalZ = (double) (offsetZ & 3) / 4.0;
    ///         int bestMatch = 0;
    ///         double smallestDistance = Double.POSITIVE_INFINITY;
    ///         for (closestPointIndex = 0; closestPointIndex < 8; ++closestPointIndex) {
    ///             boolean isXBase = (closestPointIndex & 4) == 0;
    ///             int adjustedX = isXBase ? baseX : baseX + 1;
    ///             boolean isYBase = (closestPointIndex & 2) == 0;
    ///             int adjustedY = isYBase ? baseY : baseY + 1;
    ///             boolean isZBase = (closestPointIndex & 1) == 0;
    ///             int adjustedZ = isZBase ? baseZ : baseZ + 1;
    ///             double xDist = isXBase ? fractionalX : fractionalX - 1.0;
    ///             double yDist = isYBase ? fractionalY : fractionalY - 1.0;
    ///             double zDist = isZBase ? fractionalZ : fractionalZ - 1.0;
    ///             double distance = getFiddledDistance(this.biomeZoomSeed, adjustedX, adjustedY, adjustedZ, xDist, yDist, zDist);
    ///             if (smallestDistance > distance) {
    ///                 bestMatch = closestPointIndex;
    ///                 smallestDistance = distance;
    ///             }
    ///         }
    ///         int finalX = (bestMatch & 4) == 0 ? baseX : baseX + 1;
    ///         int finalY = (bestMatch & 2) == 0 ? baseY : baseY + 1;
    ///         int finalZ = (bestMatch & 1) == 0 ? baseZ : baseZ + 1;
    ///         return this.noiseBiomeSource.getNoiseBiome(finalX, finalY, finalZ);
    ///     }
    /*
    @ModifyVariable(method = "getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;", at = @At("STORE"), ordinal = 1)
    private int injected(int i) {
        return i - 1;
    }

    @ModifyVariable(method = "getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;", at = @At("STORE"), ordinal = 3)
    private int injected2(int k) {
        return k - 1;
    }
     */

    @Inject(method = "getFiddledDistance(JIIIDDD)D", at = @At("HEAD"), cancellable = true)
    private static void getFiddledDistance(long seed, int x, int y, int z, double xOffset, double yOffset, double zOffset, CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(zOffset + yOffset + zOffset);
    }

    @Inject(method = "getFiddle(J)D", at = @At("HEAD"), cancellable = true)
    private static void getFiddle(long l, CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(0.25D);
    }

    //@Inject(method = "getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;", at = @At("RETURN"), cancellable = true)
    //private void getBiome(BlockPos blockPos, CallbackInfoReturnable<Holder<Biome>> cir) {
    //    int x = QuartPos.fromBlock(blockPos.getX());
    //    int y = QuartPos.fromBlock(blockPos.getY());
    //    int z = QuartPos.fromBlock(blockPos.getZ());
//
    //    cir.setReturnValue(this.noiseBiomeSource.getNoiseBiome(x, y, z));
    //}

    //private void getBiome(BlockPos blockPos, CallbackInfoReturnable<Holder<Biome>> cir) {
    //    int x = QuartPos.fromBlock(blockPos.getX());
    //    int y = QuartPos.fromBlock(blockPos.getY());
    //    int z = QuartPos.fromBlock(blockPos.getZ());
    //    cir.setReturnValue(this.noiseBiomeSource.getNoiseBiome(x, y, z));
    //}
}
