package net.brickcraftdream.rainworldmc_biomes.client;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.brickcraftdream.rainworldmc_biomes.biome.BiomeModify;
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
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import java.util.List;

import static net.brickcraftdream.rainworldmc_biomes.Rainworld_MC_Biomes.MOD_ID;
import static net.brickcraftdream.rainworldmc_biomes.Rainworld_MC_Biomes.ROOM_SELECTOR_ITEM;
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
    private static final long CTRL_TIMEOUT_MS = 500; // 1 second timeout

    private static MainGui mainGui = null;


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
                System.out.println("Received config " + jsonData.toString().getBytes().length + " bytes: " + payload.imageData().length);
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
                System.out.println(payload.internalBiomeName());
                BufferedImage image = BiomeImageProcessorClient.byteArrayToBufferedImage(payload.imageData());
                saveBufferedImageToConfigFolder(image, "shader_data.png");
                DynamicAssets.loadOrUpdateTexture();
            });
        }));
        Matrix4f originalRenderMatrix = RenderSystem.getProjectionMatrix();
        VertexSorting originalVertexSorting = RenderSystem.getVertexSorting();

        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {

            Vec3 cameraPos = context.camera().getPosition();
            PoseStack matrixStack = context.matrixStack();

            Minecraft minecraft = Minecraft.getInstance();
            RenderTarget renderTarget = minecraft.getMainRenderTarget();

            int fbWidth = renderTarget.width;
            int fbHeight = renderTarget.height;
            float aspect = (float) fbWidth / fbHeight;

            float orthoHalfWidth = 1.0f;
            float orthoHalfHeight = 1.0f / aspect;


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
                RenderHelper.renderBlockHighlight(matrixStack, firstCorner, cameraPos, 1.0f, 0.3f, 0.3f, 1.0f);
                RenderHelper.renderBlockHighlight(matrixStack, secondCorner, cameraPos, 0.3f, 0.3f, 1.0f, 1.0f);
            }

            RenderSystem.setProjectionMatrix(originalRenderMatrix, originalVertexSorting);
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
            if (client.screen != null) return;

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

