package dev.simulated_team.simulated.content.blocks.throttle_lever;

import dev.simulated_team.simulated.compat.create.IHaveGoggleInformation;
import dev.simulated_team.simulated.compat.create.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import dev.simulated_team.simulated.compat.create.CommonLangBuilder;
import dev.simulated_team.simulated.content.blocks.behaviour.HoldTipBehaviour;
import dev.simulated_team.simulated.data.SimLang;
import dev.simulated_team.simulated.index.SimClickInteractions;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class ThrottleLeverBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    protected int state = 0;
    protected int lastChange;
    protected LerpedFloat clientAngle;
    public final LerpedFloat clientPressedLerp = LerpedFloat.linear().chase(0, 0.45, LerpedFloat.Chaser.EXP);
    private static final MutableComponent HOLD_TIP = SimLang.translate("gui.hold_tip.hold_to_adjust").component();

    public ThrottleLeverBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
        this.clientAngle = LerpedFloat.linear();
    }

    @Override
    public void write(final CompoundTag compound, final HolderLookup.Provider registries, final boolean clientPacket) {
        compound.putInt("State", this.state);
        compound.putInt("ChangeTimer", this.lastChange);
        super.write(compound, registries, clientPacket);
    }

    @Override
    protected void read(final CompoundTag compound, final HolderLookup.Provider registries, final boolean clientPacket) {
        this.state = compound.getIntOr("State", 0);
        this.lastChange = compound.getIntOr("ChangeTimer", 0);
        this.clientAngle.chase(this.getBlockState().getValue(ThrottleLeverBlock.INVERTED) ? 15 - this.state : this.state, 0.5f, LerpedFloat.Chaser.EXP);
        super.read(compound, registries, clientPacket);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.lastChange > 0) {
            this.lastChange--;
            if (this.lastChange == 0)
                this.updateOutput();
        }

        if (this.level.isClientSide()) {
            this.clientAngle.tickChaser();
            final boolean pressed = SimClickInteractions.THROTTLE_LEVER_MANAGER.isBlockActive(this.getBlockPos());
            this.clientPressedLerp.updateChaseTarget(pressed ? 1 : 0);
            this.clientPressedLerp.tickChaser();

            ThrottleLeverClientGripHandler.tickGrip(this);
        }
    }

    @Override
    public void initialize() {
        super.initialize();

    }

    private void updateOutput() {
        ThrottleLeverBlock.updateNeighbors(this.getBlockState(), this.level, this.worldPosition);
    }

    @Override
    public void addBehaviours(final List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(new HoldTipBehaviour(this, HOLD_TIP));
    }

    public void changeState(final boolean back) {
        final int prevState = this.state;
        this.state += back ? -1 : 1;
        this.state = Mth.clamp(this.state, 0, 15);
        if (prevState != this.state)
            this.lastChange = 15;
        this.sendData();
    }

    @Override
    public boolean addToGoggleTooltip(final List<Component> tooltip, final boolean isPlayerSneaking) {
        new CommonLangBuilder("create")
                .translate("tooltip.analogStrength", this.state)
                .forGoggles(tooltip);
        return true;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return AABB.ofSize(this.getBlockPos().getCenter(), 1.5, 1.5, 1.5);
    }

    public int getState() {
        return this.state;
    }

    public void setSignal(final int signal) {
        this.state = this.getBlockState().getValue(ThrottleLeverBlock.INVERTED) ? 15 - signal : signal;
        this.lastChange = 2;
        this.level.playSound(null, this.getBlockPos(), SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.2F, 0.25F + (float)(signal + 5) / 15.0F * 0.5F);
        this.sendData();
    }
}
