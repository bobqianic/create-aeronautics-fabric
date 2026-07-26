package dev.simulated_team.simulated.mixin.ponder;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.simulated_team.simulated.mixin_interface.ponder.TextWindowElementExtension;
import com.zurrtum.create.client.ponder.foundation.element.TextWindowElement;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TextWindowElement.class)
public class TextWindowElementMixin implements TextWindowElementExtension {
    @Unique
    boolean simulated$shouldHidePointer = false;

    @Override
    public void simulated$hidePointer() {
        this.simulated$shouldHidePointer = true;
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fillGradient(IIIIII)V", ordinal = 0))
    public void simulated$removePointer(final GuiGraphics instance, final int x1, final int y1, final int x2, final int y2, final int colorFrom, final int colorTo, final Operation<Void> original) {
        if (this.simulated$shouldHidePointer) return;
        original.call(instance, x1, y1, x2, y2, colorFrom, colorTo);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fillGradient(IIIIII)V", ordinal = 1))
    public void simulated$removePointerTwoTheSqueakuel(final GuiGraphics instance, final int x1, final int y1, final int x2, final int y2, final int colorFrom, final int colorTo, final Operation<Void> original) {
        if (this.simulated$shouldHidePointer) return;
        original.call(instance, x1, y1, x2, y2, colorFrom, colorTo);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(FF)F"))
    public float simulated$shiftItALittleToTheLeftIfThereIsntALine(final float a, final float b, final Operation<Float> original) {
        if (this.simulated$shouldHidePointer) {
            return original.call(a, b - 50);
        }
        return original.call(a, b);
    }
}
