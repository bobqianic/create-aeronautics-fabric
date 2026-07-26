package dev.simulated_team.simulated.mixin.linked_typewriter;

import com.zurrtum.create.client.ponder.foundation.PonderTooltipHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PonderTooltipHandler.class)
public class PonderTooltipHandlerMixin {
    // todo have create fix this
    @Redirect(method = "makeProgressBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/MutableComponent;withStyle(Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/MutableComponent;", ordinal = 0))
    private static MutableComponent dontBreakMutabilityContracts(final MutableComponent instance, final ChatFormatting format) {
        return instance.copy().withStyle(format);
    }
}
