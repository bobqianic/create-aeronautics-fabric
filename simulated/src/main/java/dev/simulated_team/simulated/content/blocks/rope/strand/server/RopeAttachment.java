package dev.simulated_team.simulated.content.blocks.rope.strand.server;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public record RopeAttachment(RopeAttachmentPoint point, @Nullable UUID subLevelID, BlockPos blockAttachment) {
    private static final Codec<RopeAttachmentPoint> ATTACHMENT_POINT_CODEC = Codec.STRING.xmap(
            RopeAttachmentPoint::valueOf,
            RopeAttachmentPoint::name
    );

    private RopeAttachment(final RopeAttachmentPoint point, final Optional<UUID> subLevelID, final BlockPos blockAttachment) {
        this(point, subLevelID.orElse(null), blockAttachment);
    }

    public static final Codec<RopeAttachment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ATTACHMENT_POINT_CODEC.fieldOf("point").forGetter(RopeAttachment::point),
            Codec.STRING.optionalFieldOf("subLevelID")
                    .xmap(opt -> opt.map(UUID::fromString),
                            uuid -> uuid.map(UUID::toString))
                    .forGetter(x -> Optional.ofNullable(x.subLevelID())),
            BlockPos.CODEC.fieldOf("blockAttachment").forGetter(RopeAttachment::blockAttachment)
    ).apply(instance, RopeAttachment::new));

}
