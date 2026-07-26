package dev.simulated_team.simulated.client.model;

import com.google.common.base.Suppliers;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Supplier;

public record BakedItemModelPart(
        List<BakedQuad> quads,
        ModelRenderProperties properties,
        Supplier<Vector3f[]> extents
) {
    public static BakedItemModelPart bake(final ModelBaker baker, final ResourceLocation id) {
        final ResolvedModel model = baker.getModel(id);
        final TextureSlots textures = model.getTopTextureSlots();
        final List<BakedQuad> quads = model.bakeTopGeometry(textures, baker, BlockModelRotation.X0_Y0).getAll();
        return new BakedItemModelPart(
                quads,
                ModelRenderProperties.fromResolvedModel(baker, model, textures),
                Suppliers.memoize(() -> BlockModelWrapper.computeExtents(quads))
        );
    }
}
