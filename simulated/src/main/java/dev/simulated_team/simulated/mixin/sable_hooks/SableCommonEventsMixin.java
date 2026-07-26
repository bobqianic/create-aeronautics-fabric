package dev.simulated_team.simulated.mixin.sable_hooks;

import dev.ryanhcode.sable.SableCommonEvents;
import dev.simulated_team.simulated.events.SimulatedCommonEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SableCommonEvents.class)
public class SableCommonEventsMixin {

    @Inject(method = "handleBlockChange", at = @At("HEAD"))
    private static void onBlockChange(final ServerLevel level,
                                      final LevelChunk chunk,
                                      final int x,
                                      final int y,
                                      final int z,
                                      final BlockState oldState,
                                      final BlockState newState,
                                      final CallbackInfo ci) {
        // Do something when a block is modified
        SimulatedCommonEvents.onBlockModifiedEvent(level, new BlockPos(x, y, z));
    }

}
