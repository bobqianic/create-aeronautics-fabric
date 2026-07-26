package dev.simulated_team.simulated.content.blocks.rope.strand.client;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.util.UUID;

public class ClientRopeStrand {
    private final ObjectArrayList<ClientRopePoint> points = new ObjectArrayList<>();
    public Vec3 startAttachment = null;
    public Vec3 endAttachment = null;
    private boolean stopped;

    public ClientRopeStrand(final UUID uuid) {
        this.uuid = uuid;
    }

    private final UUID uuid;

    public ObjectArrayList<ClientRopePoint> getPoints() {
        return this.points;
    }

    protected void tickInterpolation(final double gameTick) {
        for (final ClientRopePoint point : this.points) {
            final ObjectList<ClientRopePoint.Snapshot> buffer = point.snapshots();

            point.previousPosition().set(point.position());

            // Remove old snapshots
            while (!buffer.isEmpty() && buffer.getFirst().interpolationTick() < gameTick - 6) {
                buffer.removeFirst();
            }

            // If we have no snapshots, we can't interpolate
            if (buffer.isEmpty()) {
                continue;
            }

            int beforeIndex = -1;
            ClientRopePoint.Snapshot before = null;
            ClientRopePoint.Snapshot after = null;

            for (int i = 0; i < buffer.size(); i++) {
                final ClientRopePoint.Snapshot snapshot = buffer.get(i);
                if (gameTick == snapshot.interpolationTick()) {
                    point.position().set(snapshot.position());
                    continue;
                }

                if (snapshot.interpolationTick() < gameTick) {
                    beforeIndex = i;
                    before = snapshot;
                } else if (snapshot.interpolationTick() > gameTick) {
                    after = snapshot;
                    break;
                }
            }

            if (before == null || after == null) {
                if (before != null) {
                    point.position().set(before.position());

                    // dead reckon for a single tick max
                    final int beforeBeforeIndex = beforeIndex - 1;
                    if (beforeBeforeIndex >= 0 && !this.stopped) {
                        final ClientRopePoint.Snapshot beforeBefore = buffer.get(beforeBeforeIndex);

                        final double deadReckoningTicks = Mth.clamp(gameTick - before.interpolationTick(), 0, 1);
                        final double fraction = deadReckoningTicks / (before.interpolationTick() - beforeBefore.interpolationTick());

                        point.position().set(beforeBefore.position())
                                .lerp(before.position(), 1.0 + fraction);
                    }
                } else if (after != null) {
                    point.position().set(after.position());
                }
            } else {
                // Calculate the interpolation factor
                final double factor = (gameTick - before.interpolationTick()) / (after.interpolationTick() - before.interpolationTick());

                // Apply the interpolated snapshot
                before.position().lerp(after.position(), factor, point.position());
            }
        }

//        if (this.startAttachment != null) {
//            final Vec3 attachment = SubLevelHelper.projectOutOfSubLevel(SableDistUtil.getClientLevel(), this.startAttachment);
//            this.points.getFirst().position().set(attachment.x, attachment.y, attachment.z);
//        }
//
//        if (this.endAttachment != null) {
//            final Vec3 attachment = SubLevelHelper.projectOutOfSubLevel(SableDistUtil.getClientLevel(), this.endAttachment);
//            this.points.getLast().position().set(attachment.x, attachment.y, attachment.z);
//        }
    }

    public void setStopped(final boolean stopped) {
        this.stopped = stopped;
    }

    /**
     * Get the current bounds of this rope strand, not accounting for point collider radius.
     *
     * @return the current bounds
     */
    public AABB getBounds() {
        if (this.points.isEmpty()) {
            return null;
        }
        final Vector3d point0 = this.points.getFirst().position();
        AABB bounds = new AABB(point0.x, point0.y, point0.z, point0.x, point0.y, point0.z);

        for (final ClientRopePoint point : this.points) {
            final Vector3d pos = point.position();
            bounds = bounds.minmax(new AABB(pos.x, pos.y, pos.z, pos.x, pos.y, pos.z));
        }

        return bounds;
    }
    public UUID getUuid() {
        return this.uuid;
    }
}
