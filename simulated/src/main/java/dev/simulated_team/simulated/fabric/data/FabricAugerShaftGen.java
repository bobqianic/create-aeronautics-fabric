package dev.simulated_team.simulated.fabric.data;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.blocks.auger_shaft.AugerShaftBlock;
import io.github.fabricators_of_create.porting_lib.models.generators.ConfiguredModel;
import io.github.fabricators_of_create.porting_lib.models.generators.ModelFile;
import io.github.fabricators_of_create.porting_lib.models.generators.MultiPartBlockStateBuilder;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;

public final class FabricAugerShaftGen {
    private FabricAugerShaftGen() {
    }

    private static ConfiguredModel.Builder<MultiPartBlockStateBuilder.PartBuilder> rotate(
            final Direction direction,
            final ConfiguredModel.Builder<MultiPartBlockStateBuilder.PartBuilder> builder) {
        builder.rotationX(direction.getAxis().isHorizontal() ? -90 : direction == Direction.UP ? 180 : 0);
        builder.rotationY(direction.getAxis().isVertical() ? 0 : ((int) direction.toYRot() + 180) % 360);
        return builder;
    }

    public static <P extends AugerShaftBlock> NonNullBiConsumer<DataGenContext<Block, P>, RegistrateBlockstateProvider> generate(
            final String name, final boolean cog) {
        return (context, provider) -> {
            final ModelFile axis = sub(provider, cog ? "cog_axis_y" : "axis_y");
            final ModelFile connection = sub(provider, "connection_top");
            final MultiPartBlockStateBuilder builder = provider.getMultipartBuilder(context.get());

            for (final Direction.Axis axisDirection : Direction.Axis.values()) {
                rotate(Direction.get(Direction.AxisDirection.POSITIVE, axisDirection), builder.part().modelFile(axis))
                        .addModel()
                        .condition(AugerShaftBlock.AXIS, axisDirection)
                        .condition(AugerShaftBlock.ENCASED, false)
                        .end();

                rotate(Direction.get(Direction.AxisDirection.POSITIVE, axisDirection),
                        builder.part().modelFile(sub(provider, "axis_y_encased")))
                        .addModel()
                        .condition(AugerShaftBlock.AXIS, axisDirection)
                        .condition(AugerShaftBlock.ENCASED, true)
                        .end();

                rotate(Direction.get(Direction.AxisDirection.POSITIVE, axisDirection), builder.part().modelFile(connection))
                        .addModel()
                        .condition(AugerShaftBlock.AXIS, axisDirection)
                        .condition(AugerShaftBlock.SECTION, AugerShaftBlock.BarrelSection.END, AugerShaftBlock.BarrelSection.SINGLE)
                        .condition(AugerShaftBlock.ENCASED, false)
                        .end();

                rotate(Direction.get(Direction.AxisDirection.NEGATIVE, axisDirection), builder.part().modelFile(connection))
                        .addModel()
                        .condition(AugerShaftBlock.AXIS, axisDirection)
                        .condition(AugerShaftBlock.SECTION, AugerShaftBlock.BarrelSection.FRONT, AugerShaftBlock.BarrelSection.SINGLE)
                        .condition(AugerShaftBlock.ENCASED, false)
                        .end();
            }

            if (!cog) {
                for (final Direction direction : Direction.values()) {
                    rotate(direction.getOpposite(), builder.part().modelFile(
                            sub(provider, "bracket_top_" + direction.getAxis().getName())))
                            .addModel()
                            .condition(AugerShaftBlock.PROPERTY_BY_DIRECTION.get(direction), true)
                            .condition(AugerShaftBlock.ENCASED, false)
                            .end();
                }
            }
        };
    }

    private static ModelFile sub(final RegistrateBlockstateProvider provider, final String suffix) {
        return provider.models().getExistingFile(Simulated.path("block/auger_shaft/" + suffix));
    }
}
