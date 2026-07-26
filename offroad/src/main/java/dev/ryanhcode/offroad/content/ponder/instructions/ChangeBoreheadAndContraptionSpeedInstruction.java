package dev.ryanhcode.offroad.content.ponder.instructions;

import dev.ryanhcode.offroad.content.blocks.borehead_bearing.BoreheadBearingBlockEntity;
import dev.ryanhcode.offroad.index.OffroadBlockEntityTypes;
import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.WorldSectionElement;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.instruction.PonderInstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class ChangeBoreheadAndContraptionSpeedInstruction extends PonderInstruction {
    private final BlockPos boreheadBearingPos;
    private final float targetSpeed;
    private final ElementLink<WorldSectionElement> contraption;
    private final RotationAxis axis;

    public boolean startSlowing;
    public boolean forcedStop;

    private boolean stopped;

    private boolean firstTick = true;

    public ChangeBoreheadAndContraptionSpeedInstruction(final BlockPos boreheadBearingPos, final ElementLink<WorldSectionElement> contraption, final RotationAxis axis, final float targetSpeed) {
        this.boreheadBearingPos = boreheadBearingPos;
        this.targetSpeed = targetSpeed;
        this.contraption = contraption;

        this.axis = axis;
    }

    @Override
    public boolean isComplete() {
        if (this.forcedStop) {
            return true;
        } else {
            return this.stopped;
        }
    }

    @Override
    public void tick(final PonderScene scene) {
        final Optional<BoreheadBearingBlockEntity> be = scene.getWorld().getBlockEntity(this.boreheadBearingPos, OffroadBlockEntityTypes.BOREHEAD_BEARING.get());
        if (be.isPresent()) {
            final BoreheadBearingBlockEntity bhb = be.get();

            if (this.firstTick) {
                bhb.setSpeed(this.targetSpeed);
                this.firstTick = false;
            }

            final WorldSectionElement resolve = scene.resolve(this.contraption);
            if (resolve != null) {
                final float currentRotationRate = bhb.getRotationSpeed();

                Vec3 vec3 = resolve.getAnimatedRotation();
                switch (this.axis) {
                    case X -> vec3 = vec3.add(currentRotationRate, 0, 0);
                    case Y -> vec3 = vec3.add(0, currentRotationRate, 0);
                    case Z -> vec3 = vec3.add(0, 0, currentRotationRate);
                    default -> vec3 = null;
                }

                resolve.setAnimatedRotation(vec3, false);
                if (this.startSlowing && resolve.getAnimatedRotation().length() < 0.1) {
                    this.stopped = true;
                }
            }
        }
    }

    @Override
    public void reset(final PonderScene scene) {
        super.reset(scene);

        this.firstTick = true;
        this.stopped = false;
        this.forcedStop = false;
        this.startSlowing = false;
    }

    public enum RotationAxis {
        X, Y, Z
    }
}
