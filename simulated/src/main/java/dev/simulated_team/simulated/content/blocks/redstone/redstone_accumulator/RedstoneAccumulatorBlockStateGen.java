package dev.simulated_team.simulated.content.blocks.redstone.redstone_accumulator;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import dev.simulated_team.simulated.Simulated;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import io.github.fabricators_of_create.porting_lib.models.generators.ConfiguredModel;
import io.github.fabricators_of_create.porting_lib.models.generators.ModelFile;
import io.github.fabricators_of_create.porting_lib.models.generators.MultiPartBlockStateBuilder;

public class RedstoneAccumulatorBlockStateGen {
    public static ConfiguredModel.Builder<MultiPartBlockStateBuilder.PartBuilder> rotateHorizontal(final Direction direction, final ConfiguredModel.Builder<MultiPartBlockStateBuilder.PartBuilder> builder) {
        final int angleOffset = 0;
        builder.rotationY(((int) direction.toYRot() + angleOffset) % 360);
        return builder;
    }

    public static <P extends RedstoneAccumulatorBlock> NonNullBiConsumer<DataGenContext<Block, P>, RegistrateBlockstateProvider> generate() {
        return (ctx, prov) -> {
            final ModelFile backOff = sub(prov, "block_back_off");
            final ModelFile backOn = sub(prov, "block_back_on");
            final ModelFile front = sub(prov, "block_front");
            final ModelFile middleOff = sub(prov, "block_middle_off");
            final ModelFile middleOn = sub(prov, "block_middle_on");
            final ModelFile torchOff = sub(prov, "torch_off");
            final ModelFile torchOn = sub(prov, "torch_on");

            final MultiPartBlockStateBuilder builder = prov.getMultipartBuilder(ctx.get());

            for (final BlockState state : ctx.get().getStateDefinition().getPossibleStates()) {
                final Direction facing = state.getValue(RedstoneAccumulatorBlock.FACING);
                final boolean powered = state.getValue(RedstoneAccumulatorBlock.POWERED);
                final boolean sidePowered = state.getValue(RedstoneAccumulatorBlock.SIDE_POWERED);
                final boolean powering = state.getValue(RedstoneAccumulatorBlock.POWERING);

                final int yRot = ((int) facing.getOpposite().toYRot());

                builder.part()
                        .modelFile(front)
                        .rotationY(yRot)
                        .addModel()
                        .condition(RedstoneAccumulatorBlock.FACING, facing)
                        .end().part()

                        .modelFile(powered ? backOn : backOff)
                        .rotationY(yRot)
                        .addModel()
                        .condition(RedstoneAccumulatorBlock.POWERED, powered)
                        .condition(RedstoneAccumulatorBlock.FACING, facing)
                        .end().part()

                        .modelFile(sidePowered ? middleOn : middleOff)
                        .rotationY(yRot)
                        .addModel()
                        .condition(RedstoneAccumulatorBlock.SIDE_POWERED, sidePowered)
                        .condition(RedstoneAccumulatorBlock.FACING, facing)
                        .end().part()

                        .modelFile(powering ? torchOn : torchOff)
                        .rotationY(yRot)
                        .addModel()
                        .condition(RedstoneAccumulatorBlock.POWERING, powering)
                        .condition(RedstoneAccumulatorBlock.FACING, facing)
                        .end();
            }
        };
    }

    private static ModelFile sub(final RegistrateBlockstateProvider p, final String suffix) {
        return p.models().getExistingFile(Simulated.path("block/redstone_accumulator/" + suffix));
    }
}
