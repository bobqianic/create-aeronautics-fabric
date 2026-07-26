package dev.ryanhcode.sable.api;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.mixinterface.EntityExtension;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * A helper class for handling interactions between sub-levels<->sub-levels and sub-levels<->levels
 */
@ApiStatus.Internal
public final class SubLevelHelper {

    private static final ThreadLocal<Map<Entity, ArrayDeque<EntityRot>>> oldRot =
            ThreadLocal.withInitial(IdentityHashMap::new);
    private static final ObjectList<BiFunction<Vector3dc, Level, Vector3dc>> windProviders = new ObjectArrayList<>();

    /**
     * Projects a full entity into a subLevel, rotation and all.
     * Pushing an entity into local space of a sub-level caches its old rotations.
     * Projects the entity position, not the eye position.
     *
     * @param subLevel The subLevel to project into
     * @param entity   The entity to project
     */
    public static void pushEntityLocal(final SubLevel subLevel, final Entity entity) {
        SubLevelHelper.pushEntityLocal(subLevel, entity, EntityAnchorArgument.Anchor.FEET);
    }

    /**
     * Projects a full entity out of a subLevel, rotation and all.
     * Uses the cached values from {@link SubLevelHelper#pushEntityLocal}.
     * Projects the entity position, not the eye position.
     *
     * @param subLevel The subLevel to project out of
     * @param player   The entity to project
     */
    public static void popEntityLocal(final SubLevel subLevel, final Entity player) {
        SubLevelHelper.popEntityLocal(subLevel, player, EntityAnchorArgument.Anchor.FEET);
    }

    /**
     * Projects a full entity into a subLevel, rotation and all.
     * Pushing an entity into local space of a sub-level caches its old rotations.
     *
     * @param subLevel The subLevel to project into
     * @param entity   The entity to project
     * @param anchor   The anchor that should be projected
     */
    public static void pushEntityLocal(final SubLevel subLevel, final Entity entity, final EntityAnchorArgument.Anchor anchor) {
        final EntityRot entityRot = new EntityRot();
        entityRot.copy(entity);
        oldRot.get().computeIfAbsent(entity, ignored -> new ArrayDeque<>()).push(entityRot);

        if (anchor == EntityAnchorArgument.Anchor.FEET) {
            ((EntityExtension) entity).sable$setPosSuperRaw(subLevel.logicalPose().transformPositionInverse(entity.position()));
        } else {
            ((EntityExtension) entity).sable$setPosSuperRaw(subLevel.logicalPose().transformPositionInverse(entity.getEyePosition()).add(0.0, -entity.getEyeHeight(), 0.0));
        }

        Vec3 playerLookAngle = entity.getLookAngle();
        playerLookAngle = subLevel.logicalPose().transformNormalInverse(playerLookAngle);

        final Vec3 pTarget = entity.getEyePosition().add(playerLookAngle);
        final Vec3 vec3 = entity.getEyePosition();
        final double d0 = pTarget.x - vec3.x;
        final double d1 = pTarget.y - vec3.y;
        final double d2 = pTarget.z - vec3.z;
        final double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        entity.setXRot(Mth.wrapDegrees((float) (-(Mth.atan2(d1, d3) * (double) (180F / (float) Math.PI)))));
        entity.setYRot(Mth.wrapDegrees((float) (Mth.atan2(d2, d0) * (double) (180F / (float) Math.PI)) - 90.0F));
        entity.setYHeadRot(entity.getYRot());
        entityRot.copyProjected(entity);

        entity.setDeltaMovement(subLevel.logicalPose().transformNormalInverse(entity.getDeltaMovement()));
    }

    /**
     * Projects a full entity out of a subLevel, rotation and all.
     * Uses the cached values from {@link SubLevelHelper#pushEntityLocal}.
     *
     * @param subLevel The subLevel to project out of
     * @param entity   The entity to project
     * @param anchor   The anchor that should be projected
     */
    public static void popEntityLocal(final SubLevel subLevel, final Entity entity, final EntityAnchorArgument.Anchor anchor) {
        final Map<Entity, ArrayDeque<EntityRot>> rotationsByEntity = oldRot.get();
        final ArrayDeque<EntityRot> rotations = rotationsByEntity.get(entity);
        if (rotations == null || rotations.isEmpty()) {
            throw new IllegalStateException("Tried to pop an entity that was not projected into a sub-level");
        }

        if (anchor == EntityAnchorArgument.Anchor.FEET) {
            ((EntityExtension) entity).sable$setPosSuperRaw(subLevel.logicalPose().transformPosition(entity.position()));
        } else {
            ((EntityExtension) entity).sable$setPosSuperRaw(subLevel.logicalPose().transformPosition(entity.getEyePosition()).add(0.0, -entity.getEyeHeight(), 0.0));
        }

        rotations.pop().applyAfterProjection(subLevel, entity);
        if (rotations.isEmpty()) {
            rotationsByEntity.remove(entity);
        }
        if (rotationsByEntity.isEmpty()) {
            oldRot.remove();
        }
        entity.setDeltaMovement(subLevel.logicalPose().transformNormal(entity.getDeltaMovement()));
    }

    /**
     * Projects an entity into a sub-level unless it is already stored in that sub-level's plot.
     *
     * @return whether the entity was projected and therefore needs to be popped
     */
    public static boolean pushEntityLocalIfNeeded(final SubLevel subLevel, final Entity entity) {
        // Entity.chunkPosition() is cached separately from position. Temporary
        // projection uses sable$setPosSuperRaw, so that cached chunk can still
        // identify a plot after the entity has been restored to physical space.
        // Inspect the live position directly before deciding that no projection
        // is necessary.
        if (Sable.HELPER.getContaining(entity.level(), entity.position()) == subLevel) {
            return false;
        }

        pushEntityLocal(subLevel, entity);
        return true;
    }

    /**
     * Pops an entity previously projected by {@link #pushEntityLocalIfNeeded(SubLevel, Entity)}.
     */
    public static void popEntityLocalIfNeeded(final SubLevel subLevel, final Entity entity, final boolean projected) {
        if (projected) {
            popEntityLocal(subLevel, entity);
        }
    }

    /**
     * Gets the global velocity of a point in a level relative to the air, taking into account sublevels and their plots/poses
     *
     * @param level the level to check
     * @param pos   the position of the point
     * @param dest  the vector to hold the result
     * @return the global velocity of the point stored in dest [m/s]
     */
    public static Vector3d getVelocityRelativeToAir(final Level level, final Vector3dc pos, final Vector3d dest) {
        final Vector3d probePos = new Vector3d(pos);
        final Vector3d velocity = Sable.HELPER.getVelocity(level, pos, dest);

        for (final BiFunction<Vector3dc, Level, Vector3dc> windProvider : windProviders) {
            final Vector3dc airVelocity = windProvider.apply(probePos, level);

            if (airVelocity != null) {
                velocity.sub(airVelocity);
            }
        }

        return velocity;
    }

    /**
     * Registers a function to get the air velocity of a point in a level
     *
     * @param function the function to register
     */
    public static void registerWindProvider(final BiFunction<Vector3dc, Level, Vector3dc> function) {
        windProviders.add(function);
    }

    /**
     * @return the chain of sub-levels that should load / unload with the given one
     */
    public static Collection<ServerSubLevel> getLoadingDependencyChain(final ServerSubLevel subLevel) {
        final ObjectOpenHashSet<ServerSubLevel> visited = new ObjectOpenHashSet<>();
        final ObjectOpenHashSet<ServerSubLevel> frontier = new ObjectOpenHashSet<>();

        frontier.add(subLevel);

        while (!frontier.isEmpty()) {
            final ServerSubLevel current = frontier.iterator().next();

            frontier.remove(current);
            visited.add(current);

            final Iterable<SubLevel> intersecting = Sable.HELPER.getAllIntersecting(current.getLevel(), new BoundingBox3d(current.boundingBox()));

            // Intersecting dependencies
            for (final SubLevel neighbor : intersecting) {
                final ServerSubLevel serverNeighbor = (ServerSubLevel) neighbor;

                if (!visited.contains(serverNeighbor)) {
                    frontier.add(serverNeighbor);
                }
            }

            // Actor dependencies
            for (final BlockEntitySubLevelActor actor : current.getPlot().getBlockEntityActors()) {
                final Iterable<SubLevel> loadingDependencies = actor.sable$getLoadingDependencies();

                if (loadingDependencies == null) continue;

                for (final SubLevel dependency : loadingDependencies) {
                    final ServerSubLevel serverDependency = (ServerSubLevel) dependency;

                    if (!visited.contains(serverDependency)) {
                        frontier.add(serverDependency);
                    }
                }
            }

        }

        return visited;
    }

    /**
     * @return the chain of sub-levels considered connected
     */
    public static Collection<SubLevel> getConnectedChain(final SubLevel subLevel) {
        final ObjectOpenHashSet<SubLevel> visited = new ObjectOpenHashSet<>();
        final ObjectOpenHashSet<SubLevel> frontier = new ObjectOpenHashSet<>();

        frontier.add(subLevel);

        while (!frontier.isEmpty()) {
            final SubLevel current = frontier.iterator().next();

            frontier.remove(current);
            visited.add(current);

            // Actor dependencies
            for (final BlockEntitySubLevelActor actor : current.getPlot().getBlockEntityActors()) {
                final Iterable<SubLevel> dependencies = actor.sable$getConnectionDependencies();

                if (dependencies == null) continue;

                for (final SubLevel dependency : dependencies) {
                    final SubLevel serverDependency = dependency;

                    if (!visited.contains(serverDependency)) {
                        frontier.add(serverDependency);
                    }
                }
            }
        }

        return visited;
    }

    private static class EntityRot {

        private float xRot;
        private float yRot;
        private float yHeadRot;
        private float projectedXRot;
        private float projectedYRot;
        private float projectedYHeadRot;

        public void applyAfterProjection(final SubLevel subLevel, final Entity entity) {
            final boolean bodyRotationChanged =
                    !approximatelyEqual(entity.getXRot(), this.projectedXRot)
                            || !approximatelyEqual(entity.getYRot(), this.projectedYRot);
            final boolean headRotationChanged =
                    !approximatelyEqual(entity.getYHeadRot(), this.projectedYHeadRot);

            if (bodyRotationChanged) {
                applyLookDirection(entity, subLevel.logicalPose().transformNormal(entity.getLookAngle()));
            } else {
                entity.setXRot(this.xRot);
                entity.setYRot(this.yRot);
            }

            if (headRotationChanged) {
                final Vec3 localHeadDirection = Vec3.directionFromRotation(0.0F, entity.getYHeadRot());
                final Vec3 globalHeadDirection = subLevel.logicalPose().transformNormal(localHeadDirection);
                entity.setYHeadRot(yRot(globalHeadDirection));
            } else {
                entity.setYHeadRot(this.yHeadRot);
            }
        }

        public void copy(final Entity entity) {
            this.xRot = entity.getXRot();
            this.yRot = entity.getYRot();
            this.yHeadRot = entity.getYHeadRot();
        }

        public void copyProjected(final Entity entity) {
            this.projectedXRot = entity.getXRot();
            this.projectedYRot = entity.getYRot();
            this.projectedYHeadRot = entity.getYHeadRot();
        }

        private static void applyLookDirection(final Entity entity, final Vec3 direction) {
            final double horizontalDistance = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
            entity.setXRot(Mth.wrapDegrees((float) (-(Mth.atan2(direction.y, horizontalDistance) * (180F / Math.PI)))));
            entity.setYRot(yRot(direction));
        }

        private static float yRot(final Vec3 direction) {
            return Mth.wrapDegrees((float) (Mth.atan2(direction.z, direction.x) * (180F / Math.PI)) - 90.0F);
        }

        private static boolean approximatelyEqual(final float first, final float second) {
            return Math.abs(first - second) < 1.0E-4F;
        }
    }
}
