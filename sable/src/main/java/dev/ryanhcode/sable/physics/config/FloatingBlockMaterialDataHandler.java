package dev.ryanhcode.sable.physics.config;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.physics.floating_block.FloatingBlockMaterial;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

public class FloatingBlockMaterialDataHandler {
    public static HashMap<ResourceLocation, FloatingBlockMaterial> allMaterials = new HashMap<>();

    public static void addMaterial(final ResourceLocation id, final FloatingBlockMaterial material) {
        allMaterials.put(id, material);
    }

    public static void clearMaterials() {
        allMaterials.clear();
    }

    // PORT-NOTE(mc26.1): SimpleJsonResourceReloadListener is now codec-based and generic; the base class
    // performs the JSON decode (and logs failures) that apply() used to do by hand.
    public static class ReloadListener extends SimpleJsonResourceReloadListener<FloatingBlockMaterial> {
        public static final String NAME = "floating_block_material";
        public static final ResourceLocation ID = Sable.sablePath(NAME);

        public static final ReloadListener INSTANCE = new ReloadListener();

        protected ReloadListener() {
            super(FloatingBlockMaterial.CODEC, FileToIdConverter.json("floating_materials"));
        }

        @Override
        protected void apply(final Map<ResourceLocation, FloatingBlockMaterial> map, final ResourceManager resourceManager, final ProfilerFiller profiler) {
            FloatingBlockMaterialDataHandler.allMaterials.clear();
            for (final Map.Entry<ResourceLocation, FloatingBlockMaterial> entry : map.entrySet()) {
                FloatingBlockMaterialDataHandler.addMaterial(entry.getKey(), entry.getValue());
            }
        }
    }
}
