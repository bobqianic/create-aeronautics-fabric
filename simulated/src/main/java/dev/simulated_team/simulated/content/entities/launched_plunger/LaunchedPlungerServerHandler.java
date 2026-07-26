package dev.simulated_team.simulated.content.entities.launched_plunger;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import com.zurrtum.create.catnip.data.WorldAttached;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LaunchedPlungerServerHandler {

    /**
     * Collections of Launched plungers associated with levels.
     */
private static final WorldAttached<Collection<LaunchedPlungerEntity>> LEVEL_PLUNGERS = new WorldAttached<>(x -> new ObjectOpenHashSet<>());

    /**
     * Add the given launched plunger entity from the collection associated with the given level
     */
    public static void addLaunchedPlunger(final Level level, final LaunchedPlungerEntity toAdd) {
        LEVEL_PLUNGERS.get(level).add(toAdd);
    }

    /**
     * Removes the given launched plunger entity from the collection associated with the given level.
     */
    public static void removeLaunchedPlunger(final Level level, final LaunchedPlungerEntity toRemove) {
        final Collection<LaunchedPlungerEntity> launchedPlungers = LEVEL_PLUNGERS.get(level);
        launchedPlungers.remove(toRemove);
    }

    public static void removePlayerPlungers(final Player player){
        final List<LaunchedPlungerEntity> plungersForRemoval = new ArrayList<>();
        for (final LaunchedPlungerEntity launchedPlungerEntity : LEVEL_PLUNGERS.get(player.level())) {
            if (launchedPlungerEntity.getOwner() == player) {
                plungersForRemoval.add(launchedPlungerEntity);
            }
        }
        for (final LaunchedPlungerEntity launchedPlungerEntity : plungersForRemoval) {
            launchedPlungerEntity.discard();
            final LaunchedPlungerEntity other = launchedPlungerEntity.getOther();
            if (other != null) {
                other.discard();
            }
        }
    }

    public static void physicsTickAllPlungers(final SubLevelPhysicsSystem physicsSystem, final double timeStep) {
        final ServerLevel level = physicsSystem.getLevel();
        final Collection<LaunchedPlungerEntity> plungers = LEVEL_PLUNGERS.get(level);

        for (final LaunchedPlungerEntity launchedPlunger : plungers) {
            final ServerSubLevel sublevel = (ServerSubLevel) Sable.HELPER.getContaining(launchedPlunger);

            if (sublevel != null) {
                launchedPlunger.physicsTick(sublevel, physicsSystem.getPhysicsHandle(sublevel), timeStep);
            }
        }
    }
}
