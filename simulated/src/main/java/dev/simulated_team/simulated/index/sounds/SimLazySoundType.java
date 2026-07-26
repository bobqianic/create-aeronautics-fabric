package dev.simulated_team.simulated.index.sounds;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;

import java.util.function.Supplier;

public class SimLazySoundType extends SoundType {

    private final LazySupplier<SoundEvent> lazyBreak;
    private final LazySupplier<SoundEvent> lazyStep;
    private final LazySupplier<SoundEvent> lazyPlace;
    private final LazySupplier<SoundEvent> lazyHit;
    private final LazySupplier<SoundEvent> lazyFall;

    public SimLazySoundType(final float volume, final float pitch, final Supplier<SoundEvent> lazyBreak, final Supplier<SoundEvent> lazyStep, final Supplier<SoundEvent> lazyPlace, final Supplier<SoundEvent> lazyHit, final Supplier<SoundEvent> lazyFall) {
        super(volume, pitch, null, null, null, null, null);
        this.lazyBreak = LazySupplier.of(lazyBreak);
        this.lazyStep = LazySupplier.of(lazyStep);
        this.lazyPlace = LazySupplier.of(lazyPlace);
        this.lazyHit = LazySupplier.of(lazyHit);
        this.lazyFall = LazySupplier.of(lazyFall);
    }

    @Override
    public SoundEvent getBreakSound() {
        return this.lazyBreak.cast();
    }

    @Override
    public SoundEvent getStepSound() {
        return this.lazyStep.cast();
    }

    @Override
    public SoundEvent getPlaceSound() {
        return this.lazyPlace.cast();
    }

    @Override
    public SoundEvent getHitSound() {
        return this.lazyHit.cast();
    }

    @Override
    public SoundEvent getFallSound() {
        return this.lazyFall.cast();
    }

    /**
     * Custom implementation of a lazy supplier, that is only called once
     *
     * @param <T> The lazy type
     */
    public static class LazySupplier<T> {

        T nullableLazy;

        Supplier<T> lazyGetter;

        public static <T> LazySupplier<T> of(final Supplier<T> getter) {
            return new LazySupplier<>(getter);
        }

        public LazySupplier(final Supplier<T> getter) {
            this.lazyGetter = getter;
        }

        public T cast() {
            if (this.nullableLazy == null) {
                this.nullableLazy = this.lazyGetter.get();
            }

            return this.nullableLazy;
        }
    }

}
