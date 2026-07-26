package dev.simulated_team.simulated.compat.create;

import com.zurrtum.create.catnip.theme.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.Consumer;

public final class CommonLangBuilder {
    private final String namespace;
    private MutableComponent component;

    public CommonLangBuilder(final String namespace) {
        this.namespace = namespace;
    }

    public CommonLangBuilder space() {
        return text(" ");
    }

    public CommonLangBuilder newLine() {
        return text("\n");
    }

    public CommonLangBuilder translate(final String key, final Object... args) {
        final Object[] resolvedArgs = args.clone();
        for (int index = 0; index < resolvedArgs.length; index++) {
            if (resolvedArgs[index] instanceof CommonLangBuilder builder) {
                resolvedArgs[index] = builder.component();
            }
        }
        return add(Component.translatable(this.namespace + "." + key, resolvedArgs));
    }

    public CommonLangBuilder text(final String text) {
        return add(Component.literal(text));
    }

    public CommonLangBuilder text(final ChatFormatting format, final String text) {
        return add(Component.literal(text).withStyle(format));
    }

    public CommonLangBuilder text(final int color, final String text) {
        return add(Component.literal(text).withColor(color));
    }

    public CommonLangBuilder add(final CommonLangBuilder builder) {
        return add(builder.component());
    }

    public CommonLangBuilder add(final Component addedComponent) {
        final MutableComponent mutable = addedComponent.copy();
        this.component = this.component == null ? mutable : this.component.append(mutable);
        return this;
    }

    public CommonLangBuilder style(final ChatFormatting format) {
        requireComponent();
        this.component.withStyle(format);
        return this;
    }

    public CommonLangBuilder color(final int color) {
        requireComponent();
        this.component.withColor(color);
        return this;
    }

    public CommonLangBuilder color(final Color color) {
        return color(color.getRGB());
    }

    public MutableComponent component() {
        requireComponent();
        return this.component;
    }

    public String string() {
        return component().getString();
    }

    public void sendStatus(final Player player) {
        player.displayClientMessage(component(), true);
    }

    public void sendChat(final Player player) {
        player.displayClientMessage(component(), false);
    }

    public void addTo(final List<? super MutableComponent> tooltip) {
        tooltip.add(component());
    }

    public void addTo(final Consumer<? super MutableComponent> tooltip) {
        tooltip.accept(component());
    }

    public void forGoggles(final List<? super MutableComponent> tooltip) {
        forGoggles(tooltip, 0);
    }

    public void forGoggles(final List<? super MutableComponent> tooltip, final int indents) {
        tooltip.add(Component.literal(" ".repeat(4 + indents)).append(component()));
    }

    private void requireComponent() {
        if (this.component == null) {
            throw new IllegalStateException("No components were added to builder");
        }
    }
}
