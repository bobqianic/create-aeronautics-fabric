package com.zurrtum.create.foundation.data;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import io.github.fabricators_of_create.porting_lib.models.generators.ModelFile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import java.util.function.Function;

public final class AssetLookup {
    private AssetLookup() {
    }

    public static <T extends Block> Function<BlockState, ModelFile> partialBaseModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov) {
        return state -> prov.models().getExistingFile(prov.modLoc("block/" + ctx.getName() + "/block"));
    }

    public static <T extends Block> Function<BlockState, ModelFile> forPowered(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov) {
        return state -> prov.models().getExistingFile(prov.modLoc("block/" + ctx.getName() + "/block" + (state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.POWERED) ? "_powered" : "")));
    }

    public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelProvider> itemModelWithPartials() {
        return (ctx, prov) -> { };
    }
}
