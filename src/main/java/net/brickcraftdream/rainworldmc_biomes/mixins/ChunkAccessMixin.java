package net.brickcraftdream.rainworldmc_biomes.mixins;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkAccess.class)
public abstract class ChunkAccessMixin {

    @Shadow public abstract int getMinBuildHeight();

    @Shadow public abstract int getHeight();

    @Shadow @Final protected LevelChunkSection[] sections;

    @Shadow @Final protected LevelHeightAccessor levelHeightAccessor;

    @Inject(method = "getNoiseBiome(III)Lnet/minecraft/core/Holder;", at = @At("RETURN"), cancellable = true)
    private void getNoiseBiome(int blockX, int blockY, int blockZ, CallbackInfoReturnable<Holder<Biome>> cir) {
        try {
            int minBuildHeightQuartPos = QuartPos.fromBlock(this.getMinBuildHeight());
            int m = minBuildHeightQuartPos + QuartPos.fromBlock(this.getHeight()) - 1;
            int n = Mth.clamp(blockY, minBuildHeightQuartPos, m);
            int o = this.levelHeightAccessor.getSectionIndex(QuartPos.toBlock(n));
            cir.setReturnValue(this.sections[o].getNoiseBiome(blockX & 3, n & 3, blockZ & 3));
        } catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Getting biome");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Biome being got");
            crashReportCategory.setDetail("Location", () -> CrashReportCategory.formatLocation((LevelHeightAccessor)this, blockX, blockY, blockZ));
            throw new ReportedException(crashReport);
        }
    }
}
