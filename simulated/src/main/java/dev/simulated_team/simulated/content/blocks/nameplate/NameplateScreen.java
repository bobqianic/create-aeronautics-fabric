package dev.simulated_team.simulated.content.blocks.nameplate;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.data.SimLang;
import dev.simulated_team.simulated.network.packets.name_plate.NameplateChangeNamePacket;
import foundry.veil.api.network.VeilPacketManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

public class NameplateScreen extends Screen {
    public static final int MAX_WIDTH = 8 * 16 - 6;

    private final NameplateBlockEntity blockEntity;
    private String message;
    private int frame;

    @Nullable
    private TextFieldHelper nameField;

    public NameplateScreen(final NameplateBlockEntity blockEntity) {
        this(blockEntity, SimLang.translate("nameplate.edit").component());
    }

    public NameplateScreen(final NameplateBlockEntity blockEntity, final Component title) {
        super(title);
        this.blockEntity = blockEntity;
        this.message = blockEntity.getName();
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onDone())
                .bounds(this.width / 2 - 100, this.height / 4 + 144, 200, 20)
                .build());
        this.nameField = new TextFieldHelper(
                () -> this.message,
                this::setMessage,
                TextFieldHelper.createClipboardGetter(this.minecraft),
                TextFieldHelper.createClipboardSetter(this.minecraft),
                value -> this.minecraft.font.width(value) <= MAX_WIDTH
        );
        this.nameField.setCursorToEnd();
    }

    private void setMessage(final String message) {
        this.message = message;
    }

    @Override
    public void tick() {
        this.frame++;
        if (!this.isValid()) {
            this.onDone();
        }
    }

    private boolean isValid() {
        return this.minecraft != null
                && this.minecraft.player != null
                && !this.blockEntity.isRemoved()
                && NameplateBlockEntity.canPlayerReach(this.blockEntity, this.minecraft.player);
    }

    @Override
    public boolean keyPressed(final KeyEvent input) {
        if (input.key() == GLFW.GLFW_KEY_ENTER || input.key() == GLFW.GLFW_KEY_KP_ENTER) {
            this.onDone();
            return true;
        }

        return this.nameField != null && this.nameField.keyPressed(input)
                || super.keyPressed(input);
    }

    @Override
    public boolean charTyped(final CharacterEvent input) {
        return this.nameField != null && this.nameField.charTyped(input);
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 40, 0xFFFFFF);
        this.renderNameplate(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderNameplate(final GuiGraphics graphics) {
        final Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(this.width / 2.0f, this.height / 2.0f - 26.0f);
        pose.scale(2.0f, 2.0f);

        this.renderNameplateBackground(graphics, this.blockEntity.getBlockState());
        this.renderNameplateText(graphics);

        pose.popMatrix();
    }

    private void renderNameplateBackground(final GuiGraphics graphics, final BlockState state) {
        final String color = ((NameplateBlock) state.getBlock()).getColor().getSerializedName();
        final Matrix3x2fStack pose = graphics.pose();

        pose.pushMatrix();
        pose.scale(15.0f / 12.0f, 15.0f / 12.0f);
        pose.translate(8.0f - 16.0f * 4.0f, 5.7f);

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                Simulated.path("textures/block/nameplate/" + color + "_nameplate.png"),
                -8, -8, 0.0f, 12.0f, 16, 10, 32, 32
        );
        for (int i = 0; i < 6; i++) {
            pose.translate(16.0f, 0.0f);
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    Simulated.path("textures/block/nameplate/" + color + "_nameplate.png"),
                    -8, -8, 8.0f, 12.0f, 16, 10, 32, 32
            );
        }
        pose.translate(16.0f, 0.0f);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                Simulated.path("textures/block/nameplate/" + color + "_nameplate.png"),
                -8, -8, 16.0f, 12.0f, 16, 10, 32, 32
        );

        pose.popMatrix();
    }

    private void renderNameplateText(final GuiGraphics graphics) {
        if (this.nameField == null) {
            return;
        }

        final int lineHeight = 8;
        final int color = this.blockEntity.getDarkColor(this.blockEntity.getTextColor());
        final boolean cursorFlash = this.frame / 6 % 2 == 0;
        final int cursorPos = this.nameField.getCursorPos();
        final int selectionPos = this.nameField.getSelectionPos();
        final int textWidth = this.font.width(this.message);
        final int textX = -textWidth / 2;

        graphics.drawString(this.font, this.message, textX, 0, color, false);

        if (cursorPos >= 0) {
            final int clampedCursor = Math.clamp(cursorPos, 0, this.message.length());
            final int cursorX = this.font.width(this.message.substring(0, clampedCursor)) - textWidth / 2;

            if (cursorFlash) {
                if (cursorPos >= this.message.length()) {
                    graphics.drawString(this.font, "_", cursorX, 0, color, false);
                } else {
                    graphics.fill(cursorX, -1, cursorX + 1, lineHeight, 0xFF000000 | color);
                }
            }

            if (selectionPos != cursorPos) {
                final int selectionStart = Math.clamp(Math.min(cursorPos, selectionPos), 0, this.message.length());
                final int selectionEnd = Math.clamp(Math.max(cursorPos, selectionPos), 0, this.message.length());
                final int selectionX1 = this.font.width(this.message.substring(0, selectionStart)) - textWidth / 2;
                final int selectionX2 = this.font.width(this.message.substring(0, selectionEnd)) - textWidth / 2;
                graphics.textHighlight(selectionX1, -1, selectionX2, lineHeight);
            }
        }
    }

    @Override
    public void onClose() {
        this.onDone();
    }

    @Override
    public void removed() {
        VeilPacketManager.server().sendPacket(new NameplateChangeNamePacket(
                this.blockEntity.findController().getBlockPos(),
                this.message
        ));
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void onDone() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
        }
    }

    public static void setScreen(final NameplateBlockEntity blockEntity) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (blockEntity != null
                && minecraft.player != null
                && NameplateBlockEntity.canPlayerReach(blockEntity, minecraft.player)) {
            minecraft.setScreen(new NameplateScreen(blockEntity.findController()));
        }
    }
}
