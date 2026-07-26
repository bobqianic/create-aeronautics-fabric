package dev.simulated_team.simulated.content.blocks.lasers.laser_sensor;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.simulated_team.simulated.content.blocks.lasers.LaserBehaviour;
import com.zurrtum.create.catnip.data.Couple;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class LaserSensorInteractorBehaviour extends LaserBehaviour {
    public static final BehaviourType<LaserSensorInteractorBehaviour> TYPE = new BehaviourType<>();

    private LaserSensorBlockEntity previousSensor = null;
    public Supplier<Integer> directPower;
    public final Predicate<LaserSensorBlockEntity> filter;

    public LaserSensorInteractorBehaviour(final SmartBlockEntity be, final Supplier<Couple<Vec3>> positions, final Supplier<Float> range, final Supplier<Integer> directPower, final Predicate<LaserSensorBlockEntity> filter) {
        super(be, positions, range);
        this.directPower = directPower;
        this.filter = filter;
    }

    @Override
    public void tick() {
        final Level level = this.blockEntity.getLevel();
        if (level == null)
            return;

        super.tick();

        if (!this.checkAndUpdateSensor(this.getBlockHitResult(), this.getEntityHitResult())) {
            this.resetPrevData();
        }
    }

    private boolean checkAndUpdateSensor(@Nullable final BlockHitResult bhr, @Nullable final EntityHitResult ehr) {
        if ((bhr == null || bhr.getType() == HitResult.Type.MISS)) {
            return false;
        }

        if (this.getClosestHitResult() instanceof EntityHitResult) {
            return false;
        }

        final BlockEntity be = this.getWorld().getBlockEntity(bhr.getBlockPos());
        if (be instanceof final LaserSensorBlockEntity lbe) {
            if (this.getProperFacing(be.getBlockState()) != bhr.getDirection() || !this.filter.test(lbe)) {
                return false;
            }

            this.updateHitSensor(lbe, bhr);
        }

        return true;
    }

    private Direction getProperFacing(final BlockState sensor) {
        Direction normal = sensor.getValue(LaserSensorBlock.FACING);

        final AttachFace target = sensor.getValue(LaserSensorBlock.TARGET);
        if (target.getSerializedName().equals("ceiling"))
            normal = Direction.UP;

        if (target.getSerializedName().equals("floor"))
            normal = Direction.DOWN;

        return normal;
    }

    private void updateHitSensor(final LaserSensorBlockEntity sensorBE, final BlockHitResult context) {
        if (sensorBE != this.previousSensor)
            this.resetPrevData();

        final float distance = (float) Math.sqrt(Sable.HELPER.distanceSquaredWithSubLevels(sensorBE.getLevel(), this.getLaserPositions().get().get(true), context.getLocation()));
        sensorBE.updateFromPointer(distance, this.directPower.get());

        // Set previous sensor BE to current hit one
        this.previousSensor = sensorBE;
    }

    private void resetPrevData() {
        this.previousSensor = null;
    }

    @Override
    public BehaviourType<?> getType() {
        return super.getType();
    }
}
