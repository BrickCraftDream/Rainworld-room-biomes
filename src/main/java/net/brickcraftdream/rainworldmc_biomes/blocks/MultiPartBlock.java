//package net.brickcraftdream.rainworldmc_biomes.blocks;
//
//
//import com.mojang.serialization.MapCodec;
//import net.minecraft.core.BlockPos;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.level.LevelAccessor;
//import net.minecraft.world.level.block.BaseEntityBlock;
//import net.minecraft.world.level.block.RenderShape;
//import net.minecraft.world.level.block.entity.BellBlockEntity;
//import net.minecraft.world.level.block.entity.BlockEntity;
//import net.minecraft.world.level.block.entity.BlockEntityTicker;
//import net.minecraft.world.level.block.entity.BlockEntityType;
//import net.minecraft.world.level.block.state.BlockState;
//import org.jetbrains.annotations.Nullable;
//
//public class MultiPartBlock extends BaseEntityBlock {
//
//    public MultiPartBlock(Properties properties) {
//        super(properties);
//    }
//
//    @Override
//    protected MapCodec<? extends BaseEntityBlock> codec() {
//        return simpleCodec(MultiPartBlock::new);
//    }
//
//    @Override
//    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
//        super.destroy(level, pos, state);
//    }
//
//    @Override
//    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
//        return new MultiPartBlockEntity(pos, state);
//    }
//
//    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state, MultiPartBlockEntity.XPosition xPosition, MultiPartBlockEntity.YPosition yPosition, MultiPartBlockEntity.ZPosition zPosition) {
//        return new MultiPartBlockEntity(pos, state, xPosition, yPosition, zPosition);
//    }
//
//    @Override
//    protected RenderShape getRenderShape(BlockState state) {
//        return RenderShape.MODEL;
//    }
//
//    public <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelpers(
//            BlockEntityType<A> serverType, BlockEntityType<E> clientType, BlockEntityTicker<? super E> ticker
//    ) {
//        return clientType == serverType ? (BlockEntityTicker<A>) ticker : null;
//    }
//
//    @Override
//    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
//        return createTickerHelper(
//                blockEntityType,
//                ModBlockEntityTypes.MULTI_PART_BLOCK,
//                (level1, pos, state1, blockEntity) -> {
//                    blockEntity.tick(level1, pos, state1);
//                }
//        );
//    }
//}
