package dev.simulated_team.simulated.util;

import com.zurrtum.create.catnip.math.VoxelShaper;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Arrays;

public class DirectionalAxisShaper extends VoxelShaper {
    private VoxelShaper axisFalse, axisTrue;

    public static DirectionalAxisShaper make(final VoxelShape shape){
        final DirectionalAxisShaper shaper = new DirectionalAxisShaper();
        shaper.axisFalse = forDirectional(shape, Direction.UP);
        shaper.axisTrue = forDirectional(rotatedCopy(shape, new Vec3(0, 90, 0)), Direction.UP);

        // Shapes for X axis need to be swapped
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
