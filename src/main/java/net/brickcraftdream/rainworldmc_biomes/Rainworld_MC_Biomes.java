package net.brickcraftdream.rainworldmc_biomes;

import com.google.gson.JsonObject;
import net.brickcraftdream.rainworldmc_biomes.biome.BiomeModify;
import net.brickcraftdream.rainworldmc_biomes.data.storage.ConfigManagerServer;
import net.brickcraftdream.rainworldmc_biomes.image.ImageGenerator;
import net.brickcraftdream.rainworldmc_biomes.networking.BiomeImageProcessorClient;
import net.brickcraftdream.rainworldmc_biomes.networking.BiomeImageProcessorServer;
import net.brickcraftdream.rainworldmc_biomes.networking.NetworkManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.ResourceBundle;

import static net.brickcraftdream.rainworldmc_biomes.ColorPicker.combineColors;
import static net.brickcraftdream.rainworldmc_biomes.ColorPicker.combineColorsBufferedImage;
import static net.brickcraftdream.rainworldmc_biomes.networking.NetworkManager.*;

public class Rainworld_MC_Biomes implements ModInitializer {
    public static final String MOD_ID = "rainworld";
    @Override
    public void onInitialize() {
        setupDefaultConfig();

        PayloadTypeRegistry.playC2S().register(BiomeUpdatePacket.ID, BiomeUpdatePacket.CODEC);
        PayloadTypeRegistry.playS2C().register(BiomeUpdatePacket.ID, BiomeUpdatePacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(BiomeUpdatePacket.ID, ((payload, context) -> {
            context.server().execute(() -> {
                float biomeId = BiomeImageProcessorServer.getNextFreeTempBiome(Objects.requireNonNull(context.server().getLevel(context.player().level().dimension())));
                if(biomeId == 69420) {
                    context.player().sendSystemMessage(Component.literal("Biome modification failed: No free biome slots available. Please contact BrickCraftDream with this issue."), true);
                    return;
                }
                else {
                    BufferedImage imageData = BiomeImageProcessorClient.byteArrayToBufferedImage(payload.imageData());
                    ResourceLocation biomeResourceLocation = BiomeImageProcessorServer.getBiomeResourceLocationFromTemp(Objects.requireNonNull(context.server().getLevel(context.player().level().dimension())), biomeId);

                    if(biomeResourceLocation == null) {
                        context.player().sendSystemMessage(Component.literal("Biome modification failed: Temp biome resource location not found. Please contact BrickCraftDream with this issue."), true);
                        return;
                    }
                    BiomeModify.modifyBiome(
                            biomeResourceLocation.getNamespace(),
                            biomeResourceLocation.getPath(),
                            getSkyColorFromPalette(payload.palette(), payload.fadePalette(), payload.fadeStrength()),
                            getFogColorFromPalette(payload.palette(), payload.fadePalette(), payload.fadeStrength()),
                            getWaterColorFromPalette(payload.palette(), payload.fadePalette(), payload.fadeStrength()),
                            getWaterFogColorFromPalette(payload.palette(), payload.fadePalette(), payload.fadeStrength())
                    );
                    ImageGenerator.roomToImage(imageData, payload.palette(), payload.fadePalette(),payload.fadeStrength(), payload.grime(), payload.effectColorA(), payload.effectColorB(), payload.dangerType(), Integer.parseInt(String.valueOf(biomeId)), "agjhhjd");
                    ConfigManagerServer.saveBufferedImageToConfigFolder(imageData, "shader_data.png");
                    JsonObject biomeSettings = ConfigManagerServer.readConfig("biome_settings.json");
                    JsonObject biomeSettingsForBiome = getJsonObject(payload);
                    biomeSettings.add(String.valueOf(biomeId), biomeSettingsForBiome);
                    ConfigManagerServer.writeConfig("biome_settings.json", biomeSettings);
                }
            });
        }));
    }

    private static @NotNull JsonObject getJsonObject(BiomeUpdatePacket payload) {
        JsonObject biomeSettingsForBiome = new JsonObject();
        biomeSettingsForBiome.addProperty("internalName", payload.internalBiomeName());
        biomeSettingsForBiome.addProperty("displayName", payload.customBiomeName());
        biomeSettingsForBiome.addProperty("palette", payload.palette());
        biomeSettingsForBiome.addProperty("fadePalette", payload.fadePalette());
        biomeSettingsForBiome.addProperty("fadeStrength", payload.fadeStrength());
        biomeSettingsForBiome.addProperty("grime", payload.grime());
        biomeSettingsForBiome.addProperty("effectColorA", payload.effectColorA());
        biomeSettingsForBiome.addProperty("effectColorB", payload.effectColorB());
        biomeSettingsForBiome.addProperty("dangerType", payload.dangerType());
        return biomeSettingsForBiome;
    }

    public void setupDefaultConfig() {
        JsonObject defaultConfig = new JsonObject();
        ConfigManagerServer.createDefaultConfigIfNotExists("biome_settings.json", defaultConfig);
    }

    private static int getColorFromPalette(int palette, int fadePalette, float fadeStrength, String x, String y) {
        BufferedImage paletteImage = BiomeImageProcessorClient.resourceLocationToBufferedImage(ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/palettes/palette" + palette + ".png"));
        BufferedImage fadePaletteImage = BiomeImageProcessorClient.resourceLocationToBufferedImage(ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/palettes/palette" + fadePalette + ".png"));

        return combineColorsBufferedImage(
                paletteImage,
                x, y,
                fadePaletteImage,
                x, y,
                fadeStrength
        );
    }

    public static int getSkyColorFromPalette(int palette, int fadePalette, float fadeStrength) {
        return getColorFromPalette(palette, fadePalette, fadeStrength, "0", "0");
    }

    public static int getFogColorFromPalette(int palette, int fadePalette, float fadeStrength) {
        return getColorFromPalette(palette, fadePalette, fadeStrength, "1", "0");
    }

    public static int getWaterColorFromPalette(int palette, int fadePalette, float fadeStrength) {
        return getColorFromPalette(palette, fadePalette, fadeStrength, "6", "0");
    }

    public static int getWaterFogColorFromPalette(int palette, int fadePalette, float fadeStrength) {
        return getColorFromPalette(palette, fadePalette, fadeStrength, "6", "0");
    }

}
