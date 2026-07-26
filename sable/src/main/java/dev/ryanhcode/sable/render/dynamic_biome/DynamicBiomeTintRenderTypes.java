package dev.ryanhcode.sable.render.dynamic_biome;

// PORT-NOTE(mc26.1): RenderType's constructor is private now (RenderSetup factory only), so this class
// can no longer subclass RenderType. The Veil-based dynamic biome tint render type was already disabled
// (body commented out upstream); this remains an unreferenced placeholder until the feature is rebuilt
// on the 26.1 render pipeline.
public class DynamicBiomeTintRenderTypes {
    private static final String NAME = "dynamic_biome_tint";

    private DynamicBiomeTintRenderTypes() {
    }
//
//    public static void hello() {
//        RenderType.CompositeState rendertype$state = RenderType.CompositeState.builder()
//                .setShaderState(VeilRenderBridge.shaderState(Sable.path("hello_there")))
//                .setTextureState(BLOCK_SHEET_MIPPED)
//                .setLightmapState(LIGHTMAP)
//                .createCompositeState(true);
//
//        RenderType.create(NAME,DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, RenderType.BIG_BUFFER_SIZE, true, false, rendertype$state);
//
//    }
}
