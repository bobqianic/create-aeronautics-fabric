package dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.screen;

import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record LinkedTypewriterMenuData(BlockPos blockPos, CompoundTag updateTag) {
    public static final StreamCodec<RegistryFriendlyByteBuf, LinkedTypewriterMenuData> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, LinkedTypewriterMenuData::blockPos,
                    ByteBufCodecs.COMPOUND_TAG, LinkedTypewriterMenuData::updateTag,
                    LinkedTypewriterMenuData::new
            );

    public static LinkedTypewriterMenuData from(final LinkedTypewriterBlockEntity blockEntity) {
        return new LinkedTypewriterMenuData(
                blockEntity.getBlockPos(),
                blockEntity.getUpdateTag(blockEntity.getLevel().registryAccess())
        );
    }
}
