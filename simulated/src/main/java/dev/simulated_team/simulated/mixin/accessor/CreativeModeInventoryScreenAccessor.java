package dev.simulated_team.simulated.mixin.accessor;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface CreativeModeInventoryScreenAccessor {

    @Accessor()
    int getLeftPos();

    @Accessor
    int getTopPos();
}
