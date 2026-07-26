package com.zurrtum.create.foundation.data;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import io.github.fabricators_of_create.porting_lib.models.generators.ModelFile;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public abstract class SpecialBlockStateGen {
    protected int horizontalAngle(Direction direction) { return (int) direction.toYRot(); }
    protected abstract int getXRotation(BlockState state);
    protected abstract int getYRotation(BlockState state);
    public abstract <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov, BlockState state);
    public <T extends Block> void generate(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov) {
    }
}
