//package net.brickcraftdream.rainworldmc_biomes.mixins.sodium;
//
//import net.caffeinemc.mods.sodium.client.world.biome.LevelBiomeSlice;
//import net.caffeinemc.mods.sodium.client.world.biome.LevelColorCache;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//@Mixin(value = LevelColorCache.class, remap = false)
//public class LevelColorCacheMixin {
//    @Shadow
//    private int blendRadius;
//
//    @Inject(method = "<init>(Lnet/caffeinemc/mods/sodium/client/world/biome/LevelBiomeSlice;I)V", at = @At("TAIL"))
//    public void init_tail(LevelBiomeSlice biomeData, int blendRadius, CallbackInfo ci) {
//        this.blendRadius = 0;
//    }
//}
