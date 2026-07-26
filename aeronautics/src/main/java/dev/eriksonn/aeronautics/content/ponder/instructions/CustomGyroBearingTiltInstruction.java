package dev.eriksonn.aeronautics.content.ponder.instructions;

import com.mojang.math.Axis;
import dev.eriksonn.aeronautics.content.blocks.propeller.bearing.gyroscopic_propeller_bearing.GyroscopicPropellerBearingBlockEntity;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.WorldSectionElement;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.instruction.TickingInstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3d;

import java.util.Objects;

public class CustomGyroBearingTiltInstruction extends TickingInstruction {

	protected final BlockPos location;
	protected final int ticks;
	protected final ElementLink<WorldSectionElement> link;
	protected final boolean directMotion;
	protected final boolean reversed;

	protected WorldSectionElement element;
	protected Quaternionf blockRot;
	protected Vec3 blockNormal;

	public CustomGyroBearingTiltInstruction(final ElementLink<WorldSectionElement> link, final BlockPos location, final int ticks, final boolean directMotion) {
		this(link, location, ticks, directMotion, false);
	}

	public CustomGyroBearingTiltInstruction(final ElementLink<WorldSectionElement> link, final BlockPos location, final int ticks, final boolean directMotion, final boolean reversed) {
		super(false, ticks);
		this.location = location;
		this.ticks = ticks;
		this.link = link;
		this.directMotion = directMotion;
		this.reversed = reversed;
	}

	static Quaternionf getBlockStateOrientation(final Direction facing) {
		final Quaternionf orientation;

		if (facing.getAxis().isHorizontal()) {
			orientation = Axis.YP.rotationDegrees(AngleHelper.horizontalAngle(facing.getOpposite()));
		} else {
			orientation = new Quaternionf();
		}

		orientation.mul(Axis.XP.rotationDegrees(-90 - AngleHelper.verticalAngle(facing)));
		return orientation;
	}

	@Override
	protected final void firstTick(final PonderScene scene) {
		super.firstTick(scene);
		final PonderLevel level = scene.getWorld();
		if (this.link != null) {
			this.element = Objects.requireNonNull(scene.resolve(this.link), "element");
		}
		if (level.getBlockState(this.location).hasProperty(BlockStateProperties.FACING)) {
			final Quaternionf q = getBlockStateOrientation(level.getBlockState(this.location).getValue(BlockStateProperties.FACING));
			this.blockNormal = Vec3.atLowerCornerOf(level.getBlockState(this.location).getValue(BlockStateProperties.FACING).getUnitVec3i());
			this.blockRot = new Quaternionf(q);
		}
	}

	@Override
	public void tick(final PonderScene scene) {
		super.tick(scene);

		final PonderLevel level = scene.getWorld();
		final BlockEntity be = level.getBlockEntity(this.location);

		if (be instanceof final GyroscopicPropellerBearingBlockEntity gbe) {
			final Vector3d target = new Vector3d(0, 1, 0);
			if (this.element != null) {
				final Vec3 rot = this.element.getAnimatedRotation();
				target.set(0, 1, 0);
				target.rotateX(Mth.DEG_TO_RAD * -rot.x)
						.rotateZ(Mth.DEG_TO_RAD * -rot.z)
						.rotateY(Mth.DEG_TO_RAD * -rot.y);
			}

			float lerpAmount = 1;
			if (!this.directMotion) {
				lerpAmount = 1 - super.remainingTicks / (float) super.totalTicks;
			}

			if (this.reversed) {
				lerpAmount = 1 - lerpAmount;
			}

			gbe.setStrictTilt(target, lerpAmount, 1);
		}
	}
}

