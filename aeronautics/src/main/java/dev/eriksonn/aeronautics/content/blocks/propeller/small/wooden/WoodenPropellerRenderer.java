package dev.eriksonn.aeronautics.content.blocks.propeller.small.wooden;

import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.SimplePropellerRenderer;
import dev.eriksonn.aeronautics.index.AeroPartialModels;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

import static dev.eriksonn.aeronautics.content.blocks.propeller.small.BasePropellerBlock.REVERSED;

public class WoodenPropellerRenderer extends SimplePropellerRenderer<WoodenPropellerBlockEntity> {

    public WoodenPropellerRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public PartialModel getCurrentModel(final WoodenPropellerBlockEntity be) {
        return be.getBlockState().getValue(REVERSED) ? AeroPartialModels.WOODEN_PROPELLER_REVERSED : AeroPartialModels.WOODEN_PROPELLER;
    }

    @Override
    public float getAngle(final float partialTicks, final Direction dir, final WoodenPropellerBlockEntity be) {
        return super.getAngle(partialTicks, dir, be) + getRotationOffsetForPosition(be, be.getBlockPos(), dir.getAxis());
    }
}
