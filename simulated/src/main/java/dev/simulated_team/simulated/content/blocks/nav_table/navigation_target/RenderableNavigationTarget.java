package dev.simulated_team.simulated.content.blocks.nav_table.navigation_target;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.simulated_team.simulated.content.blocks.nav_table.NavTableBlockEntity;
import dev.simulated_team.simulated.content.blocks.nav_table.NavTableRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public interface RenderableNavigationTarget extends NavigationTarget {

    /**
     * Renders the Item's partials centered on the {@link NavTableRenderer Navigation Table's} item Pedestal <p>
     * Because of this, The Partial model must be centered at its origin
     */
    default void renderInNavTable(final ItemStack self, final NavTableBlockEntity navBE, final BlockState navState, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer, final int light, final int overlay) {
    }
}
