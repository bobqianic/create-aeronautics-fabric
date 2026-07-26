package dev.simulated_team.simulated.ponder.elements.rope;

import dev.ryanhcode.sable.companion.math.JOMLConversion;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public class PonderRopePose {
    public final Vector3d start = new Vector3d();
    public final Vector3d end = new Vector3d();
    public double length;
    public double sog;
    public double floorHeight;

    public PonderRopePose() {

    }

    public PonderRopePose(final Vector3d start, final Vector3d end, final double length, final double sog, final double floorHeight) {
        this.start.set(start);
        this.end.set(end);
        this.length = length;
        this.sog = sog;
        this.floorHeight = floorHeight;
    }

    public void set(final PonderRopePose pose) {
        this.start.set(pose.start);
        this.end.set(pose.end);
        this.length = pose.length;
        this.sog = pose.sog;
        this.floorHeight = pose.floorHeight;
    }

    public void lerp(final PonderRopePose other, final double t) {
        this.start.lerp(other.start, t);
        this.end.lerp(other.end, t);
        this.length = Mth.lerp(t, this.length, other.length);
        this.sog = Mth.lerp(t, this.sog, other.sog);
    }

    public void lerp(final PonderRopePose a, final PonderRopePose b, final PonderRopePose dest, final double t) {
        a.start.lerp(b.start, t, dest.start);
        a.end.lerp(b.end, t, dest.end);
        dest.length = Mth.lerp(t, a.length, b.length);
        dest.sog = Mth.lerp(t, a.sog, b.sog);
    }


    public void lerp(final Vec3 from, final Vec3 to, final double length, final double sog, final double t) {
        this.start.lerp(JOMLConversion.toJOML(from), t);
        this.end.lerp(JOMLConversion.toJOML(to), t);
        this.length = Mth.lerp(t, this.length, length);
        this.sog = Mth.lerp(t, this.sog, sog);
    }
}
