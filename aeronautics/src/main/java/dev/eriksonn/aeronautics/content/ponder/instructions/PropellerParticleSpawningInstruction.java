package dev.eriksonn.aeronautics.content.ponder.instructions;

import com.mojang.math.Axis;
import dev.simulated_team.simulated.mixin.accessor.WorldSectionElementAccessor;
import dev.simulated_team.simulated.util.SimMathUtils;
import dev.eriksonn.aeronautics.content.particle.PropellerAirParticleData;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.WorldSectionElement;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.instruction.TickingInstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.Objects;

public class PropellerParticleSpawningInstruction extends TickingInstruction {

	ParticleSpawner spawner;

	public PropellerParticleSpawningInstruction(@Nullable final ElementLink<WorldSectionElement> link, final BlockPos location, final Direction direction, final int ticks, final float particleAmount, final float particleSpeed, final float radius) {
		this(link, location, direction, ticks, particleAmount, particleSpeed, radius, false);
	}

	public PropellerParticleSpawningInstruction(@Nullable final ElementLink<WorldSectionElement> link, final BlockPos location, final Direction direction, final int ticks, final float particleAmount, final float particleSpeed, final float radius, final boolean hasCollision) {
		super(false, ticks);
		spawner = new ParticleSpawner(link,location,direction,particleAmount,particleSpeed,radius,hasCollision);
	}



	@Override
	public void tick(final PonderScene scene) {
		super.tick(scene);
		spawner.tick(scene);
	}
	public static class ParticleSpawner
	{
		protected final BlockPos location;
		protected ElementLink<WorldSectionElement> link;
		protected float radius;
		protected final Quaternionf rot = new Quaternionf();

		//protected WorldSectionElement element;
		protected float particleAmount;
		protected float particleSpeed;
		protected boolean hasCollision;
		ParticleSpawner(@Nullable final ElementLink<WorldSectionElement> link, final BlockPos location, final Direction direction, final float particleAmount, final float particleSpeed, final float radius, final boolean hasCollision) {
			this.link = link;
			this.location = location;
			this.hasCollision = hasCollision;
			this.radius = radius;
			this.particleAmount = particleAmount;
			this.particleSpeed = particleSpeed / 20f;

			if (direction.getAxis().isHorizontal()) {
				this.rot.set(Axis.YP.rotationDegrees(AngleHelper.horizontalAngle(direction.getOpposite())));
			}

			this.rot.mul(Axis.XP.rotationDegrees(-90 - AngleHelper.verticalAngle(direction)));
		}
		void tick(final PonderScene scene)
		{

			final PonderLevel level = scene.getWorld();
			final float particleCount = this.particleAmount + level.random.nextFloat() - 1.0f;

			Vec3 totalOffset = VecHelper.getCenterOf(this.location);
			final Quaternionf elementRot = new Quaternionf();
			if(this.link != null) {
				WorldSectionElement element = scene.resolve(this.link);

				if (element != null) {
					final Vec3 elementOffset = element.getAnimatedOffset();
					final Vec3 rotation = new Vec3(
							Math.toRadians(element.getAnimatedRotation().x),
							Math.toRadians(element.getAnimatedRotation().y),
							Math.toRadians(element.getAnimatedRotation().z)
					);

					elementRot.mul(new Quaternionf((float) Math.sin(rotation.x / 2.0F), 0.0F, 0.0F, (float) Math.cos(rotation.x / 2.0F)));
					elementRot.mul(new Quaternionf(0.0F, 0.0F, (float) Math.sin(rotation.z / 2.0F), (float) Math.cos(rotation.z / 2.0F)));
					elementRot.mul(new Quaternionf(0.0F, (float) Math.sin(rotation.y / 2.0F), 0.0F, (float) Math.cos(rotation.y / 2.0F)));

					totalOffset = totalOffset.subtract(((WorldSectionElementAccessor) element).getCenterOfRotation());
					totalOffset = SimMathUtils.rotateQuatReverse(totalOffset, elementRot);
					totalOffset = totalOffset.add(((WorldSectionElementAccessor) element).getCenterOfRotation());
					totalOffset = totalOffset.add(elementOffset);
				}
			}

			for (int i = 0; i < particleCount; i++) {
				final double R = this.radius * Math.sqrt(level.random.nextFloat());
				final double angle = Math.PI * 2.0 * level.random.nextFloat();
				Vec3 randomOffset = VecHelper.offsetRandomly(Vec3.ZERO, RandomSource.create(), .5f);
				randomOffset = new Vec3(randomOffset.x, 0, randomOffset.z);
				Vec3 particlePos = new Vec3(Math.cos(angle) * R, 0, Math.sin(angle) * R).add(randomOffset);
				Vec3 speedVector = new Vec3(0, this.particleSpeed, 0);
				particlePos = SimMathUtils.rotateQuatReverse(particlePos, this.rot);
				particlePos = SimMathUtils.rotateQuatReverse(particlePos, elementRot);
				speedVector = SimMathUtils.rotateQuatReverse(speedVector, this.rot);
				speedVector = SimMathUtils.rotateQuatReverse(speedVector, elementRot);

				particlePos = particlePos.add(totalOffset);
				level.addParticle(new PropellerAirParticleData(this.hasCollision, true),
						particlePos.x, particlePos.y, particlePos.z,
						speedVector.x(), speedVector.y(), speedVector.z());
			}
		}
	}
}

