package net.brickcraftdream.rainworldmc_biomes;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;


public class FlatLightingMixinPlugin implements IMixinConfigPlugin {
    private boolean isUsingSodium;

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.startsWith("net.brickcraftdream.rainworldmc_biomes.mixins.sodium")) {
            return isUsingSodium;
        }
        return true;
    }

    @Override
    public void onLoad(String mixinPackage) {
        isUsingSodium = FabricLoader.getInstance().isModLoaded("sodium");
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
