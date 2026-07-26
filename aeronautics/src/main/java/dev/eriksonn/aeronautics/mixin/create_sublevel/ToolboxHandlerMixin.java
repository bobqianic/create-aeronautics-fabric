package dev.eriksonn.aeronautics.mixin.create_sublevel;

import com.zurrtum.create.content.equipment.toolbox.ToolboxBlockEntity;
import com.zurrtum.create.content.equipment.toolbox.ToolboxHandler;
import dev.ryanhcode.sable.Sable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(value = ToolboxHandler.class, remap = false)
public abstract class ToolboxHandlerMixin {

    @Inject(method = "getNearest", at = @At("HEAD"), cancellable = true)
    private static void aeronautics$getNearestAcrossSubLevels(
            final LevelAccessor world, final Player player, final int maxAmount,
            final CallbackInfoReturnable<List<ToolboxBlockEntity>> cir
    ) {
        if (!(world instanceof Level level)) {
            return;
        }

        final Vec3 location = player.position();
        final double maxRange = ToolboxHandler.getMaxRange(player);
        final List<ToolboxBlockEntity> nearest = ToolboxHandler.toolboxes.get(world).keySet().stream()
                .filter(pos -> aeronautics$distance(level, location, pos) < maxRange * maxRange)
                .sorted(Comparator.comparingDouble(pos -> aeronautics$distance(level, location, pos)))
                .limit(maxAmount)
                .map(ToolboxHandler.toolboxes.get(world)::get)
                .filter(ToolboxBlockEntity::isFullyInitialized)
                .collect(Collectors.toList());
        cir.setReturnValue(nearest);
    }

    @Inject(method = "withinRange", at = @At("HEAD"), cancellable = true)
    private static void aeronautics$withinRangeAcrossSubLevels(
            final Player player, final ToolboxBlockEntity box,
            final CallbackInfoReturnable<Boolean> cir
    ) {
        if (player.level() != box.getLevel()) {
            cir.setReturnValue(false);
            return;
        }

        final double maxRange = ToolboxHandler.getMaxRange(player);
        cir.setReturnValue(aeronautics$distance(player.level(), player.position(), box.getBlockPos()) < maxRange * maxRange);
    }

    private static double aeronautics$distance(final Level level, final Vec3 location, final BlockPos pos) {
        return Sable.HELPER.distanceSquaredWithSubLevels(
                level,
                location,
                pos.getX() + 0.5,
                pos.getY(),
                pos.getZ() + 0.5
        );
    }
}
