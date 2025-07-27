//package net.brickcraftdream.rainworldmc_biomes.blocks.libmultipart;
//
//import javax.annotation.Nonnull;
//
//import alexiil.mc.lib.net.IMsgReadCtx;
//import alexiil.mc.lib.net.NetByteBuf;
//import alexiil.mc.lib.net.NetIdDataK;
//import alexiil.mc.lib.net.ParentNetIdSingle;
//import alexiil.mc.lib.net.impl.ActiveMinecraftConnection;
//import alexiil.mc.lib.net.impl.CoreMinecraftNetUtil;
//import alexiil.mc.lib.net.impl.McNetworkStack;
//
//import alexiil.mc.lib.attributes.CombinableAttribute;
//import alexiil.mc.lib.attributes.SearchOptions;
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.Direction;
//import net.minecraft.core.HolderLookup;
//import net.minecraft.core.NonNullList;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.world.InteractionResult;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.level.block.entity.BlockEntity;
//import net.minecraft.world.level.block.entity.BlockEntityType;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.world.phys.BlockHitResult;
//
//public abstract class TileBase extends BlockEntity {
//
//    public static final ParentNetIdSingle<TileBase> NET_PARENT;
//    public static final NetIdDataK<TileBase> NET_DATA;
//
//    static {
//        NET_PARENT = McNetworkStack.BLOCK_ENTITY.subType(TileBase.class, "simplepipes:tile_base");
//        NET_DATA = NET_PARENT.idData("data").toClientOnly().setReceiver(TileBase::receiveData);
//    }
//
//    public TileBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
//        super(type, pos, state);
//    }
//
//    @Nonnull
//    public <T> T getNeighbourAttribute(CombinableAttribute<T> attr, Direction dir) {
//        return attr.get(getLevel(), getBlockPos().offset(dir.getNormal()), SearchOptions.inDirection(dir));
//    }
//
//    public NonNullList<ItemStack> removeItemsForDrop() {
//        return NonNullList.create();
//    }
//
//    protected void sendPacket(ServerLevel w, CompoundTag tag) {
//        for (ActiveMinecraftConnection c : CoreMinecraftNetUtil.getNearbyActiveConnections(this, 24)) {
//            NET_DATA.send(c, this, (t, buf, ctx) -> buf.writeNbt(tag));
//        }
//    }
//
//    @Override
//    public CompoundTag getUpdateTag(HolderLookup.Provider lookup) {
//        return toClientTag(super.getUpdateTag(lookup));
//    }
//
//    private void receiveData(NetByteBuf buffer, IMsgReadCtx ctx) {
//        readPacket(buffer.readNbt());
//    }
//
//    public CompoundTag toClientTag(CompoundTag tag) {
//        return tag;
//    }
//
//    public void readPacket(CompoundTag tag) {}
//
//    public void onPlacedBy(LivingEntity placer, ItemStack stack) {}
//
//    public InteractionResult onUse(Player player, BlockHitResult hit) {
//        return InteractionResult.PASS;
//    }
//}
