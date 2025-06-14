package net.brickcraftdream.rainworldmc_biomes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.*;

public class BoxContainerRenderer {
    public static final List<ShapeQuads> shapes = new ArrayList<>();
    public static List<GlobalPos> firstAndSecondLocations = new ArrayList<>();

    /**
         * Represents a single quad (face of a block)
         */
        public record Quad(BlockPos pos, Direction direction) {

        @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Quad quad = (Quad) o;
                return Objects.equals(pos, quad.pos) && direction == quad.direction;
            }

    }

    /**
     * Represents all quads for a single face direction (e.g., all NORTH-facing quads)
     */
    public static class FaceQuads {
        private final Direction direction;
        private final List<BlockPos> positions = new ArrayList<>();
        private final Map<Integer, List<BlockPos>> optimizedFaces = new HashMap<>();

        public FaceQuads(Direction direction) {
            this.direction = direction;
        }

        public void addQuad(Quad quad) {
            if (quad.direction() == direction) {
                positions.add(quad.pos());
            }
        }

        public Direction getDirection() {
            return direction;
        }

        public List<BlockPos> getPositions() {
            return Collections.unmodifiableList(positions);
        }

        /**
         * Optimizes the face by grouping adjacent blocks into larger quads
         */
        public void optimize() {
            // Clear previous optimizations
            optimizedFaces.clear();

            // Clone positions to avoid modification during iteration
            List<BlockPos> remainingPositions = new ArrayList<>(positions);

            // While we still have positions to process
            while (!remainingPositions.isEmpty()) {
                // Take the first position as a starting point
                BlockPos start = remainingPositions.get(0);
                remainingPositions.remove(0);

                // Find the largest possible rectangle that can be formed
                // starting from this position based on the direction
                List<BlockPos> rectangle = findLargestRectangle(start, remainingPositions, direction);

                // Add this rectangle to our optimized faces
                int key = optimizedFaces.size();
                optimizedFaces.put(key, rectangle);

                // Remove all positions in this rectangle from remaining positions
                remainingPositions.removeAll(rectangle);
            }
        }

        private List<BlockPos> findLargestRectangle(BlockPos start, List<BlockPos> availablePositions, Direction direction) {
            List<BlockPos> result = new ArrayList<>();
            result.add(start);

            // Based on the direction, determine which dimensions to expand in
            // For simplicity, this implementation creates 2D rectangles aligned with the face direction

            int maxWidth = 1;
            int maxHeight = 1;

            // Determine the expansion directions based on the face direction
            int[] widthOffset = new int[3];
            int[] heightOffset = new int[3];

            switch (direction) {
                case NORTH, SOUTH -> {
                    // For North/South faces, expand in X (width) and Y (height)
                    widthOffset[0] = 1;  // X component
                    heightOffset[1] = 1; // Y component
                }
                case EAST, WEST -> {
                    // For East/West faces, expand in Z (width) and Y (height)
                    widthOffset[2] = 1;  // Z component
                    heightOffset[1] = 1; // Y component
                }
                case UP, DOWN -> {
                    // For Up/Down faces, expand in X (width) and Z (height)
                    widthOffset[0] = 1;  // X component
                    heightOffset[2] = 1; // Z component
                }
            }

            // Try to find the largest rectangle possible
            boolean canExpandWidth = true;
            boolean canExpandHeight = true;

            while (canExpandWidth || canExpandHeight) {
                // Try to expand width
                if (canExpandWidth) {
                    boolean widthExpansionSuccessful = true;
                    List<BlockPos> newPositions = new ArrayList<>();

                    // Check if we can add a new column
                    for (int h = 0; h < maxHeight; h++) {
                        BlockPos newPos = start.offset(
                                maxWidth * widthOffset[0],
                                h * heightOffset[1],
                                maxWidth * widthOffset[2]
                        );

                        if (!availablePositions.contains(newPos) && !newPos.equals(start)) {
                            widthExpansionSuccessful = false;
                            break;
                        }
                        newPositions.add(newPos);
                    }

                    if (widthExpansionSuccessful) {
                        result.addAll(newPositions);
                        maxWidth++;
                    } else {
                        canExpandWidth = false;
                    }
                }

                // Try to expand height
                if (canExpandHeight) {
                    boolean heightExpansionSuccessful = true;
                    List<BlockPos> newPositions = new ArrayList<>();

                    // Check if we can add a new row
                    for (int w = 0; w < maxWidth; w++) {
                        BlockPos newPos = start.offset(
                                w * widthOffset[0],
                                maxHeight * heightOffset[1],
                                w * widthOffset[2]
                        );

                        if (!availablePositions.contains(newPos) && !newPos.equals(start)) {
                            heightExpansionSuccessful = false;
                            break;
                        }
                        newPositions.add(newPos);
                    }

                    if (heightExpansionSuccessful) {
                        result.addAll(newPositions);
                        maxHeight++;
                    } else {
                        canExpandHeight = false;
                    }
                }
            }

            return result;
        }

        public Map<Integer, List<BlockPos>> getOptimizedFaces() {
            return optimizedFaces;
        }
    }

    /**
     * Represents all faces for a single shape (e.g., a box)
     */
    public static class ShapeQuads {
        private final Map<Direction, FaceQuads> faceMap = new EnumMap<>(Direction.class);
        private final ResourceKey<Level> dimension;

        public ShapeQuads(ResourceKey<Level> dimension) {
            this.dimension = dimension;
            // Initialize face quads for all directions
            for (Direction dir : Direction.values()) {
                faceMap.put(dir, new FaceQuads(dir));
            }
        }

        public void addQuad(Quad quad) {
            faceMap.get(quad.direction()).addQuad(quad);
        }

        public Map<Direction, FaceQuads> getFaceMap() {
            return Collections.unmodifiableMap(faceMap);
        }

        public ResourceKey<Level> getDimension() {
            return dimension;
        }

        /**
         * Optimizes all faces in this shape
         */
        public void optimize() {
            for (FaceQuads faceQuads : faceMap.values()) {
                faceQuads.optimize();
            }
        }
    }

    public static void addBox(BlockPos firstCorner, BlockPos secondCorner, ClientLevel world) {
        if (firstCorner == null || secondCorner == null) return;



        int minX = Math.min(firstCorner.getX(), secondCorner.getX());
        int minY = Math.min(firstCorner.getY(), secondCorner.getY());
        int minZ = Math.min(firstCorner.getZ(), secondCorner.getZ());
        int maxX = Math.max(firstCorner.getX(), secondCorner.getX()) + 1;
        int maxY = Math.max(firstCorner.getY(), secondCorner.getY()) + 1;
        int maxZ = Math.max(firstCorner.getZ(), secondCorner.getZ()) + 1;
        ResourceKey<Level> dimension = world.dimension();


        ShapeQuads shapeQuads = new ShapeQuads(dimension);

        // Generate quads for box surfaces only (not interior)
        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    // Skip interior blocks (only process the shell)
                    if (x > minX && x < maxX - 1 &&
                            y > minY && y < maxY - 1 &&
                            z > minZ && z < maxZ - 1) {
                        continue;
                    }

                    BlockPos pos = new BlockPos(x, y, z);

                    // Determine which face(s) of this block are on the surface of the box
                    if (x == minX) {
                        shapeQuads.addQuad(new Quad(pos, Direction.WEST));
                    }
                    if (x == maxX - 1) {
                        shapeQuads.addQuad(new Quad(pos, Direction.EAST));
                    }
                    if (y == minY) {
                        shapeQuads.addQuad(new Quad(pos, Direction.DOWN));
                    }
                    if (y == maxY - 1) {
                        shapeQuads.addQuad(new Quad(pos, Direction.UP));
                    }
                    if (z == minZ) {
                        shapeQuads.addQuad(new Quad(pos, Direction.NORTH));
                    }
                    if (z == maxZ - 1) {
                        shapeQuads.addQuad(new Quad(pos, Direction.SOUTH));
                    }
                }
            }
        }

        // Optimize faces to combine adjacent blocks
        shapeQuads.optimize();

        // Add to the list of shapes
        shapes.add(shapeQuads);
        GlobalPos firstGlobalPos = GlobalPos.of(dimension, firstCorner);
        GlobalPos secondGlobalPos = GlobalPos.of(dimension, secondCorner);
        firstAndSecondLocations.add(firstGlobalPos);
        firstAndSecondLocations.add(secondGlobalPos);
    }

    private static void drawQuad(VertexConsumer vertexConsumer, Matrix4f matrix, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r, float g, float b, float a, int packedLight, float u0, float v0, float u1, float v1, Direction face) {
        float epsilon = 0.003f;
        int normalX = 0, normalY = 0, normalZ = 0;

        switch (face) {
            case NORTH:
                float zFront = minZ - epsilon;
                vertexConsumer.addVertex(matrix, minX - epsilon, minY - epsilon, zFront).setColor(r, g, b, a).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, minX - epsilon, maxY + epsilon, zFront).setColor(r, g, b, a).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX + epsilon, maxY + epsilon, zFront).setColor(r, g, b, a).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX + epsilon, minY - epsilon, zFront).setColor(r, g, b, a).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);

                zFront = minZ + epsilon;
                vertexConsumer.addVertex(matrix, minX + epsilon, minY + epsilon, zFront).setColor(r, g, b, a).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, minX + epsilon, maxY - epsilon, zFront).setColor(r, g, b, a).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX - epsilon, maxY - epsilon, zFront).setColor(r, g, b, a).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX - epsilon, minY + epsilon, zFront).setColor(r, g, b, a).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                break;
            case SOUTH:
                float zBack = maxZ + epsilon;
                vertexConsumer.addVertex(matrix, maxX + epsilon, minY - epsilon, zBack).setColor(r, g, b, a).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX + epsilon, maxY + epsilon, zBack).setColor(r, g, b, a).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, minX - epsilon, maxY + epsilon, zBack).setColor(r, g, b, a).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, minX - epsilon, minY - epsilon, zBack).setColor(r, g, b, a).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);

                zBack = maxZ - epsilon;
                vertexConsumer.addVertex(matrix, maxX - epsilon, minY + epsilon, zBack).setColor(r, g, b, a).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX - epsilon, maxY - epsilon, zBack).setColor(r, g, b, a).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, minX + epsilon, maxY - epsilon, zBack).setColor(r, g, b, a).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, minX + epsilon, minY + epsilon, zBack).setColor(r, g, b, a).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                break;
            case DOWN:
                float yBottom = minY - epsilon;
                vertexConsumer.addVertex(matrix, minX - epsilon, yBottom, maxZ + epsilon).setColor(r, g, b, a).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX + epsilon, yBottom, maxZ + epsilon).setColor(r, g, b, a).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX + epsilon, yBottom, minZ - epsilon).setColor(r, g, b, a).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, minX - epsilon, yBottom, minZ - epsilon).setColor(r, g, b, a).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);

                yBottom = minY + epsilon;
                vertexConsumer.addVertex(matrix, minX + epsilon, yBottom, maxZ - epsilon).setColor(r, g, b, a).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX - epsilon, yBottom, maxZ - epsilon).setColor(r, g, b, a).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX - epsilon, yBottom, minZ + epsilon).setColor(r, g, b, a).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, minX + epsilon, yBottom, minZ + epsilon).setColor(r, g, b, a).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                break;
            case UP:
                float yTop = maxY + epsilon;
                vertexConsumer.addVertex(matrix, minX - epsilon, yTop, minZ - epsilon).setColor(r, g, b, a).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX + epsilon, yTop, minZ - epsilon).setColor(r, g, b, a).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX + epsilon, yTop, maxZ + epsilon).setColor(r, g, b, a).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, minX - epsilon, yTop, maxZ + epsilon).setColor(r, g, b, a).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);

                yTop = maxY - epsilon;
                vertexConsumer.addVertex(matrix, minX + epsilon, yTop, minZ + epsilon).setColor(r, g, b, a).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX - epsilon, yTop, minZ + epsilon).setColor(r, g, b, a).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX - epsilon, yTop, maxZ - epsilon).setColor(r, g, b, a).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, minX + epsilon, yTop, maxZ - epsilon).setColor(r, g, b, a).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                break;
            case WEST:
                float xLeft = minX - epsilon;
                vertexConsumer.addVertex(matrix, xLeft, minY - epsilon, maxZ + epsilon).setColor(r, g, b, a).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, xLeft, maxY + epsilon, maxZ + epsilon).setColor(r, g, b, a).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, xLeft, maxY + epsilon, minZ - epsilon).setColor(r, g, b, a).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, xLeft, minY - epsilon, minZ - epsilon).setColor(r, g, b, a).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);

                xLeft = minX + epsilon;
                vertexConsumer.addVertex(matrix, xLeft, minY + epsilon, maxZ - epsilon).setColor(r, g, b, a).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, xLeft, maxY - epsilon, maxZ - epsilon).setColor(r, g, b, a).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, xLeft, maxY - epsilon, minZ + epsilon).setColor(r, g, b, a).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, xLeft, minY + epsilon, minZ + epsilon).setColor(r, g, b, a).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                break;
            case EAST:
                float xRight = maxX + epsilon;
                vertexConsumer.addVertex(matrix, xRight, minY - epsilon, minZ - epsilon).setColor(r, g, b, a).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, xRight, maxY + epsilon, minZ - epsilon).setColor(r, g, b, a).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, xRight, maxY + epsilon, maxZ + epsilon).setColor(r, g, b, a).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, xRight, minY - epsilon, maxZ + epsilon).setColor(r, g, b, a).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);

                xRight = maxX - epsilon;
                vertexConsumer.addVertex(matrix, xRight, minY + epsilon, minZ + epsilon).setColor(r, g, b, a).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, xRight, maxY - epsilon, minZ + epsilon).setColor(r, g, b, a).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, xRight, maxY - epsilon, maxZ - epsilon).setColor(r, g, b, a).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, xRight, minY + epsilon, maxZ - epsilon).setColor(r, g, b, a).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                break;
        }

    }

    public static void renderShapes(VertexConsumer vertexConsumer, Matrix4f matrix, float r, float g, float b, float a, int packedLight) {
        for (ShapeQuads shape : shapes) {
            for (Map.Entry<Direction, FaceQuads> faceEntry : shape.getFaceMap().entrySet()) {
                Direction direction = faceEntry.getKey();
                FaceQuads faceQuads = faceEntry.getValue();

                // Get the optimized faces
                Map<Integer, List<BlockPos>> optimizedFaces = faceQuads.getOptimizedFaces();

                // If there are no optimized faces, skip this direction
                if (optimizedFaces.isEmpty()) {
                    continue;
                }

                // Render each optimized face group
                for (List<BlockPos> faceGroup : optimizedFaces.values()) {
                    if (faceGroup == null || faceGroup.isEmpty()) {
                        continue;
                    }

                    // Find bounds of this face group
                    int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
                    int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

                    // Calculate bounds with a safety limit on iterations
                    int safetyLimit = 1000; // Limit the number of positions to process
                    int count = 0;

                    for (BlockPos pos : faceGroup) {
                        if (count++ > safetyLimit) {
                            break; // Safety measure to prevent infinite loops
                        }

                        minX = Math.min(minX, pos.getX());
                        minY = Math.min(minY, pos.getY());
                        minZ = Math.min(minZ, pos.getZ());
                        maxX = Math.max(maxX, pos.getX() + 1);
                        maxY = Math.max(maxY, pos.getY() + 1);
                        maxZ = Math.max(maxZ, pos.getZ() + 1);
                    }

                    // Skip invalid bounds
                    if (minX > maxX || minY > maxY || minZ > maxZ) {
                        continue;
                    }

                    // Create the quad
                    float adjustedMinX = minX, adjustedMinY = minY, adjustedMinZ = minZ;
                    float adjustedMaxX = maxX, adjustedMaxY = maxY, adjustedMaxZ = maxZ;

                    // Adjust the face to prevent z-fighting (0.002f offset)
                    float offset = 0.002f;
                    switch (direction) {
                        case NORTH -> adjustedMinZ = adjustedMaxZ = minZ + offset;
                        case SOUTH -> adjustedMinZ = adjustedMaxZ = maxZ - offset;
                        case WEST -> adjustedMinX = adjustedMaxX = minX + offset;
                        case EAST -> adjustedMinX = adjustedMaxX = maxX - offset;
                        case DOWN -> adjustedMinY = adjustedMaxY = minY + offset;
                        case UP -> adjustedMinY = adjustedMaxY = maxY - offset;
                    }

                    // Set texture coordinates
                    float u0 = 0.0f;
                    float v0 = 0.0f;
                    float u1 = 1.0f;
                    float v1 = 1.0f;

                    // Draw the quad
                    drawQuad(
                            vertexConsumer, matrix,
                            adjustedMinX, adjustedMinY, adjustedMinZ,
                            adjustedMaxX, adjustedMaxY, adjustedMaxZ,
                            r, g, b, a,
                            packedLight,
                            u0, v0, u1, v1,
                            direction
                    );
                }
            }
        }
    }

    public static void render(PoseStack poseStack, Vec3 cameraPos) {
        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        // Get your vertex consumer
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("rainworld", "textures/block/outline.png");
        RenderType renderType = RenderType.entityTranslucent(texture);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);

        // Get the pose matrix
        Matrix4f matrix = poseStack.last().pose();

        // Calculate light level - using full brightness here
        int packedLight = LightTexture.pack(15, 15); // Full brightness

        // Render with semi-transparent white color
        renderShapes(vertexConsumer, matrix, 1.0f, 1.0f, 1.0f, 0.5f, packedLight);
        poseStack.popPose();
    }
}