package dev.simulated_team.simulated.content.blocks.redstone_magnet;

import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import org.joml.Vector3d;

public interface SimMagnet {

    Quaternionfc getOrientation();

    SubLevel getLatestSubLevel();

    Vec3 getMagnetPosition();

    Vector3d setMagneticMoment(Vector3d v);

    boolean magnetActive();

}
