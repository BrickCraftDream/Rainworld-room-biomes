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
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Pose;
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
    private static final float EPSILON = 0.0003F;
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

        for (BoxData box : boxGroup) {
            // Generate all 6 faces of the box
            List<QuadFace> boxFaces = new ArrayList<>();
            boxFaces.add(new QuadFace(box.minX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, Direction.DOWN));
            boxFaces.add(new QuadFace(box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, Direction.UP));
            boxFaces.add(new QuadFace(box.minX, box.minY, box.minZ, box.minX, box.maxY, box.maxZ, Direction.WEST));
            boxFaces.add(new QuadFace(box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, Direction.EAST));
            boxFaces.add(new QuadFace(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, Direction.NORTH));
            boxFaces.add(new QuadFace(box.minX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, Direction.SOUTH));

            // Process each face by splitting against all other boxes
            for (QuadFace face : boxFaces) {
                List<QuadFace> splitFaces = new ArrayList<>();
                splitFaces.add(face);

                for (BoxData otherBox : boxGroup) {
                    if (otherBox == box) continue; // Skip self
                    List<QuadFace> newSplitFaces = new ArrayList<>();
                    for (QuadFace splitFace : splitFaces) {
                        newSplitFaces.addAll(splitFace(splitFace, otherBox));
                    }
                    splitFaces = newSplitFaces;
                }

                faces.addAll(splitFaces);
            }
        }

        // Filter out interior faces
        Set<QuadFace> exteriorFaces = new HashSet<>();
        for (QuadFace face : faces) {
            // Face center
            double centerX = (face.minX + face.maxX) / 2.0;
            double centerY = (face.minY + face.maxY) / 2.0;
            double centerZ = (face.minZ + face.maxZ) / 2.0;

            // Move slightly outward along the face normal
            Vec3i normal = face.direction.getNormal(); // works in both Mojang & Yarn
            double checkX = centerX + normal.getX() * EPSILON;
            double checkY = centerY + normal.getY() * EPSILON;
            double checkZ = centerZ + normal.getZ() * EPSILON;

            boolean isExterior = true;
            for (BoxData box : boxGroup) {
                if (checkX >= box.minX && checkX <= box.maxX &&
                        checkY >= box.minY && checkY <= box.maxY &&
                        checkZ >= box.minZ && checkZ <= box.maxZ) {
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
        // Clamp speed to [0.01, 1] to avoid division by zero or no animation
        speed = Math.max(0.01f, Math.min(1f, speed));

        // Faster when speed is higher, slower when speed is lower
        float cycleLength = 1000L / speed;
        float time = (System.currentTimeMillis() % (long) cycleLength) / cycleLength;

        float r = 0, g = 0, b = 0, a = 1F;

        switch (colorMode) {
            case 0: // Smooth Red ↔ Green, 3/4 red, 1/4 green
                float t; // time for the actual blend
                if (time < 0.75f) {
                    // Red-dominant segment: blend from red → green
                    t = time / 0.75f;        // scale [0,0.75] → [0,1]
                    r = 1f - 0.5f * (float)(1 - Math.cos(t * Math.PI)); // 1 → 0.5
                    g = 0.5f * (float)(1 - Math.cos(t * Math.PI));      // 0 → 0.5
                } else {
                    // Green-dominant segment: blend from green → red
                    t = (time - 0.75f) / 0.25f; // scale [0.75,1] → [0,1]
                    g = 1f - 0.5f * (float)(1 - Math.cos(t * Math.PI)); // 1 → 0.5
                    r = 0.5f * (float)(1 - Math.cos(t * Math.PI));      // 0 → 0.5
                }
                b = 0;
                break;

            case 1: // Pulsating Red
                r = 1f - (float)Math.abs(Math.sin(time * 2 * Math.PI));
                g = 0;
                b = 0;
                break;
            case 2: // Pulsating Green
                r = 0;
                g = 1f - (float)Math.abs(Math.sin(time * 2 * Math.PI));
                b = 0;
                break;
            case 3: // Pulsating Blue
                r = 0;
                g = 0;
                b = 1f - (float)Math.abs(Math.sin(time * 2 * Math.PI));
                break;

            case 4: // Smooth RGB rainbow (fixed)
                r = 0.5f + 0.5f * (float)Math.sin(time * 2 * Math.PI);
                g = 0.5f + 0.5f * (float)Math.sin(time * 2 * Math.PI + 2 * Math.PI / 3);
                b = 0.5f + 0.5f * (float)Math.sin(time * 2 * Math.PI + 4 * Math.PI / 3);
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
                r = (float)Math.random();
                g = (float)Math.random();
                b = (float)Math.random();
                break;
            case 10: // Sinusoidal RGB (phase-shifted)
                r = 0.5f + 0.5f * (float)Math.sin(time * 2 * Math.PI);
                g = 0.5f + 0.5f * (float)Math.sin(time * 2 * Math.PI + 2 * Math.PI / 3);
                b = 0.5f + 0.5f * (float)Math.sin(time * 2 * Math.PI + 4 * Math.PI / 3);
                break;
            case 11: // Sinusoidal Blue ↔ Cyan
                // Blue = (0,0,1), Cyan = (0,1,1)
                float mix = 0.5f + 0.5f * (float)Math.sin(time * 2 * Math.PI); // oscillates 0→1
                r = 0;
                g = mix; // smoothly goes 0 → 1 (Blue → Cyan)
                b = 1;
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
        RenderType renderType = CustomRenderTypes.translucent(texture);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
        RenderSystem.disableDepthTest();


        // The color for the filled box (using the same color animation as original)
        float[] color = getColor(11, 0.25F); // Mode 0 with speed 0.25
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
        RenderSystem.enableDepthTest();
    }

    // Render the outline for a group of boxes
    private static void renderGroupOutline(PoseStack poseStack, Set<BoxData> boxGroup) {
        RenderSystem.lineWidth(LINE_WIDTH_FULL);
        VertexConsumer builder = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.lines());

        // Extract edges from exterior faces instead of using box edges directly
        Set<QuadFace> exteriorFaces = calculateExteriorFaces(boxGroup);
        Set<Edge> edges = extractEdgesFromFaces(exteriorFaces);

        // Render edges with animation
        float[] color = getColor(11, 0.25F);
        float r = color[0];
        float g = color[1];
        float b = color[2];

        Matrix4f matrix = poseStack.last().pose();

        //for (Edge edge : edges) {
        //    drawLine(builder, matrix, edge.x1, edge.y1, edge.z1, edge.x2, edge.y2, edge.z2, r, g, b, 1.0F);
        //}

        for(QuadFace face : exteriorFaces) {
            renderBoxOutline(poseStack, (int) face.minX, (int) face.minY, (int) face.minZ, (int) face.maxX, (int) face.maxY, (int) face.maxZ, false, 11);
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
        renderBoxOutline(poseStack, minX, minY, minZ, maxX, maxY, maxZ, true, 0);

        poseStack.popPose();
    }

    // Renders a semi-transparent filled box
    private static void renderFilledBox(PoseStack poseStack, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

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
        RenderType renderType = CustomRenderTypes.translucent(texture);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);

        // The color for the filled box (blue with transparency)
        float[] color = getColor(0, 0.25F); // Mode 0 with speed 0.25
        float r = color[0];
        float g = color[1];
        float b = color[2];
        float a = color[3];

        Matrix4f matrix = poseStack.last().pose();

        // Draw all faces
        drawQuad(vertexConsumer, matrix, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, a, packedLight, 1, 0, 1, 1, Direction.NORTH); // Front
        drawQuad(vertexConsumer, matrix, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, a, packedLight, 0, 0, 1, 1, Direction.SOUTH); // Back
        drawQuad(vertexConsumer, matrix, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, a, packedLight, 0, 0, 1, 1, Direction.DOWN);  // Bottom
        drawQuad(vertexConsumer, matrix, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, a, packedLight, 0, 0, 1, 1, Direction.UP);    // Top
        drawQuad(vertexConsumer, matrix, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, a, packedLight, 0, 0, 1, 1, Direction.WEST);  // Left
        drawQuad(vertexConsumer, matrix, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, a, packedLight, 0, 0, 1, 1, Direction.EAST);  // Right
    }

    // Helper method to draw a quad for a face
    public static void drawQuad(VertexConsumer vertexConsumer, Matrix4f matrix, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r, float g, float b, float a, int packedLight, float u0, float v0, float u1, float v1, Direction face) {
        int normalX = 0, normalY = 0, normalZ = 0;
        float faceR = r, faceG = g, faceB = b, faceA = a;

        boolean useDebugColors = false;
        if(useDebugColors) {
            switch (face) {
                case NORTH: // Front face (Z-)
                    faceR = 0f;
                    faceG = 0f;
                    faceB = 1f; // Blue
                    break;
                case SOUTH: // Back face (Z+)
                    faceR = 1f;
                    faceG = 0.5f;
                    faceB = 0f; // Orange
                    break;
                case DOWN: // Bottom face (Y-)
                    faceR = 0f;
                    faceG = 1f;
                    faceB = 0f; // Green
                    break;
                case UP: // Top face (Y+)
                    faceR = 1f;
                    faceG = 1f;
                    faceB = 0f; // Yellow
                    break;
                case WEST: // Left face (X-)
                    faceR = 1f;
                    faceG = 0f;
                    faceB = 0f; // Red
                    break;
                case EAST: // Right face (X+)
                    faceR = 0.8f;
                    faceG = 0f;
                    faceB = 1f; // Purple
                    break;
            }
        }

        switch (face) {
            case NORTH: // Front face (Z-)
                vertexConsumer.addVertex(matrix, minX, minY, minZ).setColor(faceR, faceG, faceB, faceA).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, minX, maxY, minZ).setColor(faceR, faceG, faceB, faceA).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX, maxY, minZ).setColor(faceR, faceG, faceB, faceA).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX, minY, minZ).setColor(faceR, faceG, faceB, faceA).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                break;
            case SOUTH: // Back face (Z+)
                vertexConsumer.addVertex(matrix, maxX, minY, maxZ).setColor(faceR, faceG, faceB, faceA).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX, maxY, maxZ).setColor(faceR, faceG, faceB, faceA).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, minX, maxY, maxZ).setColor(faceR, faceG, faceB, faceA).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, minX, minY, maxZ).setColor(faceR, faceG, faceB, faceA).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                break;
            case DOWN: // Bottom face (Y-)
                vertexConsumer.addVertex(matrix, minX, minY, maxZ).setColor(faceR, faceG, faceB, faceA).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX, minY, maxZ).setColor(faceR, faceG, faceB, faceA).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX, minY, minZ).setColor(faceR, faceG, faceB, faceA).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, minX, minY, minZ).setColor(faceR, faceG, faceB, faceA).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                break;
            case UP: // Top face (Y+)
                vertexConsumer.addVertex(matrix, minX, maxY, minZ).setColor(faceR, faceG, faceB, faceA).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX, maxY, minZ).setColor(faceR, faceG, faceB, faceA).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX, maxY, maxZ).setColor(faceR, faceG, faceB, faceA).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, minX, maxY, maxZ).setColor(faceR, faceG, faceB, faceA).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                break;
            case WEST: // Left face (X-)
                vertexConsumer.addVertex(matrix, minX, minY, maxZ).setColor(faceR, faceG, faceB, faceA).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, minX, maxY, maxZ).setColor(faceR, faceG, faceB, faceA).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, minX, maxY, minZ).setColor(faceR, faceG, faceB, faceA).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, minX, minY, minZ).setColor(faceR, faceG, faceB, faceA).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                break;
            case EAST: // Right face (X+)
                vertexConsumer.addVertex(matrix, maxX, minY, minZ).setColor(faceR, faceG, faceB, faceA).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX, maxY, minZ).setColor(faceR, faceG, faceB, faceA).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX, maxY, maxZ).setColor(faceR, faceG, faceB, faceA).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                vertexConsumer.addVertex(matrix, maxX, minY, maxZ).setColor(faceR, faceG, faceB, faceA).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalX, normalY, normalZ);
                break;
        }
    }

    // Renders the outline of the box (edges)
    private static void renderBoxOutline(PoseStack poseStack, int minX, int minY, int minZ,
                                         int maxX, int maxY, int maxZ,
                                         boolean renderWhiteOutline, int colorMode) {
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        var buffer = bufferSource.getBuffer(RenderType.lines());

        RenderSystem.lineWidth(LINE_WIDTH);

        if (renderWhiteOutline) {
            // Draw the main box outline (fully opaque)
            LevelRenderer.renderLineBox(
                    poseStack,
                    buffer,
                    minX, minY, minZ,
                    maxX, maxY, maxZ,
                    1.0F, 1.0F, 1.0F, 1.0F
            );
        }

        // Color for interior lines
        float[] color = getColor(colorMode, 0.25F);
        float r = color[0], g = color[1], b = color[2], a = 0.75F;

        int longLines = 10;  // spacing for long direction
        int shortLines = 1;  // spacing for short direction

        // --- NORTH face (Z = minZ) ---
        for (int x = roundToStep(minX, longLines); x <= maxX; x += longLines) {
            renderLine(poseStack, buffer, x, minY, minZ, x, maxY, minZ, r, g, b, a);
        }
        for (int y = roundToStep(minY, shortLines); y <= maxY; y += shortLines) {
            renderLine(poseStack, buffer, minX, y, minZ, maxX, y, minZ, r, g, b, a);
        }

        // --- SOUTH face (Z = maxZ) ---
        for (int x = roundToStep(minX, longLines); x <= maxX; x += longLines) {
            renderLine(poseStack, buffer, x, minY, maxZ, x, maxY, maxZ, r, g, b, a);
        }
        for (int y = roundToStep(minY, shortLines); y <= maxY; y += shortLines) {
            renderLine(poseStack, buffer, minX, y, maxZ, maxX, y, maxZ, r, g, b, a);
        }

        // --- EAST face (X = maxX) ---
        for (int y = roundToStep(minY, shortLines); y <= maxY; y += shortLines) {
            renderLine(poseStack, buffer, maxX, y, minZ, maxX, y, maxZ, r, g, b, a);
        }
        for (int z = roundToStep(minZ, longLines); z <= maxZ; z += longLines) {
            renderLine(poseStack, buffer, maxX, minY, z, maxX, maxY, z, r, g, b, a);
        }

        // --- WEST face (X = minX) ---
        for (int y = roundToStep(minY, shortLines); y <= maxY; y += shortLines) {
            renderLine(poseStack, buffer, minX, y, minZ, minX, y, maxZ, r, g, b, a);
        }
        for (int z = roundToStep(minZ, longLines); z <= maxZ; z += longLines) {
            renderLine(poseStack, buffer, minX, minY, z, minX, maxY, z, r, g, b, a);
        }

        // --- UP face (Y = maxY) ---
        for (int x = roundToStep(minX, longLines); x <= maxX; x += longLines) {
            renderLine(poseStack, buffer, x, maxY, minZ, x, maxY, maxZ, r, g, b, a);
        }
        for (int z = roundToStep(minZ, shortLines); z <= maxZ; z += shortLines) {
            renderLine(poseStack, buffer, minX, maxY, z, maxX, maxY, z, r, g, b, a);
        }

        // --- DOWN face (Y = minY) ---
        for (int x = roundToStep(minX, longLines); x <= maxX; x += longLines) {
            renderLine(poseStack, buffer, x, minY, minZ, x, minY, maxZ, r, g, b, a);
        }
        for (int z = roundToStep(minZ, shortLines); z <= maxZ; z += shortLines) {
            renderLine(poseStack, buffer, minX, minY, z, maxX, minY, z, r, g, b, a);
        }

        bufferSource.endBatch(RenderType.lines());
    }

    /**
     * Aligns the starting coordinate to the nearest grid step
     * relative to world origin (0,0,0).
     */
    private static int roundToStep(int coord, int step) {
        int aligned = (int)Math.ceil((double)coord / step) * step;
        return aligned;
    }


    public static void renderLine(PoseStack poseStack, VertexConsumer consumer, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float red, float green, float blue, float alpha) {
        PoseStack.Pose pose = poseStack.last();

        float x1 = (float) minX;
        float y1 = (float) minY;
        float z1 = (float) minZ;
        float x2 = (float) maxX;
        float y2 = (float) maxY;
        float z2 = (float) maxZ;

        // First point of the line
        consumer.addVertex(pose, x1, y1, z1).setColor(red, green, blue, alpha).setNormal(pose, 0.0F, 1.0F, 0.0F);

        // Second point of the line
        consumer.addVertex(pose, x2, y2, z2).setColor(red, green, blue, alpha).setNormal(pose, 0.0F, 1.0F, 0.0F);
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