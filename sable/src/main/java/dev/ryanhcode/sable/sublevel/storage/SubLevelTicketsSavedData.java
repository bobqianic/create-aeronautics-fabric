package dev.ryanhcode.sable.sublevel.storage;

import com.mojang.serialization.Codec;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.ticket.SubLevelLoadingTicket;
import dev.ryanhcode.sable.api.sublevel.ticket.SubLevelLoadingTicketType;
import dev.ryanhcode.sable.api.sublevel.ticket.SubLevelTicketInfo;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.storage.holding.GlobalSavedSubLevelPointer;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

/**
 * Stores the force loading tickets for sub-levels
 */
public class SubLevelTicketsSavedData extends SavedData {
    public static final String FILE_ID = "sable_sub_level_force_load_tickets";
    private final ServerLevel level;

    private SubLevelTicketsSavedData(final ServerLevel level) {
        this.level = level;
    }

    public static SubLevelTicketsSavedData getOrLoad(final ServerLevel level) {
        // PORT-NOTE(mc26.1): SavedData.Factory was replaced by codec-based SavedDataType. The CompoundTag
        // codec bridges to the legacy load/save NBT format. SavedDataType requires a non-null DataFixTypes;
        // SAVED_DATA_COMMAND_STORAGE is the conventional inert choice for modded data (no fixers apply).
        // File moves from data/sable_sub_level_force_load_tickets.dat to
        // data/sable/sable_sub_level_force_load_tickets.dat (namespaced id).
        return level.getDataStorage().computeIfAbsent(
                new SavedDataType<>(
                        SubLevelTicketsSavedData.FILE_ID,
                        () -> new SubLevelTicketsSavedData(level),
                        CompoundTag.CODEC.xmap(
                                tag -> SubLevelTicketsSavedData.load(level, tag),
                                data -> data.save(new CompoundTag())
                        ),
                        DataFixTypes.SAVED_DATA_COMMAND_STORAGE
                ));
    }

    private static SubLevelTicketsSavedData load(final ServerLevel level, final CompoundTag tag) {
        final SubLevelTicketsSavedData data = new SubLevelTicketsSavedData(level);
        final ServerSubLevelContainer container = SubLevelContainer.getContainer(level);
        if (container == null) return data;

        final Object2ObjectMap<UUID, SubLevelTicketInfo> newTickets = new Object2ObjectOpenHashMap<>();
        final ListTag ticketInfos = tag.getListOrEmpty("tickets");

        for (int i = 0; i < ticketInfos.size(); i++) {
            final CompoundTag infoTag = ticketInfos.getCompoundOrEmpty(i);
            final UUID subLevelId = infoTag.read("uuid", net.minecraft.core.UUIDUtil.CODEC).orElseThrow();

            final ListTag entriesTag = infoTag.getListOrEmpty("entries");
            final ObjectSet<SubLevelLoadingTicket<?>> tickets = new ObjectArraySet<>();

            for (int j = 0; j < entriesTag.size(); j++) {
                final CompoundTag entryTag = entriesTag.getCompoundOrEmpty(j);
                final SubLevelLoadingTicket<?> ticket = deserializeTicket(subLevelId, entryTag);

                if (ticket != null) {
                    tickets.add(ticket);
                }
            }
            GlobalSavedSubLevelPointer pointer = null;

            if (infoTag.contains("pointer")) {
                pointer = GlobalSavedSubLevelPointer.CODEC.decode(NbtOps.INSTANCE, infoTag.get("pointer")).getOrThrow().getFirst();
            }

            if (!tickets.isEmpty()) {
                newTickets.put(subLevelId, new SubLevelTicketInfo(pointer, tickets));
            }
        }

        container.loadTickets(newTickets);
        return data;
    }

    private static <T> SubLevelLoadingTicket<T> deserializeTicket(final UUID subLevelId, final CompoundTag tag) {
        final ResourceLocation typeName = ResourceLocation.parse(tag.getStringOr("type", ""));
        @SuppressWarnings("unchecked") final SubLevelLoadingTicketType<T> type = (SubLevelLoadingTicketType<T>) SubLevelLoadingTicketType.byName(typeName);

        if (type == null) {
            Sable.LOGGER.error("Unknown sub-level loading ticket type: {}", typeName);
            return null;
        }

        final Tag keyTag = tag.get("key");
        final T key = type.codec().parse(NbtOps.INSTANCE, keyTag)
                .resultOrPartial(error -> Sable.LOGGER.warn("Failed to deserialize ticket key for type {}: {}", typeName, error))
                .orElse(null);

        if (key == null) {
            return null;
        }

        return new SubLevelLoadingTicket<>(type, subLevelId, key);
    }

    private static <T> CompoundTag serializeTicket(final SubLevelLoadingTicket<T> ticket) {
        final SubLevelLoadingTicketType<T> type = ticket.getType();
        final Codec<T> codec = type.codec();

        return codec.encodeStart(NbtOps.INSTANCE, ticket.getKey())
                .resultOrPartial(error -> Sable.LOGGER.warn("Failed to serialize ticket key for type {}: {}", type.name(), error))
                .map(keyTag -> {
                    final CompoundTag tag = new CompoundTag();
                    tag.putString("type", type.name().toString());
                    tag.put("key", keyTag);
                    return tag;
                })
                .orElse(null);
    }

    public @NotNull CompoundTag save(final CompoundTag compoundTag) {
        final ServerSubLevelContainer container = SubLevelContainer.getContainer(this.level);
        assert container != null : "Sub-level container is null";

        final ListTag ticketInfos = new ListTag();

        final Map<UUID, SubLevelTicketInfo> allTickets = container.getAllTickets();

        for (final Map.Entry<UUID, SubLevelTicketInfo> entry : allTickets.entrySet()) {
            final UUID uuid = entry.getKey();
            final SubLevelTicketInfo info = entry.getValue();

            final GlobalSavedSubLevelPointer pointer;

            final SubLevel subLevel = container.getSubLevel(uuid);

            if (subLevel instanceof final ServerSubLevel serverSubLevel) {
                pointer = serverSubLevel.getLastSerializationPointer();
            } else {
                pointer = info.getPointer();
            }

            final CompoundTag infoTag = new CompoundTag();
            infoTag.store("uuid", net.minecraft.core.UUIDUtil.CODEC, uuid);
            final ListTag entriesTag = new ListTag();

            for (final SubLevelLoadingTicket<?> ticket : info.tickets()) {
                final CompoundTag entryTag = serializeTicket(ticket);

                if (entryTag != null) {
                    entriesTag.add(entryTag);
                }
            }

            if (!entriesTag.isEmpty()) {
                if (pointer != null) {
                    infoTag.put("pointer", GlobalSavedSubLevelPointer.CODEC.encodeStart(NbtOps.INSTANCE, pointer).getOrThrow());
                } else {
                    Sable.LOGGER.error("Pointer is null for Sub-level loading ticket. This shouldn't happen- we won't be able to load the sub-level in on the next world load.");
                }

                infoTag.put("entries", entriesTag);
                ticketInfos.add(infoTag);
            }
        }

        compoundTag.put("tickets", ticketInfos);
        return compoundTag;
    }
}