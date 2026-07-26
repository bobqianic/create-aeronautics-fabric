package dev.simulated_team.simulated.mixin.new_ponder;

import com.zurrtum.create.client.ponder.foundation.PonderTooltipHandler;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PonderTooltipHandler.class)
public interface PonderTooltipHandlerAccessor {
	@Accessor
	static ItemStack getTrackingStack() {
		return null;
	}
}
