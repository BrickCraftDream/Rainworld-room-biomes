package net.brickcraftdream.rainworldmc_biomes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Additional rendering utilities that extend the BoxRenderer functionality
 */
public class RenderHelper {

    // Render a line in 3D space
    public static void renderLine(PoseStack poseStack, Vec3 start, Vec3 end, Vec3 cameraPos, float r, float g, float b, float a) {
        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());

        Matrix4f matrix = poseStack.last().pose();
        consumer.addVertex(matrix, (float)start.x, (float)start.y, (float)start.z)
                .setColor(r, g, b, a)
                .setNormal(0, 1, 0);
        consumer.addVertex(matrix, (float)end.x, (float)end.y, (float)end.z)
                .setColor(r, g, b, a)
                .setNormal(0, 1, 0);

        bufferSource.endBatch(RenderType.lines());
        poseStack.popPose();
    }

    // Render multiple connecting lines (path)
    public static void renderPath(PoseStack poseStack, Vec3[] points, Vec3 cameraPos, float r, float g, float b, float a) {
        if (points.length < 2) return;

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());

        Matrix4f matrix = poseStack.last().pose();

        for (int i = 0; i < points.length - 1; i++) {
            Vec3 start = points[i];
            Vec3 end = points[i + 1];

            consumer.addVertex(matrix, (float)start.x, (float)start.y, (float)start.z)
                    .setColor(r, g, b, a)
                    .setNormal(0, 1, 0);
            consumer.addVertex(matrix, (float)end.x, (float)end.y, (float)end.z)
                    .setColor(r, g, b, a)
                    .setNormal(0, 1, 0);
        }

        bufferSource.endBatch(RenderType.lines());
        poseStack.popPose();
    }

    // Render a series of points as markers
    public static void renderPoints(PoseStack poseStack, Vec3[] points, float size, Vec3 cameraPos, float r, float g, float b, float a) {
        if (points.length == 0) return;

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        for (Vec3 point : points) {
            renderMarker(poseStack, point, size, r, g, b, a);
        }

        poseStack.popPose();
    }

    // Render a single marker (small box) at a position
    private static void renderMarker(PoseStack poseStack, Vec3 pos, float size, float r, float g, float b, float a) {
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        float halfSize = size / 2.0f;
        AABB box = new AABB(
                pos.x - halfSize, pos.y - halfSize, pos.z - halfSize,
                pos.x + halfSize, pos.y + halfSize, pos.z + halfSize
        );

        LevelRenderer.renderLineBox(
                poseStack,
                bufferSource.getBuffer(RenderType.lines()),
                box.minX, box.minY, box.minZ,
                box.maxX, box.maxY, box.maxZ,
                r, g, b, a
        );

        bufferSource.endBatch(RenderType.lines());
    }

    // Render a text label at a 3D position
    public static void renderTextIn3D(PoseStack poseStack, Vec3 pos, String text, Vec3 cameraPos) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.font == null) return;

        poseStack.pushPose();

        // Position at the location in world space
        poseStack.translate(pos.x - cameraPos.x, pos.y - cameraPos.y, pos.z - cameraPos.z);

        // Make the text face the player (billboard)
        // Negate the viewer rotation by rotating in the opposite direction
        poseStack.mulPose(minecraft.gameRenderer.getMainCamera().rotation());
        // Apply a 180-degree rotation to face the player
        poseStack.mulPose(new Quaternionf(0f, 1f , 0f, 180f));

        // Scale down to appropriate size in world
        float scale = 2.025f;
        poseStack.scale(-scale, -scale, scale);

        // Center the text
        float textWidth = minecraft.font.width(text);
        float x = -textWidth / 2.0f;

        // Render the text
        Matrix4f matrix = poseStack.last().pose();
        minecraft.font.drawInBatch(
                text,
                x, 0, 0xFFFFFFFF, // White text
                false, // No shadow
                matrix,
                minecraft.renderBuffers().bufferSource(),
                net.minecraft.client.gui.Font.DisplayMode.NORMAL,
                0x80000000, // Semi-transparent background
                LightTexture.FULL_BRIGHT
        );

        minecraft.renderBuffers().bufferSource().endBatch();

        poseStack.popPose();
    }

    // Render a glowing highlight around a block
    public static void renderBlockHighlight(PoseStack poseStack, BlockPos pos, Vec3 cameraPos, float r, float g, float b, float a) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || pos == null) return;

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        // Get light level for the block
        //int blockLight = minecraft.level.getBrightness(LightLayer.BLOCK, pos);
        //int skyLight = minecraft.level.getBrightness(LightLayer.SKY, pos);
        //int packedLight = LightTexture.pack(blockLight, skyLight);

        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());

        // Slightly larger than the block to avoid z-fighting
        float expansion = 0.002f;

        // Draw the box outline
        LevelRenderer.renderLineBox(
                poseStack,
                consumer,
                pos.getX() - expansion,
                pos.getY() - expansion,
                pos.getZ() - expansion,
                pos.getX() + 1 + expansion,
                pos.getY() + 1 + expansion,
                pos.getZ() + 1 + expansion,
                r, g, b, a
        );

        bufferSource.endBatch(RenderType.lines());
        poseStack.popPose();
    }
}