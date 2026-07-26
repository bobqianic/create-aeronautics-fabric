package dev.eriksonn.aeronautics.content.blocks.mounted_potato_cannon;

import com.zurrtum.create.AllEntityTypes;
import com.zurrtum.create.AllSoundEvents;
import dev.simulated_team.simulated.compat.create.IHaveGoggleInformation;
import com.zurrtum.create.api.equipment.potatoCannon.PotatoCannonProjectileType;
import com.zurrtum.create.content.equipment.potatoCannon.PotatoCannonItem;
import com.zurrtum.create.content.equipment.potatoCannon.PotatoProjectileEntity;
import com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.infrastructure.particle.AirParticleData;
import dev.eriksonn.aeronautics.data.AeroLang;
import dev.eriksonn.aeronautics.index.AeroAdvancements;
import dev.eriksonn.aeronautics.mixinterface.PotatoProjectileEntityExtension;
import dev.eriksonn.aeronautics.config.AeroConfig;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.List;

public class MountedPotatoCannonBlockEntity extends KineticBlockEntity implements BlockEntitySubLevelActor, IHaveGoggleInformation, Clearable {

	private static final Vector3d RECOIL_DIR = new Vector3d();
	private static final Vector3d RECOIL_CENTER = new Vector3d();

	private State currentState;
	private final MountedPotatoCannonInventory inventory;

	private float chargeTimer;
	private int initialAmmoReloadTicks;

	private float recoilMagnitude;

	boolean needsClientUpdate;

	private boolean blocked;
	private double blockedLength;

	// client visualization
	private int itemRotationId;
	private int barrelTimer;
	private int itemTimer;
	private float animationSpeed;
	private float angle;
	private float previousAngle;

	public MountedPotatoCannonBlockEntity(final BlockEntityType<?> typeIn, final BlockPos pos, final BlockState state) {
		super(typeIn, pos, state);

		this.currentState = State.CHARGING;
		this.initialAmmoReloadTicks = -1;
		this.chargeTimer = 0;
		this.recoilMagnitude = 0;
		this.inventory = new MountedPotatoCannonInventory(this);
		this.barrelTimer = 100;

		this.needsClientUpdate = false;

		//client
		this.itemTimer = 20;
	}

	@Override
	public void initialize() {
		super.initialize();

		this.inventory.updateCachedType(this.level.registryAccess(), this.inventory.slot.getStack());
		this.resetAndUpdate();
	}

	@Override
	public void tick() {
		super.tick();

		if (this.recoilMagnitude > 0) {
			this.recoilMagnitude *= 0.5f;
		}

		if (this.level.isClientSide()) {
			previousAngle = angle;
			float targetSpeed = Math.abs(speed);
			float maxTarget = 32;
			targetSpeed = (float)(1 - Math.exp(-targetSpeed / maxTarget)) * maxTarget;
			targetSpeed *= 3 / 10f;
			animationSpeed += (targetSpeed-animationSpeed)*0.3f;
			angle += animationSpeed;
			if(angle > 360)
			{
				angle -= 360;
				previousAngle -= 360;
			}
		}
		if (this.itemTimer < 20) {
			this.itemTimer++;
		}

		if (this.barrelTimer < 100) {
			this.barrelTimer++;
		}

		this.updateBlockedState();

		switch (this.currentState) {
			case CHARGING -> {
				if (this.initialAmmoReloadTicks != -1) {
					if (this.chargeTimer < 1) {
						this.chargeTimer += this.getChargeUpSpeed();
					}

					if (this.chargeTimer >= 1) {
						this.currentState = State.CHARGED;
					}
				}
			}
			case CHARGED -> {
				boolean internalBlocked = this.blocked;
				final BlockState state = this.level.getBlockState(this.getBlockPos().relative(this.getBlockState().getValue(DirectionalKineticBlock.FACING)));
				if (Blocks.MANGROVE_TRAPDOOR == state.getBlock()) {
					internalBlocked = false;
				}

				if (!internalBlocked && this.getBlockState().getValue(MountedPotatoCannonBlock.POWERED)) {
					this.currentState = State.FIRING;
					this.barrelTimer = 0;
				}
			}
			case FIRING -> {
				animationSpeed = barrelTimer * 75;
				//wait 2 ticks before firing to sync up visuals
				if (this.barrelTimer > 2) {
					if (this.level.isClientSide()) {
						this.speed = 0.2F;
					}

					final PotatoCannonItem.Ammo ammo = this.getInventory().getAmmo();
					if (ammo != null) {
						this.getInventory().extractSlot(0, 1, false);

						Vec3 barrelPos = this.getBarrelPos();
						final BlockState state = this.level.getBlockState(this.getBlockPos().relative(this.getBlockState().getValue(DirectionalKineticBlock.FACING)));
						if (this.blocked && Blocks.MANGROVE_TRAPDOOR == state.getBlock()) {
							barrelPos = new Vec3(this.getBlockPos().getX() + 0.5, this.getBlockPos().getY() + 0.5, this.getBlockPos().getZ() + 0.5).add(this.getAimingVector().scale(this.blockedLength / 1.725f));
						}

						if (!this.level.isClientSide()) {
							final PotatoCannonProjectileType type = ammo.type();

							//motion
							final Vec3 motion = this.getAimingVector().scale((double) type.velocityMultiplier() * 2 /*cannon range*/);

							//spray
							final Vec3 sprayBase = VecHelper.rotate(new Vec3(0.0d, 0.1d, 0.0d), 360.0f * this.level.random.nextFloat(), Direction.Axis.Z);
							final float sprayChange = 360.0f / (float) type.split();

							for (int i = 0; i < type.split(); i++) {
								final PotatoProjectileEntity shootyBoomBoom = AllEntityTypes.POTATO_PROJECTILE.create(this.getLevel(), EntitySpawnReason.SPAWN_ITEM_USE);
								if (shootyBoomBoom != null) {
									shootyBoomBoom.setItem(ammo.stack());
									((PotatoProjectileEntityExtension) shootyBoomBoom).aeronautics$setDamageMultiplier(2);
									((PotatoProjectileEntityExtension) shootyBoomBoom).aeronautics$setIsFromMountedPotatoCannon(true);

									Vec3 splitMotion = motion;
									if (type.split() > 1) {
										final float imperfection = 40.0f * (this.level.random.nextFloat() - 0.5f);
										final Vec3 sprayOffset = VecHelper.rotate(sprayBase, (float) i * sprayChange + imperfection, Direction.Axis.Z);
										splitMotion = motion.add(VecHelper.lookAt(sprayOffset, motion));
									}

									shootyBoomBoom.setPos(barrelPos.x, barrelPos.y, barrelPos.z);
									shootyBoomBoom.setDeltaMovement(splitMotion);
									this.level.addFreshEntity(shootyBoomBoom);
								}
							}

							this.recoilMagnitude = ((float) type.split() / 2) * AeroConfig.server().physics.mountedPotatoCannonMagnitude.getF();

							AllSoundEvents.FWOOMP.playOnServer(this.level, this.worldPosition, 1, ammo.type().soundPitch() + this.level.random.nextFloat() * .2f);
							AeroAdvancements.HEAVIER_ARTILLERY.awardToNearby(this.getBlockPos(), this.level);
						} else {
							for (int i = 0; i < 8; i++) {
								Vec3 vel = this.getAimingVector();

								final RandomSource rnd = this.level.getRandom();

								vel = vel.add(new Vec3(rnd.nextDouble() - 0.5, rnd.nextDouble() - 0.5, rnd.nextDouble() - 0.5).scale(1.0));
								vel = vel.scale(1.5);

								this.level.addParticle(new AirParticleData(0.5f, 0.1f), barrelPos.x, barrelPos.y, barrelPos.z, vel.x, vel.y, vel.z);
							}
						}
					}

					//fire
					this.resetToCharging();
				}
			}
		}
	}

	private void updateBlockedState() {
		final Vec3 pos = new Vec3(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5);
		final Vec3 beginning = pos.add(this.getAimingVector().scale(0.65f));
		final Vec3 end = pos.add(this.getAimingVector().scale(1.15f));
		final BlockHitResult ray = this.level.clip(new ClipContext(
				beginning,
				end,
				ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE,
				CollisionContext.empty()
		));
		final Vector3dc projected = Sable.HELPER.projectOutOfSubLevel(this.getLevel(), JOMLConversion.toJOML(ray.getLocation()));
		this.blocked = ray.getType() != HitResult.Type.MISS;
		this.blockedLength = (1 - ((projected.length() - beginning.length())
				/ (end.length() - beginning.length()))) + 3 / 16f;

		if (this.blocked != this.getBlockState().getValue(MountedPotatoCannonBlock.BLOCKED)) {
			this.level.setBlockAndUpdate(this.getBlockPos(), this.getBlockState().setValue(MountedPotatoCannonBlock.BLOCKED, this.blocked));
		}
	}

	@Override
	public void sable$physicsTick(final ServerSubLevel subLevel, final RigidBodyHandle handle, final double timeStep) {
		if (this.recoilMagnitude > 0) {
			RECOIL_DIR.set(JOMLConversion.toJOML(Vec3.atLowerCornerOf(this.getBlockState().getValue(DirectionalKineticBlock.FACING).getOpposite().getUnitVec3i())));
			RECOIL_CENTER.set(JOMLConversion.toJOML(Vec3.atCenterOf(this.getBlockPos())));

			RECOIL_DIR.mul(this.recoilMagnitude);

			handle.applyImpulseAtPoint(RECOIL_CENTER, RECOIL_DIR);
		}
	}

	/**
	 * Called whenever our inventory changes stack items.
	 */
	public void resetAndUpdate() {
		this.currentState = State.CHARGING;

		this.initialAmmoReloadTicks = -1;
		this.chargeTimer = 0;
		this.itemTimer = 0;
		this.itemRotationId = -1;

		final PotatoCannonItem.Ammo ammo = this.getInventory().getAmmo();
		if (ammo != null) {
			this.initialAmmoReloadTicks = ammo.type().reloadTicks();
			this.itemRotationId = this.level.getRandom().nextInt(10000);
		}

		if (!this.level.isClientSide()) {
			this.needsClientUpdate = true;
		}
	}

	private void resetToCharging() {
		this.currentState = State.CHARGING;
		this.itemTimer = 0;
		this.chargeTimer = 0;
	}

	@Override
	protected void write(final ValueOutput compound, final boolean clientPacket) {
		super.write(compound, clientPacket);

		compound.store("inventory", CompoundTag.CODEC, this.inventory.write(this.level.registryAccess()));
		compound.putInt("ItemRotationID", this.itemRotationId);
		compound.putInt("ItemTimer", this.itemTimer);
		compound.putFloat("ChargeTimer", this.chargeTimer);
		compound.putString("State", this.currentState.name());
		compound.putInt("BarrelTimer", this.barrelTimer);

		if (clientPacket) {
			compound.putBoolean("NeedsUpdate", this.needsClientUpdate);
			this.needsClientUpdate = false;
		}
	}

	@Override
	protected void read(final ValueInput compound, final boolean clientPacket) {
		super.read(compound, clientPacket);

		this.inventory.read(compound.lookup(), compound.read("inventory", CompoundTag.CODEC).orElseGet(CompoundTag::new));
		this.inventory.updateCachedType(compound.lookup(), this.inventory.slot.getStack());
		if (clientPacket && compound.getBooleanOr("NeedsUpdate", false)) {
			this.resetAndUpdate();
		}

		this.chargeTimer = compound.getFloatOr("ChargeTimer", 0);
		this.barrelTimer = compound.getIntOr("BarrelTimer", 0);
		this.itemRotationId = compound.getIntOr("ItemRotationID", 0);
		this.itemTimer = compound.getIntOr("ItemTimer", 0);
		this.currentState = readState(compound.getStringOr("State", State.CHARGING.name()));
	}

	private static State readState(final String name) {
		for (final State state : State.values()) {
			if (state.name().equals(name)) {
				return state;
			}
		}
		return State.CHARGING;
	}

	private float getChargeUpSpeed() {
		if (this.initialAmmoReloadTicks == -1 || this.getSpeed() == 0) {
			return 0;
		}

		return (Math.abs(this.getSpeed()) / (64 * this.initialAmmoReloadTicks));
	}

	public float getBarrelDistance(final float partialTick) {
		final float normalizedTimer = (this.barrelTimer - 1 + partialTick);
		final float x = Math.max(normalizedTimer - 0.5f, 0);
		final double recoilMultiplier = 0.75;
		final float distance = (float) (Math.E * (x * recoilMultiplier) * Math.exp(-x));
		return -0.5f * distance;
	}

	public float getBellowDistance(final float partialTick) {
		final float normalizedTimer = (this.barrelTimer - 1 + partialTick);

		float distance;
		if (this.currentState == State.FIRING) {
			distance = 1 - (1 - normalizedTimer) * 0.75f;
		} else {
			distance = 1f - Math.min(this.chargeTimer + this.getChargeUpSpeed() * partialTick, 1);
		}

		distance = Math.min(distance, 1);
		distance = Math.max(distance, 0);
		distance *= 0.15F;

		return distance;
	}

	public float getCogwheelAngle(final float partialTicks) {
		return Mth.lerp(partialTicks, this.previousAngle, this.angle);
	}

	public float getCogwheelSpeed() {
		return -Mth.clamp(this.getSpeed(), -1.0F, 1.0F);
	}

	public float getItemTime(final float partialTicks) {
		return this.itemTimer + partialTicks;
	}

	public int getItemRotationId() {
		return this.itemRotationId;
	}

	public Vec3 getBarrelPos() {
		return new Vec3(this.getBlockPos().getX() + 0.5, this.getBlockPos().getY() + 0.5, this.getBlockPos().getZ() + 0.5).add(this.getAimingVector().scale(1.2));
	}

	public Vec3 getAimingVector() {
		return Vec3.atLowerCornerOf(this.getBlockState().getValue(DirectionalKineticBlock.FACING).getUnitVec3i());
	}

	public MountedPotatoCannonInventory getInventory() {
		return this.inventory;
	}

	public boolean isBlocked() {
		return this.blocked;
	}

	public double getBlockedLength() {
		return this.blockedLength;
	}

	@Override
	public AABB createRenderBoundingBox() {
		return super.createRenderBoundingBox().inflate(1);
	}

	@Override
	public boolean addToGoggleTooltip(final List<Component> tooltip, final boolean isPlayerSneaking) {
		AeroLang.emptyLine(tooltip);
		AeroLang.blockName(getBlockState()).text(":").forGoggles(tooltip);

		final PotatoCannonItem.Ammo ammo = this.inventory.getAmmo();
		if (ammo != null) {
			final PotatoCannonProjectileType type = ammo.type();

			final ItemStack currentStack = this.inventory.slot.getStack();
			AeroLang.translate("potato_cannon.ammo", currentStack.getDisplayName(), currentStack.getCount())
					.style(ChatFormatting.GRAY)
					.forGoggles(tooltip, 1);

			final float damage = type.damage() * 2.0f;
			AeroLang.translate("potato_cannon.attack_damage", damage)
					.style(ChatFormatting.DARK_GREEN)
					.forGoggles(tooltip, 1);

			if(Math.abs(getSpeed()) > 0)
				AeroLang.translate("potato_cannon.reload_ticks", Math.round(1 / this.getChargeUpSpeed()))
						.style(ChatFormatting.DARK_GREEN)
						.forGoggles(tooltip, 1);

			AeroLang.translate("potato_cannon.knockback", type.knockback())
					.style(ChatFormatting.DARK_GREEN)
					.forGoggles(tooltip, 1);

		} else {
			return false;
		}

		return true;
	}

	@Override
	public void clearContent() {
		this.inventory.clearContent();
	}

	public enum State {
		CHARGED,
		FIRING,
		CHARGING
	}
}
