package dev.simulated_team.simulated.content.blocks.nav_table;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.simulated_team.simulated.compat.create.SmartBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import dev.simulated_team.simulated.content.blocks.nav_table.navigation_target.RenderableNavigationTarget;
import dev.simulated_team.simulated.index.SimPartialModels;
import dev.simulated_team.simulated.index.SimTags;
import dev.simulated_team.simulated.util.SimColors;
import dev.simulated_team.simulated.util.SimDirectionUtil;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

public class NavTableRenderer extends SmartBlockEntityRenderer<NavTableBlockEntity> {
    public NavTableRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(final NavTableBlockEntity navBE, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer, final int light, final int overlay) {
        super.renderSafe(navBE, partialTicks, ms, buffer, light, overlay);

        final ItemStack heldItem = navBE.getHeldItem();
        final BlockState navState = navBE.getBlockState();
        final Direction facing = navState.getValue(NavTableBlock.FACING);

        // Begin pose stack manipulation
        TransformStack.of(ms)
                .pushPose().center()
                .rotate(facing.getRotation());

        // Render Redstone Indicators
        final float arrowAngle = (float) (navBE.getClientTargetAngle(partialTicks) - Math.PI / 2);
        if (isRenderingInSubLevel() || !VisualizationManager.supportsVisualization(navBE.getLevel())) {
            ms.pushPose();
            ms.translate(0, -0.5, 0);
            final Vector3f logicalDirectionF = new Vector3f();
            for (final Direction direction : SimDirectionUtil.Y_AXIS_PLANE) {
                facing.getRotation().transform(direction.getStepX(), direction.getStepY(), direction.getStepZ(), logicalDirectionF);
                final Direction logicalDirection = Direction.getNearest(Math.round(logicalDirectionF.x), Math.round(logicalDirectionF.y), Math.round(logicalDirectionF.z), Direction.NORTH);

                ms.pushPose();
                final SuperByteBuffer indicator = CachedBuffers.partial(SimPartialModels.NAV_TABLE_INDICATOR, navState);

                indicator.rotateToFace(direction);
                indicator.translate(0, 0, 0.5);
                final float signalStrength = navBE.isPowering ? Math.max(navBE.getRedstoneStrength(logicalDirection), 0) / 15.0F : 0;
                final int color = SimColors.redstone(signalStrength); // Analog indicators (mixes between colors smoothly)
                indicator.light(light)
                        .color(color)
                        .renderInto(ms.last(), buffer.getBuffer(RenderType.cutout()));

                ms.popPose();
            }
            ms.popPose();

            // Render Pointer
            ms.pushPose();
            ms.translate(0, 0.3, 0);
            final SuperByteBuffer pointer = CachedBuffers.partial(SimPartialModels.NAV_TABLE_POINTER, navState);

            pointer.rotateY(arrowAngle);
            pointer.light(light).renderInto(ms.last(), buffer.getBuffer(RenderType.cutout()));
            ms.popPose();
        }

        //keep item rendering outside of visual instances to allow for more flexability of custom renderers
        // Render Nav table's item
        ms.pushPose();
        ms.translate(0, 0.3, 0);
        final boolean blockItem = false;
        TransformStack.of(ms)
                .translate(0, blockItem ? 0.25f : 0.15f, 0)
                .rotate((float) Math.toRadians(90f), Direction.WEST)
                .scale(blockItem ? 0.5f : 0.375f);

        if (heldItem.getItem() instanceof final RenderableNavigationTarget rnti) {
            rnti.renderInNavTable(heldItem, navBE, navState, partialTicks, ms, buffer, light, overlay);
        } else {
            if (heldItem.is(SimTags.Items.ROTATE_WITH_NAV_ARROW))
                ms.mulPose(Axis.ZP.rotation(arrowAngle));
        }
        ms.popPose();

        ms.popPose();
    }
}
