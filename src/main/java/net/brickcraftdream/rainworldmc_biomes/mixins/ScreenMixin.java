package net.brickcraftdream.rainworldmc_biomes.mixins;

import net.brickcraftdream.rainworldmc_biomes.client.Rainworld_MC_BiomesClient;
import net.brickcraftdream.rainworldmc_biomes.gui.widget.BlockViewWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {
    @Inject(method = "onClose()V", at = @At("HEAD"))
    private void onClose(CallbackInfo ci) {
        Rainworld_MC_BiomesClient.ticksSinceGuiExit = 1;
        //if(BlockViewWidget.renderEffectChain != null) {
        //    BlockViewWidget.renderEffectChain.close();
        //    BlockViewWidget.renderEffectChain = null;
        //    Minecraft.getInstance().gameRenderer.shutdownEffect();
        //    BlockViewWidget.shouldRender = false;
        //    BlockViewWidget.shouldHaveActiveEffectChain = false;
        //}
    }
}
