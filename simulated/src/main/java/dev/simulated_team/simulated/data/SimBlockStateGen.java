package dev.simulated_team.simulated.data;

import com.zurrtum.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.zurrtum.create.content.redstone.DirectedDirectionalBlock;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import dev.simulated_team.simulated.content.blocks.redstone.redstone_inductor.RedstoneInductorBlock;
import dev.simulated_team.simulated.content.blocks.symmetric_sail.SymmetricSailBlock;
import dev.simulated_team.simulated.content.blocks.util.AbstractDirectionalAxisBlock;
import dev.simulated_team.simulated.service.SimBlockStateService;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class SimBlockStateGen {

    public static <T extends DirectionalAxisKineticBlock> void directionalKineticAxisBlockstate(final DataGenContext<Block, T> ctx, final RegistrateBlockstateProvider prov) {
        SimBlockStateService.INSTANCE.genericModelBuilder(
                ctx,
                prov,
                SimBlockStateGen::directionalKineticAxisRotation,
                blockState -> prov.models().getExistingFile(prov.modLoc(
                        "block/" + ctx.getName() + "/block_"
                                + (usesVerticalModel(blockState) ? "vertical" : "horizontal"))));
    }

    public static <T extends Block> void facingPoweredAxisBlockstate(final DataGenContext<Block, T> ctx, final RegistrateBlockstateProvider prov) {
        prov.directionalBlock(ctx.getEntry(),
                blockState -> prov.models().getExistingFile(
                        prov.modLoc("block/" + ctx.getName() + "/block" + (blockState.getValue(BlockStateProperties.POWERED) ? "_powered" : ""))
                )
        );
    }

    public static <T extends Block> void facingBlockstate(final DataGenContext<Block, T> ctx, final RegistrateBlockstateProvider prov, final String modelPath) {
        prov.directionalBlock(ctx.getEntry(),
                blockState -> prov.models().getExistingFile(
                        prov.modLoc(modelPath)
                )
        );
    }

    public static <T extends Block> void horizontalFacingLitBlockstate(final DataGenContext<Block, T> ctx, final RegistrateBlockstateProvider prov) {
        prov.horizontalBlock(ctx.get(), blockState -> prov.models()
                .getExistingFile(prov.modLoc("block/" + ctx.getName() + "/block" + (blockState.getValue(AbstractFurnaceBlock.LIT) ? "_lit" : ""))));
    }

    public static <T extends Block> void redstoneInductorBlockstate(final DataGenContext<Block, T> ctx, final RegistrateBlockstateProvider prov) {
        prov.horizontalBlock(ctx.getEntry(),
                blockState -> {
                    final boolean inverted = blockState.getValue(RedstoneInductorBlock.INVERTED);

                    return prov.models().getExistingFile(
                            prov.modLoc("block/" + ctx.getName() + "/block" + ((inverted ? "_inverted" : "")) + (blockState.getValue(BlockStateProperties.POWERED) ? "_powered" : "")));
                });
    }

    public static <T extends DirectionalAxisKineticBlock> void directionalPoweredAxisBlockstate(final DataGenContext<Block, T> ctx, final RegistrateBlockstateProvider prov) {
        SimBlockStateService.INSTANCE.genericModelBuilder(
                ctx,
                prov,
                SimBlockStateGen::directionalKineticAxisRotation,
                blockState -> prov.models().getExistingFile(prov.modLoc(
                        "block/" + ctx.getName() + "/block_"
                                + (usesVerticalModel(blockState) ? "vertical" : "horizontal")
                                + (blockState.getValue(BlockStateProperties.POWERED) ? "_powered" : ""))));
    }

    private static boolean usesVerticalModel(final BlockState state) {
        final boolean alongFirst = state.getValue(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE);
        final Direction direction = state.getValue(DirectionalAxisKineticBlock.FACING);
        return direction.getAxis().isHorizontal()
                && (direction.getAxis() == Direction.Axis.X) == alongFirst;
    }

    private static XYHolder directionalKineticAxisRotation(final BlockState state) {
        final boolean alongFirst = state.getValue(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE);
        final Direction direction = state.getValue(DirectionalAxisKineticBlock.FACING);
        final int xRot = direction == Direction.DOWN ? 270 : direction == Direction.UP ? 90 : 0;
        final int yRot = direction.getAxis().isVertical() ? (alongFirst ? 0 : 90) : (int) direction.toYRot();
        return new XYHolder(xRot, yRot);
    }

    public static <I extends BlockItem> NonNullBiConsumer<DataGenContext<Item, I>, RegistrateItemModelProvider> coloredBlockItemModel(final String texture, final String... folders) {
        return (c, p) -> {
            String path = "block";
            for (final String folder : folders)
                path += "/" + ("_".equals(folder) ? c.getName() : folder);

            p.withExistingParent(c.getName(), p.modLoc(path)).texture("0", p.modLoc("block/" + texture));
        };
    }

    public static <T extends AbstractDirectionalAxisBlock> void directionalAxisBlock(final DataGenContext<Block, T> ctx, final RegistrateBlockstateProvider prov) {
        SimBlockStateService.INSTANCE.directionalAxisBlock(ctx, prov, (blockState, vertical) -> prov.models()
                .getExistingFile(prov.modLoc("block/" + ctx.getName() + "/block_" + (vertical ? "vertical" : "horizontal"))));
    }

    public static XYHolder xySymmetricSail(final BlockState state) {
        final Direction.Axis axis = state.getValue(SymmetricSailBlock.AXIS);
        return new XYHolder(axis == Direction.Axis.Y ? 0 : 90, axis == Direction.Axis.X ? 90 : axis == Direction.Axis.Z ? 180 : 0);
    }

    public static XYHolder xyAltitudeSensor(final BlockState state) {
        final int yRot = ((int) state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot() + 180) % 360;
        final int xRot = state.getValue(BlockStateProperties.ATTACH_FACE).ordinal() * 90;

        return new XYHolder(xRot, yRot);
    }

    public static <I extends BlockItem, P> NonNullFunction<ItemBuilder<I, P>, P> customItemModel(final ResourceLocation path) {
        return b -> b.model(SimBlockStateGen.customBlockItemModel(path))
                .build();
    }

    /**
     * Generate item model inheriting from a seperate model in
     * models/block/folders[0]/folders[1]/.../item.json "_" will be replaced by the
     * item name
     */
    public static <I extends BlockItem> NonNullBiConsumer<DataGenContext<Item, I>, RegistrateItemModelProvider> customBlockItemModel(
            final ResourceLocation path) {
        return (c, p) -> {
            p.withExistingParent(c.getName(), path);
        };
    }

    public static XYHolder xyLaser(final BlockState state) {
        final Direction dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

        final int yRot = (int) ((dir.getAxis().isVertical() ? 0 : dir.toYRot()) + 180);
        final int xRot = switch (state.getValue(DirectedDirectionalBlock.TARGET)) {
            case CEILING -> -90;
            case WALL -> 0;
            case FLOOR -> 90;
        };

        return new SimBlockStateGen.XYHolder((xRot + 360) % 360, (yRot + 360) % 360);
    }

    public record XYHolder(int xRot, int yRot) {
    }

}
