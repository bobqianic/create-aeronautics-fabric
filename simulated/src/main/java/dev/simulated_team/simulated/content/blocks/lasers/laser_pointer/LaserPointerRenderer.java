package dev.simulated_team.simulated.content.blocks.lasers.laser_pointer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.simulated_team.simulated.content.blocks.lasers.AbstractLaserRenderer;
import dev.simulated_team.simulated.content.blocks.lasers.LaserBehaviour;
import dev.simulated_team.simulated.index.SimPartialModels;
import dev.simulated_team.simulated.index.SimRenderTypes;
import dev.simulated_team.simulated.util.SimColors;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.Util;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.joml.Vector3d;
import org.joml.Vector4f;

import java.awt.*;

public class LaserPointerRenderer extends AbstractLaserRenderer<LaserPointerBlockEntity> {

    public LaserPointerRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(final LaserPointerBlockEntity blockEntity, final float partialTicks, final PoseStack pose, final MultiBufferSource buffer, final int light, final int overlay) {
        final SuperByteBuffer superBuffer;

        final Vector4f colors = this.getColors(blockEntity, partialTicks);
        final boolean isDarkerThanDark = colors.x == 0 && colors.y == 0 && colors.z == 0;
        if (blockEntity.shouldCast() && !isDarkerThanDark) {
            superBuffer = CachedBuffers.partial(SimPartialModels.LASER_POINTER_LENS_ON, blockEntity.getBlockState());
        } else {
            superBuffer = CachedBuffers.partial(SimPartialModels.LASER_POINTER_LENS_OFF, blockEntity.getBlockState());
        }
        superBuffer.translate(0.5, 0.5, 0.5);
        superBuffer.rotateToFace(blockEntity.getBlockState().getValue(LaserPointerBlock.FACING));
        superBuffer.translate(-0.5, -0.5, -0.5);
        if (blockEntity.shouldCast()) {
            superBuffer.light(LightTexture.FULL_BRIGHT);
        } else {
            superBuffer.light(light);
        }
        superBuffer.disableDiffuse();
        superBuffer.color((int) (colors.x * 255), (int) (colors.z * 255), (int) (colors.y * 255), 255);
        superBuffer.renderInto(pose.last(), buffer.getBuffer(SimRenderTypes.lens()));

        // only draw non-black lasers
        if (!isDarkerThanDark) {
            super.renderSafe(blockEntity, partialTicks, pose, buffer, light, overlay);
        }
    }

    @Override
    public float getLaserScale(final LaserBehaviour laser) {
        return 0.48f;
    }

    @Override
    public Vector4f getColors(final LaserPointerBlockEntity blockEntity, final float partialTicks) {
        Color c = new Color(blockEntity.laserColor);
        if (blockEntity.isRainbow()) {
            final Vector3d baseLCh = SimColors.LabToLCh(SimColors.toOklab(c));
            final float t;
            if (blockEntity.isVirtual()) {
                t = (float) ((Util.getMillis() % 5000) * 2 * Math.PI / 5000);
            } else {
                final long timeOff = blockEntity.getLevel().getGameTime();
                t = (float) (((timeOff) % 100 + partialTicks) * 2 * Math.PI / 100f);
            }
            c = SimColors.LChOklab(0.8f, 0.3f, (float) (t + baseLCh.z()));
        }
        // 25% opacity at strongest
        return new Vector4f(c.getRed() / 255f, c.getBlue() / 255f, c.getGreen() / 255f, blockEntity.getPower() / 60f);
    }
}
