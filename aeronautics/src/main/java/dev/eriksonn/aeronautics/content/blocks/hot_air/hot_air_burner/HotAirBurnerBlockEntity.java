package dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner;

import dev.simulated_team.simulated.compat.create.IHaveGoggleInformation;
import dev.simulated_team.simulated.compat.create.IHaveHoveringInformation;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import dev.simulated_team.simulated.compat.create.ScrollValueBehaviour;
import dev.simulated_team.simulated.compat.create.ValueBoxTransform;
import dev.eriksonn.aeronautics.config.AeroConfig;
import dev.eriksonn.aeronautics.config.server.AeroBlockConfigs;
import dev.eriksonn.aeronautics.content.blocks.hot_air.GasEmitterRenderHandler;
import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.Balloon;
import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.ServerBalloon;
import dev.eriksonn.aeronautics.content.blocks.hot_air.lifting_gas.LiftingGasType;
import dev.eriksonn.aeronautics.content.blocks.hot_air.BlockEntityLiftingGasProvider;
import dev.eriksonn.aeronautics.content.particle.HotAirEmberParticleData;
import dev.eriksonn.aeronautics.data.AeroLang;
import dev.eriksonn.aeronautics.index.AeroLiftingGasTypes;
import dev.eriksonn.aeronautics.util.AeroSoundDistUtil;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HotAirBurnerBlockEntity extends SmartBlockEntity
        implements BlockEntityLiftingGasProvider, IHaveGoggleInformation, IHaveHoveringInformation {
    private static final MutableComponent SCROLL_OPTION_TITLE = AeroLang.translate("scroll_option.hot_air_amount").component();
    private static final String VALUE_FORMAT = "%s m\u00B3";

    public GasEmitterRenderHandler renderHandler = new GasEmitterRenderHandler();
    protected ClientBalloonInfo clientBalloonInfo;
    protected boolean powered;
    protected int signalStrength;
    protected ScrollValueBehaviour hotAirAmountBehaviour;
    protected double lastRenderTime;
    protected double renderTime;
    protected LerpedFloat intensity = LerpedFloat.linear();
    private int maxCapacity;
    private int ticksSinceSync;

    private Balloon currentBalloon;
    private @Nullable BlockPos castPosition;

    public HotAirBurnerBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
        this.setLazyTickRate(20);
    }

    @Override
    public void initialize() {
        super.initialize();
        this.updateSignal();
        if (!this.isVirtual() && this.canOutputGas()) {
            this.tickBalloonLogic();
            this.notifyUpdate();
        }
    }

    public int getSignalStrength() {
        return this.signalStrength;
    }

    /**
     * Used exclusively for ponder
     */
    public void setSignalStrength(final int signalStrength) {
        this.signalStrength = signalStrength;
    }

    @Override
    public void addBehaviours(final List<BlockEntityBehaviour<?>> behaviours) {
        this.setMaxCapacity(AeroConfig.server().blocks.hotAirBurnerMaxHotAir.get());

        this.hotAirAmountBehaviour = new HotAirBurnerValueBehaviour(SCROLL_OPTION_TITLE, this,
                new HotAirBurnerValueBoxTransform())
                .between(() -> 5, () -> AeroConfig.server().blocks.hotAirBurnerMaxHotAir.get())
                .withFormatter(VALUE_FORMAT::formatted);
        this.hotAirAmountBehaviour.setValue(this.maxCapacity);

        behaviours.add(this.hotAirAmountBehaviour);
    }

    public void updateSignal() {
        final boolean shouldPower = this.level.hasNeighborSignal(this.worldPosition);
        final int newSignalStrength = this.level.getBestNeighborSignal(this.worldPosition);
        if (newSignalStrength != this.signalStrength) {
            if (this.signalStrength == 0 && newSignalStrength != 0) {
                this.level.playSound(null, this.worldPosition, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS,
                        .125f + this.level.random.nextFloat() * .125f, .75f - this.level.random.nextFloat() * .25f);
            } else if (newSignalStrength == 0) {
                this.level.playSound(null, this.worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS,
                        .125f + this.level.random.nextFloat() * .125f, 1.1f - this.level.random.nextFloat() * .2f);
            }
            this.signalStrength = newSignalStrength;
            this.powered = shouldPower;
            this.sendData();
        }
    }

    @Override
    public void lazyTick() {
        super.lazyTick();

        if (this.level == null)
            return;

        if (this.canOutputGas() && !this.isVirtual()) {
            this.tickBalloonLogic();
        }

        if (!this.level.isClientSide()) {
            if (!this.isVirtual()) {
                this.notifyUpdate();
            }
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return AABB.encapsulatingFullBlocks(this.getBlockPos(), this.getBlockPos().above());
    }

    public void tick() {
        super.tick();

        this.ticksSinceSync++;
        if (!this.level.isClientSide()) {
            return;
        }

        final double intensityGoal = Math.max(0, this.getSignalStrength() / 15.0);
        this.intensity.chase(intensityGoal, 0.1, LerpedFloat.Chaser.EXP);
        this.intensity.tickChaser();

        this.lastRenderTime = this.renderTime;
        this.renderTime += (1.0 / 20.0) * (1.0 + (this.intensity.getValue() * this.intensity.getValue() * 1.8));

        this.renderHandler.targetFromRedstoneSignal(this.signalStrength);
        this.renderHandler.tick();
        if (this.isVirtual())
            return;

        // client particles & sounds
        double particleProbability = 0.4D * Math.sqrt(this.getGasOutput() / this.maxCapacity);
        final BlockPos pos = this.getBlockPos();
        final RandomSource random = this.level.getRandom();
        final double speed = this.signalStrength / 15.0f;

        if (particleProbability > random.nextFloat()) {
            this.level.addAlwaysVisibleParticle(ParticleTypes.LARGE_SMOKE, true,
                    pos.getX() + 0.5 + random.nextDouble() / 5.0 * (random.nextBoolean() ? 1 : -1),
                    pos.getY() + (random.nextDouble() + random.nextDouble()) * 0.5 + 0.56,
                    pos.getZ() + 0.5 + random.nextDouble() / 5.0 * (random.nextBoolean() ? 1 : -1), 0.0D, speed * speed * 0.3, 0.0D);
        }

        if (random.nextInt(20) == 0 && this.powered) {
            this.level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5,
                    pos.getZ() + 0.5, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS,
                    0.25F + random.nextFloat() * .25f, random.nextFloat() * 0.7F + 0.6F, false);
        }

        particleProbability /= 5;
        if (particleProbability > random.nextFloat()) {
            for (int i = 0; i < random.nextInt(1) + 1; ++i) {
                this.level.addParticle(ParticleTypes.LAVA, pos.getX() + 0.5,
                        pos.getY() + 0.5, pos.getZ() + 0.5,
                        random.nextFloat() / 2.0F, 5.0E-5D, random.nextFloat() / 2.0F);
            }
        }

        if (random.nextFloat() < 0.5f * this.intensity.getValue()) {
            this.level.addParticle(new HotAirEmberParticleData(this.getBlockState().getValue(HotAirBurnerBlock.VARIANT) == HotAirBurnerBlock.Variant.SOUL_FIRE),
                    pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5f,
                    pos.getY() + 0.5 + 0.1,
                    pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5f,
                    this.intensity.getValue(),
                    this.intensity.getValue(),
                    this.intensity.getValue());
        }

        if (this.canOutputGas()) {
            AeroSoundDistUtil.addPosHotAirBurnerSound(this.getBlockPos());
        } else {
            AeroSoundDistUtil.removePosHotAirBurnerSound(this.getBlockPos());
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();

        if (this.level.isClientSide()) {
            AeroSoundDistUtil.removePosHotAirBurnerSound(this.getBlockPos());
        } else {
            this.removeFromBalloon();
        }
    }

    @Override
    public void write(final ValueOutput compound, final boolean clientPacket) {
        compound.putBoolean("IsPowered", this.powered);
        compound.putInt("SignalStrength", this.signalStrength);

        if (clientPacket) {
            ClientBalloonInfo.writeToNBT(compound, (ServerBalloon) this.getBalloon());
        }

        super.write(compound, clientPacket);
    }

    @Override
    protected void read(final ValueInput tag, final boolean clientPacket) {
        this.powered = tag.getBooleanOr("IsPowered", false);
        this.signalStrength = tag.getIntOr("SignalStrength", 0);

        if (clientPacket) {
            this.ticksSinceSync = 0;
            this.clientBalloonInfo = ClientBalloonInfo.readFromNBT(tag);
        }

        super.read(tag, clientPacket);
    }

    @Override
    public Balloon getBalloon() {
        return this.currentBalloon;
    }

    @Override
    public void setBalloon(final Balloon balloon) {
        this.currentBalloon = balloon;
    }

    @Override
    public BlockPos getProviderBlockPos() {
        return super.getBlockPos();
    }

    @Override
    public Level getProviderLevel() {
        return super.getLevel();
    }

    @Override
    public @Nullable BlockPos getCastPosition() {
        return this.castPosition;
    }

    @Override
    public void doRaycast() {
        final BlockPos pos = this.getBlockPos();

        final AeroBlockConfigs blocks = AeroConfig.server().blocks;
        final int range = blocks.hotAirBurnerMaxRange.get();

        this.castPosition = this.getRaycastedPosition(this.level,
                    Vec3.upFromBottomCenterOf(pos, 1.0),
                    Vec3.upFromBottomCenterOf(pos, 1.0 + range));
    }

    @Override
    public double getGasOutput() {
        return (this.hotAirAmountBehaviour.getValue() * this.signalStrength) / 15.0;
    }

    @Override
    public LiftingGasType getLiftingGasType() {
        return AeroLiftingGasTypes.DEFAULT_GAS.get();
    }

    @Override
    public boolean canOutputGas() {
        return this.signalStrength > 0 & !this.isRemoved();
    }

    @Override
    public double getClientPredictedVolume() {
        if (this.clientBalloonInfo == null)
            return 0.0;

        return BlockEntityLiftingGasProvider.getPredictedVolume(this.clientBalloonInfo, this.ticksSinceSync);
    }

    public void setMaxCapacity(final int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    @Override
    public boolean addToGoggleTooltip(final List<Component> tooltip, final boolean isPlayerSneaking) {
        if (!this.canOutputGas()) return false;

        AeroLang.blockName(this.getBlockState()).text(":").forGoggles(tooltip, 1);
        if (this.clientBalloonInfo != null) {
            this.addBalloonGoggleInformation(tooltip, this.clientBalloonInfo, this.ticksSinceSync, this.getAirPressure(this.clientBalloonInfo, this.level));
        }
        return true;
    }

    protected float getFlameIntensity(final float partialTicks) {
        return this.intensity.getValue(partialTicks);
    }

    public float getTimeOffset() {
        return (this.getBlockPos().hashCode() % 10);
    }

    public LerpedFloat getClientIntensity() {
        return this.intensity;
    }


    private static class HotAirBurnerValueBoxTransform extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8f, 14);
        }

        @Override
        public float getScale() {
            return 0.45f;
        }

        @Override
        protected boolean isSideActive(final BlockState state, final Direction direction) {
            return direction.getAxis() != Direction.Axis.Y || direction.equals(Direction.DOWN);
        }

        @Override
        public Vec3 getLocalOffset(final LevelAccessor level, final BlockPos pos, final BlockState state) {
            if (this.getSide() == Direction.DOWN) {
                return VecHelper.voxelSpace(8, 0, 8);
            }

            Vec3 location = this.getSouthLocation();

            location = location.add(VecHelper.voxelSpace(0, -3, 1.75));
            location = VecHelper.rotateCentered(location, AngleHelper.horizontalAngle(this.getSide()), Direction.Axis.Y);

            return location;
        }
    }

}
