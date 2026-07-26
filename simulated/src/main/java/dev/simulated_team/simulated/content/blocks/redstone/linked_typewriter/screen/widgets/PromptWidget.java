package dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.screen.widgets;

import com.mojang.blaze3d.platform.InputConstants;
import com.zurrtum.create.client.catnip.gui.widget.AbstractSimiWidget;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.screen.EntryModifierScreen;
import dev.simulated_team.simulated.data.SimLang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class PromptWidget extends AbstractSimiWidget {
    private final EntryModifierScreen modifier;
    private boolean bindingActive;

    public PromptWidget(final EntryModifierScreen modifier, final int x, final int y,
                        final int width, final int height) {
        super(x, y, width, height);
        this.modifier = modifier;
    }

    @Override
    protected void doRender(final GuiGraphics graphics, final int mouseX, final int mouseY,
                            final float partialTicks) {
        if (!this.modifier.isModifying() || this.modifier.getPendingEntry() == null) {
            return;
        }

        final int keyCode = this.modifier.getPendingEntry().getKeyCode();
        Component displayName = keyCode == -1
                ? Component.empty()
                : InputConstants.Type.KEYSYM.getOrCreate(keyCode).getDisplayName();
        if (this.bindingActive) {
            displayName = SimLang.translate("linked_typewriter.bind_screen_prompt").component();
        } else if (this.modifier.getPendingEntry().getKeyCode() == -1) {
            displayName = SimLang.translate("linked_typewriter.bind_new_key").component();
        }

        graphics.drawString(
                Minecraft.getInstance().font,
                displayName,
                this.getX() + 3,
                this.getY() + 4,
                0xFFFFFFFF,
                true
        );
    }

    @Override
    public void onClick(final MouseButtonEvent click, final boolean doubled) {
        this.bindingActive = !this.bindingActive;
    }

    @Override
    public boolean keyPressed(final KeyEvent input) {
        if (this.bindingActive && this.modifier.getPendingEntry() != null) {
            this.modifier.getPendingEntry().keyCode(input.key());
            this.bindingActive = false;
            return true;
        }

        this.bindingActive = false;
        return super.keyPressed(input);
    }

    public void stopBinding() {
        this.bindingActive = false;
    }
}
