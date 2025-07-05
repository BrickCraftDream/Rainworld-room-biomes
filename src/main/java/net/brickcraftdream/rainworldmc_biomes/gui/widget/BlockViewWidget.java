package net.brickcraftdream.rainworldmc_biomes.gui.widget;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import net.brickcraftdream.rainworldmc_biomes.ColorPicker;
import net.brickcraftdream.rainworldmc_biomes.client.BoxRenderer;
import net.brickcraftdream.rainworldmc_biomes.gui.DataHandler;
import net.brickcraftdream.rainworldmc_biomes.networking.BiomeImageProcessorClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.model.AtlasSet;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

import static net.brickcraftdream.rainworldmc_biomes.Rainworld_MC_Biomes.MOD_ID;
import static net.brickcraftdream.rainworldmc_biomes.gui.DataHandler.*;

public class BlockViewWidget extends AbstractWidget {
    static final Logger LOGGER = LogUtils.getLogger();
    public static int viewSize = 0; // Size of the view in blocks (square)
    public static final Map<BlockPos, BlockState> blocks = new HashMap<>();
    public static final Map<BlockPos, ResourceLocation> customTextures = new HashMap<>();
    public final float blockScale;
    public static int centerX = 0;
    public static int centerY = 0;

    public static boolean shouldRender = false;

    public static float rotationX = -30.0F; // Default X rotation (pitch)
    public static float rotationY = 45.0F; // Default Y rotation (yaw)
    public boolean isDragging = false;
    public int lastMouseX, lastMouseY;


    // For biome tinting
    public static @Nullable Biome biome;

    private static ShaderInstance customShader;
    private static Uniform targetColorUniform;
    private static Uniform replacementColorUniform;

    public static PostChain renderEffectChain;
    public static boolean shouldHaveActiveEffectChain = false;

    public static PostChain renderEffectChainForNormalGuis;
    public static boolean shouldHaveActiveEffectChainForNormalGuis = false;

    // Data holders for the current palette
    public static int palette = 0;
    public static int fadePalette = 0;
    public static float fadeStrength = 0f;
    public static float[] rawData = new float[108];

    public static int paletteForNormalGuis = 0;
    public static int fadePaletteForNormalGuis = 0;
    public static float fadeStrengthForNormalGuis = 0f;
    public static float[] rawDataForNormalGuis = new float[108];

    public static void updateTextureValues(int paletteA, int fadePaletteA, float fadeStrengthA) {
        //System.out.println("updateTextureValues called with paletteA: " + paletteA + ", fadePaletteA: " + fadePaletteA + ", fadeStrengthA: " + fadeStrengthA);

        palette = paletteA;
        fadePalette = fadePaletteA;
        fadeStrength = fadeStrengthA;

        //System.out.println("Palette values updated. palette: " + palette + ", fadePalette: " + fadePalette + ", fadeStrength: " + fadeStrength);

        BufferedImage palette1 = BiomeImageProcessorClient.resourceLocationToBufferedImage(ResourceLocation.fromNamespaceAndPath("rainworld", "textures/palettes/palette" + paletteA + ".png"));
        BufferedImage palette2 = BiomeImageProcessorClient.resourceLocationToBufferedImage(ResourceLocation.fromNamespaceAndPath("rainworld", "textures/palettes/palette" + fadePaletteA + ".png"));

        if (palette1 == null || palette2 == null) {
            //System.out.println("Failed to load palette images for paletteA: " + paletteA + " and fadePaletteA: " + fadePaletteA);
            return;
        }

        //System.out.println("Palette images loaded successfully.");

        rawData = BiomeImageProcessorClient.paletteColor(palette1, palette2, fadeStrength);
        float[] fullRawData = BiomeImageProcessorClient.blendImagesAndSaveReturnFloat(palette1, palette2, fadeStrength, 0, 2, 29, 7, "/home/deck/IdeaProjects/Rainworld-MC_Biomes mojamap 1.21.1/build/datagen/shader_data.png");
        //System.out.println("Raw data updated: " + Arrays.toString(rawData));

        if (renderEffectChain != null) {
            //System.out.println("Render effect chain is not null. Updating uniforms.");
            for (PostPass postPass : renderEffectChain.passes) {
                int n = 0;
                while (n <= 44) {
                    try {
                        //System.out.println("Updating uniform 'colors" + (n + 1) + "' with raw data.");
                        postPass.getEffect().safeGetUniform("data" + (n + 1)).set(new Matrix4f(
                                fullRawData[0 + (n * 12)], fullRawData[1 + (n * 12)], fullRawData[2 + (n * 12)], 1,
                                fullRawData[3 + (n * 12)], fullRawData[4 + (n * 12)], fullRawData[5 + (n * 12)], 1,
                                fullRawData[6 + (n * 12)], fullRawData[7 + (n * 12)], fullRawData[8 + (n * 12)], 1,
                                fullRawData[9 + (n * 12)], fullRawData[10 + (n * 12)], fullRawData[11 + (n * 12)], 1
                        ));
                        //System.out.println("Uniform 'colors" + (n + 1) + "' updated.");
                        n++;
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        n = 99;
                    }
                }
            }
        } else {
            //System.out.println("Render effect chain is null. Skipping uniform updates.");
        }
    }

    public static void updateTextureValuesForNormalGuis(int paletteA, int fadePaletteA, float fadeStrengthA) {
        //System.out.println("updateTextureValues called with paletteA: " + paletteA + ", fadePaletteA: " + fadePaletteA + ", fadeStrengthA: " + fadeStrengthA);

        paletteForNormalGuis = paletteA;
        fadePaletteForNormalGuis = fadePaletteA;
        fadeStrengthForNormalGuis = fadeStrengthA;

        //System.out.println("Palette values updated. palette: " + paletteForNormalGuis + ", fadePalette: " + fadePaletteForNormalGuis + ", fadeStrength: " + fadeStrengthForNormalGuis);

        BufferedImage palette1 = BiomeImageProcessorClient.resourceLocationToBufferedImage(ResourceLocation.fromNamespaceAndPath("rainworld", "textures/palettes/palette" + paletteA + ".png"));
        BufferedImage palette2 = BiomeImageProcessorClient.resourceLocationToBufferedImage(ResourceLocation.fromNamespaceAndPath("rainworld", "textures/palettes/palette" + fadePaletteA + ".png"));

        if (palette1 == null || palette2 == null) {
            System.out.println("Failed to load palette images for paletteA: " + paletteA + " and fadePaletteA: " + fadePaletteA);
            return;
        }

        //System.out.println("Palette images loaded successfully.");

        rawDataForNormalGuis = BiomeImageProcessorClient.paletteColor(palette1, palette2, fadeStrengthForNormalGuis);
        //System.out.println("Raw data updated: " + Arrays.toString(rawDataForNormalGuis));

        if (renderEffectChainForNormalGuis != null) {
            //System.out.println("Render effect chain is not null. Updating uniforms.");
            for (PostPass postPass : renderEffectChainForNormalGuis.passes) {
                int n = 0;
                while (n <= 8) {
                    //System.out.println("Updating uniform 'colors" + n + "' with raw data.");
                    postPass.getEffect().safeGetUniform("colors" + n).set(new Matrix4f(
                            rawDataForNormalGuis[0 + (n * 12)], rawDataForNormalGuis[1 + (n * 12)], rawDataForNormalGuis[2 + (n * 12)], 1,
                            rawDataForNormalGuis[3 + (n * 12)], rawDataForNormalGuis[4 + (n * 12)], rawDataForNormalGuis[5 + (n * 12)], 1,
                            rawDataForNormalGuis[6 + (n * 12)], rawDataForNormalGuis[7 + (n * 12)], rawDataForNormalGuis[8 + (n * 12)], 1,
                            rawDataForNormalGuis[9 + (n * 12)], rawDataForNormalGuis[10 + (n * 12)], rawDataForNormalGuis[11 + (n * 12)], 1
                    ));
                    //System.out.println("Uniform 'colors" + n + "' updated.");
                    n++;
                }
            }
        } else {
            //System.out.println("Render effect chain is null. Skipping uniform updates.");
        }
    }

    public static void loadChain() {
        if(shouldHaveActiveEffectChain) {
            Minecraft.getInstance().gameRenderer.loadEffect(ResourceLocation.withDefaultNamespace("shaders/post/creeper.json"));
            renderEffectChain = Minecraft.getInstance().gameRenderer.postEffect;
        }
    }

    public static void loadChainForNormalGuis() {
        Minecraft.getInstance().gameRenderer.loadEffect(ResourceLocation.withDefaultNamespace("shaders/post/invert.json"));
        renderEffectChainForNormalGuis = Minecraft.getInstance().gameRenderer.postEffect;
    }

    public static void unloadChainForNormalGuis() {
        if(renderEffectChainForNormalGuis != null) {
            renderEffectChainForNormalGuis.close();
            renderEffectChainForNormalGuis = null;
            Minecraft.getInstance().gameRenderer.shutdownEffect();
            shouldHaveActiveEffectChainForNormalGuis = false;
        }
    }

    public static void loadShader() {
        try {
            // Load the shader from the assets directory
            customShader = new ShaderInstance(
                    Minecraft.getInstance().getResourceManager(),
                    "color_replace",
                    DefaultVertexFormat.BLOCK
            );
            customShader.apply();
            customShader.attachToProgram();
            targetColorUniform = customShader.getUniform("FogColor");
            replacementColorUniform = customShader.getUniform("ColorModulator");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load custom shader", e);
        }
    }

    public static void updateShaderUniforms(Vec3 targetColor, Vec3 replacementColor, float tolerance) {
        //RenderSystem.setShader(() -> customShader);
        //System.out.println(customShader.uniformMap.keySet());
        //System.out.println(customShader.uniformMap.size());
        //if(customShader.uniformMap.size() < 3) {
        //    customShader.close();
        //    loadShader();
        //}
        ////customShader.getUniform("tolerance").set(tolerance);
        //targetColorUniform.set((float) targetColor.x, (float) targetColor.y, (float) targetColor.z);
        //replacementColorUniform.set((float) replacementColor.x, (float) replacementColor.y, (float) replacementColor.z);
        //customShader.apply();
        if(renderEffectChain == null) {
            loadChain();
        }
        //for (PostPass postPass : renderEffectChain.passes) {
        //    postPass.getEffect().safeGetUniform("RedMatrix").set((float) targetColor.x, (float) targetColor.y, (float) targetColor.z);
        //    postPass.getEffect().safeGetUniform("GreenMatrix").set((float) replacementColor.x, (float) replacementColor.y, (float) replacementColor.z);
        //}
    }

    /**
     * Creates a new block view widget
     *
     * @param x X position on screen
     * @param y Y position on screen
     * @param width Widget width
     * @param height Widget height
     * @param viewSize Size of the block view in blocks (square)
     */
    public BlockViewWidget(int x, int y, int width, int height, int viewSize) {
        super(x, y, width, height, Component.empty());
        this.viewSize = viewSize;

        // Calculate scale to fit all blocks in the widget
        this.blockScale = Math.min(width, height) / (float) (viewSize * 2);

        // Calculate center for rendering
        this.centerX = width / 2;
        this.centerY = height / 2;

        //try {
        //    BlockViewWidget.updateTextureValues(Integer.parseInt(paletteBoxContent), Integer.parseInt(fadePaletteBoxContent), Float.parseFloat(fadeStrengthBoxContent));
        //}
        //catch (Exception e) {
        //    System.err.println("Error updating texture values: " + e.getMessage());
        //}
    }

    /**
     * Set the block at the specified position
     *
     * @param pos Block position within the view
     * @param state Block state to render
     * @return This widget for chaining
     */
    public BlockViewWidget setBlock(BlockPos pos, BlockState state) {
        if (pos.getX() >= 0 && pos.getX() < viewSize &&
                pos.getY() >= 0 && pos.getY() < viewSize &&
                pos.getZ() >= 0 && pos.getZ() < viewSize) {
            blocks.put(pos, state);
        }
        return this;
    }

    /**
     * Set the block at the specified position with a custom texture
     *
     * @param pos Block position within the view
     * @param state Block state to render
     * @param textureLocation Custom texture resource location
     * @return This widget for chaining
     */
    public BlockViewWidget setBlock(BlockPos pos, BlockState state, ResourceLocation textureLocation) {
        if (setBlock(pos, state) != null) {
            customTextures.put(pos, textureLocation);
        }
        return this;
    }

    /**
     * Remove a custom texture from a block position
     *
     * @param pos Block position
     * @return This widget for chaining
     */
    public BlockViewWidget removeCustomTexture(BlockPos pos) {
        customTextures.remove(pos);
        return this;
    }


    /**
     * Set the biome for tinting
     */
    public BlockViewWidget setBiome(Biome biome) {
        this.biome = biome;
        return this;
    }

    /**
     * Clear all blocks from the view
     */
    public void clearBlocks() {
        blocks.clear();
        customTextures.clear();
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (blocks.isEmpty()) return;
        shouldRender = true;
        shouldHaveActiveEffectChain = true;
    }

    public static void render(PoseStack poseStack, Vec3 camerapos, Vector3f lookVector) {
        // Save original pose state
        poseStack.pushPose();


        //if (customShader == null || customShader.uniformMap.size() < 3) {
        //    loadShader();
        //}
        float[] color = BoxRenderer.getColor(0, 0.25f);
        updateShaderUniforms(new Vec3(0.357, 0, 0), new Vec3(color[0], color[1], color[2]), 1f);
        //ShaderInstance previousShader = RenderSystem.getShader();
        //RenderSystem.setShader(() -> customShader);
        //PoseStack poseStack = guiGraphics.pose();

        Minecraft minecraft = Minecraft.getInstance();
        RenderTarget renderTarget = minecraft.getMainRenderTarget();

        int fbWidth = renderTarget.width;
        int fbHeight = renderTarget.height;
        float aspect = (float) fbWidth / fbHeight;

        float orthoHalfWidth = 1.0f;
        float orthoHalfHeight = 1.0f / aspect;



        // Position at center of widget, fixed distance in front of camera
        Vec3 lookVec = new Vec3(lookVector.x(), lookVector.y(), lookVector.z());
        double distance = 0.5; // blocks in front of camera
        Vec3 targetPos = camerapos.add(lookVec.scale(distance));
        poseStack.translate(targetPos.x - camerapos.x, targetPos.y - camerapos.y, targetPos.z - camerapos.z);

        // Dynamically calculate the offset to keep the object centered
        Minecraft mc = Minecraft.getInstance();
        float yaw = mc.gameRenderer.getMainCamera().getYRot();
        float pitch = mc.gameRenderer.getMainCamera().getXRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));

        // Apply fixed rotation to ensure the object is always at a specific angle
        //
        poseStack.mulPose(Axis.XP.rotationDegrees(rotationX));
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationY));

        // Apply scale to make blocks visible
        float renderScale = 0.06F;
        poseStack.scale(renderScale, -renderScale, renderScale);
        poseStack.mulPose(new Quaternionf().rotationX((float) Math.toRadians(180)));

        // Center blocks
        float halfSize = viewSize / 2.0F;
        poseStack.translate(-halfSize, -halfSize, -halfSize);

        // Rest of the rendering code remains the same
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        RandomSource random = minecraft.level.random;

        // Create a fake block renderer environment that supports biome tinting
        CustomBlockAndTintGetter tintGetter = new CustomBlockAndTintGetter(minecraft);

        // Use proper lighting for shading
        int light = LightTexture.pack(15, 15);
        int overlay = OverlayTexture.NO_OVERLAY;

        // Fix the draw order
        List<BlockPos> sortedPositions = new ArrayList<>(blocks.keySet());
        sortedPositions.sort((a, b) -> {
            int sumA = a.getX() + a.getZ();
            int sumB = b.getX() + b.getZ();

            if (sumA != sumB) {
                return sumA - sumB; // Sort by X+Z sum first (ascending)
            } else if (a.getY() != b.getY()) {
                return a.getY() - b.getY(); // Then by Y (ascending)
            } else if (a.getX() != b.getX()) {
                return a.getX() - b.getX(); // Then by X
            } else {
                return a.getZ() - b.getZ(); // Finally by Z
            }
        });

        // Enable depth testing and blending for transparency
        //RenderSystem.enableDepthTest();
        //RenderSystem.enableBlend();
        //RenderSystem.defaultBlendFunc();

        // First, render solid blocks
        for (BlockPos pos : sortedPositions) {
            BlockState state = blocks.get(pos);

            // Skip transparent blocks for now
            if (state.getRenderShape() != RenderShape.INVISIBLE && !state.getFluidState().isSource() &&
                    !state.is(Blocks.GLASS) && !state.is(Blocks.GLASS_PANE) && !state.is(Blocks.WATER) &&
                    !state.is(Blocks.ICE) && !state.is(Blocks.TINTED_GLASS)) {

                poseStack.pushPose();
                poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

                // Check if we have a custom texture for this block
                ResourceLocation customTexture = customTextures.get(pos);
                if (customTexture != null) {
                    renderCubeWithCustomTexture(poseStack, bufferSource, state, pos, tintGetter,
                            customTexture, light, overlay, RenderType.entitySolid(customTexture));
                } else {
                    renderCubeManually(poseStack, bufferSource, state, pos, tintGetter,
                            light, overlay, RenderType.solid());
                }

                poseStack.popPose();
            }
        }

        bufferSource.endBatch(); // Flush the solid blocks

        // Then render cutout blocks (like leaves)
        for (BlockPos pos : sortedPositions) {
            BlockState state = blocks.get(pos);

            if (state.getRenderShape() != RenderShape.INVISIBLE &&
                    (state.is(Blocks.GLASS) || state.is(Blocks.GLASS_PANE))) {

                poseStack.pushPose();
                poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

                ResourceLocation customTexture = customTextures.get(pos);
                if (customTexture != null) {
                    renderCubeWithCustomTexture(poseStack, bufferSource, state, pos, tintGetter,
                            customTexture, light, overlay, RenderType.entityCutout(customTexture));
                } else {
                    renderCubeManually(poseStack, bufferSource, state, pos, tintGetter,
                            light, overlay, RenderType.cutout());
                }

                poseStack.popPose();
            }
        }

        bufferSource.endBatch(); // Flush the cutout blocks

        // Finally render transparent blocks (like water)
        for (BlockPos pos : sortedPositions) {
            BlockState state = blocks.get(pos);

            if (state.getRenderShape() != RenderShape.INVISIBLE &&
                    (state.getFluidState().isSource() || state.is(Blocks.WATER) || state.is(Blocks.ICE) ||
                            state.is(Blocks.TINTED_GLASS))) {

                poseStack.pushPose();
                poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

                ResourceLocation customTexture = customTextures.get(pos);
                if (customTexture != null) {
                    renderCubeWithCustomTexture(poseStack, bufferSource, state, pos, tintGetter,
                            customTexture, light, overlay, RenderType.entityTranslucent(customTexture));
                } else {
                    renderCubeManually(poseStack, bufferSource, state, pos, tintGetter,
                            light, overlay, RenderType.translucent());
                }

                poseStack.popPose();
            }
        }

        // Finish rendering
        bufferSource.endBatch();
        RenderSystem.disableDepthTest();
        //RenderSystem.setShader(() -> previousShader);
        //if(renderEffectChain != null) {
        //    renderEffectChain.close();
        //    renderEffectChain = null;
        //}

        poseStack.popPose();
        shouldRender = false;
    }

    /**
     * Manually renders a cube using vertices
     */
    private static void renderCubeManually(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                           BlockState state, BlockPos pos, CustomBlockAndTintGetter level,
                                           int light, int overlay, RenderType renderType) {

        // Get appropriate buffer for this render type
        VertexConsumer builder = bufferSource.getBuffer(renderType);

        // Transform matrix from pose stack
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        // Get block color and texture
        Minecraft minecraft = Minecraft.getInstance();
        BlockColors blockColors = minecraft.getBlockColors();
        int blockColor = blockColors.getColor(state, level, pos, 0);

        // Extract RGB components (1.0 = 255, 0.0 = 0)
        float r = 1.0f;//((blockColor >> 16) & 0xFF) / 255.0F;
        float g = 1.0f;//((blockColor >> 8) & 0xFF) / 255.0F;
        float b = 1.0f;//(blockColor & 0xFF) / 255.0F;

        // Get the texture atlas for blocks
        TextureAtlas atlas = minecraft.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);

        // Check for custom texture
        ResourceLocation customTexture = customTextures.get(pos);
        TextureAtlasSprite customSprite = customTexture != null ? atlas.getSprite(customTexture) : null;

        // Get the block model
        BakedModel model = minecraft.getBlockRenderer().getBlockModel(state);

        // Array to store textures for each direction
        TextureAtlasSprite[] faceSprites = new TextureAtlasSprite[6];

        try {
            // If there's a custom texture, use it for all faces
            if (customSprite != null) {
                for (int i = 0; i < 6; i++) {
                    faceSprites[i] = customSprite;
                }
            } else {
                // Get textures for each direction from the model
                for (Direction dir : Direction.values()) {
                    List<BakedQuad> quads = model.getQuads(state, dir, RandomSource.create());
                    if (!quads.isEmpty()) {
                        faceSprites[dir.ordinal()] = quads.get(0).getSprite();
                    } else {
                        faceSprites[dir.ordinal()] = model.getParticleIcon();
                    }
                }
            }
        } catch (Exception e) {
            // Fallback to missing texture if we can't get the proper ones
            TextureAtlasSprite missingSprite = atlas.getSprite(MissingTextureAtlasSprite.getLocation());
            for (int i = 0; i < 6; i++) {
                //faceSprites[i] = missingSprite;
            }
            e.printStackTrace();
        }

        // Render each face of the cube
        renderFace(builder, pose, normal,
                0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, // Front face
                faceSprites[Direction.NORTH.ordinal()].getU0(), faceSprites[Direction.NORTH.ordinal()].getV1(),
                faceSprites[Direction.NORTH.ordinal()].getU1(), faceSprites[Direction.NORTH.ordinal()].getV1(),
                faceSprites[Direction.NORTH.ordinal()].getU1(), faceSprites[Direction.NORTH.ordinal()].getV0(),
                faceSprites[Direction.NORTH.ordinal()].getU0(), faceSprites[Direction.NORTH.ordinal()].getV0(),
                r, g, b, 1.0F, light, overlay, 0, 0, -1);

        renderFace(builder, pose, normal,
                1, 0, 1, 0, 0, 1, 0, 1, 1, 1, 1, 1, // Back face
                faceSprites[Direction.SOUTH.ordinal()].getU0(), faceSprites[Direction.SOUTH.ordinal()].getV1(),
                faceSprites[Direction.SOUTH.ordinal()].getU1(), faceSprites[Direction.SOUTH.ordinal()].getV1(),
                faceSprites[Direction.SOUTH.ordinal()].getU1(), faceSprites[Direction.SOUTH.ordinal()].getV0(),
                faceSprites[Direction.SOUTH.ordinal()].getU0(), faceSprites[Direction.SOUTH.ordinal()].getV0(),
                r, g, b, 1.0F, light, overlay, 0, 0, 1);

        renderFace(builder, pose, normal,
                0, 1, 1, 0, 1, 0, 1, 1, 0, 1, 1, 1, // Top face
                faceSprites[Direction.UP.ordinal()].getU0(), faceSprites[Direction.UP.ordinal()].getV1(),
                faceSprites[Direction.UP.ordinal()].getU1(), faceSprites[Direction.UP.ordinal()].getV1(),
                faceSprites[Direction.UP.ordinal()].getU1(), faceSprites[Direction.UP.ordinal()].getV0(),
                faceSprites[Direction.UP.ordinal()].getU0(), faceSprites[Direction.UP.ordinal()].getV0(),
                r, g, b, 1.0F, light, overlay, 0, 1, 0);

        renderFace(builder, pose, normal,
                0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 0, 0, // Bottom face
                faceSprites[Direction.DOWN.ordinal()].getU0(), faceSprites[Direction.DOWN.ordinal()].getV1(),
                faceSprites[Direction.DOWN.ordinal()].getU1(), faceSprites[Direction.DOWN.ordinal()].getV1(),
                faceSprites[Direction.DOWN.ordinal()].getU1(), faceSprites[Direction.DOWN.ordinal()].getV0(),
                faceSprites[Direction.DOWN.ordinal()].getU0(), faceSprites[Direction.DOWN.ordinal()].getV0(),
                r, g, b, 1.0F, light, overlay, 0, -1, 0);

        renderFace(builder, pose, normal,
                0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 1, // Left face
                faceSprites[Direction.WEST.ordinal()].getU0(), faceSprites[Direction.WEST.ordinal()].getV1(),
                faceSprites[Direction.WEST.ordinal()].getU1(), faceSprites[Direction.WEST.ordinal()].getV1(),
                faceSprites[Direction.WEST.ordinal()].getU1(), faceSprites[Direction.WEST.ordinal()].getV0(),
                faceSprites[Direction.WEST.ordinal()].getU0(), faceSprites[Direction.WEST.ordinal()].getV0(),
                r, g, b, 1.0F, light, overlay, -1, 0, 0);

        renderFace(builder, pose, normal,
                1, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 0, // Right face
                faceSprites[Direction.EAST.ordinal()].getU0(), faceSprites[Direction.EAST.ordinal()].getV1(),
                faceSprites[Direction.EAST.ordinal()].getU1(), faceSprites[Direction.EAST.ordinal()].getV1(),
                faceSprites[Direction.EAST.ordinal()].getU1(), faceSprites[Direction.EAST.ordinal()].getV0(),
                faceSprites[Direction.EAST.ordinal()].getU0(), faceSprites[Direction.EAST.ordinal()].getV0(),
                r, g, b, 1.0F, light, overlay, 1, 0, 0);
    }

    /**
     * Helper method to render a single face of the cube
     */
    private static void renderFace(VertexConsumer vertexConsumer, Matrix4f pose, Matrix3f normal,
                                   float x1, float y1, float z1,
                                   float x2, float y2, float z2,
                                   float x3, float y3, float z3,
                                   float x4, float y4, float z4,
                                   float u1, float v1,
                                   float u2, float v2,
                                   float u3, float v3,
                                   float u4, float v4,
                                   float r, float g, float b, float a,
                                   int light, int overlay,
                                   float normalX, float normalY, float normalZ) {
        normalX = 0 + normalX / 100f;
        normalY = 0.99f + normalY / 100f;
        normalZ = 0 + normalZ / 100f;

        //// Calculate shading based on face normal relative to light direction
        //// Using a light direction of (0.2, 1.0, -0.7) normalized
        //float lightDirX = 0.2f;
        //float lightDirY = 1.0f;
        //float lightDirZ = -0.7f;
//
        //// Normalize light direction
        //float lightLength = (float) Math.sqrt(lightDirX * lightDirX + lightDirY * lightDirY + lightDirZ * lightDirZ);
        //lightDirX /= lightLength;
        //lightDirY /= lightLength;
        //lightDirZ /= lightLength;
//
        //// Calculate dot product between face normal and light direction (ranges from -1 to 1)
        //float dotProduct = normalX * lightDirX + normalY * lightDirY + normalZ * lightDirZ;
//
        //// Calculate shading factor (ranges from 0.7 to 1.0 for subtle shading)
        //float shadingFactor = 0.7f + 0.3f * Math.max(0, dotProduct);
//
        //// Apply shading using HSV transformation to preserve color hue and saturation
        //float[] shadedRgb = shadeColorHsv(r, g, b, shadingFactor);
        //float shadedR = shadedRgb[0];
        //float shadedG = shadedRgb[1];
        //float shadedB = shadedRgb[2];


        // Directly use the original RGB values without applying shading
        float shadedR = r;
        float shadedG = g;
        float shadedB = b;

        // Add each vertex with all its attributes
        vertexConsumer.addVertex(pose, x1, y1, z1)
                .setColor(shadedR, shadedG, shadedB, a)
                .setUv(u1, v1)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(normalX, normalY, normalZ);

        vertexConsumer.addVertex(pose, x2, y2, z2)
                .setColor(shadedR, shadedG, shadedB, a)
                .setUv(u2, v2)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(normalX, normalY, normalZ);

        vertexConsumer.addVertex(pose, x3, y3, z3)
                .setColor(shadedR, shadedG, shadedB, a)
                .setUv(u3, v3)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(normalX, normalY, normalZ);

        vertexConsumer.addVertex(pose, x4, y4, z4)
                .setColor(shadedR, shadedG, shadedB, a)
                .setUv(u4, v4)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(normalX, normalY, normalZ);
    }


    /**
     * Shades a color using HSV transformation to preserve hue and saturation
     * @param r Red component (0-1)
     * @param g Green component (0-1)
     * @param b Blue component (0-1)
     * @param shadingFactor Factor to adjust brightness (0-1)
     * @return Shaded RGB values as float array [r, g, b]
     */
    private static float[] shadeColorHsv(float r, float g, float b, float shadingFactor) {
        // Convert RGB to HSV
        float[] hsv = rgbToHsv(r, g, b);

        // Adjust only the Value (brightness) component
        hsv[2] = hsv[2] * shadingFactor;

        // Convert back to RGB
        return hsvToRgb(hsv[0], hsv[1], hsv[2]);
    }

    /**
     * Converts RGB color values to HSV
     * @param r Red component (0-1)
     * @param g Green component (0-1)
     * @param b Blue component (0-1)
     * @return HSV values as float array [h, s, v]
     */
    private static float[] rgbToHsv(float r, float g, float b) {
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        // Calculate hue
        float h = 0;
        if (delta > 0.000001f) {
            if (max == r) {
                h = (g - b) / delta;
                if (g < b) {
                    h += 6;
                }
            } else if (max == g) {
                h = 2 + (b - r) / delta;
            } else {
                h = 4 + (r - g) / delta;
            }
            h *= 60;
        }

        // Calculate saturation
        float s = (max > 0.000001f) ? delta / max : 0;

        // Value is the max component
        float v = max;

        return new float[] {h, s, v};
    }

    /**
     * Converts HSV color values to RGB
     * @param h Hue component (0-360)
     * @param s Saturation component (0-1)
     * @param v Value component (0-1)
     * @return RGB values as float array [r, g, b]
     */
    private static float[] hsvToRgb(float h, float s, float v) {
        float r = 0, g = 0, b = 0;

        if (s <= 0.0f) {
            r = g = b = v;
        } else {
            h = (h < 0 ? 0 : h > 360 ? 360 : h) / 60.0f;
            int i = (int) Math.floor(h);
            float f = h - i;
            float p = v * (1 - s);
            float q = v * (1 - s * f);
            float t = v * (1 - s * (1 - f));

            switch (i) {
                case 0:
                    r = v; g = t; b = p;
                    break;
                case 1:
                    r = q; g = v; b = p;
                    break;
                case 2:
                    r = p; g = v; b = t;
                    break;
                case 3:
                    r = p; g = q; b = v;
                    break;
                case 4:
                    r = t; g = p; b = v;
                    break;
                default:  // case 5
                    r = v; g = p; b = q;
                    break;
            }
        }

        return new float[] {r, g, b};
    }


    /**
     * Renders a block with a custom texture
     */
    private static void renderCubeWithCustomTexture(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                                    BlockState state, BlockPos pos, CustomBlockAndTintGetter level,
                                                    ResourceLocation textureLocation, int light, int overlay,
                                                    RenderType renderType) {

        // Get the custom texture sprite
        Minecraft minecraft = Minecraft.getInstance();

        // Get appropriate buffer for this render type
        bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        renderType = RenderType.entityTranslucent(textureLocation);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);

        // Transform matrix from pose stack
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normalMatrix = poseStack.last().normal();

        // Get block color and texture
        BlockColors blockColors = minecraft.getBlockColors();
        int blockColor = blockColors.getColor(state, level, pos, 0);

        //int blockColor = ColorPicker.combineColorsBufferedImage(palette1, "28", "5", palette2, "28", "5", opacity);

        // Extract RGB components (1.0 = 255, 0.0 = 0)
        float r = 1.0f;//((blockColor >> 16) & 0xFF) / 255.0F;
        float g = 1.0f;//((blockColor >> 8) & 0xFF) / 255.0F;
        float b = 1.0f;//(blockColor & 0xFF) / 255.0F;
        float a = 1f;

        // Use the custom sprite

        // Draw all six faces of the cube with the custom texture
        // Front face (facing towards negative Z)
        renderFace(vertexConsumer, pose, normalMatrix,
                0, 0, 0,  // bottom-left
                1, 0, 0,  // bottom-right
                1, 1, 0,  // top-right
                0, 1, 0,  // top-left

                0, 1,//missingSprite.getU0(), missingSprite.getV1(),
                1, 1,//missingSprite.getU1(), missingSprite.getV1(),
                1, 0,//missingSprite.getU1(), missingSprite.getV0(),
                0, 0,//missingSprite.getU0(), missingSprite.getV0(),
                r, g, b, a, light, overlay, 0, 0, -1); // Normal facing negative Z

        // The remaining 5 faces follow the same pattern as in renderCubeManually
        // Back face (facing towards positive Z)
        renderFace(vertexConsumer, pose, normalMatrix,
                1, 0, 1,  // bottom-left
                0, 0, 1,  // bottom-right
                0, 1, 1,  // top-right
                1, 1, 1,  // top-left

                0, 1,//missingSprite.getU0(), missingSprite.getV1(),
                1, 1,//missingSprite.getU1(), missingSprite.getV1(),
                1, 0,//missingSprite.getU1(), missingSprite.getV0(),
                0, 0,//missingSprite.getU0(), missingSprite.getV0(),
                r, g, b, a, light, overlay, 0, 0, 1); // Normal facing positive Z

        // Top face (facing towards positive Y)
        renderFace(vertexConsumer, pose, normalMatrix,
                0, 1, 1,  // bottom-left
                0, 1, 0,  // bottom-right
                1, 1, 0,  // top-right
                1, 1, 1,  // top-left

                0, 1,//missingSprite.getU0(), missingSprite.getV1(),
                1, 1,//missingSprite.getU1(), missingSprite.getV1(),
                1, 0,//missingSprite.getU1(), missingSprite.getV0(),
                0, 0,//missingSprite.getU0(), missingSprite.getV0(),
                r, g, b, a, light, overlay, 0, 1, 0); // Normal facing positive Y

        // Bottom face (facing towards negative Y)
        renderFace(vertexConsumer, pose, normalMatrix,
                0, 0, 0,  // bottom-left
                0, 0, 1,  // bottom-right
                1, 0, 1,  // top-right
                1, 0, 0,  // top-left

                0, 1,//missingSprite.getU0(), missingSprite.getV1(),
                1, 1,//missingSprite.getU1(), missingSprite.getV1(),
                1, 0,//missingSprite.getU1(), missingSprite.getV0(),
                0, 0,//missingSprite.getU0(), missingSprite.getV0(),
                r, g, b, a, light, overlay, 0, -1, 0); // Normal facing negative Y

        // Left face (facing towards negative X)
        renderFace(vertexConsumer, pose, normalMatrix,
                0, 0, 1,  // bottom-left
                0, 0, 0,  // bottom-right
                0, 1, 0,  // top-right
                0, 1, 1,  // top-left

                0, 1,//missingSprite.getU0(), missingSprite.getV1(),
                1, 1,//missingSprite.getU1(), missingSprite.getV1(),
                1, 0,//missingSprite.getU1(), missingSprite.getV0(),
                0, 0,//missingSprite.getU0(), missingSprite.getV0(),
                r, g, b, a, light, overlay, -1, 0, 0); // Normal facing negative X

        // Right face (facing towards positive X)
        renderFace(vertexConsumer, pose, normalMatrix,
                1, 0, 0,  // bottom-left
                1, 0, 1,  // bottom-right
                1, 1, 1,  // top-right
                1, 1, 0,  // top-left

                0, 1,//missingSprite.getU0(), missingSprite.getV1(),
                1, 1,//missingSprite.getU1(), missingSprite.getV1(),
                1, 0,//missingSprite.getU1(), missingSprite.getV0(),
                0, 0,//missingSprite.getU0(), missingSprite.getV0(),
                r, g, b, a, light, overlay, 1, 0, 0); // Normal facing positive X
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered && button == 0) { // Left mouse button
            isDragging = true;
            lastMouseX = (int) mouseX;
            lastMouseY = (int) mouseY;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging && button == 0) {
            // Calculate rotation deltas
            int deltaX = (int) mouseX - lastMouseX;
            int deltaY = (int) mouseY - lastMouseY;

            // Update rotations - adjust these multipliers as needed for sensitivity
            rotationY += deltaX * 0.5F;
            //rotationX += deltaY * 0.5F;

            // Limit X rotation to prevent flipping
            //rotationX = Mth.clamp(rotationX, -89.0F, 89.0F);

            // Update last mouse position
            lastMouseX = (int) mouseX;
            lastMouseY = (int) mouseY;

            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isDragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    /**
     * Reset rotation to default values
     * @return This widget for chaining
     */
    public BlockViewWidget resetRotation() {
        rotationX = 30.0F;
        rotationY = 45.0F;
        return this;
    }

    /**
     * Set custom rotation values
     * @param x X rotation (pitch)
     * @param y Y rotation (yaw)
     * @return This widget for chaining
     */
    public BlockViewWidget setRotation(float x, float y) {
        rotationX = Mth.clamp(x, -89.0F, 89.0F);
        rotationY = y;
        return this;
    }



    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    /**
     * Custom block and tint getter that can override textures
     */

}
