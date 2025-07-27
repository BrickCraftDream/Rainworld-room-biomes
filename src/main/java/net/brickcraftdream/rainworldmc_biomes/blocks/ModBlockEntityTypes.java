package net.brickcraftdream.rainworldmc_biomes.blocks;

import net.brickcraftdream.rainworldmc_biomes.Rainworld_MC_Biomes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntityTypes {

    //public static final BlockEntityType<MultiPartBlockEntity> MULTI_PART_BLOCK = register(
    //        "multi_part_block",
    //        MultiPartBlockEntity::new,
    //        ModBlocks.MULTI_PART_BLOCK
    //);

    //public static final BlockEntityType<MultiPartBlockCollisionBlockEntity> MULTI_PART_BLOCK_COLLISION = register(
    //        "multi_part_block_collision",
    //        MultiPartBlockCollisionBlockEntity::new,
    //        ModBlocks.MULTI_PART_BLOCK_COLLISION
    //);

    public static void initialize() {
    }

    public static <T extends BlockEntityType<?>> T register(String path, T blockEntityType) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("tutorial", path), blockEntityType);
    }

    private static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType.BlockEntitySupplier<? extends T> entityFactory, Block... blocks) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Rainworld_MC_Biomes.MOD_ID, name);

        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, BlockEntityType.Builder.<T>of(entityFactory, blocks).build());
    }
}
