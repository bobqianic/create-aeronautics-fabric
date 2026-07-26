package dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.graph;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongMaps;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.core.BlockPos;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A slice/layer of a balloon, containing 8x8x1 chunks stored as longs, and a state of current processing
 */
public class BalloonLayerData {

    /**
     * Shift for 8x8 chunks
     */
    public static final byte LAYER_CHUNK_SHIFT = 3;

    /**
     * Mask for 8x8 chunks
     */
    public static final byte LAYER_CHUNK_MASK = 7;

    public static int blockToChunkCoord(final int x) {
        return x >> LAYER_CHUNK_SHIFT;
    }

    public static int getBlockIndex(final int x, final int z) {
        return ((x & LAYER_CHUNK_MASK) << LAYER_CHUNK_SHIFT) + (z & LAYER_CHUNK_MASK);
    }

    public static long asLong(final int x, final int z) {
        return (long)x & 0xFFFFFFFFL | ((long)z & 0xFFFFFFFFL) << 32;
    }

    public static int getChunkX(final long l) {
        return (int)(l & 0xFFFFFFFFL);
    }

    public static int getChunkZ(final long l) {
        return (int)(l >>> 32 & 0xFFFFFFFFL);
    }

    /**
     * The current state of processing of this layer
     */
    private State state = State.NEEDS_DOWN_PASS;

    /**
     * A map of packed x, z 8x8 block chunk coordinates -> a long representing the 8x8 chunk of
     * hot air blocks
     */
    private final Long2LongMap chunks = new Long2LongOpenHashMap();

    /**
     * A map of packed x, z 8x8 block chunk coordinates -> a long representing the 8x8 chunk of
     * solid non-airtight blocks
     */
    private final Long2LongMap solidChunks = new Long2LongOpenHashMap();

    /**
     * All graph connections going into this layer
     */
    public final Collection<BalloonLayerData> inwardConnections = new ObjectArraySet<>();

    /**
     * All graph connections going out of this layer
     */
    public final Collection<BalloonLayerData> outwardConnections = new ObjectArraySet<>();

    /**
     * The Y coordinate of this layer
     */
    private final int yLevel;

    /**
     * The total hot air count
     */
    public int hotAirCount;

    /**
     * The total solid block count
     */
    public int solidCount;

    public boolean boundsInitialized = false;
    public int minChunkX, minChunkZ, maxChunkX, maxChunkZ;

    public BalloonLayerData(final int yLevel) {
        this.yLevel = yLevel;
    }

    /**
     * @return the hot air chunk at a given block X and Z
     */
    public long getHotAirChunkAtBlock(final int x, final int z) {
        return this.chunks.get(asLong(x >> LAYER_CHUNK_SHIFT, z >> LAYER_CHUNK_SHIFT));
    }

    /**
     * @return the solid block chunk at a given block X and Z
     */
    public long getSolidsChunkAtBlock(final int x, final int z) {
        return this.solidChunks.get(asLong(x >> LAYER_CHUNK_SHIFT, z >> LAYER_CHUNK_SHIFT));
    }

    public void addHotAirBlock(final int x, final int z) {
        final int chunkX = x >> LAYER_CHUNK_SHIFT;
        final int chunkZ = z >> LAYER_CHUNK_SHIFT;

        final long key = asLong(chunkX, chunkZ);
        final long l = this.chunks.get(key);

        if (l == 0) {
            if (!this.boundsInitialized) {
                this.minChunkX = chunkX;
                this.minChunkZ = chunkZ;
                this.maxChunkX = chunkX;
                this.maxChunkZ = chunkZ;
                this.boundsInitialized = true;
            } else {
                this.minChunkX = Math.min(this.minChunkX, chunkX);
                this.minChunkZ = Math.min(this.minChunkZ, chunkZ);
                this.maxChunkX = Math.max(this.maxChunkX, chunkX);
                this.maxChunkZ = Math.max(this.maxChunkZ, chunkZ);
            }
        }

        this.chunks.put(key, l | (1L << getBlockIndex(x, z)));
    }

    public void addSolidBlock(final int x, final int z) {
        final long key = asLong(x >> LAYER_CHUNK_SHIFT, z >> LAYER_CHUNK_SHIFT);
        final long l = this.solidChunks.get(key);
        this.solidChunks.put(key, l | (1L << getBlockIndex(x, z)));
    }

    public void removeHotAirBlock(final int x, final int z) {
        final long key = asLong(x >> LAYER_CHUNK_SHIFT, z >> LAYER_CHUNK_SHIFT);
        final long l = this.chunks.get(key);
        if (l == 0) return;
        this.chunks.put(key, l & ~(1L << getBlockIndex(x, z)));
    }

    public void removeSolidBlock(final int x, final int z) {
        final long key = asLong(x >> LAYER_CHUNK_SHIFT, z >> LAYER_CHUNK_SHIFT);
        final long l = this.solidChunks.get(key);
        if (l == 0) return;
        this.solidChunks.put(key, l & ~(1L << getBlockIndex(x, z)));
    }

    public boolean overlaps(final BalloonLayerData other) {
        if (!this.boundsInitialized || !other.boundsInitialized) {
            return false;
        }

        final int minChunkX = Math.max(this.minChunkX, other.minChunkX);
        final int minChunkZ = Math.max(this.minChunkZ, other.minChunkZ);
        final int maxChunkX = Math.min(this.maxChunkX, other.maxChunkX);
        final int maxChunkZ = Math.min(this.maxChunkZ, other.maxChunkZ);

        if (minChunkX > maxChunkX || minChunkZ > maxChunkZ) {
            return false;
        }

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                final long key = asLong(chunkX, chunkZ);

                final long thisBits = this.chunks.get(key);
                final long otherBits = other.chunks.get(key);

                // at-least 1 overlapping bit, we vibing
                if ((thisBits & otherBits) != 0L) {
                    return true;
                }
            }
        }

        return false;
    }

    public State getState() {
        return this.state;
    }

    public void setState(final State state) {
        this.state = state;
    }

    public boolean getHotAirBlock(final int x, final int z) {
        final long chunk = this.getHotAirChunkAtBlock(x, z);
        if (chunk == 0) return false;

        final int blockIndex = getBlockIndex(x, z);
        return ((chunk >> blockIndex) & 1) != 0;
    }

    public boolean getSolidBlock(final int x, final int z) {
        final long chunk = this.getSolidsChunkAtBlock(x, z);
        if (chunk == 0) return false;

        final int blockIndex = getBlockIndex(x, z);
        return ((chunk >> blockIndex) & 1) != 0;
    }

    public Long2LongMap getChunks() {
        return this.chunks;
    }

    public Long2LongMap getSolidChunks() {
        return this.solidChunks;
    }

    public int getYLevel() {
        return this.yLevel;
    }

    public Iterator<BlockPos> blockIterator() {
        return new LayerBlockIterator(false);
    }

    public Iterator<BlockPos> nonSolidBlockIterator() {
        return new LayerBlockIterator(true);
    }

    public enum State {
        NEEDS_DOWN_PASS,
        COMPLETE
    }

    private final class LayerBlockIterator implements Iterator<BlockPos> {
        private final Iterator<Long2LongMap.Entry> chunkIter;
        private final boolean ignoreSolids;
        private Long2LongMap.Entry currentEntry;

        private boolean hasSolidsChunk;
        private long solidsChunkBits;

        private int bitIndex = -1;

        private boolean hasNext = false;
        private final BlockPos.MutableBlockPos nextPos = new BlockPos.MutableBlockPos();
        private final BlockPos.MutableBlockPos resultPos = new BlockPos.MutableBlockPos();

        private LayerBlockIterator(final boolean ignoreSolids) {
            this.chunkIter = Long2LongMaps.fastIterator(BalloonLayerData.this.chunks);
            this.ignoreSolids = ignoreSolids;
            this.advance();
        }

        private void advance() {
            this.hasNext = false;

            while (true) {
                if (this.currentEntry == null && this.chunkIter.hasNext()) {
                    this.currentEntry = this.chunkIter.next();

                    if (this.ignoreSolids) {
                        this.solidsChunkBits = BalloonLayerData.this.solidChunks.get(this.currentEntry.getLongKey());
                        this.hasSolidsChunk = this.solidsChunkBits != 0;
                    }

                    this.bitIndex = -1;
                } else if (this.currentEntry == null) {
                    return;
                }

                long chunkBits = this.currentEntry.getLongValue();

                if (this.hasSolidsChunk) {
                    chunkBits = chunkBits & ~this.solidsChunkBits;
                }

                final boolean chunkFull = chunkBits == 0b1111111111111111111111111111111111111111111111111111111111111111L;

                while (++this.bitIndex < 64) {
                    if (chunkFull || ((chunkBits >>> this.bitIndex) & 1L) != 0) {
                        final long key = this.currentEntry.getLongKey();
                        final int chunkX = getChunkX(key);
                        final int chunkZ = getChunkZ(key);

                        final int localX = this.bitIndex >> LAYER_CHUNK_SHIFT; // divide by 8
                        final int localZ = this.bitIndex & LAYER_CHUNK_MASK;   // mod 8

                        final int worldX = (chunkX << LAYER_CHUNK_SHIFT) + localX;
                        final int worldZ = (chunkZ << LAYER_CHUNK_SHIFT) + localZ;

                        this.nextPos.set(worldX, BalloonLayerData.this.yLevel, worldZ);
                        this.hasNext = true;
                        return;
                    }
                }

                this.currentEntry = null;
            }
        }

        @Override
        public boolean hasNext() {
            return this.hasNext;
        }

        @Override
        public BlockPos next() {
            if (!this.hasNext) throw new NoSuchElementException();
            this.resultPos.set(this.nextPos);
            this.advance();
            return this.resultPos;
        }
    }
}
