package foundry.veil.api.client.color;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public class Color implements Colorc {
    public static final Codec<Integer> ARGB_INT_CODEC = Codec.STRING.comapFlatMap(Color::decode, Color::encode);
    public static final Colorc WHITE = new Color(0xFFFFFFFF, true);
    public static final Colorc BLACK = new Color(0xFF000000, true);

    private float red;
    private float green;
    private float blue;
    private float alpha;

    public Color() {
        this(0xFF000000, true);
    }

    public Color(final int color) {
        this(color, false);
    }

    public Color(final int color, final boolean alpha) {
        this.setArgb(alpha ? color : 0xFF000000 | color);
    }

    public Color(final float red, final float green, final float blue, final float alpha) {
        this.set(red, green, blue, alpha);
    }

    public Color set(final float red, final float green, final float blue, final float alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        return this;
    }

    private void setArgb(final int color) {
        this.alpha = ((color >>> 24) & 0xFF) / 255.0F;
        this.red = ((color >>> 16) & 0xFF) / 255.0F;
        this.green = ((color >>> 8) & 0xFF) / 255.0F;
        this.blue = (color & 0xFF) / 255.0F;
    }

    @Override
    public float red() {
        return this.red;
    }

    @Override
    public float green() {
        return this.green;
    }

    @Override
    public float blue() {
        return this.blue;
    }

    @Override
    public float alpha() {
        return this.alpha;
    }

    private static DataResult<Integer> decode(final String value) {
        try {
            final String hex = value.startsWith("#") ? value.substring(1) : value;
            return DataResult.success((int) Long.parseLong(hex, 16));
        } catch (final NumberFormatException exception) {
            return DataResult.error(() -> "Invalid ARGB color: " + value);
        }
    }

    private static String encode(final int value) {
        return "#%08x".formatted(value);
    }
}
