package dev.simulated_team.simulated.content.item_attributes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttributeType;
import dev.ryanhcode.sable.mixinterface.block_properties.BlockStateExtension;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyTypes;
import dev.simulated_team.simulated.index.SimItemAttributeTypes;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record BlockMassItemAttribute(double mass) implements ItemAttribute {
	public static final MapCodec<BlockMassItemAttribute> CODEC = Codec.DOUBLE
			.xmap(BlockMassItemAttribute::new, BlockMassItemAttribute::mass)
			.fieldOf("value");

	public static final StreamCodec<ByteBuf, BlockMassItemAttribute> STREAM_CODEC = ByteBufCodecs.DOUBLE
			.map(BlockMassItemAttribute::new, BlockMassItemAttribute::mass);

	@Override
	public boolean appliesTo(final ItemStack stack, final Level world) {
		if(stack.getItem() instanceof final BlockItem item) {
			final BlockStateExtension extension = (BlockStateExtension) item.getBlock().defaultBlockState();
			return extension.sable$getProperty(PhysicsBlockPropertyTypes.MASS.get()) == this.mass();
		}
		return false;
	}

	@Override
	public ItemAttributeType getType() {
		return SimItemAttributeTypes.BLOCK_MASS.get();
	}

	@Override
	public String getTranslationKey() {
		return "block_mass";
	}

	@Override
	public Object[] getTranslationParameters() {
		return new Object[]{ this.mass() };
	}

	public static class Type implements ItemAttributeType {

		@Override
		public @NotNull ItemAttribute createAttribute() {
			return new BlockMassItemAttribute(1.0);
		}

		@Override
		public List<ItemAttribute> getAllAttributes(final ItemStack stack, final Level level) {
			if(stack.getItem() instanceof final BlockItem item) {
				final BlockStateExtension extension = (BlockStateExtension) item.getBlock().defaultBlockState();
				final double mass = extension.sable$getProperty(PhysicsBlockPropertyTypes.MASS.get());
				return List.of(new BlockMassItemAttribute(mass));
			}
			return List.of();
		}

		@Override
		public MapCodec<? extends ItemAttribute> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, ? extends ItemAttribute> packetCodec() {
			return STREAM_CODEC;
		}
	}
}
