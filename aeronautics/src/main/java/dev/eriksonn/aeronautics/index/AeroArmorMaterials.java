package dev.eriksonn.aeronautics.index;

import dev.eriksonn.aeronautics.Aeronautics;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.List;

public class AeroArmorMaterials {
	public static final ResourceKey<EquipmentAsset> AVIATORS_GOGGLES_ASSET = ResourceKey.create(
			EquipmentAssets.ROOT_ID, Aeronautics.path("aviators_goggles"));

	public static final ArmorMaterial AVIATORS_GOGGLES = new ArmorMaterial(
			15,
			new Object2ObjectOpenHashMap<>() {{
				this.put(ArmorType.HELMET, 1);
			}},
			15,
			SoundEvents.ARMOR_EQUIP_LEATHER,
			0.0f,
			0.0f,
			AeroTags.ItemTags.LEATHERS,
			AVIATORS_GOGGLES_ASSET);

	public static void init() {}
}
