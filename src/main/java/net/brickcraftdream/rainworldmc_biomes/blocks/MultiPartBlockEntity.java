//package net.brickcraftdream.rainworldmc_biomes.blocks;
//
//import net.brickcraftdream.rainworldmc_biomes.Rainworld_MC_Biomes;
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.HolderLookup;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.level.block.Blocks;
//import net.minecraft.world.level.block.entity.BlockEntity;
//import net.minecraft.world.level.block.entity.BlockEntityTicker;
//import net.minecraft.world.level.block.entity.BlockEntityType;
//import net.minecraft.world.level.block.state.BlockState;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class MultiPartBlockEntity extends BlockEntity{
//    //private static final Logger log = LoggerFactory.getLogger(MultiPartBlockCollisionBlockEntity.class);
//
//
//    public enum XPosition {
//        LEFT(1),
//        MIDDLE(0),
//        RIGHT(-1);
//
//        private final int offsetX;
//
//        XPosition(int offsetX) {
//            this.offsetX = offsetX;
//        }
//
//        public int getOffsetX() {
//            return offsetX;
//        }
//    }
//
//    public enum YPosition {
//        TOP(1),
//        MIDDLE(0),
//        BOTTOM(-1);
//
//        private final int offsetY;
//
//        YPosition(int offsetY) {
//            this.offsetY = offsetY;
//        }
//
//        public int getOffsetY() {
//            return offsetY;
//        }
//    }
//
//    public enum ZPosition {
//        FRONT(1),
//        MIDDLE(0),
//        BACK(-1);
//
//        private final int offsetZ;
//
//        ZPosition(int offsetZ) {
//            this.offsetZ = offsetZ;
//        }
//
//        public int getOffsetZ() {
//            return offsetZ;
//        }
//    }
//
//    private static int linkedX = 69420;
//    private static int linkedY = 69420;
//    private static int linkedZ = 69420;
//
//    public static XPosition xPosition;
//    public static YPosition yPosition;
//    public static ZPosition zPosition;
//
//    private static String test = "test";
//
//    private static BlockPos originalPos;
//
//    public MultiPartBlockEntity(BlockPos pos, BlockState blockState) {
//        super(ModBlockEntityTypes.MULTI_PART_BLOCK, pos, blockState);
//        this.originalPos = pos;
//    }
//
//    public MultiPartBlockEntity(BlockPos pos, BlockState blockState, XPosition xPosition, YPosition yPosition, ZPosition zPosition) {
//        super(ModBlockEntityTypes.MULTI_PART_BLOCK, pos, blockState);
//        this.originalPos = pos;
//        this.xPosition = xPosition;
//        this.yPosition = yPosition;
//        this.zPosition = zPosition;
//    }
//
//    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
//        //if(level.isEmptyBlock(BlockPos.of(BlockPos.asLong(linkedX, linkedY, linkedZ)))) {
//        //    System.out.println("Removing block at originalPos: " + originalPos + " linkedPosX: " + linkedX + " linkedPosY: " + linkedY + " linkedPosZ: " + linkedZ);
//        //    System.out.println(test);
//        //    if(linkedX != 69420 || linkedY != 69420 || linkedZ != 69420) {
//        //        //level.setBlockAndUpdate(BlockPos.of(BlockPos.asLong(linkedX, linkedY, linkedZ)), Blocks.RED_CONCRETE.defaultBlockState());
//        //        Rainworld_MC_Biomes.addToBeRemovedBlock(originalPos);
//        //    }
//        //}
//        BlockPos pos = BlockPos.of(BlockPos.asLong(this.worldPosition.getX() + xPosition.offsetX, this.worldPosition.getY() + yPosition.offsetY, this.worldPosition.getZ() + zPosition.offsetZ));
//        if(pos != originalPos && level.getBlockState(pos).is(Blocks.AIR)) {
//            Rainworld_MC_Biomes.addToBeRemovedBlock(originalPos);
//        }
//    }
//
//    @Override
//    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
//        // Save the current value of the number to the nbt
//        super.saveAdditional(nbt, registries);
//        nbt.putInt("linkedX", linkedX);
//        nbt.putInt("linkedY", linkedY);
//        nbt.putInt("linkedZ", linkedZ);
//    }
//
//    @Override
//    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
//        super.loadAdditional(nbt, registries);
//
//        linkedX = nbt.getInt("linkedX");
//        linkedY = nbt.getInt("linkedY");
//        linkedZ = nbt.getInt("linkedZ");
//    }
//
//    public static int getLinkedX() {
//        return linkedX;
//    }
//
//    public void setLinkedX(int linkedX) {
//        System.out.println(MultiPartBlockEntity.linkedX);
//        MultiPartBlockEntity.linkedX = linkedX;
//        System.out.println(MultiPartBlockEntity.linkedX);
//        test = this.toString();
//    }
//
//    public static int getLinkedY() {
//        return linkedY;
//    }
//
//    public void setLinkedY(int linkedY) {
//        MultiPartBlockEntity.linkedY = linkedY;
//    }
//
//    public static int getLinkedZ() {
//        return linkedZ;
//    }
//
//    public void setLinkedZ(int linkedZ) {
//        MultiPartBlockEntity.linkedZ = linkedZ;
//    }
//
//    public BlockPos getOriginalPos() {
//        return originalPos;
//    }
//
//    public void setOriginalPos(BlockPos originalPos) {
//        MultiPartBlockEntity.originalPos = originalPos;
//    }
//
//}
