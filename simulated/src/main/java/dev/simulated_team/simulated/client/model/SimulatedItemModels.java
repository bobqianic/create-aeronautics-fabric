package dev.simulated_team.simulated.client.model;

import com.mojang.serialization.MapCodec;
import dev.simulated_team.simulated.content.items.plunger_launcher.PlungerLauncherItemModel;
import dev.simulated_team.simulated.content.physics_staff.PhysicsStaffItemModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.resources.ResourceLocation;

public final class SimulatedItemModels {
    private SimulatedItemModels() {
    }

    public static void register() {
        register(PhysicsStaffItemModel.ID, PhysicsStaffItemModel.Unbaked.CODEC);
        register(PlungerLauncherItemModel.ID, PlungerLauncherItemModel.Unbaked.CODEC);
    }

    private static <T extends ItemModel.Unbaked> void register(final ResourceLocation id, final MapCodec<T> codec) {
        ItemModels.ID_MAPPER.put(id, codec);
    }
}
