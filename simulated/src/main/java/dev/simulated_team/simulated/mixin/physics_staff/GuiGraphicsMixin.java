package dev.simulated_team.simulated.mixin.physics_staff;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.simulated_team.simulated.index.SimItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {

    @Shadow public abstract int guiWidth();

    @Shadow @Final private Minecraft minecraft;

    @Shadow public abstract void fill(int minX, int minY, int maxX, int maxY, int color);

    @Shadow public abstract void enableScissor(int minX, int minY, int maxX, int maxY);

    @Shadow public abstract void disableScissor();

    @WrapMethod(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V")
    private void simulated$renderPhysicsStaff(final LivingEntity entity,
                                              final Level level,
                                              final ItemStack stack,
                                              final int x,
                                              final int y,
                                              final int seed,
                                              final Operation<Void> original) {
        final boolean isStaff = stack.is(SimItems.PHYSICS_STAFF);


        if (isStaff) {
            this.enableScissor(x, y, x + 16, y + 16);
        }

        original.call(entity, level, stack, x, y, seed);

        if (isStaff) {
            this.disableScissor();
        }
    }

}
