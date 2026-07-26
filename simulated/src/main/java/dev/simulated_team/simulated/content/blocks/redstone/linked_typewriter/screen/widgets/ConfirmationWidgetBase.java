package dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.screen.widgets;

import com.zurrtum.create.client.catnip.gui.element.ScreenElement;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ConfirmationWidgetBase extends IconButton {
    public boolean confirmation;
    private Component message = Component.empty();

    public ConfirmationWidgetBase(final int x, final int y, final ScreenElement icon) {
        super(x, y, icon);
    }

    public ConfirmationWidgetBase withMessage(final Component component) {
        this.message = component;
        return this;
    }

    @Override
    public void doRender(final GuiGraphics graphics, final int mouseX, final int mouseY,
                         final float partialTicks) {
        super.doRender(graphics, mouseX, mouseY, partialTicks);
        if (this.isHovered && this.visible && this.active && this.confirmation) {
            graphics.setComponentTooltipForNextFrame(
                    Minecraft.getInstance().font,
                    List.of(this.message.copy().withColor(0xFF5555)),
                    mouseX,
                    mouseY
            );
        }
    }

    @Override
    public void onClick(final MouseButtonEvent click, final boolean doubled) {
        if (this.confirmation) {
            this.runCallback(click.x(), click.y());
            this.confirmation = false;
        } else {
            this.confirmation = true;
        }
    }

    public void cancelConfirmation() {
        this.confirmation = false;
    }
}
