package dev.ryanhcode.offroad.handlers.client;

import com.zurrtum.create.content.kinetics.base.BlockBreakingKineticBlockEntity;
import dev.simulated_team.simulated.compat.create.CommonLevelAttached;
import dev.ryanhcode.offroad.handlers.MultiminingDataTickResult;
import dev.ryanhcode.offroad.mixin.client.multimining_destruction_progress.ClientLevelAccessor;
import dev.ryanhcode.offroad.mixin_interface.level_renderer.MultiMiningDestructionExtension;
import dev.ryanhcode.sable.util.LevelAccelerator;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Iterator;
import java.util.Map;

public class MultiMiningClientHandler {

    public static final CommonLevelAttached<MultiMiningClientHandler> LEVEL_ATTACHED = new CommonLevelAttached<>(level -> {
        if (level instanceof PonderLevel || !level.isClientSide()) {
            return null;
        }

        MultiMiningClientHandler clientHandler = new MultiMiningClientHandler();
        clientHandler.accelerator = new LevelAccelerator((Level) level);

        return clientHandler;
    });

    private static int soundCount = 0;

    private final Map<BlockPos, ClientBlockBreakingData> clientBlockBreakingData = new Object2ObjectOpenHashMap<>();
    private final Map<BlockPos, ClientBlockBreakingData> dirtyData = new Object2ObjectOpenHashMap<>();

    private int breakingID;

    private LevelAccelerator accelerator;

    public static void handleInboundClientUpdate(final Level level, final Map<BlockPos, ClientBlockBreakingData> incomingData, final int breakingID) {
        if (!level.isClientSide() || incomingData.isEmpty()) {
            return;
        }

        final MultiMiningClientHandler clientHandler = LEVEL_ATTACHED.get(level);
        if (clientHandler == null) {
            return;
        }

        //I guess this works? kind of destroys existing data but, I suppose that is fine...
        clientHandler.clientBlockBreakingData.putAll(incomingData);
        clientHandler.breakingID = breakingID;
    }

    public static void tick(final Level level) {
        if (level instanceof PonderLevel || !level.isClientSide()) {
            return;
        }

        final MultiMiningClientHandler clientHandler = LEVEL_ATTACHED.get(level);
        if (clientHandler != null) {
            clientHandler.handleTick(level);
        }
    }

    private void handleTick(final Level level) {
        final int blocksBeingBroken = this.clientBlockBreakingData.size();

        final Iterator<Map.Entry<BlockPos, ClientBlockBreakingData>> iter = this.clientBlockBreakingData.entrySet().iterator();
        while(iter.hasNext()) {
            final Map.Entry<BlockPos, ClientBlockBreakingData> dataSet = iter.next();

            final MultiminingDataTickResult result = dataSet.getValue().tick(level, this.accelerator, dataSet.getKey(), blocksBeingBroken);
            switch (result) {
                case BROKEN -> {
                    this.dirtyData.put(dataSet.getKey(), dataSet.getValue());
                    iter.remove();
                }

                case CONTINUE -> {
                    this.dirtyData.put(dataSet.getKey(), dataSet.getValue());
                }
            }
        }

        this.accelerator.clearCache();
        if (level.getGameTime() % 20 == 0) {
            soundCount = 0;
        }

        if (!this.dirtyData.isEmpty()) {
            this.bulkUpdateDestructionProgress(level);
            this.dirtyData.clear();
        }
    }

    private void bulkUpdateDestructionProgress(final Level level) {
        final LevelRenderer levelRenderer = ((ClientLevelAccessor) level).getLevelRenderer();
        ((MultiMiningDestructionExtension) levelRenderer).offroad$manuallyAddMultiDestructionProgress(this.breakingID, this.dirtyData);
    }

    public static class ClientBlockBreakingData {
        public boolean invalid;
        public float destroyProgress;

        /**
         * @param level             the level associated with this client data
         * @param pos               The pos associated with this client data
         * @param blocksBeingBroken The total amount of blocks being broken this tick. Does not change as entries get removed.
         * @return Whether this client data should continue ticking.
         */
        public MultiminingDataTickResult tick(final Level level, final LevelAccelerator accelerator, final BlockPos pos, final int blocksBeingBroken) {
            final BlockState state = accelerator.getBlockState(pos);
            if (!BlockBreakingKineticBlockEntity.isBreakable(state, state.getDestroySpeed(level, pos)) || this.invalid || this.destroyProgress >= 10 || this.destroyProgress < 0) {
                this.destroyProgress = -1;
                return MultiminingDataTickResult.BROKEN;
            }

            final ClientLevel clientLevel = (ClientLevel) level;
            final double radius = 0.5;
            if (accelerator.getBlockState(pos.below()).isAir()) {
                if (level.random.nextFloat() > 0.8) {
                    final BlockParticleOption blockBreakingParticles = new BlockParticleOption(ParticleTypes.BLOCK, state);

                    clientLevel.addParticle(blockBreakingParticles,
                            pos.getX() + 0.5 + (level.random.nextFloat() - 0.5) * 2 * radius,
                            pos.getY(),
                            pos.getZ() + 0.5 + (level.random.nextFloat() - 0.5) * 2 * radius,
                            0,
                            -1,
                            0);

                    if (level.random.nextFloat() > 0.8) {
                        clientLevel.addParticle(ParticleTypes.ASH,
                                pos.getX() + 0.5 + (level.random.nextFloat() - 0.5) * 2 * radius,
                                pos.getY(),
                                pos.getZ() + 0.5 + (level.random.nextFloat() - 0.5) * 2 * radius,
                                0,
                                -1,
                                0);
                    }
                }
            } else if (accelerator.getBlockState(pos.above()).isAir()) {
                if (level.random.nextFloat() > 0.9) {
                    if (level.random.nextFloat() > 0.5) {
                        clientLevel.addParticle(ParticleTypes.CRIT,
                                pos.getX() + 0.5 + (level.random.nextFloat() - 0.5) * 2 * radius,
                                pos.getY() + 1.2,
                                pos.getZ() + 0.5 + (level.random.nextFloat() - 0.5) * 2 * radius,
                                0,
                                0,
                                0);
                    }
                }
            }

            final double chance = 1 - (0.4 / (Math.sqrt(Math.pow(blocksBeingBroken, 2))));
            if (soundCount < 64 && level.random.nextFloat() > chance) {
                soundCount++;
                clientLevel.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, state.getSoundType()
                        .getHitSound(), SoundSource.BLOCKS, .4f, 0.1f, false);
            }

            return MultiminingDataTickResult.CONTINUE;
        }
    }

}
