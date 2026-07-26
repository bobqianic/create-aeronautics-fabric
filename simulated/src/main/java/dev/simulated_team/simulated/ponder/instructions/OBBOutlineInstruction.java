package dev.simulated_team.simulated.ponder.instructions;

import dev.ryanhcode.sable.companion.math.JOMLConversion;
import com.zurrtum.create.client.ponder.api.PonderPalette;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.instruction.TickingInstruction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3d;
import org.joml.Vector3d;

public class OBBOutlineInstruction extends TickingInstruction {

    final AABB bb;
    final Vec3 rotation;
    final boolean bigLines;
    final PonderPalette color;
    final String slot;

    final Vec3 pivotPoint;

    final Matrix3d m;
    final Vector3d[] lines;

    public OBBOutlineInstruction(final AABB bb, final Vec3 rotation, final boolean bigLines, final PonderPalette color, final String slot, final int ticks) {
        super(false, ticks);
        this.bb = bb;
        this.rotation = rotation;
        this.bigLines = bigLines;
        this.color = color;

        this.pivotPoint = bb.getCenter();

        this.m = getRotationMatrix(rotation.x, rotation.y, rotation.z);
        this.lines = getAABBLines(bb);
        this.slot = slot;

        for (final Vector3d line : this.lines) {
            this.m.transform(line.sub(this.pivotPoint.x, this.pivotPoint.y, this.pivotPoint.z)).add(this.pivotPoint.x, this.pivotPoint.y, this.pivotPoint.z);
        }
    }

    @Override
    public void tick(final PonderScene scene) {
        super.tick(scene);
        for (int i = 0; i < this.lines.length; i += 2) {
            scene.getOutliner()
                    .showLine(this.slot + i, JOMLConversion.toMojang(this.lines[i]), JOMLConversion.toMojang(this.lines[i+1]))
                    .lineWidth(this.bigLines ? 1 / 8f : 1 / 16f)
                    .colored(this.color.getColor());
        }
    }

    private static @NotNull Vector3d[] getAABBLines(final AABB bb) {
        final double minX = bb.minX;
        final double maxX = bb.maxX;
        final double minY = bb.minY;
        final double maxY = bb.maxY;
        final double minZ = bb.minZ;
        final double maxZ = bb.maxZ;

        return new Vector3d[]{
                // top square
                new Vector3d(minX, maxY, minZ), new Vector3d(maxX, maxY, minZ),
                new Vector3d(minX, maxY, minZ), new Vector3d(minX, maxY, maxZ),
                new Vector3d(maxX, maxY, maxZ), new Vector3d(maxX, maxY, minZ),
                new Vector3d(maxX, maxY, maxZ), new Vector3d(minX, maxY, maxZ),
                // bottom square
                new Vector3d(minX, minY, minZ), new Vector3d(maxX, minY, minZ),
                new Vector3d(minX, minY, minZ), new Vector3d(minX, minY, maxZ),
                new Vector3d(maxX, minY, maxZ), new Vector3d(maxX, minY, minZ),
                new Vector3d(maxX, minY, maxZ), new Vector3d(minX, minY, maxZ),
                // vertical lines
                new Vector3d(minX, minY, minZ), new Vector3d(minX, maxY, minZ),
                new Vector3d(maxX, minY, minZ), new Vector3d(maxX, maxY, minZ),
                new Vector3d(minX, minY, maxZ), new Vector3d(minX, maxY, maxZ),
                new Vector3d(maxX, minY, maxZ), new Vector3d(maxX, maxY, maxZ),
        };
    }

    private static @NotNull Matrix3d getRotationMatrix(final double x, final double y, final double z) {
        final double sinA = Math.sin(Math.toRadians(x));
        final double cosA = Math.cos(Math.toRadians(x));
        final double sinB = Math.sin(Math.toRadians(y));
        final double cosB = Math.cos(Math.toRadians(y));
        final double sinY = Math.sin(Math.toRadians(z));
        final double cosY = Math.cos(Math.toRadians(z));


        return new Matrix3d(
                cosB * cosY, sinA * sinB * cosY - cosA * sinY, cosA * sinB * cosY + sinA * sinY,
                cosB * sinY, sinA * sinB * sinY + cosA * cosY, cosA * sinB * sinY - sinA * cosY,
                -sinB, sinA * cosB, cosA * cosB
        );
    }
}
