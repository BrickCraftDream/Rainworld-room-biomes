package net.brickcraftdream.rainworldmc_biomes;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RoomSelectorItem extends Item {
    public RoomSelectorItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean canAttackBlock(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, Player player) {
        return !player.isCreative();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, List<Component> tooltip, @NotNull TooltipFlag tooltipFlag) {
        tooltip.add(Component.translatable("itemTooltip.rainworld.room_selector_item.first_line.use").withStyle(ChatFormatting.GOLD)
                .append(" ")
                .append(Component.translatable("itemTooltip.rainworld.room_selector_item.first_line.left_click").withStyle(ChatFormatting.RED))
                .append(" ")
                .append(Component.translatable("itemTooltip.rainworld.room_selector_item.first_line.and").withStyle(ChatFormatting.GOLD))
                .append(" ")
                .append(Component.translatable("itemTooltip.rainworld.room_selector_item.first_line.right_click").withStyle(ChatFormatting.RED))
                .append(" ")
                .append(Component.translatable("itemTooltip.rainworld.room_selector_item.first_line.rest").withStyle(ChatFormatting.GOLD))
        );
        tooltip.add(Component.translatable("itemTooltip.rainworld.room_selector_item.second_line.press").withStyle(ChatFormatting.GOLD)
                .append(" ")
                .append(Component.translatable("itemTooltip.rainworld.room_selector_item.second_line.ctrl").withStyle(ChatFormatting.RED))
                .append(" ")
                .append(Component.translatable("itemTooltip.rainworld.room_selector_item.second_line.confirm").withStyle(ChatFormatting.GOLD))
        );
    }

}
