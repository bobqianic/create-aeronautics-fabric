package dev.simulated_team.simulated.fabric.service;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import dev.simulated_team.simulated.content.blocks.auger_shaft.AugerShaftBlock;
import dev.simulated_team.simulated.content.blocks.util.AbstractDirectionalAxisBlock;
import dev.simulated_team.simulated.data.SimBlockStateGen;
import dev.simulated_team.simulated.fabric.data.FabricAugerShaftGen;
import dev.simulated_team.simulated.service.SimBlockStateService;
import io.github.fabricators_of_create.porting_lib.models.generators.ConfiguredModel;
import io.github.fabricators_of_create.porting_lib.models.generators.ModelFile;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class FabricSimBlockStateService implements SimBlockStateService {
    @Override
    public <T extends Block> void genericModelBuilder(
            final DataGenContext<Block, T> context,
            final RegistrateBlockstateProvider provider,
            final Function<BlockState, SimBlockStateGen.XYHolder> rotationGetter,
            final Function<BlockState, Object> modelGetter) {
        provider.getVariantBuilder(context.getEntry()).forAllStates(state -> {
            final Object value = modelGetter.apply(state);
            if (!(value instanceof final ModelFile model)) {
                throw new IllegalArgumentException("Model getter must return a ModelFile");
            }
            final SimBlockStateGen.XYHolder rotation = rotationGetter.apply(state);
            return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationX(rotation.xRot())
                    .rotationY(rotation.yRot())
                    .build();
        });
    }

    @Override
    public <P extends AugerShaftBlock> NonNullBiConsumer<DataGenContext<Block, P>, RegistrateBlockstateProvider> augerShaftGenerate(
            final String name, final boolean cog) {
        return FabricAugerShaftGen.generate(name, cog);
    }

    @Override
    public <T extends AbstractDirectionalAxisBlock> void directionalAxisBlock(
            final DataGenContext<Block, T> context,
            final RegistrateBlockstateProvider provider,
            final BiFunction<BlockState, Boolean, Object> modelFunction) {
        provider.getVariantBuilder(context.getEntry()).forAllStates(state -> {
            final boolean alongFirst = state.getValue(AbstractDirectionalAxisBlock.AXIS_ALONG_FIRST_COORDINATE);
            final Direction direction = state.getValue(AbstractDirectionalAxisBlock.FACING);
            final boolean vertical = direction.getAxis().isHorizontal()
                    && (direction.getAxis() == Direction.Axis.X) == alongFirst;
            final int xRotation = direction == Direction.DOWN ? 270 : direction == Direction.UP ? 90 : 0;
            final int yRotation = direction.getAxis().isVertical() ? alongFirst ? 0 : 90 : (int) direction.toYRot();
            final Object value = modelFunction.apply(state, vertical);
            if (!(value instanceof final ModelFile model)) {
                throw new IllegalArgumentException("Model function must return a ModelFile");
            }
            return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationX(xRotation)
                    .rotationY(yRotation)
                    .build();
        });
    }
}
