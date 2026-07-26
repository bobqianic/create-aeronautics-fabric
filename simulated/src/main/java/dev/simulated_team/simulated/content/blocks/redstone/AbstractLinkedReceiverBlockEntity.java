package dev.simulated_team.simulated.content.blocks.redstone;

import com.zurrtum.create.Create;
import com.zurrtum.create.content.redstone.link.IRedstoneLinkable;
import com.zurrtum.create.content.redstone.link.ServerLinkBehaviour;
import com.zurrtum.create.content.redstone.link.RedstoneLinkBlock;
import com.zurrtum.create.content.redstone.link.RedstoneLinkNetworkHandler;
import dev.simulated_team.simulated.compat.create.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import dev.simulated_team.simulated.compat.create.ValueBoxTransform;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.SubLevel;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractLinkedReceiverBlockEntity extends SmartBlockEntity {

    protected ServerLinkBehaviour link;
    protected int lastCheckedStatus;
    public int receivedSignal;
    public double rawSignalValue;
    protected boolean receivedSignalChanged;

    public AbstractLinkedReceiverBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(final List<BlockEntityBehaviour<?>> behaviours) {
        this.createLink();
        behaviours.add(this.link);
    }

    protected void createLink() {
        this.link = ServerLinkBehaviour.receiver(this, signal -> setSignal(signal, signal));
    }

    public void setSignal(final int power, final double rawValue) {
        if (this.receivedSignal != power || rawValue != this.rawSignalValue)
            this.receivedSignalChanged = true;
        this.receivedSignal = power;
        this.rawSignalValue = rawValue;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level.isClientSide()) {
            return;
        }

        final int networkStatus = Create.REDSTONE_LINK_NETWORK_HANDLER.globalPowerVersion.get();
        if (networkStatus != this.lastCheckedStatus) {
            this.lastCheckedStatus = networkStatus;
        }
        this.updateSignal();

        final BlockState blockState = this.getBlockState();

        if ((this.getReceivedSignal() > 0) != blockState.getValue(RedstoneLinkBlock.POWERED)) {
            this.receivedSignalChanged = true;
            this.level.setBlockAndUpdate(this.worldPosition, blockState.cycle(RedstoneLinkBlock.POWERED));
        }

        if (this.receivedSignalChanged) {
            final Direction attachedFace = blockState.getValue(RedstoneLinkBlock.FACING)
                    .getOpposite();
            final BlockPos attachedPos = this.worldPosition.relative(attachedFace);
            this.level.updateNeighborsAt(this.worldPosition, this.level.getBlockState(this.worldPosition).getBlock());
            this.level.updateNeighborsAt(attachedPos, this.level.getBlockState(attachedPos).getBlock());
            this.receivedSignalChanged = false;
            //sendData();
        }

    }

    public void updateSignal() {
        int newSignal = 0;
        double rawValue = 0;
        final Map<Couple<RedstoneLinkNetworkHandler.Frequency>, Set<IRedstoneLinkable>> map = Create.REDSTONE_LINK_NETWORK_HANDLER.networksIn(this.level);

        final Couple<RedstoneLinkNetworkHandler.Frequency> freq = this.link.getNetworkKey();
        final Set<IRedstoneLinkable> set = map.get(freq);
        if (set != null && !set.isEmpty()) {
            Vector3d currentPos = JOMLConversion.atCenterOf(this.getBlockPos());

            final SubLevel subLevel = Sable.HELPER.getContaining(this);
            if (subLevel != null) {
                subLevel.logicalPose().transformPosition(currentPos);
            }

            for (final IRedstoneLinkable link : set)
                if (link.getTransmittedStrength() > 0) {
                    Vector3d targetPos = JOMLConversion.atCenterOf(link.getLocation());

                    final SubLevel targetWs = Sable.HELPER.getContaining(this.level, link.getLocation());
                    if (targetWs != null) {
                        targetWs.logicalPose().transformPosition(targetPos);
                    }

                    Vector3d relativePos = targetPos.sub(currentPos);
                    if(subLevel != null) {
                        subLevel.logicalPose().transformNormalInverse(relativePos);
                    }

                    final Tuple<Integer, Double> signal = this.getSignalFromLink(JOMLConversion.toMojang(relativePos), link.getTransmittedStrength());
                    if (signal.getA() > newSignal) {
                        newSignal = signal.getA();
                        rawValue = signal.getB();
                    }
                }
        }
        this.setSignal(newSignal, rawValue);
    }

    @Override
    public void write(final CompoundTag compound, final HolderLookup.Provider registries, final boolean clientPacket) {
        compound.putInt("Receive", this.getReceivedSignal());
        compound.putDouble("ReceivedValue", this.rawSignalValue);
        compound.putBoolean("ReceivedChanged", this.receivedSignalChanged);
        super.write(compound, registries, clientPacket);
    }

    @Override
    protected void read(final CompoundTag compound, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        this.receivedSignal = compound.getIntOr("Receive", 0);
        this.rawSignalValue = compound.getDoubleOr("ReceivedValue", 0);
        this.receivedSignalChanged = compound.getBooleanOr("ReceivedChanged", false);
    }

    public int getReceivedSignal() {
        return this.receivedSignal;
    }

    public Couple<RedstoneLinkNetworkHandler.Frequency> getFrequency() {
        return this.link.getNetworkKey();
    }

    public abstract Tuple<Integer, Double> getSignalFromLink(Vec3 relativePosition, int transmittedStrength);

    @Override
    public void remove() {
        super.remove();

        final Direction attachedFace = this.getBlockState().getValue(RedstoneLinkBlock.FACING)
                .getOpposite();
        final BlockPos attachedPos = this.worldPosition.relative(attachedFace);
        this.level.updateNeighborsAt(this.worldPosition, this.level.getBlockState(this.worldPosition).getBlock());
        this.level.updateNeighborsAt(attachedPos, this.level.getBlockState(attachedPos).getBlock());
    }
}
