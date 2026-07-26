package dev.ryanhcode.offroad.events;

import com.zurrtum.create.AllBlocks;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import dev.ryanhcode.offroad.content.components.TireLike;
import dev.ryanhcode.offroad.handlers.client.MultiMiningClientHandler;
import dev.ryanhcode.offroad.handlers.server.MultiMiningServerManager;
import dev.ryanhcode.offroad.index.OffroadDataComponents;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import dev.simulated_team.simulated.events.SimulatedCommonEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class OffroadCommonEvents {

    public static void modifyDefaultComponents(final BiConsumer<ItemLike, Consumer<SimulatedCommonEvents.ComponentBuilder>> modify) {
        modify.accept(AllBlocks.FLYWHEEL, builder -> { builder.set(OffroadDataComponents.TIRE, TireLike.FLYWHEEL); });
        modify.accept(AllBlocks.LARGE_WATER_WHEEL, builder -> { builder.set(OffroadDataComponents.TIRE, TireLike.LARGE_WATER_WHEEL); });
        modify.accept(AllBlocks.CRUSHING_WHEEL, builder -> { builder.set(OffroadDataComponents.TIRE, TireLike.CRUSHING_WHEEL); });
        modify.accept(AllBlocks.WATER_WHEEL, builder -> { builder.set(OffroadDataComponents.TIRE, TireLike.WATER_WHEEL); });
        modify.accept(AllBlocks.MECHANICAL_ROLLER, builder -> { builder.set(OffroadDataComponents.TIRE, TireLike.MECHANICAL_ROLLER); });
    }

    public static void physicsTick(final SubLevelPhysicsSystem physicsSystem, final double timeStep) {
        final ServerLevel level = physicsSystem.getLevel();
        WheelMountBlockEntity.applyAllBatchedForces(level, timeStep);
    }

    public static void tickLevelEvent(final Level level) {
        if (!level.isClientSide()) {
            MultiMiningServerManager.tick(level);
        } else {
            MultiMiningClientHandler.tick(level);
        }
    }
}
