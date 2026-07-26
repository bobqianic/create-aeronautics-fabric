package dev.ryanhcode.offroad.handlers.server;

import com.zurrtum.create.content.kinetics.base.BlockBreakingKineticBlockEntity;
import com.zurrtum.create.foundation.utility.BlockHelper;
import dev.simulated_team.simulated.compat.create.CommonLevelAttached;
import dev.ryanhcode.offroad.handlers.MultiminingDataTickResult;
import dev.ryanhcode.offroad.network.borehead_bearing.ClientboundMultiMiningSync;
import foundry.veil.api.network.VeilPacketManager;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiMiningServerManager {

    public static final CommonLevelAttached<MultiMiningServerManager> ATTACHED_DATA = new CommonLevelAttached<>(level -> {
        if (level.isClientSide()) {
            return null;
        }

        return new MultiMiningServerManager();
    });

    /**
     * We use Create's block breaking kinetic BE ID to avoid ID conflicts
     */
    private static final AtomicInteger MULTIMINING_IDS = BlockBreakingKineticBlockEntity.NEXT_BREAKER_ID;

    /**
     * Block breaking ID that is synced to client to ensure that we don't needlessly create excess IDs and to reduce data sent to client
     */
    private final int breakingId = -MULTIMINING_IDS.incrementAndGet();

    private final Map<BlockPos, BlockBreakingData> breakingDataMap = new Object2ObjectOpenHashMap<>();
    private final Map<BlockPos, ClientboundMultiMiningSync> dirtyBreakingData = new Object2ObjectOpenHashMap<>();

    /**
     * @return Whether the given position is a newly added one
     */
    public static boolean addOrRefreshPos(final Level level, final BlockPos pos, final MultiMiningSupplier supplier) {
        if (level.isClientSide()) {
            return false;
        }

        final MultiMiningServerManager breakingDataMap = ATTACHED_DATA.get(level);
        if (breakingDataMap != null) {
            return breakingDataMap.handleAddedPos(pos, supplier);
        }

        return false;
    }

    public static void tick(final Level level) {
        if (level.isClientSide()) {
            return;
        }

        final MultiMiningServerManager breakingDataMap = ATTACHED_DATA.get(level);
        if (breakingDataMap != null) {
            breakingDataMap.handleTicking(level);
        }
    }

    public boolean handleAddedPos(final BlockPos pos, final MultiMiningSupplier supplier) {
        BlockBreakingData existingData = this.breakingDataMap.get(pos);

        if (existingData == null) {
            existingData = new BlockBreakingData(pos);
            this.breakingDataMap.put(pos, existingData);

            existingData.addSupplier(supplier, true);
            return true;
        }

        existingData.addSupplier(supplier, true);
        return false;
    }

    private void handleTicking(final Level level) {
        final Iterator<Map.Entry<BlockPos, BlockBreakingData>> iter = this.breakingDataMap.entrySet().iterator();
        while (iter.hasNext()) {
            final Map.Entry<BlockPos, BlockBreakingData> set = iter.next();

            final BlockBreakingData data = set.getValue();

            final byte beforeProgress = data.getDestroyProgressByted();
            final MultiminingDataTickResult result = data.tick(level);
            switch (result) {
                case BROKEN -> {
                    iter.remove();
                }

                case STOP -> {
                    data.setDestroyProgress(-1);
                    this.addSyncingData(data);
                    iter.remove();
                }

                case CONTINUE -> {
                    if (beforeProgress != data.getDestroyProgressByted()) {
                        this.addSyncingData(data);
                    }
                }
            }
        }

        if (!this.dirtyBreakingData.isEmpty()) {
            for (final Map.Entry<BlockPos, ClientboundMultiMiningSync> set : this.dirtyBreakingData.entrySet()) {
                VeilPacketManager.tracking((ServerLevel) level, set.getKey())
                        .sendPacket(set.getValue());
            }

            this.dirtyBreakingData.clear();
        }
    }

    private void addSyncingData(final BlockBreakingData data) {
        if (data.getLastKnownSupplierPosition() != null) {
            this.dirtyBreakingData.computeIfAbsent(data.getLastKnownSupplierPosition(), p -> ClientboundMultiMiningSync.serverOutboundData(this.breakingId))
                    .inData.put(data.getBreakingPos(), data);
        }
    }

    /**
     * Breaking data associated with a certain block position. primarily used for handler borehead bearing interfacing
     */
    public static class BlockBreakingData {

        private final static int MAX_TIMEOUT = 20;

        /**
         * The suppliers giving this BlockBreakingData its speed and activity. Usually a BoreheadBearingBlockEntity.
         */
        private final List<MultiMiningSupplier> suppliers = new ArrayList<>();

        /**
         * The block position associated with this BlockBreakingData
         */
        private final BlockPos breakingPos;

        /**
         * The amount of ticks before timing out this block breaking data
         */
        private float timeoutTicks = 0;

        private float destroyProgress = 0;

        private BlockPos lastKnownSupplierPosition = null;

        public BlockBreakingData(final BlockPos breakingPos) {
            this.breakingPos = breakingPos;
        }

        /**
         * @param level The level containing this data
         * @return Whether this BlockBreakingData should continue being broken
         */
        public MultiminingDataTickResult tick(final Level level) {
            if (this.timeoutTicks++ > MAX_TIMEOUT) {
                this.cleanData();
                return MultiminingDataTickResult.STOP;
            }

            this.suppliers.removeIf(supplier -> !supplier.isActive());
            if (this.suppliers.isEmpty()) {
                this.cleanData();
                return MultiminingDataTickResult.STOP;
            }

            this.lastKnownSupplierPosition = this.suppliers.getFirst().getLocation();

            final BlockState state = level.getBlockState(this.getBreakingPos());
            final float hardness = state.getDestroySpeed(level, this.getBreakingPos());
            if (state.isAir() || !BlockBreakingKineticBlockEntity.isBreakable(state, hardness)) {
                this.cleanData();
                return MultiminingDataTickResult.STOP;
            }

            if (this.timeoutTicks < 5) {
                float averageMiningSpeed = 0;
                for (final MultiMiningSupplier supplier : this.suppliers) {
                    averageMiningSpeed += Math.abs(supplier.getBreakingSpeed(level, this.breakingPos, state)) / this.suppliers.size();
                }

                final double nextProgressStep = averageMiningSpeed / hardness;
                this.setDestroyProgress((float) (this.getDestroyProgress() + nextProgressStep));

                if (this.getDestroyProgress() >= 10) {
                    Collections.shuffle(this.suppliers);
                    BlockHelper.destroyBlock(level, this.getBreakingPos(), 1, stack -> {
                        for (final MultiMiningSupplier supplier : this.suppliers) {
                            supplier.itemCallback(stack);
                            if (stack.isEmpty()) {
                                break;
                            }
                        }

                        if (!stack.isEmpty()) {
                            Block.popResource(level, this.getBreakingPos(), stack);
                        }
                    });

                    this.cleanData();
                    return MultiminingDataTickResult.BROKEN;
                }
            }

            return MultiminingDataTickResult.CONTINUE;
        }

        private void cleanData() {
            this.suppliers.clear();
        }

        public void addSupplier(final MultiMiningSupplier supplier, final boolean refreshTimeout) {
            if (!this.suppliers.contains(supplier)) {
                this.suppliers.add(supplier);
            }

            if (refreshTimeout) {
                this.timeoutTicks = 0;
            }
        }

        public byte getDestroyProgressByted() {
            return (byte) this.getDestroyProgress();
        }

        public void clientAimedSerialization(final ByteBuf buf) {
            final boolean invalid = this.getDestroyProgress() >= 10;
            buf.writeBoolean(invalid);
            if (!invalid) {
                buf.writeByte((byte) this.getDestroyProgress());
            }
        }

        public BlockPos getBreakingPos() {
            return this.breakingPos;
        }

        public BlockPos getLastKnownSupplierPosition() {
            return this.lastKnownSupplierPosition;
        }

        /**
         * The destruction progress of this data. Ranges from 0 to 10, with anything above or equal 10 being considered broken
         */
        public float getDestroyProgress() {
            return this.destroyProgress;
        }

        public void setDestroyProgress(final float destroyProgress) {
            this.destroyProgress = destroyProgress;
        }
    }

}
