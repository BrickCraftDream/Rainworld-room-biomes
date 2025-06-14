package net.brickcraftdream.rainworldmc_biomes;

import com.google.common.hash.Hashing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.util.LinearCongruentialGenerator;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;

public class BiomeManager {
    public static final int CHUNK_CENTER_QUART = QuartPos.fromBlock(8);
    private static final int ZOOM_BITS = 2;
    private static final int ZOOM = 4;
    private static final int ZOOM_MASK = 3;
    private final BiomeManager.NoiseBiomeSource noiseBiomeSource;
    private final long biomeZoomSeed;

    public BiomeManager(BiomeManager.NoiseBiomeSource noiseBiomeSource, long l) {
        this.noiseBiomeSource = noiseBiomeSource;
        this.biomeZoomSeed = l;
    }

    public static long obfuscateSeed(long l) {
        return Hashing.sha256().hashLong(l).asLong();
    }

    public BiomeManager withDifferentSource(NoiseBiomeSource noiseBiomeSource) {
        return new BiomeManager(noiseBiomeSource, this.biomeZoomSeed);
    }

    public Holder<Biome> getBiome(BlockPos blockPos) {
        int closestPointIndex;
        int offsetX = blockPos.getX() - 2;
        int offsetY = blockPos.getY() - 2;
        int offsetZ = blockPos.getZ() - 2;
        int baseX = offsetX >> 2;
        int baseY = offsetY >> 2;
        int baseZ = offsetZ >> 2;
        double fractionalX = (double) (offsetX & 3) / 4.0;
        double fractionalY = (double) (offsetY & 3) / 4.0;
        double fractionalZ = (double) (offsetZ & 3) / 4.0;
        int bestMatch = 0;
        double smallestDistance = Double.POSITIVE_INFINITY;
        for (closestPointIndex = 0; closestPointIndex < 8; ++closestPointIndex) {
            boolean isXBase = (closestPointIndex & 4) == 0;
            int adjustedX = isXBase ? baseX : baseX + 1;
            boolean isYBase = (closestPointIndex & 2) == 0;
            int adjustedY = isYBase ? baseY : baseY + 1;
            boolean isZBase = (closestPointIndex & 1) == 0;
            int adjustedZ = isZBase ? baseZ : baseZ + 1;
            double xDist = isXBase ? fractionalX : fractionalX - 1.0;
            double yDist = isYBase ? fractionalY : fractionalY - 1.0;
            double zDist = isZBase ? fractionalZ : fractionalZ - 1.0;
            double distance = getFiddledDistance(this.biomeZoomSeed, adjustedX, adjustedY, adjustedZ, xDist, yDist, zDist);
            if (smallestDistance > distance) {
                bestMatch = closestPointIndex;
                smallestDistance = distance;
            }
        }
        int finalX = (bestMatch & 4) == 0 ? baseX : baseX + 1;
        int finalY = (bestMatch & 2) == 0 ? baseY : baseY + 1;
        int finalZ = (bestMatch & 1) == 0 ? baseZ : baseZ + 1;
        return this.noiseBiomeSource.getNoiseBiome(finalX, finalY, finalZ);
    }

    public Holder<Biome> getNoiseBiomeAtPosition(double x, double y, double z) {
        int quartX = QuartPos.fromBlock(Mth.floor(x));
        int quartY = QuartPos.fromBlock(Mth.floor(y));
        int quartZ = QuartPos.fromBlock(Mth.floor(z));
        return this.getNoiseBiomeAtQuart(quartX, quartY, quartZ);
    }

    public Holder<Biome> getNoiseBiomeAtPosition(BlockPos position) {
        int quartX = QuartPos.fromBlock(position.getX());
        int quartY = QuartPos.fromBlock(position.getY());
        int quartZ = QuartPos.fromBlock(position.getZ());
        return this.getNoiseBiomeAtQuart(quartX, quartY, quartZ);
    }

    public Holder<Biome> getNoiseBiomeAtQuart(int quartX, int quartY, int quartZ) {
        return this.noiseBiomeSource.getNoiseBiome(quartX, quartY, quartZ);
    }

    private static double getFiddledDistance(long seed, int x, int y, int z, double xOffset, double yOffset, double zOffset) {
        long currentSeed = seed;
        currentSeed = LinearCongruentialGenerator.next(currentSeed, x);
        currentSeed = LinearCongruentialGenerator.next(currentSeed, y);
        currentSeed = LinearCongruentialGenerator.next(currentSeed, z);
        currentSeed = LinearCongruentialGenerator.next(currentSeed, x);
        currentSeed = LinearCongruentialGenerator.next(currentSeed, y);
        currentSeed = LinearCongruentialGenerator.next(currentSeed, z);
        double xFiddle = getFiddle(currentSeed);
        currentSeed = LinearCongruentialGenerator.next(currentSeed, seed);
        double yFiddle = getFiddle(currentSeed);
        currentSeed = LinearCongruentialGenerator.next(currentSeed, seed);
        double zFiddle = getFiddle(currentSeed);
        return Mth.square(zOffset + zFiddle) + Mth.square(yOffset + yFiddle) + Mth.square(xOffset + xFiddle);
    }

    private static double getFiddle(long seed) {
        //double normalizedValue = (double) Math.floorMod(seed >> 24, 1024) / 1024.0;
        //return (normalizedValue - 0.5) * 0.9;
        return 0.25;
    }

    public static interface NoiseBiomeSource {
        public Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ);
    }
}