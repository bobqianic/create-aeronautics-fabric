package dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.screen;

import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.content.redstone.link.RedstoneLinkNetworkHandler;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterEntries;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.screen.widgets.ConfirmationWidgetBase;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.screen.widgets.PromptWidget;
import dev.simulated_team.simulated.data.SimLang;
import dev.simulated_team.simulated.index.SimGUITextures;
import dev.simulated_team.simulated.network.packets.linked_typewriter.TypewriterMenuModifySlots;
import foundry.veil.api.network.VeilPacketManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class EntryModifierScreen {
    private static final SimGUITextures MODIFICATION_MENU =
            SimGUITextures.LINKED_TYPEWRITER_KEY_MODIFICATION_MENU;
    private static final SimGUITextures MODIFICATION_ENTRY =
            SimGUITextures.LINKED_TYPEWRITER_BIND;

    private final LinkedTypewriterScreen parent;
    private Consumer<LinkedTypewriterEntries.KeyboardEntry> finishedCallback;
    private PendingKeyboardEntry pendingEntry;
    private boolean modifying;

    private PromptWidget promptWidget;
    private IconButton confirmationWidget;
    private ConfirmationWidgetBase cancelEntryWidget;

    public EntryModifierScreen(final LinkedTypewriterScreen parent) {
        this.parent = parent;
    }

    public void init() {
        this.promptWidget = new PromptWidget(this, 0, 0, 68, 16);
        this.cancelEntryWidget = new ConfirmationWidgetBase(0, 0, AllIcons.I_TRASH)
                .withMessage(SimLang.translate("linked_typewriter.delete.key").component())
                .withCallback(this::finishWithoutEntry);
        this.confirmationWidget = new IconButton(0, 0, AllIcons.I_CONFIRM)
                .withCallback(() -> {
                    if (this.pendingEntry != null) {
                        this.pendingEntry.finishModifications();
                    }
                });
        this.resetPositions();
    }

    public void resetPositions() {
        if (this.promptWidget == null) {
            return;
        }

        final int widgetY = this.getCenterY() + 32;
        this.promptWidget.setX(this.getCenterX() + 19);
        this.promptWidget.setY(widgetY);
        this.confirmationWidget.setX(this.getCenterX() + MODIFICATION_ENTRY.width - 56);
        this.confirmationWidget.setY(widgetY - 1);
        this.cancelEntryWidget.setX(this.getCenterX() + MODIFICATION_ENTRY.width - 33);
        this.cancelEntryWidget.setY(widgetY - 1);
    }

    public PendingKeyboardEntry startModifying(
            @Nullable final LinkedTypewriterEntries.KeyboardEntry entry,
            final Consumer<LinkedTypewriterEntries.KeyboardEntry> onFinish) {
        final PendingKeyboardEntry pending = new PendingKeyboardEntry();
        if (entry != null) {
            pending.keyCode(entry.glfwKeyCode)
                    .first(entry.getFirst())
                    .second(entry.getSecond());
            this.parent.getNewEntries().getKeyMap().remove(entry.glfwKeyCode);
        } else {
            pending.first(RedstoneLinkNetworkHandler.Frequency.EMPTY)
                    .second(RedstoneLinkNetworkHandler.Frequency.EMPTY);
        }

        this.finishedCallback = onFinish;
        this.pendingEntry = pending;
        this.modifying = true;
        this.cancelEntryWidget.cancelConfirmation();
        this.promptWidget.stopBinding();
        this.parent.addTypewriterWidget(this.cancelEntryWidget);
        this.parent.addTypewriterWidget(this.confirmationWidget);
        this.parent.addTypewriterWidget(this.promptWidget);

        final LinkedTypewriterMenuCommon menu = this.parent.getMenu();
        menu.slotsActive = true;
        final ItemStack first = pending.first.getStack();
        final ItemStack second = pending.second.getStack();
        menu.ghostInventory.setItem(0, first);
        menu.ghostInventory.setItem(1, second);
        VeilPacketManager.server().sendPacket(new TypewriterMenuModifySlots(first, second, true));
        return pending;
    }

    public void renderBackground(final GuiGraphics graphics) {
        if (!this.modifying) {
            return;
        }

        graphics.fillGradient(0, 0, this.parent.width, this.parent.height,
                -1072689136, -804253680);
        MODIFICATION_MENU.render(graphics, this.getCenterX(), this.getCenterY());
        this.parent.renderPlayerInventory(
                graphics,
                this.getCenterX() + 19,
                this.getCenterY() + 72
        );
    }

    private int getCenterX() {
        return this.parent.getLeftPos() + 11;
    }

    private int getCenterY() {
        return this.parent.getTopPos() - 31;
    }

    public void finishWithoutEntry() {
        if (this.finishedCallback != null) {
            this.finishedCallback.accept(null);
        }
        this.disable();
    }

    public void disable() {
        if (!this.modifying) {
            return;
        }

        final LinkedTypewriterMenuCommon menu = this.parent.getMenu();
        this.modifying = false;
        this.pendingEntry = null;
        this.parent.removeTypewriterWidget(this.cancelEntryWidget);
        this.parent.removeTypewriterWidget(this.promptWidget);
        this.parent.removeTypewriterWidget(this.confirmationWidget);
        this.finishedCallback = null;
        this.promptWidget.stopBinding();
        menu.slotsActive = false;
        menu.ghostInventory.setItem(0, ItemStack.EMPTY);
        menu.ghostInventory.setItem(1, ItemStack.EMPTY);
        VeilPacketManager.server().sendPacket(
                new TypewriterMenuModifySlots(ItemStack.EMPTY, ItemStack.EMPTY, false));
    }

    public boolean isModifying() {
        return this.modifying;
    }

    @Nullable
    public PendingKeyboardEntry getPendingEntry() {
        return this.pendingEntry;
    }

    public class PendingKeyboardEntry {
        private int keyCode = -1;
        private RedstoneLinkNetworkHandler.Frequency first;
        private RedstoneLinkNetworkHandler.Frequency second;

        public int getKeyCode() {
            return this.keyCode;
        }

        public PendingKeyboardEntry keyCode(final int newCode) {
            this.keyCode = newCode;
            return this;
        }

        public PendingKeyboardEntry first(final RedstoneLinkNetworkHandler.Frequency frequency) {
            this.first = frequency;
            return this;
        }

        public PendingKeyboardEntry second(final RedstoneLinkNetworkHandler.Frequency frequency) {
            this.second = frequency;
            return this;
        }

        public void finishModifications() {
            if (this.keyCode == -1) {
                EntryModifierScreen.this.finishWithoutEntry();
                return;
            }

            final LinkedTypewriterMenuCommon menu = EntryModifierScreen.this.parent.getMenu();
            this.first(RedstoneLinkNetworkHandler.Frequency.of(menu.ghostInventory.getItem(0)));
            this.second(RedstoneLinkNetworkHandler.Frequency.of(menu.ghostInventory.getItem(1)));
            final LinkedTypewriterEntries.KeyboardEntry entry =
                    new LinkedTypewriterEntries.KeyboardEntry(
                            this.first,
                            this.second,
                            this.keyCode,
                            EntryModifierScreen.this.parent.clientBe.getBlockPos()
                    );

            if (EntryModifierScreen.this.finishedCallback != null) {
                EntryModifierScreen.this.finishedCallback.accept(entry);
            }
            EntryModifierScreen.this.disable();
        }
    }
}
