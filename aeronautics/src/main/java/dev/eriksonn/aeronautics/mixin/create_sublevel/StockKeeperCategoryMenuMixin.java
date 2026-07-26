package dev.eriksonn.aeronautics.mixin.create_sublevel;

import com.zurrtum.create.content.logistics.stockTicker.StockKeeperCategoryMenu;
import com.zurrtum.create.content.logistics.stockTicker.StockTickerBlockEntity;
import dev.ryanhcode.sable.Sable;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = StockKeeperCategoryMenu.class, remap = false)
public abstract class StockKeeperCategoryMenuMixin {

    @Inject(
            method = "stillValid(Lnet/minecraft/world/entity/player/Player;)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = true
    )
    private void aeronautics$stillValidAcrossSubLevels(
            final Player player, final CallbackInfoReturnable<Boolean> cir
    ) {
        final StockTickerBlockEntity ticker = ((StockKeeperCategoryMenu) (Object) this).contentHolder;
        final double range = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 4;
        cir.setReturnValue(
                !ticker.isRemoved()
                        && Sable.HELPER.distanceSquaredWithSubLevels(
                        player.level(),
                        player.position(),
                        Vec3.atCenterOf(ticker.getBlockPos())
                ) < range * range
        );
    }
}
