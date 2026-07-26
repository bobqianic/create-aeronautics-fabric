package foundry.veil.api.client.color;

public interface Colorc {
    float red();

    float green();

    float blue();

    float alpha();

    default int argb() {
        return ((int) (this.alpha() * 255.0F) & 0xFF) << 24
                | ((int) (this.red() * 255.0F) & 0xFF) << 16
                | ((int) (this.green() * 255.0F) & 0xFF) << 8
                | (int) (this.blue() * 255.0F) & 0xFF;
    }

    default int rgb() {
        return this.argb() & 0xFFFFFF;
    }

    default Color mix(final Colorc color, final float amount, final Color store) {
        return store.set(
                this.red() * (1.0F - amount) + color.red() * amount,
                this.green() * (1.0F - amount) + color.green() * amount,
                this.blue() * (1.0F - amount) + color.blue() * amount,
                this.alpha() * (1.0F - amount) + color.alpha() * amount);
    }

    default Color darken(final float amount, final Color store) {
        return this.mix(Color.BLACK, amount, store);
    }
}
