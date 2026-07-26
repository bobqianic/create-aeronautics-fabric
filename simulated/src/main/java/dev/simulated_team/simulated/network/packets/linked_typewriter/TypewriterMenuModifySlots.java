package dev.simulated_team.simulated.network.packets.linked_typewriter;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.screen.LinkedTypewriterMenuCommon;
import foundry.veil.api.network.handler.ServerPacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record TypewriterMenuModifySlots(ItemStack first, ItemStack second, boolean active) implements CustomPacketPayload {

    public static Type<TypewriterMenuModifySlots> TYPE = new Type<>(Simulated.path("entry_modify"));

    public static StreamCodec<RegistryFriendlyByteBuf, TypewriterMenuModifySlots> CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, TypewriterMenuModifySlots::first,
            ItemStack.OPTIONAL_STREAM_CODEC, TypewriterMenuModifySlots::second,
            net.minecraft.network.codec.ByteBufCodecs.BOOL, TypewriterMenuModifySlots::active,
            TypewriterMenuModifySlots::new
    );

    public void handle(final ServerPacketContext context) {
        final ServerPlayer player = context.player();

        if (player.containerMenu instanceof final LinkedTypewriterMenuCommon menu) {
            menu.ghostInventory.setItem(0, this.first);
            menu.ghostInventory.setItem(1, this.second);
            menu.slotsActive = this.active;
            menu.broadcastChanges();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
