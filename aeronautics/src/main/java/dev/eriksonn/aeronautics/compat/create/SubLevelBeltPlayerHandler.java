package dev.eriksonn.aeronautics.compat.create;

import com.zurrtum.create.content.equipment.armor.DivingBootsItem;
import com.zurrtum.create.content.kinetics.belt.BeltBlock;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import com.zurrtum.create.content.kinetics.belt.BeltHelper;
import com.zurrtum.create.content.kinetics.belt.transport.BeltMovementHandler;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class SubLevelBeltPlayerHandler {

    private SubLevelBeltPlayerHandler() {
    }

    public static @Nullable BeltContact findContact(
            final Player player,
            final SubLevel subLevel,
            final @Nullable BeltBlockEntity requiredController
    ) {
        if (subLevel.isRemoved()
                || !BeltMovementHandler.canBeTransported(player)
                || player.getAbilities().flying
                || DivingBootsItem.isWornBy(player)) {
            return null;
        }

        final var physicalBounds = subLevel.boundingBox();
        if (player.getX() < physicalBounds.minX() - 2.0
                || player.getX() > physicalBounds.maxX() + 2.0
                || player.getY() < physicalBounds.minY() - 2.0
                || player.getY() > physicalBounds.maxY() + 2.0
                || player.getZ() < physicalBounds.minZ() - 2.0
                || player.getZ() > physicalBounds.maxZ() + 2.0) {
            return null;
        }

        final Level level = player.level();
        final Vec3 localPosition = subLevel.logicalPose().transformPositionInverse(player.position());
        final BlockPos localFeetPos = BlockPos.containing(localPosition);
        final AABB localBounds = new BoundingBox3d(player.getBoundingBox())
                .transformInverse(subLevel.logicalPose(), new BoundingBox3d())
                .toMojang();
        final int minX = Mth.floor(localBounds.minX + 1.0E-7);
        final int maxX = Mth.floor(localBounds.maxX - 1.0E-7);
        final int minZ = Mth.floor(localBounds.minZ + 1.0E-7);
        final int maxZ = Mth.floor(localBounds.maxZ - 1.0E-7);

        BeltContact nearestContact = null;
        double nearestDistanceSquared = Double.POSITIVE_INFINITY;
        for (int below = 0; below <= 1; below++) {
            final int beltY = localFeetPos.getY() - below;
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    final BlockPos beltPos = new BlockPos(x, beltY, z);
                    final BlockState beltState = level.getBlockState(beltPos);
                    if (!BeltBlock.canTransportObjects(beltState)) {
                        continue;
                    }

                    // Flat belts are 13/16 high. Slopes can put the player's feet in the
                    // block above, which is why both the current and lower block are checked.
                    final double heightAboveBelt = localPosition.y - beltPos.getY();
                    if (heightAboveBelt < 0.25 || heightAboveBelt > 1.35) {
                        continue;
                    }

                    final BeltBlockEntity controller = BeltHelper.getControllerBE(level, beltPos);
                    if (controller == null
                            || !controller.isController()
                            || Math.abs(controller.getSpeed()) < 1.0F
                            || requiredController != null && controller != requiredController) {
                        continue;
                    }

                    final double distanceX = distanceToBlock(localPosition.x, x);
                    final double distanceZ = distanceToBlock(localPosition.z, z);
                    final double distanceSquared =
                            distanceX * distanceX + distanceZ * distanceZ + below * 1.0E-6;
                    if (distanceSquared < nearestDistanceSquared) {
                        nearestDistanceSquared = distanceSquared;
                        nearestContact = new BeltContact(
                                controller,
                                beltPos.immutable(),
                                beltState
                        );
                    }
                }
            }
        }

        return nearestContact;
    }

    private static double distanceToBlock(final double coordinate, final int blockCoordinate) {
        if (coordinate < blockCoordinate) {
            return blockCoordinate - coordinate;
        }
        if (coordinate > blockCoordinate + 1.0) {
            return coordinate - (blockCoordinate + 1.0);
        }
        return 0.0;
    }

    public record BeltContact(BeltBlockEntity controller, BlockPos pos, BlockState state) {
    }
}
