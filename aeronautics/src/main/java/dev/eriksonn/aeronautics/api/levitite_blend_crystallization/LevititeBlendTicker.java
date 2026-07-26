package dev.eriksonn.aeronautics.api.levitite_blend_crystallization;

import dev.eriksonn.aeronautics.index.AeroRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;

public class LevititeBlendTicker {
	private final BlockPos pos;
	private final Level level;
	private final CrystalPropagationContext context;
	public int age;
	public int attempts = 0;
	public boolean isDormant;
	private boolean requiresCatalyst;

	// Constructor used for loading this levitite blend ticker
	public LevititeBlendTicker(final CompoundTag toDeserialize, final Level level) {
		this.level = level;
		this.pos = toDeserialize.read("pos", BlockPos.CODEC).orElse(BlockPos.ZERO);
		this.context = AeroRegistries.LEVITITE_CRYSTAL_PROPAGATION_CONTEXT.asVanillaRegistry().getValue(
				ResourceLocation.parse(toDeserialize.getStringOr("context", "aeronautics:standard")));

		this.deserialize(toDeserialize);
	}

	/**
	 * @param age              The age of this levitite blend. Decremented after its initial dormant phase
	 * @param pos              The position of this levitite blend
	 * @param level            The level of this levitite blend
	 * @param requiresCatalyst Whether this levitite blend is required to be near a catalyst
	 * @param isDormant        Whether this levitite blend is in its dormant phase
	 * @param context          The Levitite Context of this levitite blend
	 */
	public LevititeBlendTicker(final int age, @NotNull final BlockPos pos, @NotNull final Level level, final boolean requiresCatalyst, final boolean isDormant, @NotNull final CrystalPropagationContext context) {
		this.age = age;
		this.pos = pos;
		this.level = level;
		this.requiresCatalyst = requiresCatalyst;
		this.context = context;
		this.isDormant = isDormant;
	}

	/**
	 * @return Whether this levitite blend should be removed from the ticking group
	 */
	public boolean tick() {
		if (this.age > 0) {
			if (this.checkSurroundingCatalyst()) {
				return true;
			}

			this.age--;
		} else {
			// attempt to crystallize
			final FluidState state = this.level.getBlockState(this.pos).getFluidState();
			if (state.getType() instanceof LevititeBlendDummyInterface) {
				if (this.context.shouldCrystallize(this.level, this.attempts, this.isDormant)) {
					LevititeBlendHelper.crystallizeLevititeBlend(this.level, this.pos, this.context);
					return true;
				} else {
					this.context.onCrystallizationFail(this.level, this.pos, this.attempts, this.isDormant);
					this.age = this.context.getNewAge(this.level, this.attempts, this.isDormant);
					this.attempts++;
				}
			} else {
				// invalid block
				return true;
			}
		}

		return false;
	}

	private boolean checkSurroundingCatalyst() {
		if (this.requiresCatalyst) {
			for (final Direction dir : Direction.values()) {
				final CrystalPropagationContext ctx = LevititeBlendHelper.getContextFromBlock(this.level, this.pos.relative(dir));
				if (ctx != this.context) {
					continue;
				}

				return false;
			}

			//No valid catalyst around
			return true;
		}

		return false;
	}

	public BlockPos getPos() {
		return this.pos;
	}

	public LevelAccessor getLevel() {
		return this.level;
	}

	public CrystalPropagationContext getContext() {
		return this.context;
	}

	public CompoundTag serialize() {
		final CompoundTag tag = new CompoundTag();

		tag.putInt("age", this.age);
		tag.putInt("attempts", this.attempts);

		tag.putBoolean("requiresCatalyst", this.requiresCatalyst);

		tag.store("pos", BlockPos.CODEC, this.getPos());
		ResourceLocation resourceLocation = AeroRegistries.LEVITITE_CRYSTAL_PROPAGATION_CONTEXT.asVanillaRegistry().getKey(this.context);
		tag.putString("context", resourceLocation.toString());

		tag.putBoolean("isDormant", this.isDormant);
		return tag;
	}

	public void deserialize(final CompoundTag tag) {
		this.age = tag.getIntOr("age", 0);
		this.attempts = tag.getIntOr("attempts", 0);

		this.requiresCatalyst = tag.getBooleanOr("requiresCatalyst", false);

		this.isDormant = tag.getBooleanOr("isDormant", false);
	}
}
