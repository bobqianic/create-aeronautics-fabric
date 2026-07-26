package dev.simulated_team.simulated.util;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;

public record SimMovementContext(Level level, Vec3 localPosition, Vec3 globalPosition, Quaterniond orientation, @Nullable SubLevel subLevel) {

    public static SimMovementContext getMovementContext(final Level level, final Vec3 position) {
        final SubLevel subLevel = Sable.HELPER.getContaining(level, position);

        if (subLevel != null) {
            final Pose3d logicalPose = subLevel.logicalPose();
            final Vec3 globalPosition = logicalPose.transformPosition(position);
            final Quaterniond orientation = logicalPose.orientation();
            return new SimMovementContext(level, position, globalPosition, orientation, subLevel);
        } else {
            return new SimMovementContext(level, position, position, new Quaterniond(), null);
        }
    }

    public BlockPos localBlockPos() {
        return BlockPos.containing(this.localPosition.x(), this.localPosition.y(), this.localPosition.z());
    }
}
