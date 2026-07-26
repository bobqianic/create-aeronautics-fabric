package dev.simulated_team.simulated.content.blocks.steering_wheel;

import com.zurrtum.create.content.redstone.thresholdSwitch.ThresholdSwitchBlock;
import com.zurrtum.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import dev.simulated_team.simulated.Simulated;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import io.github.fabricators_of_create.porting_lib.models.generators.ConfiguredModel;
import io.github.fabricators_of_create.porting_lib.models.generators.ModelFile;

public class SteeringWheelGenerator extends SpecialBlockStateGen {
    @Override
    public <T extends Block> void generate(final DataGenContext<Block, T> ctx,
                                           final RegistrateBlockstateProvider prov) {
        prov.getVariantBuilder(ctx.getEntry()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(getModel(ctx, prov, state))
                .rotationX(Math.floorMod(getXRotation(state), 360))
                .rotationY(Math.floorMod(getYRotation(state), 360))
                .build());
    }

    @Override
    protected int getXRotation(final BlockState state) {
        return 0;
    }

    @Override
    protected int getYRotation(final BlockState state) {
        return this.horizontalAngle(state.getValue(ThresholdSwitchBlock.FACING)) + 180;
    }

    @Override
    public <T extends Block> ModelFile getModel(final DataGenContext<Block, T> ctx, final RegistrateBlockstateProvider prov, final BlockState state) {
        return prov.models().getExistingFile(Simulated.path(state.getValue(
                SteeringWheelBlock.ON_FLOOR) ? "block/steering_wheel/block" : "block/steering_wheel/block_up"
        ));
    }
}
