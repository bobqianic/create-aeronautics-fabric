package dev.simulated_team.simulated.content.blocks.velocity_sensor;

import dev.simulated_team.simulated.index.SimBlockShapes;
import com.zurrtum.create.catnip.math.VoxelShaper;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Arrays;

public class VelocitySensorShaper extends VoxelShaper {
    private VoxelShaper axisFalse, axisTrue;

    static VelocitySensorShaper make() {
        final VelocitySensorShaper shaper = new VelocitySensorShaper();
        shaper.axisFalse = forDirectional(SimBlockShapes.VELOCITY_SENSOR, Direction.UP);
        shaper.axisTrue = forDirectional(rotatedCopy(SimBlockShapes.VELOCITY_SENSOR, new Vec3(0, 90, 0)), Direction.UP);
        Arrays.asList(Direction.EAST, Direction.WEST).forEach(direction -> {
            final VoxelShape mem = shaper.axisFalse.get(direction);
            shaper.axisFalse.withShape(shaper.axisTrue.get(direction), direction);
            shaper.axisTrue.withShape(mem, direction);
        });
        return shaper;
    }

    public VoxelShape get(final Direction direction, final boolean axisAlong) {
        return (axisAlong ? this.axisTrue : this.axisFalse).get(direction);
    }
}
