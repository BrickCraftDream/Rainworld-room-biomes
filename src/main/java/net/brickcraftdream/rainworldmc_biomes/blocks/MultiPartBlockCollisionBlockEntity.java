/*
package net.brickcraftdream.rainworldmc_biomes.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiPartBlockCollisionBlockEntity extends BlockEntity {
    private static final Logger log = LoggerFactory.getLogger(MultiPartBlockCollisionBlockEntity.class);
    private static int linkedX;
    private static int linkedY;
    private static int linkedZ;

    private static BlockPos originalPos;

    public MultiPartBlockCollisionBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.MULTI_PART_BLOCK_COLLISION, pos, blockState);
        originalPos = pos;
        linkedX = 0;
        linkedY = 0;
        linkedZ = 0;
    }

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, MultiPartBlockEntity blockEntity) {
        //if(level.isEmptyBlock(BlockPos.of(BlockPos.asLong(linkedX, linkedY, linkedZ)))) {
        //    level.removeBlockEntity(originalPos);
        //    level.removeBlock(originalPos, false);
        //}
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        // Save the current value of the number to the nbt
        super.saveAdditional(nbt, registries);
        nbt.putInt("linkedX", linkedX);
        nbt.putInt("linkedY", linkedY);
        nbt.putInt("linkedZ", linkedZ);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);

        linkedX = nbt.getInt("linkedX");
        linkedY = nbt.getInt("linkedY");
        linkedZ = nbt.getInt("linkedZ");
    }
}

 */
