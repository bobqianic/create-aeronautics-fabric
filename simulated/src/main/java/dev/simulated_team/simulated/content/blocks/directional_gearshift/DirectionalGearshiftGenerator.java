package dev.simulated_team.simulated.content.blocks.directional_gearshift;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import dev.simulated_team.simulated.Simulated;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import io.github.fabricators_of_create.porting_lib.models.generators.ModelFile;
import io.github.fabricators_of_create.porting_lib.models.generators.MultiPartBlockStateBuilder;

public class DirectionalGearshiftGenerator {
    public static <P extends DirectionalGearshiftBlock> void generate(final DataGenContext<Block, P> context, final RegistrateBlockstateProvider provider) {
        final MultiPartBlockStateBuilder builder = provider.getMultipartBuilder(context.get());

        for (final BlockState state : context.get().getStateDefinition().getPossibleStates()) {
            final boolean alongFirst = state.getValue(DirectionalGearshiftBlock.AXIS_ALONG_FIRST_COORDINATE);
            final Direction direction = state.getValue(DirectionalGearshiftBlock.FACING);
            final boolean leftOn = state.getValue(DirectionalGearshiftBlock.LEFT_POWERED);
            final boolean rightOn = state.getValue(DirectionalGearshiftBlock.RIGHT_POWERED);

            final boolean vertical = direction.getAxis()
                    .isHorizontal() && (direction.getAxis() == Direction.Axis.X) == alongFirst;
            final int xRot = direction == Direction.DOWN ? 270 : direction == Direction.UP ? 90 : 0;
            final int yRot = direction.getAxis()
                    .isVertical() ? alongFirst ? 0 : 90 : (int) direction.toYRot();

            builder.part()
                    .modelFile(model(provider, "middle", false, vertical))
                    .rotationY(yRot)
                    .rotationX(xRot)
                    .addModel()
                    .condition(DirectionalGearshiftBlock.FACING, direction)
                    .condition(DirectionalGearshiftBlock.AXIS_ALONG_FIRST_COORDINATE, alongFirst)
                    .end().part()

                    .modelFile(model(provider, "left", leftOn, vertical))
                    .rotationY(yRot)
                    .rotationX(xRot)
                    .addModel()
                    .condition(DirectionalGearshiftBlock.LEFT_POWERED, leftOn)
                    .condition(DirectionalGearshiftBlock.FACING, direction)
                    .condition(DirectionalGearshiftBlock.AXIS_ALONG_FIRST_COORDINATE, alongFirst)
                    .end().part()

                    .modelFile(model(provider, "right", rightOn, vertical))
                    .rotationY(yRot)
                    .rotationX(xRot)
                    .addModel()
                    .condition(DirectionalGearshiftBlock.RIGHT_POWERED, rightOn)
                    .condition(DirectionalGearshiftBlock.FACING, direction)
                    .condition(DirectionalGearshiftBlock.AXIS_ALONG_FIRST_COORDINATE, alongFirst)
                    .end().part();
        }
    }

    private static ModelFile model(final RegistrateBlockstateProvider p, final String part, final boolean powered, final boolean vertical) {
        return p.models().getExistingFile(Simulated.path("block/directional_gearshift/" + (vertical ? "vertical/" : "horizontal/") + part + (powered ? "_powered" : "")));
    }
}
