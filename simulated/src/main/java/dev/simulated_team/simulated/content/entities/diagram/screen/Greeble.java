package dev.simulated_team.simulated.content.entities.diagram.screen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record Greeble(ResourceLocation texture, List<TextureSlice> slices, int width, int height, float weight) {
	public static final Codec<Greeble> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.fieldOf("texture").forGetter(g -> g.texture),
			TextureSlice.CODEC.listOf().fieldOf("slices").forGetter(g -> g.slices),
			Codec.INT.fieldOf("width").forGetter(g -> g.width),
			Codec.INT.fieldOf("height").forGetter(g -> g.height),
			Codec.FLOAT.optionalFieldOf("weight", 100f).forGetter(g -> g.weight)
	).apply(instance, Greeble::new));

	public TextureSlice random(RandomSource random) {
		return this.slices.get(random.nextInt(this.slices.size()));
	}

	public ArrayList<TextureSlice> shuffled() {
		final ArrayList<TextureSlice> list = new ArrayList<>(this.slices());
		Collections.shuffle(list);
		return list;
	}

	public record TextureSlice(int x, int y, int width, int height) {
		public static Codec<TextureSlice> CODEC = Codec.INT.listOf(4, 4).xmap(TextureSlice::new, TextureSlice::asList);

		public TextureSlice(final List<Integer> list) {
			this(list.get(0), list.get(1), list.get(2), list.get(3));
		}

		public List<Integer> asList() {
			return List.of(this.x(), this.y(), this.width(), this.height());
		}
	}
}
