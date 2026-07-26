package dev.simulated_team.simulated.ponder.elements;

import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.element.ScreenElement;
import com.zurrtum.create.catnip.math.Pointing;
import com.zurrtum.create.client.ponder.api.PonderPalette;
import com.zurrtum.create.client.ponder.api.element.InputElementBuilder;
import com.zurrtum.create.client.ponder.enums.PonderGuiTextures;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.element.InputWindowElement;
import com.zurrtum.create.client.ponder.foundation.ui.PonderUI;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2fStack;

import javax.annotation.Nullable;

/**
 * Window element that renders a key mapping.
 *
 * @author Ocelot
 */
public class KeybindWindowElement extends InputWindowElement {

    private final Vec3 sceneSpace;
    private final Pointing direction;
    @Nullable
    Component keybind;
    @Nullable
    ScreenElement icon;
    ItemStack item = ItemStack.EMPTY;

    public KeybindWindowElement(final Vec3 sceneSpace, final Pointing direction) {
        super(sceneSpace, direction);
        this.sceneSpace = sceneSpace;
        this.direction = direction;
    }

    public @NotNull Builder builder() {
        return new KeybindWindowElement.Builder();
    }

    public class Builder implements InputElementBuilder {

        @Override
        public Builder withItem(final ItemStack stack) {
            KeybindWindowElement.this.item = stack;
            return this;
        }

        @Override
        public Builder leftClick() {
            KeybindWindowElement.this.icon = PonderGuiTextures.ICON_LMB;
            return this;
        }

        @Override
        public Builder scroll() {
            KeybindWindowElement.this.icon = PonderGuiTextures.ICON_SCROLL;
            return this;
        }

        @Override
        public Builder rightClick() {
            KeybindWindowElement.this.icon = PonderGuiTextures.ICON_RMB;
            return this;
        }

        @Override
        public Builder showing(final ScreenElement icon) {
            KeybindWindowElement.this.icon = icon;
            return this;
        }

        @Override
        public Builder whileSneaking() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Builder whileCTRL() {
            throw new UnsupportedOperationException();
        }

        public Builder keybind(final String keybind) {
            KeybindWindowElement.this.keybind = Component.keybind(keybind).append(" +");
            return this;
        }
    }

    @Override
    public void render(final @NotNull PonderScene scene, final PonderUI screen, final @NotNull GuiGraphics graphics, final float partialTicks, final float fade) {
        final Font font = screen.getFontRenderer();
        int width = 0;
        int height = 0;

        float xFade = this.direction == Pointing.RIGHT ? -1 : this.direction == Pointing.LEFT ? 1 : 0;
        float yFade = this.direction == Pointing.DOWN ? -1 : this.direction == Pointing.UP ? 1 : 0;
        xFade *= 10 * (1 - fade);
        yFade *= 10 * (1 - fade);

        final boolean hasItem = !this.item.isEmpty();
        final boolean hasText = this.keybind != null;
        final boolean hasIcon = this.icon != null;
        int keyWidth = 0;
        final Component text = hasText ? this.keybind : Component.empty();

        if (fade < 1 / 16f) {
            return;
        }
        final Vec2 sceneToScreen = scene.getTransform()
                .sceneToScreen(this.sceneSpace, partialTicks);

        if (hasIcon) {
            width += 24;
            height = 24;
        }

        if (hasText) {
            keyWidth = font.width(text);
            width += keyWidth;
        }

        if (hasItem) {
            width += 24;
            height = 24;
        }

        final Matrix3x2fStack poseStack = graphics.pose();
        poseStack.pushMatrix();
        poseStack.translate(sceneToScreen.x + xFade, sceneToScreen.y + yFade);

        PonderUI.renderSpeechBox(graphics, 0, 0, width, height, false, this.direction, true);

        graphics.nextStratum();

        if (hasText) {
            graphics.drawString(font, text, 2, (int) ((height - font.lineHeight) / 2f + 2),
                    PonderPalette.WHITE.getColorObject().scaleAlpha(fade).getRGB(), false);
        }

        if (hasIcon) {
            poseStack.pushMatrix();
            poseStack.translate(keyWidth, 0);
            poseStack.scale(1.5f, 1.5f);
            this.icon.render(graphics, 0, 0);
            poseStack.popMatrix();
        }

        if (hasItem) {
            GuiGameElement.of(this.item)
                    .<GuiGameElement.GuiRenderBuilder>at(keyWidth + (hasIcon ? 24 : 0), 0)
                    .scale(1.5f)
                    .render(graphics);
        }

        poseStack.popMatrix();
    }

}
