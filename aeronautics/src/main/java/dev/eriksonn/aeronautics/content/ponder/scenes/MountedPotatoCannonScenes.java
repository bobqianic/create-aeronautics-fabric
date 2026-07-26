package dev.eriksonn.aeronautics.content.ponder.scenes;

import com.zurrtum.create.AllEntityTypes;
import com.zurrtum.create.content.equipment.potatoCannon.PotatoProjectileEntity;
import com.zurrtum.create.infrastructure.particle.AirParticleData;
import com.zurrtum.create.client.foundation.ponder.CreateSceneBuilder;
import dev.eriksonn.aeronautics.content.blocks.mounted_potato_cannon.MountedPotatoCannonBlockEntity;
import com.zurrtum.create.catnip.math.Pointing;
import com.zurrtum.create.catnip.nbt.NBTHelper;
import com.zurrtum.create.client.ponder.api.PonderPalette;
import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.EntityElement;
import com.zurrtum.create.client.ponder.api.scene.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.Random;

public class MountedPotatoCannonScenes {
	public static void mountedPotatoCannonIntro(final SceneBuilder builder, final SceneBuildingUtil util) {
		final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		final CreateSceneBuilder.WorldInstructions world = scene.world();
		final SelectionUtil select = util.select();
		final OverlayInstructions overlay = scene.overlay();
		final VectorUtil vector = util.vector();
		final PositionUtil grid = util.grid();

		scene.title("mounted_potato_cannon", "Using the Mounted Potato Cannon");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();

		final BlockPos cannon = grid.at(2, 2, 2);
		final BlockPos lever = grid.at(1, 2, 3);
		final BlockPos beltInput = grid.at(0, 1, 3);

		final Selection cannonAll = select.fromTo(2, 1, 2, 2, 2, 3);
		final Selection itemInput = select.fromTo(0, 1, 2, 1, 2, 5).substract(select.position(lever)).add(select.position(2, 0, 5));
		final Selection kinetics = select.fromTo(3, 0, 2, 5, 2, 2).substract(select.fromTo(3, 0, 2, 4, 0, 2));

		final ItemStack potato = new ItemStack(Items.BAKED_POTATO);
		final Vec3 aimingVec = vector.of(0, 0, -0.9);
		final Vec3 barrelVec = vector.centerOf(cannon).add(aimingVec.scale(1.2));

		final Random rnd = new Random();

		world.showSection(cannonAll.substract(select.position(cannon)), Direction.UP);
		world.showSection(select.position(lever), Direction.UP);
		scene.idle(15);
		world.showSection(select.position(cannon), Direction.DOWN);
		scene.idle(20);
		overlay.showText(80)
				.text("The Mounted Potato Cannon is a block version of the Potato Cannon")
				.placeNearTarget()
				.pointAt(vector.centerOf(cannon));
		scene.idle(100);
		overlay.showText(70)
				.text("It accepts the same ammo as the Potato Cannon...")
				.colored(PonderPalette.INPUT)
				.pointAt(vector.centerOf(cannon))
				.placeNearTarget()
				.attachKeyFrame();
		scene.idle(10);
		overlay.showControls(vector.blockSurface(cannon, Direction.UP), Pointing.DOWN, 30)
				.withItem(potato)
				.rightClick();
		scene.idle(70);
		world.showSection(itemInput, Direction.DOWN);
		world.setKineticSpeed(select.position(2, 0, 5), 32);
		world.setKineticSpeed(select.fromTo(1, 1, 5, 0, 1, 2), -32);
		scene.idle(20);
		overlay.showText(80)
				.text("...and can be loaded by automated means")
				.colored(PonderPalette.INPUT)
				.pointAt(vector.centerOf(cannon.west()))
				.placeNearTarget();
		scene.idle(10);
		for (int i = 0; i < 4; i++) {
			world.createItemOnBelt(beltInput, Direction.WEST, potato);
			scene.idle(20);
			world.removeItemsFromBelt(grid.at(1, 1, 3));
			world.flapFunnel(grid.at(1, 2, 3), false);
		}
		scene.idle(20);
		world.hideSection(itemInput, Direction.UP);
		scene.idle(30);
		world.showSection(kinetics, Direction.DOWN);
		scene.idle(6);
		world.setKineticSpeed(select.position(cannon), -16);
		world.setKineticSpeed(select.fromTo(5, 2, 2, 3, 2, 2), -16);
		world.setKineticSpeed(select.fromTo(5, 0, 2, 5, 1, 2), 16);
		windUpMountedPotatoCannon(scene, select.position(cannon), 30);
		overlay.showText(80)
				.attachKeyFrame()
				.text("Use rotational force to charge up the Cannon")
				.placeNearTarget()
				.pointAt(vector.blockSurface(cannon, Direction.WEST));
		scene.idle(100);
		overlay.showText(80)
				.attachKeyFrame()
				.text("After the Cannon is fully charged, power it with redstone to fire")
				.colored(PonderPalette.RED)
				.placeNearTarget()
				.pointAt(vector.blockSurface(lever, Direction.EAST).add(-0.25, 0, 0));
		scene.idle(40);
		scene.effects().indicateRedstone(lever);
		world.toggleRedstonePower(select.position(lever));
		world.cycleBlockProperty(cannon, BlockStateProperties.POWERED);

		for (int i = 0; i < 3; ++i) {
			if (i == 1) {
				overlay.showText(60)
						.text("The Cannon can be continuously powered for automatic fire")
						.colored(PonderPalette.BLUE)
						.placeNearTarget()
						.pointAt(vector.centerOf(cannon));
			}
			fireMountedPotatoCannon(scene, select.position(cannon));
			playMountedPotatoCannonParticles(scene, barrelVec, aimingVec, rnd);
			spawnPotatoCannonProjectile(scene, potato, barrelVec, aimingVec, false);
			scene.idle(10);
			windUpMountedPotatoCannon(scene, select.position(cannon), 30);
			if (i == 2) {
				scene.effects().indicateRedstone(lever);
				world.toggleRedstonePower(select.position(lever));
				world.cycleBlockProperty(cannon, BlockStateProperties.POWERED);
			}
			scene.idle(50);
		}
	}

	private static void windUpMountedPotatoCannon(final SceneBuilder scene, final Selection cannon, final int windupTime) {
		//todo fix snapping to fully inflated in one tick, maybe fix removing current kinetic speed too that'd be nice
		scene.world().modifyBlockEntityNBT(cannon, MountedPotatoCannonBlockEntity.class, tag -> {
			final CompoundTag inventory = new CompoundTag();
			inventory.put("item", ItemStack.OPTIONAL_CODEC.encodeStart(NbtOps.INSTANCE, new ItemStack(Items.POTATO)).result().orElseThrow());
			tag.put("inventory", inventory);
			tag.putInt("ItemTimer", 20);
			NBTHelper.writeEnum(tag, "State", MountedPotatoCannonBlockEntity.State.CHARGING);
			tag.putInt("ChargeTimer", windupTime);
		});
	}

	private static void fireMountedPotatoCannon(final SceneBuilder scene, final Selection cannon) {
		scene.world().modifyBlockEntityNBT(cannon, MountedPotatoCannonBlockEntity.class, tag -> {
			tag.put("inventory", new CompoundTag());
			tag.putInt("BarrelTimer", 0);
			tag.putInt("ItemTimer", 0);
			NBTHelper.writeEnum(tag, "State", MountedPotatoCannonBlockEntity.State.FIRING);
		});
	}

	private static void playMountedPotatoCannonParticles(final SceneBuilder scene, final Vec3 pos, final Vec3 aiming, final Random rnd) {
		final EffectInstructions effects = scene.effects();
		for (int i = 0; i < 8; i++) {
			final Vec3 vel = aiming.add(new Vec3(rnd.nextDouble() - 0.5, rnd.nextDouble() - 0.5, rnd.nextDouble() - 0.5)).scale(1.5);
			effects.emitParticles(pos, effects.simpleParticleEmitter(new AirParticleData(0.5f, 0.1f), vel), 1, 1);
		}
	}

	private static ElementLink<EntityElement> spawnPotatoCannonProjectile(final SceneBuilder scene, final ItemStack stack, final Vec3 pos, final Vec3 aiming, final boolean physics) {
		return scene.world().createEntity(level -> {
			final PotatoProjectileEntity entity = Objects.requireNonNull(AllEntityTypes.POTATO_PROJECTILE.create(level, EntitySpawnReason.SPAWN_ITEM_USE), "entity");
			entity.setItem(stack);
			entity.setPos(pos);
			entity.xo = pos.x;
			entity.yo = pos.y;
			entity.zo = pos.z;
			entity.setDeltaMovement(aiming.scale(2.5));
			entity.noPhysics = !physics;
			return entity;
		});
	}
}
