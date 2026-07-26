package dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.screen.widgets;

import com.mojang.blaze3d.platform.InputConstants;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.client.catnip.gui.element.ScreenElement;
import com.zurrtum.create.client.catnip.gui.widget.AbstractSimiWidget;
import com.zurrtum.create.content.redstone.link.RedstoneLinkNetworkHandler;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterEntries;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.screen.LinkedTypewriterScreen;
import dev.simulated_team.simulated.index.SimGUITextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class KeyWidget extends AbstractSimiWidget {
    private final int keyCode;
    private final ScreenElement icon;
    private final LinkedTypewriterScreen screen;
    private boolean keyboardActive = true;

    public KeyWidget(final int x, final int y, final int width, final int keyCode,
                     final ScreenElement icon, final LinkedTypewriterScreen screen) {
        super(x, y, width, 14, Component.empty());
        this.keyCode = keyCode;
        this.icon = icon;
        this.screen = screen;
    }

    public void setKeyboardActive(final boolean keyboardActive) {
        this.keyboardActive = keyboardActive;
        this.active = keyboardActive;
    }

    @Override
    protected void renderWidget(final GuiGraphics graphics, final int mouseX, final int mouseY,
                                final float partialTicks) {
        final boolean bound = this.screen.getNewEntries().getKeyMap().containsKey(this.keyCode);
        final SimGUITextures start = bound ? SimGUITextures.KEY_START : SimGUITextures.INACTIVE_KEY_START;
        final SimGUITextures middle = bound ? SimGUITextures.KEY_MIDDLE : SimGUITextures.INACTIVE_KEY_MIDDLE;
        final SimGUITextures end = bound ? SimGUITextures.KEY_END : SimGUITextures.INACTIVE_KEY_END;
        final int y = this.getY() + (this.isHovered && this.keyboardActive ? 2 : 0);

        start.render(graphics, this.getX(), y);
        for (int x = start.width; x < this.width - end.width; x += middle.width) {
            middle.render(graphics, this.getX() + x, y);
        }
        end.render(graphics, this.getX() + this.width - end.width, y);

        if (this.icon != null) {
            this.icon.render(graphics, this.getX() + 3, y + 1);
        }

        if (this.isHovered && this.keyboardActive) {
            this.renderHover(graphics);
        }
    }

    private void renderHover(final GuiGraphics graphics) {
        LinkedTypewriterEntries.KeyboardEntry entry = this.screen.getNewEntries().getEntry(this.keyCode);
        if (entry == null) {
            entry = new LinkedTypewriterEntries.KeyboardEntry(
                    RedstoneLinkNetworkHandler.Frequency.EMPTY,
                    RedstoneLinkNetworkHandler.Frequency.EMPTY,
                    this.keyCode,
                    BlockPos.ZERO
            );
        }

        final Font font = Minecraft.getInstance().font;
        final SimGUITextures arrow = SimGUITextures.LINKED_TYPEWRITER_TOOLTIP_ARROW;
        final SimGUITextures frequency = SimGUITextures.LINKED_TYPEWRITER_FREQUENCY;
        final Component keyName = this.keyName();
        final int textWidth = font.width(keyName);
        final int backgroundWidth = Math.max(arrow.width + 24, textWidth + 8);
        final int backgroundHeight = font.lineHeight + 32;
        final int backgroundX = this.getX() + (this.width - backgroundWidth) / 2;
        final int backgroundY = this.getY() - backgroundHeight - 8;
        final int frequencyX = this.getX() + (this.width - frequency.width) / 2;

        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                SimGUITextures.LINKED_TYPEWRITER_TOOLTIP_BACKGROUND.location,
                backgroundX,
                backgroundY,
                backgroundWidth,
                backgroundHeight
        );
        arrow.render(
                graphics,
                this.getX() + (this.width - arrow.width) / 2,
                this.getY() - 10
        );
        frequency.render(graphics, frequencyX, backgroundY + 4);

        final Couple<RedstoneLinkNetworkHandler.Frequency> frequencies = entry.getAsCouple();
        graphics.renderItem(frequencies.getFirst().getStack(), frequencyX + 1, backgroundY + 5);
        graphics.renderItem(frequencies.getSecond().getStack(), frequencyX + 19, backgroundY + 5);
        graphics.drawString(
                font,
                keyName,
                this.getX() + (this.width - textWidth) / 2,
                backgroundY + backgroundHeight - font.lineHeight - 4,
                0xFF241412,
                false
        );
    }

    private Component keyName() {
        return InputConstants.Type.KEYSYM.getOrCreate(this.keyCode).getDisplayName();
    }
}
