//package net.brickcraftdream.rainworldmc_biomes.blocks.libmultipart;
//
//import net.minecraft.core.BlockPos;
//import net.minecraft.world.Containers;
//import net.minecraft.world.InteractionResult;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.level.block.Block;
//import net.minecraft.world.level.block.entity.BlockEntity;
//import net.minecraft.world.level.block.state.BlockBehaviour;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.world.phys.BlockHitResult;
//
//public abstract class BlockBase extends Block {
//
//    public BlockBase(BlockBehaviour.Properties settings) {
//        super(settings);
//    }
//
//    @Override
//        public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
//        super.setPlacedBy(world, pos, state, placer, stack);
//        BlockEntity be = world.getBlockEntity(pos);
//        if (be instanceof TileBase) {
//            ((TileBase) be).onPlacedBy(placer, stack);
//        }
//    }
//
//    @Override
//    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player,
//                                            BlockHitResult hit) {
//        BlockEntity be = world.getBlockEntity(pos);
//        if (be instanceof TileBase) {
//            return ((TileBase) be).onUse(player, hit);
//        }
//        return super.useWithoutItem(state, world, pos, player, hit);
//    }
//
//    @Override
//    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState,
//                                boolean _unknown_boolean) {
//
//        if (this != newState.getBlock()) {
//            BlockEntity be = world.getBlockEntity(pos);
//            if (be instanceof TileBase) {
//                Containers.dropContents(world, pos, ((TileBase) be).removeItemsForDrop());
//            }
//        }
//
//        super.onRemove(state, world, pos, newState, _unknown_boolean);
//    }
//}