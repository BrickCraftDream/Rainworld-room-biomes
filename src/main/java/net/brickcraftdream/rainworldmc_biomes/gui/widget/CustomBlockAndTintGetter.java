package net.brickcraftdream.rainworldmc_biomes.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

import static net.brickcraftdream.rainworldmc_biomes.gui.widget.BlockViewWidget.*;

public class CustomBlockAndTintGetter implements BlockAndTintGetter {
    private final Minecraft minecraft;
    private BlockPos currentPos = BlockPos.ZERO;
    private TextureAtlasSprite currentSprite = null;

    public CustomBlockAndTintGetter(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void setCurrentPos(BlockPos pos) {
        this.currentPos = pos;
    }

    public void setCurrentSprite(TextureAtlasSprite sprite) {
        this.currentSprite = sprite;
    }

    @Override
    public float getShade(Direction direction, boolean shade) {
        // Adjust shading for each face to get a nice 3D effect
        if (!shade) return 1.0F;

        return switch (direction) {
            case UP -> 1.0F;
            case DOWN -> 0.5F;
            case NORTH, SOUTH -> 0.8F;
            case WEST, EAST -> 0.6F;
        };
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return minecraft.level.getLightEngine();
    }

    @Override
    public int getBlockTint(BlockPos blockPos, ColorResolver colorResolver) {
        return biome != null ? colorResolver.getColor(biome, blockPos.getX(), blockPos.getZ()) :
                minecraft.level.getBlockTint(blockPos, colorResolver);
    }

    @Override
    public int getBrightness(LightLayer lightLayer, BlockPos blockPos) {
        return 15; // Full brightness
    }

    @Override
    public FluidState getFluidState(BlockPos blockPos) {
        BlockState state = getBlockState(blockPos);
        return state.getFluidState();
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        return blocks.getOrDefault(blockPos, minecraft.level.getBlockState(BlockPos.ZERO));
    }

    @Override
    public int getHeight() {
        return viewSize;
    }

    @Override
    public int getMinBuildHeight() {
        return 0;
    }
}
