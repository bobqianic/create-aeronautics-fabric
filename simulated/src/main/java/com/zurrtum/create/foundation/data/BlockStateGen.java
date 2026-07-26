package com.zurrtum.create.foundation.data;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import io.github.fabricators_of_create.porting_lib.models.generators.ConfiguredModel;
import io.github.fabricators_of_create.porting_lib.models.generators.ModelFile;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class BlockStateGen {
    private BlockStateGen() {
    }

    public record XYHolder(int xRot, int yRot) {
    }

    public static <T extends Block> void directionalAxisBlock(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
                                                               BiFunction<BlockState, Boolean, ModelFile> models) {
    }

    public static <T extends Block> void axisBlock(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
                                                   Function<BlockState, ModelFile> models) {
    }

    public static <T extends Block> void simpleBlock(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
                                                      Function<BlockState, ModelFile> models) {
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> directionalBlockProvider(boolean ignored) {
        return (ctx, prov) -> { };
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> horizontalAxisBlockProvider(boolean customItem) {
        return (ctx, prov) -> {
            final Function<BlockState, ModelFile> model = customItem
                    ? AssetLookup.partialBaseModel(ctx, prov)
                    : state -> prov.models().getExistingFile(prov.modLoc("block/" + ctx.getName()));

            prov.getVariantBuilder(ctx.getEntry()).forAllStates(state -> ConfiguredModel.builder()
                    .modelFile(model.apply(state))
                    .rotationY(state.getValue(BlockStateProperties.HORIZONTAL_AXIS) == Direction.Axis.X ? 90 : 0)
                    .build());
        };
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> directionalAxisBlockProvider() {
        return (ctx, prov) -> { };
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> horizontalBlockProvider(boolean ignored) {
        return (ctx, prov) -> { };
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> generate() {
        return (ctx, prov) -> { };
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> coloredBlockItemModel() {
        return (ctx, prov) -> { };
    }
}
