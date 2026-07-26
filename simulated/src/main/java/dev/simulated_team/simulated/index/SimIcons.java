package dev.simulated_team.simulated.index;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import dev.simulated_team.simulated.Simulated;
import com.zurrtum.create.client.catnip.gui.element.DelegatedStencilElement;
import com.zurrtum.create.catnip.theme.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class SimIcons extends AllIcons {
    public static final ResourceLocation ICON_ATLAS = Simulated.path("textures/gui/icons.png");
    public static final int ICON_ATLAS_SIZE = 64;

    private static int x = 0, y = -1;
    private final int iconX;
    private final int iconY;

    public static final SimIcons
            //Docking connector
            HALF_EXTEND = newRow(),
            FULL_EXTEND = next(),

            //linked typewriter
            ADD_OR_EDIT = newRow(),
            HAMBURGER = next(),
            CANCEL = next(),
            CONFIG = next(),

            //small arrows
            KEY_ARROW_UP = newRow(),
            KEY_ARROW_LEFT = next(),
            KEY_ARROW_DOWN = next(),
            KEY_ARROW_RIGHT = next();

    public SimIcons(final int x, final int y) {
        super(x, y);
        this.iconX = x * 16;
        this.iconY = y * 16;
    }

    private static SimIcons next() {
        return new SimIcons(++x, y);
    }

    private static SimIcons newRow() {
        return new SimIcons(x = 0, ++y);
    }

    @Override
    public RenderType bind() {
        return RenderType.text(ICON_ATLAS);
    }

    @Override
    public void render(final GuiGraphics graphics, final int x, final int y) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, ICON_ATLAS, x, y, this.iconX, this.iconY, 16, 16, 64, 64);
    }

    public void render(final PoseStack ms, final MultiBufferSource buffer, final int color) {
        final VertexConsumer builder = buffer.getBuffer(RenderType.text(ICON_ATLAS));
        final Matrix4f matrix = ms.last().pose();
        final Color rgb = new Color(color);
        final int light = LightTexture.FULL_BRIGHT;

        final Vec3 vec1 = new Vec3(0, 0, 0);
        final Vec3 vec2 = new Vec3(0, 1, 0);
        final Vec3 vec3 = new Vec3(1, 1, 0);
        final Vec3 vec4 = new Vec3(1, 0, 0);

        final float u1 = this.iconX * 1f / ICON_ATLAS_SIZE;
        final float u2 = (this.iconX + 16) * 1f / ICON_ATLAS_SIZE;
        final float v1 = this.iconY * 1f / ICON_ATLAS_SIZE;
        final float v2 = (this.iconY + 16) * 1f / ICON_ATLAS_SIZE;

        this.vertex(builder, matrix, vec1, rgb, u1, v1, light);
        this.vertex(builder, matrix, vec2, rgb, u1, v2, light);
        this.vertex(builder, matrix, vec3, rgb, u2, v2, light);
        this.vertex(builder, matrix, vec4, rgb, u2, v1, light);
    }

    private void vertex(final VertexConsumer builder, final Matrix4f matrix, final Vec3 vec, final Color rgb, final float u, final float v, final int light) {
        builder.addVertex(matrix, (float) vec.x, (float) vec.y, (float) vec.z)
                .setColor(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), 255)
                .setUv(u, v)
                .setLight(light);
    }

    public DelegatedStencilElement asStencil() {
        return new DelegatedStencilElement().withStencilRenderer((ms, w, h, alpha) -> this.render(ms, 0, 0)).withBounds(16, 16);
    }

}
