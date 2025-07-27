package net.brickcraftdream.rainworldmc_biomes.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;

import net.brickcraftdream.rainworldmc_biomes.networking.NetworkManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.awt.Color;
import java.util.*;

import static net.brickcraftdream.rainworldmc_biomes.Rainworld_MC_Biomes.MOD_ID;

public class BoxRenderer {
    private static final float LINE_WIDTH = 3.0F;
    private static final float ALPHA = 0.75F;
    private static final float EPSILON = 0.001F;
    private static final float LINE_WIDTH_FULL = 0.0F;

    // Store connected boxes
    private static final List<BoxData> connectedBoxes = new ArrayList<>();
    private static final Map<UUID, List<BoxData>> otherPeoplesConnectedBoxes = new HashMap<>();

    public static List<GlobalPos> locations = new ArrayList<>();
    public static List<GlobalPos> otherPeoplesLocations = new ArrayList<>();
    public static List<GlobalPos> firstAndSecondLocations = new ArrayList<>();

    //private static BoxData selectionBox = new BoxData(0, 0, 0, 0, 0, 0);

    // Class to store box data
    public static class BoxData {
        public final int minX, minY, minZ;
        public final int maxX, maxY, maxZ;

        public BoxData(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }

        public AABB toAABB() {
            return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        }

        public boolean isConnected(BoxData other) {
            // Check if boxes are adjacent or overlapping
            AABB thisBox = this.toAABB();
            AABB otherBox = other.toAABB();

            // Check if boxes are adjacent or overlapping on any axis
            return thisBox.intersects(otherBox) ||
                    isAdjacent(this.minX, this.maxX, other.minX, other.maxX) &&
                            isOverlapping(this.minY, this.maxY, other.minY, other.maxY) &&
                            isOverlapping(this.minZ, this.maxZ, other.minZ, other.maxZ) ||
                    isAdjacent(this.minY, this.maxY, other.minY, other.maxY) &&
                            isOverlapping(this.minX, this.maxX, other.minX, other.maxX) &&
                            isOverlapping(this.minZ, this.maxZ, other.minZ, other.maxZ) ||
                    isAdjacent(this.minZ, this.maxZ, other.minZ, other.maxZ) &&
                            isOverlapping(this.minX, this.maxX, other.minX, other.maxX) &&
                            isOverlapping(this.minY, this.maxY, other.minY, other.maxY);
        }

        private boolean isAdjacent(int min1, int max1, int min2, int max2) {
            return max1 == min2 || max2 == min1;
        }

        private boolean isOverlapping(int min1, int max1, int min2, int max2) {
            return (min1 <= min2 && max1 >= min2) || (min2 <= min1 && max2 >= min1);
        }
    }

    // Add a box to the connected set
    public static void addBox(BlockPos firstCorner, BlockPos secondCorner, ClientLevel world, UUID playerName) {
        if (firstCorner == null || secondCorner == null) return;

        int minX = Math.min(firstCorner.getX(), secondCorner.getX());
        int minY = Math.min(firstCorner.getY(), secondCorner.getY());
        int minZ = Math.min(firstCorner.getZ(), secondCorner.getZ());
        int maxX = Math.max(firstCorner.getX(), secondCorner.getX()) + 1;
        int maxY = Math.max(firstCorner.getY(), secondCorner.getY()) + 1;
        int maxZ = Math.max(firstCorner.getZ(), secondCorner.getZ()) + 1;
        ResourceKey<Level> dimension = world.dimension();

        for(int x = minX; x < maxX; x++) {
            for(int y = minY; y < maxY; y++) {
                for(int z = minZ; z < maxZ; z++) {
                    GlobalPos globalPos = GlobalPos.of(dimension, new BlockPos(x, y, z));
                    locations.add(globalPos);
                }
            }
        }

        GlobalPos firstGlobalPos = GlobalPos.of(dimension, firstCorner);
        GlobalPos secondGlobalPos = GlobalPos.of(dimension, secondCorner);
        firstAndSecondLocations.add(firstGlobalPos);
        firstAndSecondLocations.add(secondGlobalPos);

        ClientPlayNetworking.send(new NetworkManager.SelectedLocationPayload(firstGlobalPos, secondGlobalPos, playerName));

        BoxData newBox = new BoxData(minX, minY, minZ, maxX, maxY, maxZ);
        connectedBoxes.add(newBox);
    }

    public static void addOtherPeoplesBox(BlockPos firstCorner, BlockPos secondCorner, ClientLevel world, UUID playerName) {
        if (firstCorner == null || secondCorner == null) return;

        int minX = Math.min(firstCorner.getX(), secondCorner.getX());
        int minY = Math.min(firstCorner.getY(), secondCorner.getY());
        int minZ = Math.min(firstCorner.getZ(), secondCorner.getZ());
        int maxX = Math.max(firstCorner.getX(), secondCorner.getX()) + 1;
        int maxY = Math.max(firstCorner.getY(), secondCorner.getY()) + 1;
        int maxZ = Math.max(firstCorner.getZ(), secondCorner.getZ()) + 1;
        ResourceKey<Level> dimension = world.dimension();

        for(int x = minX; x < maxX; x++) {
            for(int y = minY; y < maxY; y++) {
                for(int z = minZ; z < maxZ; z++) {
                    GlobalPos globalPos = GlobalPos.of(dimension, new BlockPos(x, y, z));
                    otherPeoplesLocations.add(globalPos);

                }
            }
        }

        BoxData newBox = new BoxData(minX, minY, minZ, maxX, maxY, maxZ);
        List<BoxData> list = otherPeoplesConnectedBoxes.getOrDefault(playerName, new ArrayList<>());
        list.add(newBox);
        otherPeoplesConnectedBoxes.put(playerName, list);
    }

    // Clear all boxes
    public static void clearBoxes() {
        connectedBoxes.clear();
        locations.clear();
        firstAndSecondLocations.clear();
    }

    public static List<GlobalPos> getLocations() {
        return locations;
    }

    // Main method to render connected boxes
    public static void renderConnectedBoxes(PoseStack poseStack, Vec3 cameraPos) {
        if (connectedBoxes.isEmpty()) return;

        // Find connected groups of boxes
        List<Set<BoxData>> connectedGroups = findConnectedGroups();

        poseStack.pushPose();
        // Adjust for camera position
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        // Render each connected group
        for (Set<BoxData> group : connectedGroups) {
            renderBoxGroup(poseStack, group);
        }

        poseStack.popPose();
    }

    // Find connected groups of boxes
    private static List<Set<BoxData>> findConnectedGroups() {
        List<Set<BoxData>> groups = new ArrayList<>();
        Set<BoxData> unprocessed = new HashSet<>(connectedBoxes);

        while (!unprocessed.isEmpty()) {
            Set<BoxData> currentGroup = new HashSet<>();
            Queue<BoxData> queue = new LinkedList<>();

            BoxData start = unprocessed.iterator().next();
            queue.add(start);
            unprocessed.remove(start);
            currentGroup.add(start);

            while (!queue.isEmpty()) {
                BoxData current = queue.poll();

                Iterator<BoxData> iterator = unprocessed.iterator();
                while (iterator.hasNext()) {
                    BoxData next = iterator.next();
                    if (current.isConnected(next)) {
                        queue.add(next);
                        currentGroup.add(next);
                        iterator.remove();
                    }
                }
            }

            groups.add(currentGroup);
        }

        return groups;
    }

    public static void renderOtherPeoplesConnectedBoxes(PoseStack poseStack, Vec3 cameraPos) {
        if (otherPeoplesConnectedBoxes.isEmpty()) return;
        for (UUID playerName : otherPeoplesConnectedBoxes.keySet()) {
            List<BoxData> boxDataList = otherPeoplesConnectedBoxes.get(playerName);
            if (boxDataList.isEmpty()) continue;
            // Find connected groups of boxes
            List<Set<BoxData>> connectedGroups = findOtherPeoplesConnectedGroups(playerName);

            poseStack.pushPose();
            // Adjust for camera position
            poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            // Render each connected group
            for (Set<BoxData> group : connectedGroups) {
                renderBoxGroup(poseStack, group);
            }

            poseStack.popPose();
        }
    }

    // Find connected groups of boxes
    private static List<Set<BoxData>> findOtherPeoplesConnectedGroups(UUID playerName) {
        List<Set<BoxData>> groups = new ArrayList<>();
        Set<BoxData> unprocessed = new HashSet<>(otherPeoplesConnectedBoxes.get(playerName));

        while (!unprocessed.isEmpty()) {
            Set<BoxData> currentGroup = new HashSet<>();
            Queue<BoxData> queue = new LinkedList<>();

            BoxData start = unprocessed.iterator().next();
            queue.add(start);
            unprocessed.remove(start);
            currentGroup.add(start);

            while (!queue.isEmpty()) {
                BoxData current = queue.poll();

                Iterator<BoxData> iterator = unprocessed.iterator();
                while (iterator.hasNext()) {
                    BoxData next = iterator.next();
                    if (current.isConnected(next)) {
                        queue.add(next);
                        currentGroup.add(next);
                        iterator.remove();
                    }
                }
            }

            groups.add(currentGroup);
        }

        return groups;
    }

    public static void removeOtherPeoplesBoxesByUUID(UUID playerName) {
        otherPeoplesConnectedBoxes.remove(playerName);
    }

    // Render a group of connected boxes
    private static void renderBoxGroup(PoseStack poseStack, Set<BoxData> boxGroup) {
        // Calculate the faces that need to be rendered (exterior only)
        Set<QuadFace> faces = calculateExteriorFaces(boxGroup);

        // Render filled faces and outline
        renderFilledFaces(poseStack, faces);
        renderGroupOutline(poseStack, boxGroup);
    }

    // Class to represent a quad face
    private static class QuadFace {
        public final float minX, minY, minZ;
        public final float maxX, maxY, maxZ;
        public final Direction direction;

        public QuadFace(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, Direction direction) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
            this.direction = direction;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            QuadFace quadFace = (QuadFace) o;
            return Float.compare(quadFace.minX, minX) == 0 &&
                    Float.compare(quadFace.minY, minY) == 0 &&
                    Float.compare(quadFace.minZ, minZ) == 0 &&
                    Float.compare(quadFace.maxX, maxX) == 0 &&
                    Float.compare(quadFace.maxY, maxY) == 0 &&
                    Float.compare(quadFace.maxZ, maxZ) == 0 &&
                    direction == quadFace.direction;
        }

        @Override
        public int hashCode() {
            return Objects.hash(minX, minY, minZ, maxX, maxY, maxZ, direction);
        }
    }

    // Calculate exterior faces for the group
    private static Set<QuadFace> calculateExteriorFaces(Set<BoxData> boxGroup) {
        Set<QuadFace> faces = new HashSet<>();

        // Add all faces from each box
        for (BoxData box : boxGroup) {
            // Generate all 6 faces of the box
            List<QuadFace> boxFaces = new ArrayList<>();
            boxFaces.add(new QuadFace(box.minX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, Direction.DOWN));
            boxFaces.add(new QuadFace(box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, Direction.UP));
            boxFaces.add(new QuadFace(box.minX, box.minY, box.minZ, box.minX, box.maxY, box.maxZ, Direction.WEST));
            boxFaces.add(new QuadFace(box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, Direction.EAST));
            boxFaces.add(new QuadFace(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, Direction.NORTH));
            boxFaces.add(new QuadFace(box.minX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, Direction.SOUTH));

            // Process each face by checking against all other boxes
            for (QuadFace face : boxFaces) {
                List<QuadFace> splitFaces = new ArrayList<>();
                splitFaces.add(face);

                for (BoxData otherBox : boxGroup) {
                    if (otherBox == box) continue; // Skip self

                    List<QuadFace> newSplitFaces = new ArrayList<>();
                    for (QuadFace splitFace : splitFaces) {
                        // Add resulting split faces to the new list
                        newSplitFaces.addAll(splitFace(splitFace, otherBox));
                    }
                    splitFaces = newSplitFaces;
                }

                // Add all remaining split faces to the result
                faces.addAll(splitFaces);
            }
        }

        // Filter out faces that are inside other boxes
        Set<QuadFace> exteriorFaces = new HashSet<>();
        for (QuadFace face : faces) {
            // Check if this face is exterior
            boolean isExterior = true;

            // Get face center
            float centerX = (face.minX + face.maxX) / 2;
            float centerY = (face.minY + face.maxY) / 2;
            float centerZ = (face.minZ + face.maxZ) / 2;

            // Adjust check position based on face direction (move slightly outside the face)
            switch (face.direction) {
                case DOWN:
                    centerY = face.minY - EPSILON;
                    break;
                case UP:
                    centerY = face.maxY + EPSILON;
                    break;
                case WEST:
                    centerX = face.minX - EPSILON;
                    break;
                case EAST:
                    centerX = face.maxX + EPSILON;
                    break;
                case NORTH:
                    centerZ = face.minZ - EPSILON;
                    break;
                case SOUTH:
                    centerZ = face.maxZ + EPSILON;
                    break;
            }

            // If the face center point (offset outward) is inside any box, it's interior
            for (BoxData box : boxGroup) {
                if (centerX >= box.minX && centerX <= box.maxX &&
                        centerY >= box.minY && centerY <= box.maxY &&
                        centerZ >= box.minZ && centerZ <= box.maxZ) {
                    isExterior = false;
                    break;
                }
            }

            if (isExterior) {
                exteriorFaces.add(face);
            }
        }

        return exteriorFaces;
    }

    private static List<QuadFace> splitFace(QuadFace face, BoxData box) {
        List<QuadFace> result = new ArrayList<>();

        // Check if face and box intersect
        if (!doIntersect(face, box)) {
            result.add(face); // No intersection, keep the original face
            return result;
        }

        // Calculate intersection bounds
        float intersectMinX = Math.max(face.minX, box.minX);
        float intersectMaxX = Math.min(face.maxX, box.maxX);
        float intersectMinY = Math.max(face.minY, box.minY);
        float intersectMaxY = Math.min(face.maxY, box.maxY);
        float intersectMinZ = Math.max(face.minZ, box.minZ);
        float intersectMaxZ = Math.min(face.maxZ, box.maxZ);

        // Based on face direction, split accordingly
        switch (face.direction) {
            case UP:
            case DOWN:
                // Split along X and Z
                // Up to 4 rectangles can be created around the intersection

                // Left of intersection (minX to intersectMinX)
                if (face.minX < intersectMinX) {
                    result.add(new QuadFace(
                            face.minX, face.minY, face.minZ,
                            intersectMinX, face.maxY, face.maxZ,
                            face.direction
                    ));
                }

                // Right of intersection (intersectMaxX to maxX)
                if (intersectMaxX < face.maxX) {
                    result.add(new QuadFace(
                            intersectMaxX, face.minY, face.minZ,
                            face.maxX, face.maxY, face.maxZ,
                            face.direction
                    ));
                }

                // Front of intersection (minZ to intersectMinZ)
                if (face.minZ < intersectMinZ) {
                    result.add(new QuadFace(
                            Math.max(face.minX, intersectMinX), face.minY, face.minZ,
                            Math.min(face.maxX, intersectMaxX), face.maxY, intersectMinZ,
                            face.direction
                    ));
                }

                // Back of intersection (intersectMaxZ to maxZ)
                if (intersectMaxZ < face.maxZ) {
                    result.add(new QuadFace(
                            Math.max(face.minX, intersectMinX), face.minY, intersectMaxZ,
                            Math.min(face.maxX, intersectMaxX), face.maxY, face.maxZ,
                            face.direction
                    ));
                }
                break;

            case EAST:
            case WEST:
                // Split along Y and Z

                // Bottom of intersection (minY to intersectMinY)
                if (face.minY < intersectMinY) {
                    result.add(new QuadFace(
                            face.minX, face.minY, face.minZ,
                            face.maxX, intersectMinY, face.maxZ,
                            face.direction
                    ));
                }

                // Top of intersection (intersectMaxY to maxY)
                if (intersectMaxY < face.maxY) {
                    result.add(new QuadFace(
                            face.minX, intersectMaxY, face.minZ,
                            face.maxX, face.maxY, face.maxZ,
                            face.direction
                    ));
                }

                // Front of intersection (minZ to intersectMinZ)
                if (face.minZ < intersectMinZ) {
                    result.add(new QuadFace(
                            face.minX, Math.max(face.minY, intersectMinY), face.minZ,
                            face.maxX, Math.min(face.maxY, intersectMaxY), intersectMinZ,
                            face.direction
                    ));
                }

                // Back of intersection (intersectMaxZ to maxZ)
                if (intersectMaxZ < face.maxZ) {
                    result.add(new QuadFace(
                            face.minX, Math.max(face.minY, intersectMinY), intersectMaxZ,
                            face.maxX, Math.min(face.maxY, intersectMaxY), face.maxZ,
                            face.direction
                    ));
                }
                break;

            case NORTH:
            case SOUTH:
                // Split along X and Y

                // Left of intersection (minX to intersectMinX)
                if (face.minX < intersectMinX) {
                    result.add(new QuadFace(
                            face.minX, face.minY, face.minZ,
                            intersectMinX, face.maxY, face.maxZ,
                            face.direction
                    ));
                }

                // Right of intersection (intersectMaxX to maxX)
                if (intersectMaxX < face.maxX) {
                    result.add(new QuadFace(
                            intersectMaxX, face.minY, face.minZ,
                            face.maxX, face.maxY, face.maxZ,
                            face.direction
                    ));
                }

                // Bottom of intersection (minY to intersectMinY)
                if (face.minY < intersectMinY) {
                    result.add(new QuadFace(
                            Math.max(face.minX, intersectMinX), face.minY, face.minZ,
                            Math.min(face.maxX, intersectMaxX), intersectMinY, face.maxZ,
                            face.direction
                    ));
                }

                // Top of intersection (intersectMaxY to maxY)
                if (intersectMaxY < face.maxY) {
                    result.add(new QuadFace(
                            Math.max(face.minX, intersectMinX), intersectMaxY, face.minZ,
                            Math.min(face.maxX, intersectMaxX), face.maxY, face.maxZ,
                            face.direction
                    ));
                }
                break;
        }

        // If no sub-faces were created, it means the face is entirely covered
        // In this case, we don't add anything to the result

        return result;
    }

    // Check if a face and box intersect
    private static boolean doIntersect(QuadFace face, BoxData box) {
        // Simple AABB intersection check
        return !(face.maxX <= box.minX || face.minX >= box.maxX ||
                face.maxY <= box.minY || face.minY >= box.maxY ||
                face.maxZ <= box.minZ || face.minZ >= box.maxZ);
    }

    public static float[] getColor(int colorMode, float speed) {
        float time = ((System.currentTimeMillis() % 1000L) / 1000.0F) * speed;
        float r = 0, g = 0, b = 0, a = 0.3F;

        switch (colorMode) {
            case 0: // Sinusoidal RGB
                r = (float) Math.sin(time * 2 * Math.PI);
                g = (float) Math.sin((time + 0.333) * 2 * Math.PI);
                b = (float) Math.sin((time + 0.666) * 2 * Math.PI);
                break;
            case 1: // Pulsating Red
                r = (float) Math.abs(Math.sin(time * 2 * Math.PI));
                g = 0;
                b = 0;
                break;
            case 2: // Pulsating Green
                r = 0;
                g = (float) Math.abs(Math.sin(time * 2 * Math.PI));
                b = 0;
                break;
            case 3: // Pulsating Blue
                r = 0;
                g = 0;
                b = (float) Math.abs(Math.sin(time * 2 * Math.PI));
                break;
            case 4: // Gradient RGB
                r = (time % 1.0F);
                g = ((time + 0.333F) % 1.0F);
                b = ((time + 0.666F) % 1.0F);
                break;
            case 5: // Static Yellow
                r = 1.0F;
                g = 1.0F;
                b = 0.0F;
                break;
            case 6: // Static Cyan
                r = 0.0F;
                g = 1.0F;
                b = 1.0F;
                break;
            case 7: // Static Magenta
                r = 1.0F;
                g = 0.0F;
                b = 1.0F;
                break;
            case 8: // Alternating Red/Green
                r = (time % 1.0F) < 0.5F ? 1.0F : 0.0F;
                g = (time % 1.0F) >= 0.5F ? 1.0F : 0.0F;
                b = 0.0F;
                break;
            case 9: // Random Colors
                r = (float) Math.random();
                g = (float) Math.random();
                b = (float) Math.random();
                break;
            default: // Default to white
                r = 1.0F;
                g = 1.0F;
                b = 1.0F;
                break;
        }

        // Clamp values to [0, 1]
        r = Math.max(0, Math.min(1, r));
        g = Math.max(0, Math.min(1, g));
        b = Math.max(0, Math.min(1, b));

        return new float[]{r, g, b, a};
    }

    // Render all filled faces
    private static void renderFilledFaces(PoseStack poseStack, Set<QuadFace> faces) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        // Get light level for the area
        // Using a single light level for all faces for simplicity
        int packedLight = LightTexture.pack(15, 15);

        // Create a translucent filled box
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/block/outline.png");
        RenderType renderType = RenderType.entityTranslucent(texture);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);

        // The color for the filled box (using the same color animation as original)
        float[] color = getColor(4, 0.25F); // Mode 0 with speed 0.25
        float r = color[0];
        float g = color[1];
        float b = color[2];
        float a = color[3];

        Matrix4f matrix = poseStack.last().pose();

        // Draw each face
        for (QuadFace face : faces) {
            drawQuad(vertexConsumer, matrix, face.minX, face.minY, face.minZ,
                    face.maxX, face.maxY, face.maxZ, r, g, b, a, packedLight, 0, 0, 1, 1, face.direction);
        }

        bufferSource.endBatch(renderType);
    }

    // Render the outline for a group of boxes
    private static void renderGroupOutline(PoseStack poseStack, Set<BoxData> boxGroup) {
        RenderSystem.lineWidth(LINE_WIDTH_FULL);
        VertexConsumer builder = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.lines());

        // Extract edges from exterior faces instead of using box edges directly
        Set<QuadFace> exteriorFaces = calculateExteriorFaces(boxGroup);
        Set<Edge> edges = extractEdgesFromFaces(exteriorFaces);

        // Render edges with animation
        float speed = 0.25F;
        float time = ((System.currentTimeMillis() % 1000L) / 1000.0F) * speed;
        float r = (float) Math.sin(time * 2 * Math.PI);
        float g = (float) Math.sin((time + 0.333) * 2 * Math.PI);
        float b = (float) Math.sin((time + 0.666) * 2 * Math.PI);

        Matrix4f matrix = poseStack.last().pose();

        for (Edge edge : edges) {
            //drawLine(builder, matrix, edge.x1, edge.y1, edge.z1, edge.x2, edge.y2, edge.z2, r, g, b, 1.0F);
        }

        // End batch rendering
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch(RenderType.lines());
    }

    // Extract edges from the given faces
    private static Set<Edge> extractEdgesFromFaces(Set<QuadFace> faces) {
        Set<Edge> edges = new HashSet<>();

        for (QuadFace face : faces) {
            switch (face.direction) {
                case UP:
                case DOWN:
                    // Add the 4 horizontal edges of this face
                    edges.add(new Edge(face.minX, face.minY, face.minZ, face.maxX, face.minY, face.minZ));
                    edges.add(new Edge(face.minX, face.minY, face.maxZ, face.maxX, face.minY, face.maxZ));
                    edges.add(new Edge(face.minX, face.minY, face.minZ, face.minX, face.minY, face.maxZ));
                    edges.add(new Edge(face.maxX, face.minY, face.minZ, face.maxX, face.minY, face.maxZ));
                    break;

                case EAST:
                case WEST:
                    // Add the 4 vertical edges of this face
                    edges.add(new Edge(face.minX, face.minY, face.minZ, face.minX, face.maxY, face.minZ));
                    edges.add(new Edge(face.minX, face.minY, face.maxZ, face.minX, face.maxY, face.maxZ));
                    edges.add(new Edge(face.minX, face.minY, face.minZ, face.minX, face.minY, face.maxZ));
                    edges.add(new Edge(face.minX, face.maxY, face.minZ, face.minX, face.maxY, face.maxZ));
                    break;

                case NORTH:
                case SOUTH:
                    // Add the 4 edges of this face
                    edges.add(new Edge(face.minX, face.minY, face.minZ, face.maxX, face.minY, face.minZ));
                    edges.add(new Edge(face.minX, face.maxY, face.minZ, face.maxX, face.maxY, face.minZ));
                    edges.add(new Edge(face.minX, face.minY, face.minZ, face.minX, face.maxY, face.minZ));
                    edges.add(new Edge(face.maxX, face.minY, face.minZ, face.maxX, face.maxY, face.minZ));
                    break;
            }
        }

        return edges;
    }


    // Helper class for edges
    private static class Edge {
        public final float x1, y1, z1, x2, y2, z2;

        public Edge(float x1, float y1, float z1, float x2, float y2, float z2) {
            this.x1 = x1;
            this.y1 = y1;
            this.z1 = z1;
            this.x2 = x2;
            this.y2 = y2;
            this.z2 = z2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Edge edge = (Edge) o;
            return (Float.compare(edge.x1, x1) == 0 &&
                    Float.compare(edge.y1, y1) == 0 &&
                    Float.compare(edge.z1, z1) == 0 &&
                    Float.compare(edge.x2, x2) == 0 &&
                    Float.compare(edge.y2, y2) == 0 &&
                    Float.compare(edge.z2, z2) == 0) ||
                    (Float.compare(edge.x1, x2) == 0 &&
                            Float.compare(edge.y1, y2) == 0 &&
                            Float.compare(edge.z1, z2) == 0 &&
                            Float.compare(edge.x2, x1) == 0 &&
                            Float.compare(edge.y2, y1) == 0 &&
                            Float.compare(edge.z2, z1) == 0);
        }

        @Override
        public int hashCode() {
            // Order-independent hash
            return Objects.hash(
                    Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2),
                    Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2)
            );
        }
    }

    // Check if a point is inside any box in the group
    private static boolean isPointInsideAnyBox(float x, float y, float z, Set<BoxData> boxGroup) {
        for (BoxData box : boxGroup) {
            if (x >= box.minX && x <= box.maxX &&
                    y >= box.minY && y <= box.maxY &&
                    z >= box.minZ && z <= box.maxZ) {
                return true;
            }
        }
        return false;
    }

    // Helper for drawing a line
    private static void drawLine(VertexConsumer builder, Matrix4f matrix, float x1, float y1, float z1,
                                 float x2, float y2, float z2, float r, float g, float b, float a) {
        builder.addVertex(matrix, x1, y1, z1)
                .setColor(r, g, b, a)
                .setNormal(0, 1, 0);
        builder.addVertex(matrix, x2, y2, z2)
                .setColor(r, g, b, a)
                .setNormal(0, 1, 0);
    }



    // Main method to render a box between two corners
    public static void renderBox(PoseStack poseStack, BlockPos firstCorner, BlockPos secondCorner, Vec3 cameraPos) {
        if (firstCorner == null || secondCorner == null) return;

        // Create the bounding box
        int minX = Math.min(firstCorner.getX(), secondCorner.getX());
        int minY = Math.min(firstCorner.getY(), secondCorner.getY());
        int minZ = Math.min(firstCorner.getZ(), secondCorner.getZ());
        int maxX = Math.max(firstCorner.getX(), secondCorner.getX()) + 1;
        int maxY = Math.max(firstCorner.getY(), secondCorner.getY()) + 1;
        int maxZ = Math.max(firstCorner.getZ(), secondCorner.getZ()) + 1;

        // Render box lines and faces
        poseStack.pushPose();
        // Adjust for camera position
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        // Draw filled box (semi-transparent) and outline
        renderFilledBox(poseStack, minX, minY, minZ, maxX, maxY, maxZ);
        renderBoxOutline(poseStack, minX, minY, minZ, maxX, maxY, maxZ);

        poseStack.popPose();
    }

    // Renders a semi-transparent filled box
    private static void renderFilledBox(PoseStack poseStack, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        // Get light level for the area
        BlockPos centerPos = new BlockPos((minX + maxX) / 2, (minY + maxY) / 2, (minZ + maxZ) / 2);
        int blockLight = minecraft.level.getBrightness(LightLayer.BLOCK, centerPos);
        int skyLight = minecraft.level.getBrightness(LightLayer.SKY, centerPos);
        int packedLight = LightTexture.pack(15, 15);

        // Create a translucent filled box
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/block/outline.png");
        //RenderType renderType = RenderType.create("test", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder()
        //        .setShaderState(RenderStateShard.RENDERTYPE_CUTOUT_SHADER)
        //        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
        //        .setLightmapState(RenderStateShard.LIGHTMAP)
        //        .setOverlayState(RenderStateShard.OVERLAY)
        //        .setCullState(RenderStateShard.NO_CULL)
        //        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
        //        .createCompositeState(true));
        RenderType renderType = RenderType.entityTranslucent(texture);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);

        // The color for the filled box (blue with transparency)
        float[] color = getColor(0, 0.25F); // Mode 0 with speed 0.25
        float r = color[0];
        float g = color[1];
        float b = color[2];
        float a = color[3];

        Matrix4f matrix = poseStack.last().pose();

        // Draw all faces
        drawQuad(vertexConsumer, matrix, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, a, packedLight, 0, 0, 1, 1, Direction.NORTH); // Front
        drawQuad(vertexConsumer, matrix, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, a, packedLight, 0, 0, 1, 1, Direction.SOUTH); // Back
        drawQuad(vertexConsumer, matrix, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, a, packedLight, 0, 0, 1, 1, Direction.DOWN);  // Bottom
        drawQuad(vertexConsumer, matrix, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, a, packedLight, 0, 0, 1, 1, Direction.UP);    // Top
        drawQuad(vertexConsumer, matrix, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, a, packedLight, 0, 0, 1, 1, Direction.WEST);  // Left
        drawQuad(vertexConsumer, matrix, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, a, packedLight, 0, 0, 1, 1, Direction.EAST);  // Right
    }

    // Helper method to draw a quad for a face
    public static void drawQuad(VertexConsumer vertexConsumer, Matrix4f matrix, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r, float g, float b, float a, int packedLight, float u0, float v0, float u1, float v1, Direction face) {
        float epsilon = 0.0003f;
        int normalX = 0, normalY = 0, normalZ = 0;
        float faceR = r, faceG = g, faceB = b, faceA = a;

        // Set face-specific colors (can be customized)
        switch (face) {
            case NORTH: // Front face (Z-)
                //faceR = 0f; faceG = 0f; faceB = 1f; // Blue
                normalZ = -1;
                break;
            case SOUTH: // Back face (Z+)
                //faceR = 1f; faceG = 0.5f; faceB = 0f; // Orange
                normalZ = 1;
                break;
            case DOWN: // Bottom face (Y-)
                //faceR = 0f; faceG = 1f; faceB = 0f; // Green
                normalY = -1;
                break;
            case UP: // Top face (Y+)
                //faceR = 1f; faceG = 1f; faceB = 0f; // Yellow
                normalY = 1;
                break;
            case WEST: // Left face (X-)
                //faceR = 1f; faceG = 0f; faceB = 0f; // Red
                normalX = -1;
                break;
            case EAST: // Right face (X+)
                //faceR = 0.8f; faceG = 0f; faceB = 1f; // Purple
                normalX = 1;
                break;
        }

        // Determine vertices based on face
        switch (face) {
            case NORTH: // Front face (Z-)
                // Always offset the Z position outward
                float zFront = minZ - epsilon;
                // Original face (outward)
                vertexConsumer.addVertex(matrix, minX - epsilon, minY - epsilon, zFront).setColor(faceR, faceG, faceB, faceA).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, minX - epsilon, maxY + epsilon, zFront).setColor(faceR, faceG, faceB, faceA).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX + epsilon, maxY + epsilon, zFront).setColor(faceR, faceG, faceB, faceA).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX + epsilon, minY - epsilon, zFront).setColor(faceR, faceG, faceB, faceA).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);

                zFront = minZ + epsilon;
                // Inverted epsilon face (inward)
                vertexConsumer.addVertex(matrix, minX + epsilon, minY + epsilon, zFront).setColor(faceR, faceG, faceB, faceA).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, minX + epsilon, maxY - epsilon, zFront).setColor(faceR, faceG, faceB, faceA).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX - epsilon, maxY - epsilon, zFront).setColor(faceR, faceG, faceB, faceA).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX - epsilon, minY + epsilon, zFront).setColor(faceR, faceG, faceB, faceA).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                break;
            case SOUTH: // Back face (Z+)
                // Always offset the Z position outward
                float zBack = maxZ + epsilon;
                // Original face (outward)
                vertexConsumer.addVertex(matrix, maxX + epsilon, minY - epsilon, zBack).setColor(faceR, faceG, faceB, faceA).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX + epsilon, maxY + epsilon, zBack).setColor(faceR, faceG, faceB, faceA).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, minX - epsilon, maxY + epsilon, zBack).setColor(faceR, faceG, faceB, faceA).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, minX - epsilon, minY - epsilon, zBack).setColor(faceR, faceG, faceB, faceA).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);

                zBack = maxZ - epsilon;
                // Inverted epsilon face (inward)
                vertexConsumer.addVertex(matrix, maxX - epsilon, minY + epsilon, zBack).setColor(faceR, faceG, faceB, faceA).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX - epsilon, maxY - epsilon, zBack).setColor(faceR, faceG, faceB, faceA).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, minX + epsilon, maxY - epsilon, zBack).setColor(faceR, faceG, faceB, faceA).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, minX + epsilon, minY + epsilon, zBack).setColor(faceR, faceG, faceB, faceA).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                break;
            case DOWN: // Bottom face (Y-)
                // Always offset the Y position outward
                float yBottom = minY - epsilon;
                // Original face (outward)
                vertexConsumer.addVertex(matrix, minX - epsilon, yBottom, maxZ + epsilon).setColor(faceR, faceG, faceB, faceA).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX + epsilon, yBottom, maxZ + epsilon).setColor(faceR, faceG, faceB, faceA).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX + epsilon, yBottom, minZ - epsilon).setColor(faceR, faceG, faceB, faceA).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, minX - epsilon, yBottom, minZ - epsilon).setColor(faceR, faceG, faceB, faceA).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);

                yBottom = minY + epsilon;
                // Inverted epsilon face (inward)
                vertexConsumer.addVertex(matrix, minX + epsilon, yBottom, maxZ - epsilon).setColor(faceR, faceG, faceB, faceA).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX - epsilon, yBottom, maxZ - epsilon).setColor(faceR, faceG, faceB, faceA).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX - epsilon, yBottom, minZ + epsilon).setColor(faceR, faceG, faceB, faceA).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, minX + epsilon, yBottom, minZ + epsilon).setColor(faceR, faceG, faceB, faceA).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                break;
            case UP: // Top face (Y+)
                // Always offset the Y position outward
                float yTop = maxY + epsilon;
                // Original face (outward)
                vertexConsumer.addVertex(matrix, minX - epsilon, yTop, minZ - epsilon).setColor(faceR, faceG, faceB, faceA).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX + epsilon, yTop, minZ - epsilon).setColor(faceR, faceG, faceB, faceA).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX + epsilon, yTop, maxZ + epsilon).setColor(faceR, faceG, faceB, faceA).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, minX - epsilon, yTop, maxZ + epsilon).setColor(faceR, faceG, faceB, faceA).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);

                yTop = maxY - epsilon;
                // Inverted epsilon face (inward)
                vertexConsumer.addVertex(matrix, minX + epsilon, yTop, minZ + epsilon).setColor(faceR, faceG, faceB, faceA).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX - epsilon, yTop, minZ + epsilon).setColor(faceR, faceG, faceB, faceA).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX - epsilon, yTop, maxZ - epsilon).setColor(faceR, faceG, faceB, faceA).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, minX + epsilon, yTop, maxZ - epsilon).setColor(faceR, faceG, faceB, faceA).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                break;
            case WEST: // Left face (X-)
                // Always offset the X position outward
                float xLeft = minX - epsilon;
                // Original face (outward)
                vertexConsumer.addVertex(matrix, xLeft, minY - epsilon, maxZ + epsilon).setColor(faceR, faceG, faceB, faceA).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, xLeft, maxY + epsilon, maxZ + epsilon).setColor(faceR, faceG, faceB, faceA).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, xLeft, maxY + epsilon, minZ - epsilon).setColor(faceR, faceG, faceB, faceA).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, xLeft, minY - epsilon, minZ - epsilon).setColor(faceR, faceG, faceB, faceA).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);

                xLeft = minX + epsilon;
                // Inverted epsilon face (inward)
                vertexConsumer.addVertex(matrix, xLeft, minY + epsilon, maxZ - epsilon).setColor(faceR, faceG, faceB, faceA).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, xLeft, maxY - epsilon, maxZ - epsilon).setColor(faceR, faceG, faceB, faceA).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, xLeft, maxY - epsilon, minZ + epsilon).setColor(faceR, faceG, faceB, faceA).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, xLeft, minY + epsilon, minZ + epsilon).setColor(faceR, faceG, faceB, faceA).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                break;
            case EAST: // Right face (X+)
                // Always offset the X position outward
                float xRight = maxX + epsilon;
                // Original face (outward)
                vertexConsumer.addVertex(matrix, xRight, minY - epsilon, minZ - epsilon).setColor(faceR, faceG, faceB, faceA).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, xRight, maxY + epsilon, minZ - epsilon).setColor(faceR, faceG, faceB, faceA).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, xRight, maxY + epsilon, maxZ + epsilon).setColor(faceR, faceG, faceB, faceA).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, xRight, minY - epsilon, maxZ + epsilon).setColor(faceR, faceG, faceB, faceA).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);

                xRight = maxX - epsilon;
                // Inverted epsilon face (inward)
                vertexConsumer.addVertex(matrix, xRight, minY + epsilon, minZ + epsilon).setColor(faceR, faceG, faceB, faceA).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, xRight, maxY - epsilon, minZ + epsilon).setColor(faceR, faceG, faceB, faceA).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, xRight, maxY - epsilon, maxZ - epsilon).setColor(faceR, faceG, faceB, faceA).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, xRight, minY + epsilon, maxZ - epsilon).setColor(faceR, faceG, faceB, faceA).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                break;
        }

    }

    // Renders the outline of the box (edges)
    private static void renderBoxOutline(PoseStack poseStack, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        // Use LevelRenderer's built-in box outline renderer
        Color lineColor = new Color(1.0f, 1.0f, 1.0f, ALPHA);
        RenderSystem.lineWidth(LINE_WIDTH);

        // Draw box outline using the built-in outline renderer
        LevelRenderer.renderLineBox(
                poseStack,
                Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.lines()),
                minX, minY, minZ,
                maxX, maxY, maxZ,
                1.0F, 1.0F, 1.0F, 1.0F
        );

        Minecraft.getInstance().renderBuffers().bufferSource().endBatch(RenderType.lines());
    }

    public static GlobalPos getCenterOfAllBoxes() {
        if (locations.isEmpty()) {
            return null;
        }

        // Initialize sums for each coordinate
        double sumX = 0;
        double sumY = 0;
        double sumZ = 0;
        ResourceKey<Level> dimension = null;

        // Sum up all coordinates
        for (GlobalPos pos : locations) {
            sumX += pos.pos().getX();
            sumY += pos.pos().getY();
            sumZ += pos.pos().getZ();
            dimension = pos.dimension(); // Store dimension from last position
        }

        // Calculate average (center) position
        int centerX = (int) Math.round(sumX / locations.size());
        int centerY = (int) Math.round(sumY / locations.size());
        int centerZ = (int) Math.round(sumZ / locations.size());

        // Return new GlobalPos with the center coordinates
        return GlobalPos.of(dimension, new BlockPos(centerX, centerY, centerZ));
    }

}