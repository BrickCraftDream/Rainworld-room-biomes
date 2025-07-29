package net.brickcraftdream.rainworldmc_biomes.blocks;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

import static net.brickcraftdream.rainworldmc_biomes.Rainworld_MC_Biomes.MOD_ID;

public class ModBlocks {

    public static final Block DEBUG_BLOCK = register(
            new Block(BlockBehaviour.Properties.of().sound(SoundType.AMETHYST)),
            "debug_block",
            true
    );

    //public static final MultiPartBlock MULTI_PART_BLOCK = register(
    //        new MultiPartBlock(BlockBehaviour.Properties.of().sound(SoundType.AMETHYST)),
    //        "multi_part_block",
    //        true
    //);
    //public static final MultiPartBlockCollisionBlock MULTI_PART_BLOCK_COLLISION = register(
    //        new MultiPartBlockCollisionBlock(BlockBehaviour.Properties.of().sound(SoundType.AMETHYST)),
    //        "multi_part_block_collision",
    //        true
    //);

    public static void initialize() {

    }

    private static <T extends Block> T register(T block, String name, boolean shouldRegisterItem) {
        // Register the block and its item.
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MOD_ID, name);

        // Register the item if needed
        if (shouldRegisterItem) {
            BlockItem blockItem = new BlockItem(block, new Item.Properties());
            Registry.register(BuiltInRegistries.ITEM, id, blockItem);
        }

        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    private static Block registerBlockAsMultiBlock(String name, Block block) {
        registerBlockItem(name, block);
        return (Block)Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath("rw_block_mod", name), block);
    }

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return (Block)Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath("rw_block_mod", name), block);
    }

    private static Item registerBlockItem(String name, Block block) {
        return (Item)Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("rw_block_mod", name), new BlockItem(block, new Item.Properties()));
    }

    /*
    public static Block register(Block block, String name, boolean shouldRegisterItem) {
        // Register the block and its item.
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MOD_ID, name);

        // Sometimes, you may not want to register an item for the block.
        // Eg: if it's a technical block like `minecraft:air` or `minecraft:end_gateway`
        if (shouldRegisterItem) {
            BlockItem blockItem = new BlockItem(block, new Item.Properties());
            Registry.register(BuiltInRegistries.ITEM, id, blockItem);
        }

        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }
     */
}
