package com.zurrtum.create.foundation.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface ICustomParticleData<D extends ParticleOptions> {
    MapCodec<D> getCodec(ParticleType<D> type);

    StreamCodec<? super RegistryFriendlyByteBuf, D> getStreamCodec();

    default ParticleType<D> createType() {
        return new ParticleType<>(false) {
            @Override
            public MapCodec<D> codec() {
                return ICustomParticleData.this.getCodec(this);
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, D> streamCodec() {
                return ICustomParticleData.this.getStreamCodec();
            }
        };
    }
}
