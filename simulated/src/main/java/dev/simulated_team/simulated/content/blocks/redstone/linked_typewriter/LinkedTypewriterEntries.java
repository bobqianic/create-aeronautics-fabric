package dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.Create;
import com.zurrtum.create.content.redstone.link.IRedstoneLinkable;
import com.zurrtum.create.content.redstone.link.RedstoneLinkNetworkHandler;
import dev.simulated_team.simulated.Simulated;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import com.zurrtum.create.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class LinkedTypewriterEntries {

    private final Int2ObjectLinkedOpenHashMap<KeyboardEntry> keyMap;

    private final Set<KeyboardEntry> newlyActivatedKeyboardEntries;
    private final Set<KeyboardEntry> newlyDeactivatedKeyboardEntries;

    public LinkedTypewriterEntries() {
        this.keyMap = new Int2ObjectLinkedOpenHashMap<>();

        this.newlyActivatedKeyboardEntries = new HashSet<>();
        this.newlyDeactivatedKeyboardEntries = new HashSet<>();
    }

    public static LinkedTypewriterEntries readKeys(final HolderLookup.Provider registryAccess, final ListTag tags, final BlockPos pos) {
        final LinkedTypewriterEntries keys = new LinkedTypewriterEntries();

        for (final Tag tag : tags) {
            final RegistryOps<Tag> ops = registryAccess.createSerializationContext(NbtOps.INSTANCE);
            final DataResult<Pair<KeyboardEntry, Tag>> result = KeyboardEntry.CODEC.decode(ops, tag);
            if (result.isError()) { //if there was an error saving the entry, we want to output to the console instead of crashing
                Simulated.LOGGER.error(result.error().get().message());
            } else {
                final KeyboardEntry entry = result.getOrThrow().getFirst();
                entry.setLocation(pos);
                keys.setKey(entry.glfwKeyCode, entry);
            }
        }

        return keys;
    }

    /**
     * Handles all redstoneLinkFrequency handling when adding and removing and updating to the server handler
     */
    public void updateNetworks(final Level level) {
        if (!level.isClientSide()) { //Iterate through activated keys first then deactivated keys to make sure if a key is activated and deactivated in the same tick it always deactivates
            for (final KeyboardEntry keyboardEntry : this.newlyActivatedKeyboardEntries) {
                Create.REDSTONE_LINK_NETWORK_HANDLER.addToNetwork(level, keyboardEntry);
            }
            this.newlyActivatedKeyboardEntries.clear();

            for (final KeyboardEntry keyboardEntry : this.newlyDeactivatedKeyboardEntries) {
                Create.REDSTONE_LINK_NETWORK_HANDLER.removeFromNetwork(level, keyboardEntry);
            }
            this.newlyDeactivatedKeyboardEntries.clear();
        }
    }

    public void activateKey(final int index, final LinkedTypewriterBlockEntity lbe) {
        final KeyboardEntry frequency = this.getEntry(index);
        if (frequency != null) {
            frequency.activate();
            this.newlyActivatedKeyboardEntries.add(frequency);
        }
    }

    public void deactivateKey(final int index) {
        final KeyboardEntry frequency = this.getEntry(index);
        if (frequency != null) {
            frequency.deactivate();
            this.newlyDeactivatedKeyboardEntries.add(frequency);
        }
    }

    public void deactivateAll() {
        this.keyMap.forEach((index, key) -> {
            if (key.isAlive()) {
                this.newlyDeactivatedKeyboardEntries.add(key);
            }

            key.deactivate();
        });
    }

    /**
     * Sets or removes the specified key from this map
     *
     * @param index         The key index to set or remove
     * @param keyboardEntry Nullable. The key to set to the given key Index. If null, removes the given index.
     */
    public void setKey(final int index, @Nullable final LinkedTypewriterEntries.KeyboardEntry keyboardEntry) {
        if (keyboardEntry == null) {
            this.keyMap.remove(index);
            return;
        }

        if (this.keyMap.containsKey(index)) {
            this.keyMap.get(index).deactivate();
        }

        this.keyMap.put(index, keyboardEntry);
    }

    public KeyboardEntry getEntry(final int key) {
        return this.keyMap.get(key);
    }

    public void clearAll() {
        this.deactivateAll();
        this.keyMap.clear();
        this.newlyDeactivatedKeyboardEntries.clear();
        this.newlyActivatedKeyboardEntries.clear();
    }

    public void addAll(final Map<Integer, KeyboardEntry> newMap) {
        this.keyMap.putAll(newMap);
    }

    public ListTag saveKeys(final HolderLookup.Provider registryAccess) {
        final ListTag tags = new ListTag();
        if (this.keyMap.isEmpty()) {
            return tags;
        }

        for (final Map.Entry<Integer, KeyboardEntry> set : this.keyMap.entrySet()) {
            final RegistryOps<Tag> ops = registryAccess.createSerializationContext(NbtOps.INSTANCE);
            final DataResult<Tag> result = KeyboardEntry.CODEC.encodeStart(ops, set.getValue());
            if (result.isError()) { //if there was an error saving the entry, we want to output to the console instead of crashing
                Simulated.LOGGER.error(result.error().get().message());
            } else {
                tags.add(result.getOrThrow());
            }
        }

        return tags;
    }

    /**
     * @return An immutable copy of the entry list.
     */
    public List<KeyboardEntry> getEntries() {
        return List.copyOf(this.keyMap.sequencedValues());
    }

    public int getSize() {
        return this.keyMap.size();
    }

    public Map<Integer, KeyboardEntry> getKeyMap() {
        return this.keyMap;
    }

    public static class KeyboardEntry implements IRedstoneLinkable {

        public static final Codec<KeyboardEntry> CODEC = RecordCodecBuilder.create((instance) ->
                instance.group(ItemStack.OPTIONAL_CODEC.fieldOf("FirstItem")
                                        .forGetter(KeyboardEntry::getFirstAsItemStack),
                                ItemStack.OPTIONAL_CODEC.fieldOf("SecondItem")
                                        .forGetter(KeyboardEntry::getSecondAsItemStack),
                                Codec.INT.fieldOf("GLFWKey").forGetter(KeyboardEntry::getGLFWKeyCode))
                        .apply(instance, KeyboardEntry::createFromCodec));

        public static final StreamCodec<RegistryFriendlyByteBuf, KeyboardEntry> STREAM_CODEC = StreamCodec.composite(
                ItemStack.OPTIONAL_STREAM_CODEC, KeyboardEntry::getFirstAsItemStack,
                ItemStack.OPTIONAL_STREAM_CODEC, KeyboardEntry::getSecondAsItemStack,
                ByteBufCodecs.INT, KeyboardEntry::getGLFWKeyCode,
                KeyboardEntry::createFromCodec);


        public final int glfwKeyCode;

        private final RedstoneLinkNetworkHandler.Frequency first;
        private final RedstoneLinkNetworkHandler.Frequency second;

        private boolean currentlyActive;
        private BlockPos pos;

        public KeyboardEntry(RedstoneLinkNetworkHandler.Frequency first, RedstoneLinkNetworkHandler.Frequency second, final int constant, final BlockPos pos) {
            if (first == null) {
                first = RedstoneLinkNetworkHandler.Frequency.EMPTY;
            }

            if (second == null) {
                second = RedstoneLinkNetworkHandler.Frequency.EMPTY;
            }

            this.first = first;
            this.second = second;

            this.currentlyActive = false;
            this.pos = pos;

            this.glfwKeyCode = constant;
        }

        private KeyboardEntry(final RedstoneLinkNetworkHandler.Frequency first, final RedstoneLinkNetworkHandler.Frequency second, final int constant) {
            this(first, second, constant, null);
        }

        public static KeyboardEntry createFromCodec(final ItemStack first, final ItemStack second, final int glfwKey) {
            final RedstoneLinkNetworkHandler.Frequency firstFreq = RedstoneLinkNetworkHandler.Frequency.of(first);
            final RedstoneLinkNetworkHandler.Frequency secondFreq = RedstoneLinkNetworkHandler.Frequency.of(second);
            return new KeyboardEntry(firstFreq, secondFreq, glfwKey);
        }

        private static Optional<Item> mapItem(final Item item) {
            return item == Items.AIR ? Optional.empty() : Optional.of(item);
        }

        private static @NotNull Item mapOptional(final Optional<Item> optional) {
            return optional.orElse(Items.AIR);
        }

        public void activate() {
            this.currentlyActive = true;
        }

        public void deactivate() {
            this.currentlyActive = false;
        }

        public Couple<RedstoneLinkNetworkHandler.Frequency> getAsCouple() {
            return Couple.create(this.first, this.second);
        }

        public int getGLFWKeyCode() {
            return this.glfwKeyCode;
        }

        public RedstoneLinkNetworkHandler.Frequency getFirst() {
            return this.first;
        }

        public ItemStack getFirstAsItemStack() {
            return this.getFirst().getStack();
        }

        private Item getFirstItem() {
            return this.getFirstAsItemStack().getItem();
        }

        public RedstoneLinkNetworkHandler.Frequency getSecond() {
            return this.second;
        }

        public ItemStack getSecondAsItemStack() {
            return this.getSecond().getStack();
        }

        private Item getSecondItem() {
            return this.getSecondAsItemStack().getItem();
        }

        // redstone link functionality
        @Override
        public int getTransmittedStrength() {
            return this.isAlive() ? 15 : 0;
        }

        @Override
        public void setReceivedStrength(final int i) {
        }

        @Override
        public boolean isListening() {
            return false;
        }

        @Override
        public boolean isAlive() {
            return this.currentlyActive;
        }

        @Override
        public Couple<RedstoneLinkNetworkHandler.Frequency> getNetworkKey() {
            return this.getAsCouple();
        }

        @Override
        public BlockPos getLocation() {
            return this.pos;
        }

        public void setLocation(final BlockPos newPos) {
            this.pos = newPos;
        }
    }
}
