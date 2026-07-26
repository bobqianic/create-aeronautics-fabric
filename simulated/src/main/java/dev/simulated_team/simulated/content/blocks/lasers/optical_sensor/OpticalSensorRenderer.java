package dev.simulated_team.simulated.content.blocks.lasers.optical_sensor;

import dev.simulated_team.simulated.content.blocks.lasers.AbstractLaserRenderer;
import dev.simulated_team.simulated.content.blocks.lasers.LaserBehaviour;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.HitResult;
import org.joml.Vector4f;

public class OpticalSensorRenderer extends AbstractLaserRenderer<OpticalSensorBlockEntity> {
    public OpticalSensorRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public Vector4f getColors(final OpticalSensorBlockEntity blockEntity, final float partialTicks) {
        final Vector4f laserColor = new Vector4f(0.75f, 0.15f, 0.15f, 0.4f * blockEntity.getOpacity());

        if (blockEntity.getBlockState().getValue(OpticalSensorBlock.POWERED))
            laserColor.set(0.0f, 0.05f, 0.8f, 0.4f * blockEntity.getOpacity());

        return laserColor;
    }

    @Override
    public float getLaserScale(final LaserBehaviour laser) {
        return 0.378f;
    }

    @Override
    public HitResult getRenderedHitResult(final LaserBehaviour laser) {
        return laser.getBlockHitResult();
    }
}
