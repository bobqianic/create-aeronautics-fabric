package dev.simulated_team.simulated.content.blocks.rope.rope_connector;

import dev.simulated_team.simulated.compat.create.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import dev.simulated_team.simulated.content.blocks.rope.RopeStrandHolderBehavior;
import dev.simulated_team.simulated.content.blocks.rope.RopeStrandHolderBlockEntity;
import dev.simulated_team.simulated.content.blocks.rope.strand.client.ClientRopeStrand;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class RopeConnectorBlockEntity extends SmartBlockEntity implements RopeStrandHolderBlockEntity {
    public static final double RENDER_BOUNDING_BOX_INFLATION = 3.0;
    private RopeStrandHolderBehavior ropeHolder;

    public RopeConnectorBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
    }

    public RopeStrandHolderBehavior getRopeHolder() {
        return this.ropeHolder;
    }

    @Override
    public void addBehaviours(final List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(this.ropeHolder = new RopeStrandHolderBehavior(this));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level.isClientSide()) {
            this.invalidateRenderBoundingBox();
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        final ClientRopeStrand rope = this.ropeHolder.getClientStrand();
        if (rope != null && this.ropeHolder.ownsRope()) {
            final AABB bounds = rope.getBounds();

            if (bounds == null) {
                return super.getRenderBoundingBox();
            }

            return bounds.inflate(RENDER_BOUNDING_BOX_INFLATION);
        } else {
            return super.getRenderBoundingBox();
        }
    }

    @Override
    public RopeStrandHolderBehavior getBehavior() {
        return this.ropeHolder;
    }

    @Override
    public Vec3 getAttachmentPoint(final BlockPos pos, final BlockState state) {
        final Direction facing = state.getValue(RopeConnectorBlock.FACING);
        final double offset = -3.0 / 16.0;

        return pos.getCenter().add(facing.getStepX() * offset, facing.getStepY() * offset, facing.getStepZ() * offset);
    }

    public Vec3 getVisualAttachmentPoint(final BlockPos pos, final BlockState state) {
        final Direction facing = state.getValue(RopeConnectorBlock.FACING);
        final double offset = -4.0 / 16.0;

        return pos.getCenter().add(facing.getStepX() * offset, facing.getStepY() * offset, facing.getStepZ() * offset);
    }
}
