package dev.eriksonn.aeronautics.content.blocks.propeller.small.andesite;

import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.SimplePropellerRenderer;
import dev.eriksonn.aeronautics.index.AeroPartialModels;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

import static dev.eriksonn.aeronautics.content.blocks.propeller.small.BasePropellerBlock.REVERSED;

public class AndesitePropellerRenderer extends SimplePropellerRenderer<AndesitePropellerBlockEntity> {

    public AndesitePropellerRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public PartialModel getCurrentModel(final AndesitePropellerBlockEntity be) {
        return be.getBlockState().getValue(REVERSED) ? AeroPartialModels.ANDESITE_PROPELLER_REVERSED : AeroPartialModels.ANDESITE_PROPELLER;
    }

    @Override
    public float getAngle(final float partialTicks, final Direction dir, final AndesitePropellerBlockEntity be) {
        return super.getAngle(partialTicks, dir, be) + getRotationOffsetForPosition(be, be.getBlockPos(), dir.getAxis());
    }
}
