package dev.simulated_team.simulated.content.entities.diagram.screen;

import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import dev.simulated_team.simulated.index.SimGUITextures;
import dev.simulated_team.simulated.index.SimSoundEvents;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import com.zurrtum.create.catnip.theme.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3x2fStack;

import javax.annotation.Nullable;
import java.util.List;

public class DiagramForceGroupToggle extends AbstractWidget {
    private final ResourceLocation groupId;
    private final ForceGroup group;
    private final DiagramScreen diagramScreen;
    private int forceCount;

    public DiagramForceGroupToggle(final DiagramScreen diagramScreen, final ForceGroup forceGroup, final int x, final int y) {
        super(x, y, 90, 10, forceGroup.name());

        this.groupId = ForceGroups.REGISTRY.getKey(forceGroup);
        this.diagramScreen = diagramScreen;
        this.group = forceGroup;
    }

    private boolean isEnabled() {
        return this.diagramScreen.config.enabledForceGroups().contains(this.groupId);
    }

    private void toggleActive() {
        if (this.isEnabled()) {
            this.diagramScreen.config.enabledForceGroups().remove(this.groupId);
        } else {
            this.diagramScreen.config.enabledForceGroups().add(this.groupId);
        }
        this.diagramScreen.setConfigDirty();
    }

    public void updateForceState(@Nullable final DiagramDataPacket serverData) {
        if (serverData == null) {
            this.forceCount = 0;
            return;
        }
        final List<QueuedForceGroup.PointForce> forces = serverData.forces().get(this.group);
        this.forceCount = forces != null ? forces.size() : 0;
    }

    @Override
    public void onClick(final MouseButtonEvent event, final boolean doubleClick) {
        super.onClick(event, doubleClick);
        this.toggleActive();
    }

    public void renderTab(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTicks) {
        final boolean isEnabled = this.isEnabled();

        final int groupColor = this.isEnabled() ? (255 << 24) | this.group.color() : 0xffaaaaaa;
        final Matrix3x2fStack ps = guiGraphics.pose();

        ps.pushMatrix();
        final float paperOffset = this.diagramScreen.getPaperOffset(partialTicks);
        ps.translate(DiagramScreen.MAX_PAPER_OFFSET - paperOffset, 1);

        float tabHide = 1.0f - this.diagramScreen.getTabOffset(partialTicks);
        tabHide *= 9;

        if (!isEnabled)
            tabHide = Math.max(tabHide, 3);

        ps.translate(tabHide, 0);
        SimGUITextures.DIAGRAM_TAB.render(guiGraphics, this.getX() - 1, this.getY() - 1, new Color(groupColor));
        ps.popMatrix();
    }

    @Override
    protected void renderWidget(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTicks) {
        final Font font = Minecraft.getInstance().font;
        final boolean isEnabled = this.isEnabled();
        final int groupColor = (255 << 24) | this.group.color();

        final Matrix3x2fStack ps = guiGraphics.pose();

        ps.pushMatrix();
        final float paperOffset = this.diagramScreen.getPaperOffset(partialTicks);
        ps.translate(DiagramScreen.MAX_PAPER_OFFSET - paperOffset, 1);

        // NO Z SCALE!!! STRIKETHROUGH MY BEHATED
        ps.translate(this.getX() + 18, this.getY() + 1);
        ps.scale(0.75F, 0.75F);

        final MutableComponent name = MutableComponent.create(this.group.name().getContents());

        if (isEnabled) {
            guiGraphics.drawString(font, name, 1, 1, 0xffe2d9c3, false);
            guiGraphics.drawString(font, name, 0, 0, groupColor, false);
        } else {
            name.withStyle(ChatFormatting.STRIKETHROUGH);
            guiGraphics.drawString(font, name, 0, 0, 0xaaaaaaaa, false);
        }

        if (this.forceCount > 0) {
            final String forceCountText = String.valueOf(this.forceCount);
            final int x = 95 - font.width(forceCountText);

            if (isEnabled) {
                guiGraphics.drawString(font, forceCountText, x + 1, 1, 0xffe2d9c3, false);
                guiGraphics.drawString(font, forceCountText, x, 0, groupColor, false);
            } else {
                guiGraphics.drawString(font, forceCountText, x, 0, 0xaaaaaaaa, false);
            }
        }

        ps.popMatrix();
    }

    @Override
    public void playDownSound(final SoundManager handler) {
        if (this.isEnabled()) {
            handler.play(SimpleSoundInstance.forUI(SimSoundEvents.DIAGRAM_ERASE.event(), 1.0F));
        } else {
            handler.play(SimpleSoundInstance.forUI(SimSoundEvents.DIAGRAM_CHECKMARK.event(), 1.0F));
        }
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.getMessage());
    }

}
