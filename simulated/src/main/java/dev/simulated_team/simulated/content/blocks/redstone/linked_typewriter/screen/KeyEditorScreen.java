package dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterEntries;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.screen.widgets.ConfirmationWidgetBase;
import dev.simulated_team.simulated.index.SimGUITextures;
import dev.simulated_team.simulated.index.SimIcons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class KeyEditorScreen {
    private static final SimGUITextures KEY_MENU = SimGUITextures.LINKED_TYPEWRITER_KEYS_MENU;
    private static final SimGUITextures KEY_ENTRY = SimGUITextures.LINKED_TYPEWRITER_KEY_ENTRY;
    private static final int ENTRY_SPACING = KEY_ENTRY.height + 3;

    private final LinkedTypewriterScreen parent;
    private final List<KeyEntryWidget> entries = new ArrayList<>();
    private final IconButton addButton;
    private final IconButton confirmButton;
    private final ConfirmationWidgetBase removeAllButton;

    private boolean active;
    private int scroll;

    public KeyEditorScreen(final LinkedTypewriterScreen parent) {
        this.parent = parent;
        this.addButton = new IconButton(0, 0, AllIcons.I_ADD)
                .withCallback(() -> this.modifyEntry(null));
        this.confirmButton = new IconButton(0, 0, AllIcons.I_CONFIRM)
                .withCallback(() -> this.parent.switchScreen(false));
        this.removeAllButton = new ConfirmationWidgetBase(0, 0, AllIcons.I_TRASH)
                .withMessage(Component.translatable("simulated.linked_typewriter.confirm_delete_all"))
                .withCallback(() -> {
                    this.parent.sendNewKeys(true);
                    this.rebuildEntries();
                });
    }

    public void startEditing() {
        this.active = true;
        this.resetPositions();
        this.parent.addTypewriterWidget(this.addButton);
        this.parent.addTypewriterWidget(this.confirmButton);
        this.parent.addTypewriterWidget(this.removeAllButton);
        this.rebuildEntries();
    }

    public void endEditing() {
        this.parent.removeTypewriterWidget(this.addButton);
        this.parent.removeTypewriterWidget(this.confirmButton);
        this.parent.removeTypewriterWidget(this.removeAllButton);
        for (final KeyEntryWidget entry : this.entries) {
            entry.removeWidgets();
        }
        this.entries.clear();
        this.active = false;
        this.scroll = 0;
    }

    public void resetPositions() {
        final int buttonY = this.topPos() + KEY_MENU.height - 24;
        this.addButton.setX(this.leftPos() + KEY_MENU.width - 54);
        this.addButton.setY(buttonY);
        this.confirmButton.setX(this.leftPos() + KEY_MENU.width - 25);
        this.confirmButton.setY(buttonY);
        this.removeAllButton.setX(this.leftPos() + 8);
        this.removeAllButton.setY(buttonY);
    }

    public void shiftEntries(final boolean upward) {
        this.scroll += (upward ? -1 : 1) * 19;
        this.clampScroll();
    }

    private void clampScroll() {
        final int maxScroll = Math.max(0, (this.entries.size() - 4) * ENTRY_SPACING);
        this.scroll = Math.clamp(this.scroll, 0, maxScroll);
    }

    public void renderBackground(final GuiGraphics graphics, final float partialTick) {
        KEY_MENU.render(graphics, this.leftPos(), this.topPos());
        final Component title = Component.translatable(
                "simulated.linked_typewriter.bind_screen_title");
        graphics.drawString(
                Minecraft.getInstance().font,
                title,
                this.leftPos() + (KEY_MENU.width - Minecraft.getInstance().font.width(title)) / 2,
                this.topPos() + 4,
                0xFF592424,
                false
        );

        final int clipTop = this.topPos() + 20;
        final int clipBottom = this.topPos() + KEY_MENU.height - 35;
        graphics.enableScissor(this.leftPos() + 7, clipTop, this.leftPos() + 231, clipBottom);
        for (int index = 0; index < this.entries.size(); index++) {
            this.entries.get(index).renderBackground(graphics, index);
        }
        graphics.disableScissor();

        graphics.fillGradient(
                this.leftPos() + 7, clipTop,
                this.leftPos() + 231, clipTop + 10,
                0x77000000, 0x00000000
        );
        graphics.fillGradient(
                this.leftPos() + 7, clipBottom - 10,
                this.leftPos() + 231, clipBottom,
                0x00000000, 0x77000000
        );
    }

    private void rebuildEntries() {
        for (final KeyEntryWidget entry : this.entries) {
            entry.removeWidgets();
        }
        this.entries.clear();

        for (final LinkedTypewriterEntries.KeyboardEntry entry
                : this.parent.getNewEntries().getEntries()) {
            final KeyEntryWidget widget = new KeyEntryWidget(entry);
            this.entries.add(widget);
            if (this.active) {
                widget.addWidgets();
            }
        }
        this.clampScroll();
    }

    private void removeEntry(final KeyEntryWidget widget) {
        this.parent.getNewEntries().setKey(widget.entry.glfwKeyCode, null);
        this.rebuildEntries();
    }

    private void modifyEntry(@Nullable final KeyEntryWidget widget) {
        this.setWidgetsActive(false);
        this.parent.modifier.startModifying(widget == null ? null : widget.entry, newEntry -> {
            if (newEntry != null) {
                this.parent.getNewEntries().setKey(newEntry.glfwKeyCode, newEntry);
            }
            this.rebuildEntries();
            this.setWidgetsActive(true);
        });
    }

    private void setWidgetsActive(final boolean widgetsActive) {
        this.addButton.active = widgetsActive;
        this.addButton.visible = widgetsActive;
        this.confirmButton.active = widgetsActive;
        this.confirmButton.visible = widgetsActive;
        this.removeAllButton.active = widgetsActive;
        this.removeAllButton.visible = widgetsActive;
        for (final KeyEntryWidget entry : this.entries) {
            entry.setActive(widgetsActive);
        }
    }

    public boolean isActive() {
        return this.active;
    }

    private int leftPos() {
        return this.parent.getLeftPos();
    }

    private int topPos() {
        return this.parent.getTopPos() - 40;
    }

    private class KeyEntryWidget {
        private final IconButton editButton;
        private final IconButton deleteButton;
        private final LinkedTypewriterEntries.KeyboardEntry entry;

        private KeyEntryWidget(final LinkedTypewriterEntries.KeyboardEntry entry) {
            this.entry = entry;
            this.editButton = new IconButton(0, 0, SimIcons.ADD_OR_EDIT)
                    .withCallback(() -> KeyEditorScreen.this.modifyEntry(this));
            this.deleteButton = new IconButton(0, 0, AllIcons.I_TRASH)
                    .withCallback(() -> KeyEditorScreen.this.removeEntry(this));
        }

        private int currentY(final int index) {
            return KeyEditorScreen.this.topPos() + 25
                    - KeyEditorScreen.this.scroll
                    + index * ENTRY_SPACING;
        }

        private void renderBackground(final GuiGraphics graphics, final int index) {
            final int x = KeyEditorScreen.this.leftPos() + 12;
            final int y = this.currentY(index);
            KEY_ENTRY.render(graphics, x, y);

            final Component keyName = InputConstants.Type.KEYSYM
                    .getOrCreate(this.entry.glfwKeyCode)
                    .getDisplayName();
            graphics.drawString(
                    Minecraft.getInstance().font,
                    keyName,
                    x + 9,
                    y + 11,
                    0xFFFFFFFF,
                    true
            );
            graphics.renderItem(this.entry.getFirstAsItemStack(), x + 82, y + 7);
            graphics.renderItem(this.entry.getSecondAsItemStack(), x + 100, y + 7);

            this.editButton.setX(x + 167);
            this.editButton.setY(y + 6);
            this.deleteButton.setX(x + 190);
            this.deleteButton.setY(y + 6);

            final boolean inView = !KeyEditorScreen.this.parent.modifier.isModifying()
                    && y + KEY_ENTRY.height > KeyEditorScreen.this.topPos() + 20
                    && y < KeyEditorScreen.this.topPos() + KEY_MENU.height - 35;
            this.editButton.visible = inView;
            this.deleteButton.visible = inView;
        }

        private void addWidgets() {
            KeyEditorScreen.this.parent.addTypewriterWidget(this.editButton);
            KeyEditorScreen.this.parent.addTypewriterWidget(this.deleteButton);
        }

        private void removeWidgets() {
            KeyEditorScreen.this.parent.removeTypewriterWidget(this.editButton);
            KeyEditorScreen.this.parent.removeTypewriterWidget(this.deleteButton);
        }

        private void setActive(final boolean value) {
            this.editButton.active = value;
            this.deleteButton.active = value;
            this.editButton.visible = value;
            this.deleteButton.visible = value;
        }
    }
}
