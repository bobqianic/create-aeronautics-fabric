package dev.simulated_team.simulated.content.blocks.handle;

import dev.simulated_team.simulated.index.SimBlockShapes;
import com.zurrtum.create.catnip.math.VoxelShaper;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Arrays;

public class HandleShaper extends VoxelShaper {
    private VoxelShaper axisFalse, axisTrue;

    public static HandleShaper make() {
        final HandleShaper shaper = new HandleShaper();
        shaper.axisFalse = forDirectional(SimBlockShapes.HANDLE, Direction.UP);
        shaper.axisTrue = forDirectional(rotatedCopy(SimBlockShapes.HANDLE, new Vec3(0, 90, 0)), Direction.UP);
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
