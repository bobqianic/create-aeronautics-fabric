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

public record BlockBouncinessItemAttribute(double bounciness) implements ItemAttribute {
	public static final MapCodec<BlockBouncinessItemAttribute> CODEC = Codec.DOUBLE
			.xmap(BlockBouncinessItemAttribute::new, BlockBouncinessItemAttribute::bounciness)
			.fieldOf("value");

	public static final StreamCodec<ByteBuf, BlockBouncinessItemAttribute> STREAM_CODEC = ByteBufCodecs.DOUBLE
			.map(BlockBouncinessItemAttribute::new, BlockBouncinessItemAttribute::bounciness);

	@Override
	public boolean appliesTo(final ItemStack stack, final Level world) {
		if(stack.getItem() instanceof final BlockItem item) {
			final BlockStateExtension extension = (BlockStateExtension) item.getBlock().defaultBlockState();
			return extension.sable$getProperty(PhysicsBlockPropertyTypes.FRICTION.get()) == this.bounciness();
		}
		return false;
	}

	@Override
	public ItemAttributeType getType() {
		return SimItemAttributeTypes.BLOCK_BOUNCINESS.get();
	}

	@Override
	public String getTranslationKey() {
		return "block_bounciness";
	}

	@Override
	public Object[] getTranslationParameters() {
		return new Object[]{ this.bounciness() };
	}

	public static class Type implements ItemAttributeType {

		@Override
		public @NotNull ItemAttribute createAttribute() {
			return new BlockBouncinessItemAttribute(1.0);
		}

		@Override
		public List<ItemAttribute> getAllAttributes(final ItemStack stack, final Level level) {
			if(stack.getItem() instanceof final BlockItem item) {
				final BlockStateExtension extension = (BlockStateExtension) item.getBlock().defaultBlockState();
				final double mass = extension.sable$getProperty(PhysicsBlockPropertyTypes.RESTITUTION.get());
				return List.of(new BlockBouncinessItemAttribute(mass));
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
