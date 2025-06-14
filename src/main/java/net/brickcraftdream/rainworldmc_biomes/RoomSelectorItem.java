package net.brickcraftdream.rainworldmc_biomes;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;

public class RoomSelectorItem extends Item {
    public RoomSelectorItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        return InteractionResult.SUCCESS;
    }

}
