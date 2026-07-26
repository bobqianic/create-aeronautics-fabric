package dev.simulated_team.simulated.content.blocks.nameplate;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.simulated_team.simulated.compat.create.SafeBlockEntityRenderer;
import dev.simulated_team.simulated.data.SimLang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class NameplateRenderer extends SafeBlockEntityRenderer<NameplateBlockEntity> {

    //taken from sign renderer
    private static final int OUTLINE_RENDER_DISTANCE = Mth.square(16);

    private final BlockEntityRendererProvider.Context context;
    public NameplateRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public void renderSafe(final NameplateBlockEntity be, final float pPartialTick, final PoseStack ps, final MultiBufferSource pBuffer, int packedLight, final int pPackedOverlay) {
        final Font font = Minecraft.getInstance().font;

        final BlockState state = be.getBlockState();
        final Direction facing = state.getValue(NameplateBlock.FACING);

        // can't just use be.isController() because it is never set properly on create contraptions
        final NameplateBlock.Position pos = state.getValue(NameplateBlock.POSITION);
        if (pos == NameplateBlock.Position.LEFT) {
            // the controllerWidth also isn't set properly, so this needs to be called
//            be.controllerCheckTick();
        } else if (pos != NameplateBlock.Position.SINGLE) {
            return;
        }

        ps.pushPose();

        ps.translate(0.5, 0.5, 0.5);
        ps.mulPose(Axis.YP.rotationDegrees(-facing.toYRot() + 180.0f));
        ps.translate(-0.5, -0.5, -0.5);

        ps.translate(1.0, 1.0, 1.0);

        // push 4 pixels out
        ps.translate(0.0, 0.0, -4.05 / 16.0);

        final int pixelsTall = be.glowing ? 5 : 6;
        final int pixelsLeft = 3;

        ps.translate(-pixelsLeft / 16.0f, -(16.0 - pixelsTall) / 16.0 / 2.0, 0.0);
        ps.scale((float) (pixelsTall / 16.0), (float) (pixelsTall / 16.0), (float) (pixelsTall / 16.0));

        ps.scale(1 / 7f, 1 / 7f, 1 / 7f);

        ps.mulPose(Axis.ZP.rotationDegrees(180.0f));

        final int availableSpace = ((be.getControllerWidth()) * 16 - pixelsLeft * 2) * 7 / pixelsTall + 1;
        final String trimmed = font.plainSubstrByWidth(be.getName(), availableSpace);

        final int width = font.width(trimmed);

        final double centerPixels = (availableSpace - 1) / 2.0 - width / 2.0;

        // translate to center
        ps.translate(centerPixels, 0.0, 0.0);

        final MutableComponent textComponent = SimLang.text(trimmed).component();
        final List<FormattedCharSequence> sequences = font.split(textComponent, width);

        final int textColor;
        final boolean glowing;
        if (be.glowing) {
            textColor = be.getTextColor().getTextColor();
            glowing = isOutlineVisible(be.getBlockPos(), textColor);
            packedLight = 15728880;
        } else {
            textColor = be.getDarkColor(be.getTextColor());
            glowing = false;
        }

        for (final FormattedCharSequence sequence : sequences) {
            if (glowing) {
                font.drawInBatch8xOutline(sequence, 0, 0, textColor, be.getDarkColor(be.getTextColor()), ps.last().pose(), pBuffer, packedLight);
            } else {
                font.drawInBatch(sequence, 0f /*x offset*/, 0f /*y offset*/, textColor, false, ps.last().pose(), pBuffer, Font.DisplayMode.NORMAL, 0x000000, packedLight);
            }
        }

        ps.popPose();
    }

    //taken from sign renderer
    private static boolean isOutlineVisible(final BlockPos blockPos, final int i) {
        if (i == DyeColor.BLACK.getTextColor()) {
            return true;
        } else {
            final Minecraft minecraft = Minecraft.getInstance();
            final LocalPlayer localPlayer = minecraft.player;
            if (localPlayer != null && minecraft.options.getCameraType().isFirstPerson() && localPlayer.isScoping()) {
                return true;
            } else {
                final Entity entity = minecraft.getCameraEntity();
                return entity != null && entity.distanceToSqr(Vec3.atCenterOf(blockPos)) < (double)OUTLINE_RENDER_DISTANCE;
            }
        }
    }
}
