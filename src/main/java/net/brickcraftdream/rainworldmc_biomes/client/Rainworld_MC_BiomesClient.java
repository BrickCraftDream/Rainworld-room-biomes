package net.brickcraftdream.rainworldmc_biomes.client;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.brickcraftdream.rainworldmc_biomes.Rainworld_MC_Biomes;
import net.brickcraftdream.rainworldmc_biomes.biome.BiomeModify;
import net.brickcraftdream.rainworldmc_biomes.blocks.ModBlocks;
//import net.brickcraftdream.rainworldmc_biomes.blocks.MultiPartBlock;
//import net.brickcraftdream.rainworldmc_biomes.blocks.MultiPartBlockEntity;
import net.brickcraftdream.rainworldmc_biomes.data.storage.ConfigManagerClient;
import net.brickcraftdream.rainworldmc_biomes.data.storage.ConfigManagerServer;
import net.brickcraftdream.rainworldmc_biomes.gui.MainGui;
import net.brickcraftdream.rainworldmc_biomes.gui.widget.BlockViewWidget;
import net.brickcraftdream.rainworldmc_biomes.image.DynamicAssets;
import net.brickcraftdream.rainworldmc_biomes.image.ImageGenerator;
import net.brickcraftdream.rainworldmc_biomes.networking.BiomeImageProcessorClient;
import net.brickcraftdream.rainworldmc_biomes.networking.BiomeImageProcessorServer;
import net.brickcraftdream.rainworldmc_biomes.networking.NetworkManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.ArrayUtils;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.brickcraftdream.rainworldmc_biomes.Rainworld_MC_Biomes.MOD_ID;
import static net.brickcraftdream.rainworldmc_biomes.Rainworld_MC_Biomes.ROOM_SELECTOR_ITEM;
import static net.brickcraftdream.rainworldmc_biomes.gui.widget.BlockViewWidget.*;
import static net.brickcraftdream.rainworldmc_biomes.networking.NetworkManager.*;

@Environment(EnvType.CLIENT)
public class Rainworld_MC_BiomesClient implements ClientModInitializer {
    // Selection data
    private static final List<AreaSelection> selections = new ArrayList<>();
    private static BlockPos firstCorner = null;
    private static BlockPos secondCorner = null;
    private static boolean isSelectionConfirmed = false;

    // Input tracking
    private static boolean prevRight = false;
    private static boolean prevLeft = false;
    private static boolean prevCtrl = false;
    private static double scrollPosition = 0;
    private static double prevScrollPosition = 0;
    private static double scrollAccumulator = 0;

    private static long ctrlPressTime = 0;
    private static final long CTRL_TIMEOUT_MS = 300; // 1 second timeout

    private static MainGui mainGui = null;

    private static float lastTickTemp = 0;

    private static int maxTicksToIgnoreInputsAfterGuiExit = 6;
    public static int ticksSinceGuiExit = 0;


    // Mode handling
    public enum SelectionMode {
        AREA_SELECT("Area Select"),
        ROOM_SELECT("Room Select"),
        REGION_SELECT("Region Select"),
        ROOM_MODIFY("Room Add/Modify");

        private final String displayName;

        SelectionMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private static SelectionMode currentMode = SelectionMode.AREA_SELECT;

    // Class to store area selections
    private static class AreaSelection {
        private final BlockPos corner1;
        private final BlockPos corner2;

        public AreaSelection(BlockPos corner1, BlockPos corner2) {
            this.corner1 = corner1;
            this.corner2 = corner2;
        }

        public BlockPos getCorner1() {
            return corner1;
        }

        public BlockPos getCorner2() {
            return corner2;
        }
    }

    public static void saveBufferedImageToConfigFolder(BufferedImage bufferedImage, String fileName) {
        try {
            // Get the config directory for your mod
            Path configDir = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);

            // Create the directory if it doesn't exist
            Files.createDirectories(configDir);

            // Create the file path
            Path filePath = configDir.resolve(fileName);

            // Write the BufferedImage to the file
            ImageGenerator.saveImageToFile(bufferedImage, "png", filePath.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] array;

    private static void initializeConfigFolder() {
        try {
            ConfigManagerServer.saveBufferedImageToConfigFolder(BiomeImageProcessorClient.resourceLocationToBufferedImage(ResourceLocation.fromNamespaceAndPath("rainworld", "textures/dynamic/shader_data.png")), "shader_data.png");
        }
        catch (Exception e) {
            System.out.println("Failed to initialize config folder (you can safely ignore this): " + e.getMessage());
        }
    }

    @Override
    public void onInitializeClient() {
        //initializeConfigFolder();

        ClientPlayNetworking.registerGlobalReceiver(BiomeSyncFromClientInitializationFromServerPacket.ID, (payload, context) -> {
            context.client().execute(() -> {
                byte[] data = ConfigManagerServer.readDataFromConfigFolder("shader_data.png");
                if(data == null) {
                    ConfigManagerServer.saveBufferedImageToConfigFolder(BiomeImageProcessorClient.resourceLocationToBufferedImage(ResourceLocation.fromNamespaceAndPath("rainworld", "textures/dynamic/shader_data.png")), "shader_data.png");
                    data = ConfigManagerServer.readDataFromConfigFolder("shader_data.png");
                    if(data != null) {
                        ClientPlayNetworking.send(new BiomeSyncFromClientPacket(data));
                    }
                }
                else {
                    ClientPlayNetworking.send(new BiomeSyncFromClientPacket(data));
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(HeyTheBiomeIsPlacedYouCanDiscardYourSelectionPacket.ID, (payload, context) -> {
            context.client().execute(BoxRenderer::clearBoxes);
        });

        ClientPlayNetworking.registerGlobalReceiver(BiomeSyncPacket.ID, (payload, context) -> {
            context.client().execute(() -> {
                BufferedImage image = BiomeImageProcessorClient.byteArrayToBufferedImage(payload.imageData());
                JsonElement jsonData = payload.configData();
                ConfigManagerServer.saveBufferedImageToConfigFolder(image, "shader_data.png");
                ConfigManagerServer.writeConfig("biome_settings.json", jsonData.getAsJsonObject());
                DynamicAssets.loadOrUpdateTexture();
                //System.out.println("Received config " + jsonData.toString().getBytes().length + " bytes: " + payload.imageData().length);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(BiomeCacheUpdatePacket.ID, (payload, context) -> {
            context.client().execute(() -> {
                BiomeModify.updateClientBiomeVisuals(payload.key(), 0, 0, 0, 0);
            });
        });


        ClientPlayNetworking.registerGlobalReceiver(BiomeUpdatePacket.ID, (payload, context) -> {
            context.client().execute(() -> {
                // Client receives the biome data back plus the new byte array
                BufferedImage image = BiomeImageProcessorClient.byteArrayToBufferedImage(payload.imageData());
                //if(System.getProperty("os.name").toLowerCase().contains("win")) {
                    //ImageGenerator.saveImageToFile(image, "png", Minecraft.getInstance().gameDirectory.getPath() + "\\config\\" + MOD_ID + "\\shader_stuff\\texture.png");
                //}
                //else {
                    //ImageGenerator.saveImageToFile(image, "png", Minecraft.getInstance().gameDirectory.getPath() + "/config/" + MOD_ID + "/shader_stuff/texture.png");
                //}
                ConfigManagerServer.saveBufferedImageToConfigFolder(image, "shader_stuff/texture.png");
                DynamicAssets.loadOrUpdateTexture();
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SelectedLocationPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                //context.player().sendSystemMessage(Component.literal("Received location: " + payload.globalPos().pos()));
                if(!(BoxRenderer.firstAndSecondLocations.contains(payload.firstPos()) && BoxRenderer.firstAndSecondLocations.contains(payload.secondPos()))) {
                    //BoxRenderer.addBox(payload.firstPos().pos(), payload.secondPos().pos(), context.client().level);
                    BoxRenderer.addOtherPeoplesBox(payload.firstPos().pos(), payload.secondPos().pos(), context.client().level, payload.playerName());
                    //BoxContainerRenderer.addBox(payload.firstPos().pos(), payload.secondPos().pos(), context.client().level);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(RemoveBoxPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                BoxRenderer.removeOtherPeoplesBoxesByUUID(payload.playerName());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(BiomeUpdatePacket.ID, ((payload, context) -> {
            context.client().execute(() -> {
                //System.out.println(payload.internalBiomeName());
                BufferedImage image = BiomeImageProcessorClient.byteArrayToBufferedImage(payload.imageData());
                saveBufferedImageToConfigFolder(image, "shader_data.png");
                DynamicAssets.loadOrUpdateTexture();
            });
        }));
        //Matrix4f originalRenderMatrix = RenderSystem.getProjectionMatrix();
        //VertexSorting originalVertexSorting = RenderSystem.getVertexSorting();


        /*
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (!level.isClientSide()) {
                ItemStack itemStack = player.getItemInHand(hand);

                if (itemStack.getItem() instanceof BlockItem blockItem) {
                    Block block = blockItem.getBlock();
                    ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
                    String namespace = id.getNamespace();
                    String path = id.getPath();

                    if (namespace.equals(MOD_ID) && path.equals("debug_block")) {
                        BlockState stateToPlace = ModBlocks.MULTI_PART_BLOCK.defaultBlockState();
                        BlockPos targetPos = hitResult.getBlockPos().above().above();

                        // Place the block
                        boolean success = level.setBlock(targetPos, stateToPlace, 3); // Flag 3 = update neighbors + client

                        if (success) {
                            // Now get the block entity from the world

                            //BlockEntity be = level.getBlockEntity(targetPos);
                            if(stateToPlace.getBlock() instanceof MultiPartBlock block1) {
                                //BlockEntity be = level.getBlockEntity(targetPos);
                                BlockEntity be = block1.newBlockEntity(targetPos, stateToPlace,
                                        MultiPartBlockEntity.XPosition.MIDDLE,
                                        MultiPartBlockEntity.YPosition.BOTTOM,
                                        MultiPartBlockEntity.ZPosition.MIDDLE);

                                be = block1.newBlockEntity(targetPos, stateToPlace,
                                        MultiPartBlockEntity.XPosition.LEFT,
                                        MultiPartBlockEntity.YPosition.BOTTOM,
                                        MultiPartBlockEntity.ZPosition.MIDDLE);
                                /*
                                if (be instanceof MultiPartBlockEntity mpbe) {

                                    mpbe.setLinkedX(targetPos.getX());
                                    mpbe.setLinkedY(targetPos.getY() - 1);
                                    mpbe.setLinkedZ(targetPos.getZ());

                                    // Mark dirty so it gets saved
                                    mpbe.setChanged();
                                    // If you're using a custom sync packet, send it here
                                    System.out.println("Updated linked values for MultiPartBlockEntity at target " + targetPos + ": " + "linkedPosX: " + mpbe.getLinkedX() + ", linkedPosY: " + mpbe.getLinkedY() + ", linkedPosZ: " + mpbe.getLinkedZ());
                                    if(mpbe.getOriginalPos() != targetPos) {
                                        System.out.println("Warning: MultiPartBlockEntity at " + targetPos + " has a different BlockPos than expected: " + mpbe.getOriginalPos());
                                        mpbe.setOriginalPos(targetPos);
                                    }

                                } else {
                                    System.out.println("Expected MultiPartBlockEntity at " + targetPos + ", but got: " + be);
                                }

                                 *//*
                            }

                        } else {
                            System.out.println("Failed to place block at " + targetPos);
                        }

                        return InteractionResult.PASS; // Optionally stop further processing
                    }
                }
            }

            return InteractionResult.PASS;
        });
*/

        WorldRenderEvents.LAST.register(context -> {

            Vec3 cameraPos = context.camera().getPosition();
            PoseStack matrixStack = context.matrixStack();

            //Minecraft minecraft = Minecraft.getInstance();
            //RenderTarget renderTarget = minecraft.getMainRenderTarget();

            //int fbWidth = renderTarget.width;
            //int fbHeight = renderTarget.height;
            //float aspect = (float) fbWidth / fbHeight;

            //float orthoHalfWidth = 1.0f;
            //float orthoHalfHeight = 1.0f / aspect;


            if (!BoxRenderer.locations.isEmpty()) {
                BoxRenderer.renderConnectedBoxes(matrixStack, cameraPos);
                //assert matrixStack != null;
                //BoxContainerRenderer.render(matrixStack, cameraPos);
            }
            if(!BoxRenderer.otherPeoplesLocations.isEmpty()) {
                BoxRenderer.renderOtherPeoplesConnectedBoxes(matrixStack, cameraPos);
            }
            if(!isSelectionConfirmed && firstCorner != null && secondCorner != null) {
                BoxRenderer.renderBox(matrixStack, firstCorner, secondCorner, cameraPos);
                RenderHelper.renderBlockHighlight(matrixStack, firstCorner, cameraPos, 1.0f, 0.3f, 0.3f, 0.95f);
                RenderHelper.renderBlockHighlight(matrixStack, secondCorner, cameraPos, 0.3f, 0.3f, 1.0f, 0.95f);
            }

            //RenderSystem.setProjectionMatrix(originalRenderMatrix, originalVertexSorting);
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            Vec3 cameraPos = context.camera().getPosition();
            PoseStack matrixStack = context.matrixStack();
            if(BlockViewWidget.shouldRender) {
                //RenderSystem.setProjectionMatrix(
                //        new Matrix4f().setOrtho(
                //                -orthoHalfWidth,
                //                orthoHalfWidth,
                //                -orthoHalfHeight,
                //                orthoHalfHeight,
                //                -1000f, 1000f
                //        ),
                //        VertexSorting.ORTHOGRAPHIC_Z
                //);
                BlockViewWidget.render(matrixStack, cameraPos, context.camera().getLookVector());
            }
         });

        ClientTickEvents.START_WORLD_TICK.register((world) -> {
            // Check if the world is null or the player is null
            try {
                if (world == null || Minecraft.getInstance().player == null) return;
                float temperature = world.getBiome(Minecraft.getInstance().player.blockPosition()).value().getBaseTemperature();
                if(temperature != lastTickTemp) {
                    if (!hasDecimal(temperature)) {
                        lastTickTemp = temperature;
                        BufferedImage image = null;
                        try {
                            image = BiomeImageProcessorClient.resourceLocationToBufferedImage(ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/dynamic/shader_data.png"));
                        } catch (Exception e) {
                            System.out.println("Failed to load shader data image: " + e.getMessage());
                        }

                        if (image != null) {
                            Object[] data = ImageGenerator.imageToRoom(image, (int) temperature);
                            if (data.length > 0) {
                                // Update the shader data with the new temperature
                                ShaderInstance instance = GameRenderer.getRendertypeEntityCutoutShader();
                                BufferedImage palette1 = null;
                                BufferedImage palette2 = null;
                                try {
                                    palette1 = BiomeImageProcessorClient.resourceLocationToBufferedImage(ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/palettes/palette" + (int) data[0] + ".png"));
                                    palette2 = BiomeImageProcessorClient.resourceLocationToBufferedImage(ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/palettes/palette" + (int) data[1] + ".png"));
                                } catch (Exception e) {
                                    System.out.println("Failed to load palettes: " + e.getMessage());
                                }
                                //try {
                                //    ImageGenerator.saveImageToFile(image, "png", "/home/deck/IdeaProjects/Rainworld-MC_Biomes mojamap 1.21.1/build/datagen/TEST_FAILSAFE.png");
                                //} catch (Exception e) {
                                //    System.out.println("Failed to save image: " + e.getMessage());
                                //}
                                if (instance != null && palette1 != null && palette2 != null) {
                                    float[] fullRawData = BiomeImageProcessorClient.blendImagesAndSaveReturnFloat(palette1, palette2, (float) data[2] * 100, 0, 2, 29, 7, "/home/deck/IdeaProjects/Rainworld-MC_Biomes mojamap 1.21.1/build/datagen/shader_data.png");
                                    //float[] rawDataForNormalGuis = BiomeImageProcessorClient.paletteColor(palette1, palette2, (float) data[2] * 100);
                                    int n = 0;
                                    //while (n <= 8) {
                                    //    instance.safeGetUniform("colors" + n).set(new Matrix4f(
                                    //            rawDataForNormalGuis[0 + (n * 12)], rawDataForNormalGuis[1 + (n * 12)], rawDataForNormalGuis[2 + (n * 12)], 1,
                                    //            rawDataForNormalGuis[3 + (n * 12)], rawDataForNormalGuis[4 + (n * 12)], rawDataForNormalGuis[5 + (n * 12)], 1,
                                    //            rawDataForNormalGuis[6 + (n * 12)], rawDataForNormalGuis[7 + (n * 12)], rawDataForNormalGuis[8 + (n * 12)], 1,
                                    //            rawDataForNormalGuis[9 + (n * 12)], rawDataForNormalGuis[10 + (n * 12)], rawDataForNormalGuis[11 + (n * 12)], 1
                                    //    ));
                                    //    n++;
                                    //}
                                    while (n <= 44) {
                                        try {
                                            instance.safeGetUniform("data" + (n + 1)).set(new Matrix4f(
                                                    fullRawData[0 + (n * 12)], fullRawData[1 + (n * 12)], fullRawData[2 + (n * 12)], 1,
                                                    fullRawData[3 + (n * 12)], fullRawData[4 + (n * 12)], fullRawData[5 + (n * 12)], 1,
                                                    fullRawData[6 + (n * 12)], fullRawData[7 + (n * 12)], fullRawData[8 + (n * 12)], 1,
                                                    fullRawData[9 + (n * 12)], fullRawData[10 + (n * 12)], fullRawData[11 + (n * 12)], 1
                                            ));
                                            n++;
                                        }
                                        catch (Exception e) {
                                            e.printStackTrace();
                                            n = 99;
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        lastTickTemp = temperature;
                        // Update the shader data with the new temperature
                        ShaderInstance instance = GameRenderer.getRendertypeEntityCutoutShader();
                        BufferedImage palette1 = null;
                        BufferedImage palette2 = null;
                        try {
                            palette1 = BiomeImageProcessorClient.resourceLocationToBufferedImage(ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/palettes/palette" + 0 + ".png")); //69420
                            palette2 = BiomeImageProcessorClient.resourceLocationToBufferedImage(ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/palettes/palette" + 0 + ".png"));
                        } catch (Exception e) {
                            System.out.println("Failed to load palettes: " + e.getMessage());
                        }
                        if (instance != null && palette1 != null && palette2 != null) {
                            float[] fullRawData = BiomeImageProcessorClient.blendImagesAndSaveReturnFloat(palette1, palette2, (float) 0 * 100, 0, 2, 29, 7, "/home/deck/IdeaProjects/Rainworld-MC_Biomes mojamap 1.21.1/build/datagen/shader_data.png");
                            //float[] rawDataForNormalGuis = BiomeImageProcessorClient.paletteColor(palette1, palette2, (float) data[2] * 100);
                            int n = 0;
                            //while (n <= 8) {
                            //    instance.safeGetUniform("colors" + n).set(new Matrix4f(
                            //            rawDataForNormalGuis[0 + (n * 12)], rawDataForNormalGuis[1 + (n * 12)], rawDataForNormalGuis[2 + (n * 12)], 1,
                            //            rawDataForNormalGuis[3 + (n * 12)], rawDataForNormalGuis[4 + (n * 12)], rawDataForNormalGuis[5 + (n * 12)], 1,
                            //            rawDataForNormalGuis[6 + (n * 12)], rawDataForNormalGuis[7 + (n * 12)], rawDataForNormalGuis[8 + (n * 12)], 1,
                            //            rawDataForNormalGuis[9 + (n * 12)], rawDataForNormalGuis[10 + (n * 12)], rawDataForNormalGuis[11 + (n * 12)], 1
                            //    ));
                            //    n++;
                            //}
                            while (n <= 44) {
                                try {
                                    instance.safeGetUniform("data" + (n + 1)).set(new Matrix4f(
                                            fullRawData[0 + (n * 12)], fullRawData[1 + (n * 12)], fullRawData[2 + (n * 12)], 1,
                                            fullRawData[3 + (n * 12)], fullRawData[4 + (n * 12)], fullRawData[5 + (n * 12)], 1,
                                            fullRawData[6 + (n * 12)], fullRawData[7 + (n * 12)], fullRawData[8 + (n * 12)], 1,
                                            fullRawData[9 + (n * 12)], fullRawData[10 + (n * 12)], fullRawData[11 + (n * 12)], 1
                                    ));
                                    n++;
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                    n = 99;
                                }
                            }
                        }
                    }
                }
            }
            catch (Exception e) {
                System.out.println("Something went wrong whilst updating biomes for the shader: " + e.getMessage());
                e.printStackTrace();
            }
        });


        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            ItemStack heldItem = player.getItemInHand(hand);

            if (heldItem.getItem() == ROOM_SELECTOR_ITEM) {
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.PASS;
        });

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            ItemStack heldItem = player.getItemInHand(hand);

            if (heldItem.getItem() == ROOM_SELECTOR_ITEM) {
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null || client.player == null) return;
            shouldHaveActiveEffectChain = true;
            //if(renderEffectChain == null) {
            //    loadChain();
            //}
            if (client.screen != null) return;
            if (ticksSinceGuiExit > 0) {
                ticksSinceGuiExit++;
                if (ticksSinceGuiExit >= maxTicksToIgnoreInputsAfterGuiExit) {
                    ticksSinceGuiExit = 0;
                    if(mainGui != null) {
                        mainGui.isClosing = false;
                    }
                    //loadChain();
                }
                return;
            }

            if(Minecraft.getInstance().options.biomeBlendRadius().get() != 0) {
                Minecraft.getInstance().options.biomeBlendRadius().set(0);
            }

            // Check if the player is holding the room selector item
            ItemStack mainHandItem = client.player.getMainHandItem();
            if (mainHandItem.getItem() != ROOM_SELECTOR_ITEM) {
                return;
            }

            // Continue with your existing code for handling input...
            long window = client.getWindow().getWindow();
            boolean rightClick = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
            boolean leftClick = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
            boolean ctrlPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS ||
                    GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;

            // Handle mouse inputs based on current mode
            if (currentMode == SelectionMode.AREA_SELECT) {
                handleAreaSelectMode(client, leftClick, rightClick, ctrlPressed);
            } else {
                // For all other modes, left or right click opens the corresponding GUI
                if ((leftClick && !prevLeft) || (rightClick && !prevRight)) {
                    openModeGUI(client);
                }
            }

            // Update previous input states
            prevRight = rightClick;
            prevLeft = leftClick;
            prevCtrl = ctrlPressed;
        });


        /*
        // Add this mouse scroll callback
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            MouseHandler mouse = client.mouseHandler;

            // Create a mouse scroll callback
            GLFW.glfwSetScrollCallback(client.getWindow().getWindow(), (window, xOffset, yOffset) -> {
                // Check if both conditions are met:
                // 1. Ctrl key is held down
                // 2. Room config item is in main hand
                boolean ctrlPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;

                boolean holdingRoomItem = false;
                if (client.player != null) {
                    ItemStack mainHandItem = client.player.getMainHandItem();
                    holdingRoomItem = mainHandItem.getItem() == ROOM_SELECTOR_ITEM;
                }

                // If both conditions are met, handle our custom scrolling logic
                if (ctrlPressed && holdingRoomItem) {
                    // Don't call the original scroll handler, so it won't scroll to the next/previous slot

                    scrollAccumulator += yOffset;
                    prevScrollPosition = scrollPosition;
                    scrollPosition = scrollAccumulator;

                    // Change mode directly
                    int direction = yOffset > 0 ? 1 : -1;
                    switchMode(direction);
                    if (client.player != null) {
                        client.player.displayClientMessage(Component.literal("Mode changed to: " + currentMode.getDisplayName()), true);
                    }
                } else {
                    // Otherwise, let the normal scroll behavior work
                    // Call the original mouse scroll handler for default functionality
                    mouse.onScroll(window, xOffset, yOffset);
                }
            });
        });

         */

    }

    public static boolean hasDecimal(float number) {
        return number % 1 != 0; // Check if there's a remainder when dividing by 1
    }

    private void handleAreaSelectMode(Minecraft client, boolean leftClick, boolean rightClick, boolean ctrlPressed) {
        // Track when control is initially pressed
        if (ctrlPressed && !prevCtrl) {
            ctrlPressTime = System.currentTimeMillis();
        }

        // Check for control key release
        if (!ctrlPressed && prevCtrl) {
            // Only perform actions if the timeout hasn't been reached
            long currentTime = System.currentTimeMillis();
            if (currentTime - ctrlPressTime <= CTRL_TIMEOUT_MS) {
                // If we have a complete but unconfirmed selection, confirm it
                if (firstCorner != null && secondCorner != null && !isSelectionConfirmed) {
                    confirmSelection(client);
                    return; // Exit to prevent multiple actions on the same release
                }

                // If selection is already confirmed, open the GUI
                if (isSelectionConfirmed) {
                    openRoomSelectionGUI(client);
                    return;
                }
            }
        }

        // Reset press time if timeout is reached while control is still pressed
        if (ctrlPressed) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - ctrlPressTime > CTRL_TIMEOUT_MS) {
                // Optional: You could notify the user that the control action was discarded
                // client.player.displayClientMessage(Component.literal("Control action timed out"), true);
                ctrlPressTime = 0; // Reset the timer
            }
        }

        // Handle clicks for corner selection (only if Ctrl is not pressed)
        if (!ctrlPressed) {
            if (leftClick && !prevLeft) {
                setCorner(client, true);
            }

            if (rightClick && !prevRight) {
                setCorner(client, false);
            }
        }
    }



    private void setCorner(Minecraft client, boolean isFirstCorner) {
        BlockPos pos = null;
        HitResult hitResult = client.hitResult;

        // Use block position if hitting a block
        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            pos = ((BlockHitResult) hitResult).getBlockPos();
        }
        // Otherwise use player's feet position
        else if (client.player != null) {
            pos = BlockPos.containing(client.player.position());
            //client.player.displayClientMessage(Component.literal("No block in range, using player position"), true);
        }

        if (pos != null) {
            if (isFirstCorner) {
                firstCorner = pos;
                client.player.displayClientMessage(Component.literal(
                        "First corner set at: " + firstCorner.toShortString()), true);
                isSelectionConfirmed = false;
            } else {
                secondCorner = pos;
                client.player.displayClientMessage(Component.literal(
                        "Second corner set at: " + secondCorner.toShortString()), true);

                // Calculate dimensions when both corners are set
                if (firstCorner != null) {
                    int xSize = Math.abs(secondCorner.getX() - firstCorner.getX()) + 1;
                    int ySize = Math.abs(secondCorner.getY() - firstCorner.getY()) + 1;
                    int zSize = Math.abs(secondCorner.getZ() - firstCorner.getZ()) + 1;
                    int volume = xSize * ySize * zSize;

                    //client.player.displayClientMessage(Component.literal(
                    //        String.format("Room dimensions: %d x %d x %d = %d blocks",
                    //                xSize, ySize, zSize, volume)), false);
                }
            }
        }
    }

    private void confirmSelection(Minecraft client) {
        // Add selection to list
        //selections.add(new AreaSelection(firstCorner, secondCorner));
        assert client.player != null;
        BoxRenderer.addBox(firstCorner, secondCorner, client.level, client.player.getUUID());
        isSelectionConfirmed = true;
        client.player.displayClientMessage(Component.literal(
                "Selection confirmed. Press Ctrl again to open room selection GUI, or continue adding areas."), true);
    }

    private void openRoomSelectionGUI(Minecraft client) {
        //client.player.displayClientMessage(Component.literal(
        //        "Opening room selection GUI for all selected areas"), true);
        if(mainGui == null) {
            mainGui = new MainGui(client.player, MainGui.GuiType.ROOM_EDIT);
        }
        client.setScreen(mainGui);
        //client.setScreen(new MainGui(client.player, MainGui.GuiType.ROOM_EDIT));
        // Reset selections after GUI processing
    }

    private void openModeGUI(Minecraft client) {
        switch (currentMode) {
            case ROOM_SELECT:
                //client.player.displayClientMessage(Component.literal(
                //        "Opening room selection GUI"), true);
                if(mainGui == null) {
                    mainGui = new MainGui(client.player, MainGui.GuiType.ROOM_EDIT);
                }
                client.setScreen(mainGui);
                //client.setScreen(new MainGui(client.player, MainGui.GuiType.ROOM_REGION_SELECT));
                break;
            case REGION_SELECT:
                //client.player.displayClientMessage(Component.literal(
                //        "Opening region selection GUI"), true);
                if(mainGui == null) {
                    mainGui = new MainGui(client.player, MainGui.GuiType.ROOM_EDIT);
                }
                client.setScreen(mainGui);
                //client.setScreen(new MainGui(client.player, MainGui.GuiType.ROOM_REGION_SELECT));
                break;
            case ROOM_MODIFY:
                //client.player.displayClientMessage(Component.literal(
                //        "Opening room add/modify GUI"), true);
                if(mainGui == null) {
                    mainGui = new MainGui(client.player, MainGui.GuiType.ROOM_EDIT);
                }
                client.setScreen(mainGui);
                //client.setScreen(new MainGui(client.player, MainGui.GuiType.ROOM_CREATE));
                break;
            default:
                break;
        }
    }

    private void switchMode(int direction) {
        // Get all modes in an array
        SelectionMode[] modes = SelectionMode.values();

        // Find current mode index
        int currentIndex = 0;
        for (int i = 0; i < modes.length; i++) {
            if (modes[i] == currentMode) {
                currentIndex = i;
                break;
            }
        }

        // Calculate new index with wrapping
        int newIndex = (currentIndex + direction) % modes.length;
        if (newIndex < 0) newIndex = modes.length - 1;

        // Set new mode
        currentMode = modes[newIndex];

        // Reset selections when changing modes
        //resetSelections();
    }

    public static void resetSelections(Player player) {
        ClientPlayNetworking.send(new RemoveBoxPayload(player.getUUID()));
        firstCorner = null;
        secondCorner = null;
        isSelectionConfirmed = false;
        selections.clear();
    }

}

