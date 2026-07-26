package dev.ryanhcode.sable.mixin.compatibility.etched;

import dev.ryanhcode.sable.sound.MovingSoundInstanceDelegate;
import dev.ryanhcode.sable.sound.SoundInstanceDelegated;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

@Pseudo
@Mixin(targets = "gg.moonflower.etched.api.sound.StopListeningSound")
public class StopListeningSoundMixin implements SoundInstanceDelegated {

    @Unique
    private MovingSoundInstanceDelegate sable$delegate;

    @Override
    public MovingSoundInstanceDelegate getDelegate() {
        return this.sable$delegate;
    }

    @Override
    public void setDelegate(final MovingSoundInstanceDelegate delegate) {
        this.sable$delegate = delegate;
    }
}
