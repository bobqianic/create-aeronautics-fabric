package dev.eriksonn.aeronautics.content.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.foundation.particle.ICustomParticleDataWithSprite;
import dev.eriksonn.aeronautics.index.AeroParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class PropellerAirParticleData implements ParticleOptions, ICustomParticleDataWithSprite<PropellerAirParticleData> {

    private static final MapCodec<PropellerAirParticleData> CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(
                    Codec.BOOL.fieldOf("collision").forGetter((p) -> p.enableCollision),
                    Codec.BOOL.fieldOf("virtual").forGetter(p -> p.isVirtual))
            .apply(i, PropellerAirParticleData::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, PropellerAirParticleData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, (p) -> p.enableCollision,
            ByteBufCodecs.BOOL, (p) -> p.isVirtual,
            PropellerAirParticleData::new);


    boolean enableCollision;
    boolean isVirtual;

    public PropellerAirParticleData(boolean enableCollision, boolean isVirtual) {
        this.enableCollision = enableCollision;
        this.isVirtual = isVirtual;
    }

    public PropellerAirParticleData() {
        this(true, false);
    }

    @Override
    public ParticleType<?> getType() {
        return AeroParticleTypes.PROPELLER_AIR_FLOW.get();
    }

    @Override
    public MapCodec<PropellerAirParticleData> getCodec(ParticleType<PropellerAirParticleData> particleType) {
        return CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, PropellerAirParticleData> getStreamCodec() {
        return STREAM_CODEC;
    }
}
