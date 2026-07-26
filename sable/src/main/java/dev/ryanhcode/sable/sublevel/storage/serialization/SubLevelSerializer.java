package dev.ryanhcode.sable.sublevel.storage.serialization;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.SableConfig;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.plot.ServerLevelPlot;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import dev.ryanhcode.sable.util.SableNBTUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.List;
import java.util.UUID;

/**
 * Serializes and saves sub-levels to disk.
 */
public class SubLevelSerializer {

    /**
     * For when absolutely everything is broken, and I need to step through the state management.
     */
    public static final boolean SUPER_DEBUG_MODE = false;

    private static @NotNull CompoundTag serialize(final ServerSubLevel subLevel, final List<UUID> dependencies) {
        final CompoundTag tag = new CompoundTag();
        final ServerLevelPlot plot = subLevel.getPlot();
        final ListTag dependencyTags = new ListTag();

        for (final UUID dependency : dependencies) {
            // PORT-NOTE(mc26.1): NbtUtils.createUUID/loadUUID were removed; UUIDUtil.CODEC writes the
            // same IntArrayTag format.
            dependencyTags.add(UUIDUtil.CODEC.encodeStart(NbtOps.INSTANCE, dependency).getOrThrow());
        }

        final Pose3d serializedPose = new Pose3d(subLevel.logicalPose());
        final Vector3dc selfCenterOfMass = subLevel.getSelfMassTracker().getCenterOfMass();
        serializedPose.position().set(subLevel.logicalPose().transformPosition(new Vector3d(selfCenterOfMass)));
        serializedPose.rotationPoint().set(selfCenterOfMass);

        tag.store("uuid", net.minecraft.core.UUIDUtil.CODEC, subLevel.getUniqueId());
        tag.put("plot", plot.save());
        tag.put("pose", SableNBTUtils.writePose3d(serializedPose));
        tag.put("world_bounds", SableNBTUtils.writeBoundingBox(subLevel.boundingBox()));

        final RigidBodyHandle handle = RigidBodyHandle.of(subLevel);
        if (handle != null) {
            final Vector3d linearVelocity = handle.getLinearVelocity(new Vector3d());
            final Vector3d angularVelocity = handle.getAngularVelocity(new Vector3d());

            if (linearVelocity.lengthSquared() > 0.1 * 0.1) {
                tag.put("linear_velocity", SableNBTUtils.writeVector3d(linearVelocity));
            }

            if (angularVelocity.lengthSquared() > Math.toRadians(1.0)) {
                tag.put("angular_velocity", SableNBTUtils.writeVector3d(angularVelocity));
            }
        }

        if (subLevel.getName() != null) {
            tag.putString("display_name", subLevel.getName());
        }

        if (!dependencies.isEmpty()) {
            tag.put("loading_dependencies", dependencyTags);
        }

        final CompoundTag userDataTag = subLevel.getUserDataTag();
        if (userDataTag != null) {
            tag.put("user_data", userDataTag);
        }

        return tag;
    }

    /**
     * Loads a sub-level from the level data folder with a name
     *
     * @param tag tag to load from
     * @return the half loaded sub-level, or null if the loading fails
     */
    @Nullable
    public static SubLevelData fromData(final CompoundTag tag) {
        final UUID uuid = tag.read("uuid", net.minecraft.core.UUIDUtil.CODEC).orElseThrow();

        List<UUID> dependencies = List.of();
        if (tag.contains("loading_dependencies")) {
            final ListTag dependencyUUIDS = tag.getListOrEmpty("loading_dependencies");

            dependencies = new ObjectArrayList<>();

            for (final Tag dependencyUUIDTag : dependencyUUIDS) {
                final UUID dependencyUUID = UUIDUtil.CODEC.parse(NbtOps.INSTANCE, dependencyUUIDTag).getOrThrow();

                dependencies.add(dependencyUUID);
            }
        }

        return new SubLevelData(
                uuid,
                SableNBTUtils.readBoundingBox(tag.getCompoundOrEmpty("world_bounds")),
                SableNBTUtils.readPose3d(tag.getCompoundOrEmpty("pose")),
                dependencies,
                tag
        );
    }

    /**
     * Fully loads a sub-level, adding it to the level
     *
     * @param level              the level to load the sub-level into
     * @param halfLoadedSubLevel the half loaded sub-level to fully load
     */
    public static ServerSubLevel fullyLoad(final ServerLevel level, final SubLevelData halfLoadedSubLevel) {
        final CompoundTag tag = halfLoadedSubLevel.fullTag();
        final CompoundTag plotTag = tag.getCompoundOrEmpty("plot");

        final int plotX = plotTag.getIntOr("plot_x", 0);
        final int plotZ = plotTag.getIntOr("plot_z", 0);

        final Pose3d pose = SableNBTUtils.readPose3d(tag.getCompoundOrEmpty("pose"));

        final Vector3d position = pose.position();
        final Vector3d cor = pose.rotationPoint();
        if (Double.isNaN(position.x) ||
                Double.isNaN(position.y) ||
                Double.isNaN(position.z) ||
                Double.isNaN(cor.x) ||
                Double.isNaN(cor.y) ||
                Double.isNaN(cor.z)
        ) {
            Sable.LOGGER.error("Failed to load sub-level, invalid pose: {}", pose);
            return null;
        }

        final ServerSubLevelContainer plotContainer = SubLevelContainer.getContainer(level);

        final ServerSubLevel subLevel;
        try {
            subLevel = (ServerSubLevel) plotContainer.allocateSubLevel(halfLoadedSubLevel.uuid(), plotX, plotZ, pose);
        } catch (final IllegalArgumentException e) {
            Sable.LOGGER.error("Failed to load sub-level, skipping", halfLoadedSubLevel, e);
            return null;
        }

        final ServerLevelPlot plot = subLevel.getPlot();
        plot.load(plotTag);

        if (plot.getBoundingBox() == BoundingBox3i.EMPTY || plot.getBoundingBox().volume() <= 0) {
            Sable.LOGGER.error("Failed to load sub-level, invalid plot bounds: {}", plot.getBoundingBox() == BoundingBox3i.EMPTY ? "EMPTY" : plot.getBoundingBox());
            plotContainer.removeSubLevel(subLevel, SubLevelRemovalReason.REMOVED);
            return null;
        }

        final SubLevelPhysicsSystem physicsSystem = plotContainer.physicsSystem();
        subLevel.logicalPose().set(pose);
        physicsSystem.getPipeline().teleport(subLevel, position, pose.orientation());
        subLevel.updateLastPose();

        Vector3dc linearVelocity = JOMLConversion.ZERO;
        Vector3dc angularVelocity = JOMLConversion.ZERO;

        if (tag.contains("linear_velocity")) {
            linearVelocity = SableNBTUtils.readVector3d(tag.getCompoundOrEmpty("linear_velocity"))
                    .mul(SableConfig.VELOCITY_RETAINED_ON_LOAD.getAsDouble());
        }

        if (tag.contains("angular_velocity")) {
            angularVelocity = SableNBTUtils.readVector3d(tag.getCompoundOrEmpty("angular_velocity"))
                    .mul(SableConfig.VELOCITY_RETAINED_ON_LOAD.getAsDouble());
        }

        physicsSystem.getPipeline().addLinearAndAngularVelocity(subLevel, linearVelocity, angularVelocity);

        if (tag.contains("display_name")) {
            subLevel.setName(tag.getStringOr("display_name", ""));
        }

        if (tag.contains("user_data")) {
            subLevel.setUserDataTag(tag.getCompoundOrEmpty("user_data"));
        }

        return subLevel;
    }

    /**
     * Serializes a sub-level and converts it to a {@link SubLevelData}
     *
     * @param subLevel the sub-level to serialize
     */
    public static SubLevelData toData(final ServerSubLevel subLevel, final @NotNull List<UUID> dependencies) {
        final List<UUID> filteredDependencies = new ObjectArrayList<>(dependencies);
        filteredDependencies.remove(subLevel.getUniqueId());

        final CompoundTag tag = serialize(subLevel, filteredDependencies);

        return new SubLevelData(
                subLevel.getUniqueId(),
                new BoundingBox3d(subLevel.boundingBox()),
                new Pose3d(subLevel.logicalPose()),
                filteredDependencies,
                tag
        );
    }
}
