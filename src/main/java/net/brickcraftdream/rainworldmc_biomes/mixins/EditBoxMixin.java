package net.brickcraftdream.rainworldmc_biomes.mixins;

import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Predicate;

@Mixin(EditBox.class)
public class EditBoxMixin {
    @Shadow
    private String value;

    @Shadow
    private int maxLength;

    @Shadow
    private Predicate<String> filter;

    @Unique
    public void setValueSilently(String value) {
        if (this.filter.test(value)) {
            if (value.length() > this.maxLength) {
                this.value = value.substring(0, this.maxLength);
            } else {
                this.value = value;
            }
        }
    }
}
