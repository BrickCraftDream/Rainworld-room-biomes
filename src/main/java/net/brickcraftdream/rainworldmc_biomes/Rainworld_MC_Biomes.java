package net.brickcraftdream.rainworldmc_biomes;

import com.google.gson.JsonObject;
import net.brickcraftdream.rainworldmc_biomes.biome.BiomeModify;
import net.brickcraftdream.rainworldmc_biomes.biome.ExtendedBiome;
import net.brickcraftdream.rainworldmc_biomes.command.RoomCommand;
import net.brickcraftdream.rainworldmc_biomes.data.storage.ConfigManagerServer;
import net.brickcraftdream.rainworldmc_biomes.image.ImageGenerator;
import net.brickcraftdream.rainworldmc_biomes.networking.BiomeImageProcessorClient;
import net.brickcraftdream.rainworldmc_biomes.networking.BiomeImageProcessorServer;
import net.brickcraftdream.rainworldmc_biomes.networking.NetworkManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.UUID;

import static net.brickcraftdream.rainworldmc_biomes.ColorPicker.combineColors;
import static net.brickcraftdream.rainworldmc_biomes.ColorPicker.combineColorsBufferedImage;
import static net.brickcraftdream.rainworldmc_biomes.networking.NetworkManager.*;

public class Rainworld_MC_Biomes implements ModInitializer {
    public static final String MOD_ID = "rainworld";
    // Define your item as a static field
    public static final Item ROOM_SELECTOR_ITEM = new RoomSelectorItem(new Item.Properties());

    @Override
    public void onInitialize() {
        setupDefaultConfig();

        PayloadTypeRegistry.playC2S().register(BiomeUpdatePacket.ID, BiomeUpdatePacket.CODEC);
        PayloadTypeRegistry.playS2C().register(BiomeUpdatePacket.ID, BiomeUpdatePacket.CODEC);
        PayloadTypeRegistry.playS2C().register(BiomeUpdateDataRequestPacket.ID, BiomeUpdateDataRequestPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(BiomeUpdateDataRequestPacket.ID, BiomeUpdateDataRequestPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(BiomeSyncPacket.ID, BiomeSyncPacket.CODEC);
        //PayloadTypeRegistry.playS2C().register(BiomeSyncPacket_image_part1.ID, BiomeSyncPacket_image_part1.CODEC);
        //PayloadTypeRegistry.playS2C().register(BiomeSyncPacket_image_part2.ID, BiomeSyncPacket_image_part2.CODEC);
        //PayloadTypeRegistry.playS2C().register(BiomeSyncPacket_image_part3.ID, BiomeSyncPacket_image_part3.CODEC);
        //PayloadTypeRegistry.playS2C().register(BiomeSyncPacket_image_part4.ID, BiomeSyncPacket_image_part4.CODEC);
        PayloadTypeRegistry.playS2C().register(BiomeCacheUpdatePacket.ID, BiomeCacheUpdatePacket.CODEC);


        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            //sender.sendPacket(new BiomeSyncPacket_config(ConfigManagerServer.readConfig("biome_settings.json")));
            //sender.sendPacket(new BiomeSyncPacket_image_part1(ConfigManagerServer.readDataFromConfigFolder("shader_data.png")));
            //sender.sendPacket(new BiomeSyncPacket_image_part2(ConfigManagerServer.readDataFromConfigFolder("shader_data.png")));
            //sender.sendPacket(new BiomeSyncPacket_image_part3(ConfigManagerServer.readDataFromConfigFolder("shader_data.png")));
            //sender.sendPacket(new BiomeSyncPacket_image_part4(ConfigManagerServer.readDataFromConfigFolder("shader_data.png")));
            sender.sendPacket(new BiomeSyncPacket(ConfigManagerServer.readConfig("biome_settings.json"), ConfigManagerServer.readDataFromConfigFolder("shader_data.png")));
        });


        ServerPlayNetworking.registerGlobalReceiver(BiomeUpdatePacket.ID, ((payload, context) -> {
            context.server().execute(() -> {
                float biomeId = BiomeImageProcessorServer.getNextFreeTempBiome(Objects.requireNonNull(context.server().getLevel(context.player().level().dimension())));
                payload.imageData();
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
                    BiomeModify.modifyBiome2(
                            biomeResourceLocation.getNamespace(),
                            biomeResourceLocation.getPath(),
                            getSkyColorFromPalette(payload.palette(), payload.fadePalette(), payload.fadeStrength()),
                            getFogColorFromPalette(payload.palette(), payload.fadePalette(), payload.fadeStrength()),
                            getWaterColorFromPalette(payload.palette(), payload.fadePalette(), payload.fadeStrength()),
                            getWaterFogColorFromPalette(payload.palette(), payload.fadePalette(), payload.fadeStrength()),
                            context.server()
                    );
                    System.out.println("Biome ID: " + String.valueOf(biomeId).substring(0, String.valueOf(biomeId).indexOf(".")));
                    System.out.println("Biome Resource Location: " + biomeResourceLocation);
                    ImageGenerator.roomToImage(imageData, payload.palette(), payload.fadePalette(), payload.fadeStrength(), payload.grime(), payload.effectColorA(), payload.effectColorB(), payload.dangerType(), Integer.parseInt(String.valueOf(biomeId).substring(0, String.valueOf(biomeId).indexOf("."))), "agjhhjd");
                    ConfigManagerServer.saveBufferedImageToConfigFolder(imageData, "shader_data.png");
                    JsonObject biomeSettings = ConfigManagerServer.readConfig("biome_settings.json");
                    JsonObject biomeSettingsForBiome = getJsonObject(payload, biomeResourceLocation);
                    biomeSettings.add(String.valueOf(biomeId), biomeSettingsForBiome);
                    ConfigManagerServer.writeConfig("biome_settings.json", biomeSettings);
                    byte[] imageDataBytes = BiomeImageProcessorClient.bufferedImageToByteArray(imageData, "png");
                    PlayerLookup.all(context.server()).forEach(p -> ServerPlayNetworking.send(p, new BiomeUpdatePacket(payload.customBiomeName(), payload.internalBiomeName(), payload.palette(), payload.fadePalette(), payload.fadeStrength(), payload.grime(), payload.effectColorA(), payload.effectColorB(), payload.dangerType(), imageDataBytes)));
                    context.server().reloadResources(context.server().getPackRepository().getSelectedIds()).thenRun(() -> {
                        context.player().sendSystemMessage(Component.literal("Resources reloaded!"), true);
                    });
                }
            });
        }));
        PayloadTypeRegistry.playS2C().register(SelectedLocationPayload.ID, SelectedLocationPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SelectedLocationPayload.ID, SelectedLocationPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(BiomePlacePayload2.ID, BiomePlacePayload2.CODEC);
        PayloadTypeRegistry.playC2S().register(RemoveBoxPayload.ID, RemoveBoxPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(RemoveBoxPayload.ID, RemoveBoxPayload.CODEC);


        ServerPlayNetworking.registerGlobalReceiver(RemoveBoxPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                syncRemoveBox(context.server(), payload.playerName());
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(SelectedLocationPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                syncSelectedLocations(context.server(), payload.firstPos(), payload.secondPos(), payload.playerName());
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(BiomePlacePayload2.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                ServerLevel level = player.serverLevel();

                ResourceKey<Biome> biomeKey = ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(payload.biomeNamespace(), payload.biomePath()));

                Registry<Biome> biomeRegistry = level.registryAccess().registryOrThrow(Registries.BIOME);
                Holder<Biome> biomeEntry = biomeRegistry.getHolderOrThrow(biomeKey);

                for (GlobalPos pos : payload.pos()) {
                    ResourceKey<Level> dimension = pos.dimension();
                    if (level.dimension().equals(dimension)) {
                        LevelChunk levelChunk = level.getChunkAt(pos.pos());
                        LevelChunkSection section = levelChunk.getSection(levelChunk.getSectionIndex(pos.pos().getY()));
                        PalettedContainerRO<Holder<Biome>> biomeContainer = section.getBiomes();
                        BlockPos blockPos = pos.pos();


                        if (biomeContainer instanceof PalettedContainer<Holder<Biome>> palettedContainer) {
                            for (int sx = 0; sx < 4; sx++) {
                                for (int sy = 0; sy < 4; sy++) {
                                    for (int sz = 0; sz < 4; sz++) {
                                        int biomeX = (blockPos.getX() & 15) >> 2;
                                        int biomeY = (blockPos.getY() & 15) >> 2;
                                        int biomeZ = (blockPos.getZ() & 15) >> 2;

                                        // Set only the specific biome position
                                        palettedContainer.set(biomeX, biomeY, biomeZ, biomeEntry);

                                    }
                                }
                            }
                        }
                        levelChunk.setUnsaved(true);
                        level.getChunkSource().blockChanged(pos.pos());
                        PlayerLookup.all(context.server()).forEach(player1 -> player1.connection.send(new ClientboundLevelChunkWithLightPacket(levelChunk, level.getLightEngine(), null, null)));
                    }
                }
            });
        });

        // Register your item
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            RoomCommand.register(dispatcher);
        });

        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "room_selector_item"), ROOM_SELECTOR_ITEM);

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.OP_BLOCKS).register(entries -> {
            entries.accept(ROOM_SELECTOR_ITEM);
        });

    }

    public void syncSelectedLocations(MinecraftServer server, GlobalPos firstPos, GlobalPos secondPos, UUID playerUUID) {
        PlayerLookup.all(server).forEach(p -> ServerPlayNetworking.send(p, new SelectedLocationPayload(firstPos, secondPos, playerUUID)));
    }

    public void syncRemoveBox(MinecraftServer server, UUID playerUUID) {
        PlayerLookup.all(server).forEach(p -> ServerPlayNetworking.send(p, new RemoveBoxPayload(playerUUID)));
    }

    private static @NotNull JsonObject getJsonObject(BiomeUpdatePacket payload, ResourceLocation biomeLocation) {
        JsonObject biomeSettingsForBiome = new JsonObject();
        biomeSettingsForBiome.addProperty("internalName", biomeLocation.getPath());
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
