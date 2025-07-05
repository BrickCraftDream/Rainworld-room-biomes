package net.brickcraftdream.rainworldmc_biomes.mixins;

import net.brickcraftdream.rainworldmc_biomes.client.Rainworld_MC_BiomesClient;
import net.brickcraftdream.rainworldmc_biomes.gui.MainGui;
import net.brickcraftdream.rainworldmc_biomes.gui.widget.BlockViewWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.brickcraftdream.rainworldmc_biomes.gui.widget.BlockViewWidget.*;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"))
    private void onSetScreen(Screen guiScreen, CallbackInfo ci) {
        if(guiScreen == null) {
            Rainworld_MC_BiomesClient.ticksSinceGuiExit = 1;
        }
    //    if(guiScreen instanceof MainGui) {
//
    //    }
    //    else {
    //        if (guiScreen == null) {
    //            System.out.println("Unloading BlockViewWidget on screen change.");
    //            // Unloads the post processing shader
    //            unloadChainForNormalGuis();
    //        } else {
    //            System.out.println("Loading BlockViewWidget on screen change.");
    //            // Loads the post processing shader
    //            loadChainForNormalGuis();
    //        }
    //    }
    }
}
