package dev.simulated_team.simulated.content.blocks.rope.rope_winch;

import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import dev.simulated_team.simulated.compat.create.LegacyKineticBlockEntity;
import com.zurrtum.create.content.redstone.thresholdSwitch.ThresholdSwitchObservable;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.simulated_team.simulated.api.SimpleResourceManager;
import dev.simulated_team.simulated.config.server.blocks.SimBlockConfigs;
import dev.simulated_team.simulated.content.blocks.rope.RopeStrandHolderBehavior;
import dev.simulated_team.simulated.content.blocks.rope.RopeStrandHolderBlockEntity;
import dev.simulated_team.simulated.content.blocks.rope.rope_connector.RopeConnectorBlockEntity;
import dev.simulated_team.simulated.content.blocks.rope.strand.client.ClientRopeStrand;
import dev.simulated_team.simulated.content.blocks.rope.strand.server.ServerRopeStrand;
import dev.simulated_team.simulated.data.SimLang;
import dev.simulated_team.simulated.index.SimSoundEvents;
import dev.simulated_team.simulated.service.SimConfigService;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.util.List;

public class RopeWinchBlockEntity extends LegacyKineticBlockEntity implements RopeStrandHolderBlockEntity, ThresholdSwitchObservable {
    private RopeStrandHolderBehavior ropeHolder;

    private int stretchTimer = 0;
    private boolean stretched;
    private boolean stretchedLastTick;

    protected LerpedFloat clientAngle = LerpedFloat.linear();

    public RopeWinchBlockEntity(final BlockEntityType<?> typeIn, final BlockPos pos, final BlockState state) {
        super(typeIn, pos, state);
    }

    public RopeStrandHolderBehavior getRopeHolder() {
        return this.ropeHolder;
    }

    public void addBehaviours(final List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        behaviours.add(this.ropeHolder = new RopeStrandHolderBehavior(this));
    }

    @Override
    protected void read(final CompoundTag compound, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.read(compound, registries, clientPacket);
    }

    @Override
    protected void write(final CompoundTag compound, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.write(compound, registries, clientPacket);
    }

    @Override
    public void tick() {
        super.tick();

        final ServerRopeStrand strand = this.ropeHolder.getOwnedStrand();
        final boolean hasRope = strand != null;

        if (this.level.isClientSide()) {
            this.invalidateRenderBoundingBox();
            this.clientAngle.setValue(this.clientAngle.getValue() + this.getMovementSpeed());
        }

        if (!this.level.isClientSide() && hasRope && this.ropeHolder.ownsRope()) {
            this.updateRopeStrandExtension(strand);
        }

        if (this.stretchTimer > 0) {
            this.stretchTimer--;
        }
    }

    /**
     * Updates the extension of the attached rope strand on the server
     *
     * @param strand the rope strand to update
     */
    private void updateRopeStrandExtension(final ServerRopeStrand strand) {
        final SimBlockConfigs config = SimConfigService.INSTANCE.server().blocks;

        float movementSpeed = this.getMovementSpeed();

        final double desiredExtension = strand.getExtension() + (strand.getPoints().size() - 2) * ServerRopeStrand.SEGMENT_LENGTH;
        final double currentExtension = strand.getCurrentExtension();

        this.stretched = currentExtension > desiredExtension * (1.0 + (config.maxRopeStretchAllowed.get() / 100.0));
        if (this.stretched) {
            if (!this.stretchedLastTick) {
                this.effects.triggerOverStressedEffect();
            }

            if (this.stretchTimer == 0) {
                this.stretchTimer = this.level.random.nextIntBetweenInclusive(5 * 20, 15 * 20);
                this.level.playSound(null, this.getBlockPos(), SimSoundEvents.ROPE_WINCH_STRETCH.event(), SoundSource.BLOCKS, 0.1f, 0.8f + this.level.random.nextFloat() * 0.2f);
            }

            movementSpeed = Math.max(0.0f, movementSpeed);
        }
        this.stretchedLastTick = this.stretched;

        if (currentExtension > config.maxRopeRange.get()) {
            movementSpeed = Math.min(0.0f, movementSpeed);
        }

        // extension of the source-most segment
        double extension = strand.getExtension();
        extension += movementSpeed;

        final int minPointCount = 2;

        if (extension < 1.0 && strand.getPoints().size() == minPointCount) {
            extension = 1.0;
        } else {
            while (extension < 0.0) {
                strand.removeFirstPoint();
                extension += ServerRopeStrand.SEGMENT_LENGTH;

                if (extension < 1.0 && strand.getPoints().size() == minPointCount) {
                    extension = 1.0;
                    break;
                }
            }

            while (extension > ServerRopeStrand.SEGMENT_LENGTH) {
                final Vector3d point = JOMLConversion.toJOML(Sable.HELPER.projectOutOfSubLevel(this.level, this.ropeHolder.getAttachmentPoint()));
                strand.addPoint(point);
                extension -= 1.0;
            }

            if (extension < 1.0 && strand.getPoints().size() <= minPointCount) {
                extension = 1.0;
            }
        }

        strand.updateFirstSegmentExtension(extension);
    }

    @Override
    public AABB getRenderBoundingBox() {
        final ClientRopeStrand rope = this.ropeHolder.getClientStrand();
        if (rope != null && this.ropeHolder.ownsRope()) {
            final AABB bounds = rope.getBounds();

            if (bounds == null) {
                return super.getRenderBoundingBox();
            }

            return bounds.inflate(RopeConnectorBlockEntity.RENDER_BOUNDING_BOX_INFLATION);
        } else {
            return super.getRenderBoundingBox();
        }
    }

    public float getMovementSpeed() {
        return Mth.clamp(convertToLinear(this.getSpeed()), -.49f, .49f);
    }

    @Override
    public RopeStrandHolderBehavior getBehavior() {
        return this.ropeHolder;
    }

    @Override
    public Vec3 getAttachmentPoint(final BlockPos pos, final BlockState state) {
        return pos.getCenter();
    }

    @Override
    public int getMaxValue() {
        return (int) SimConfigService.INSTANCE.server().blocks.maxRopeRange.getF();
    }

    @Override
    public int getMinValue() {
        return 0;
    }

    @Override
    public int getCurrentValue() {
        final ServerRopeStrand strand = this.ropeHolder.getOwnedStrand();
        if (strand != null) {
            return (int) strand.getCurrentExtension();
        }
        return 0;
    }

    @Override
    public MutableComponent format(final int value) {
        return SimLang.translate("gui.threshold_switch.rope_winch_length", value).component();
    }
}
