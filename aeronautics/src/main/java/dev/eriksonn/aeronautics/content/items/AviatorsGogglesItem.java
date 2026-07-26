package dev.eriksonn.aeronautics.content.items;

import com.zurrtum.create.content.equipment.goggles.GogglesItem;
import dev.eriksonn.aeronautics.index.AeroArmorMaterials;
import dev.eriksonn.aeronautics.index.AeroItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;

public class AviatorsGogglesItem extends Item {

	public AviatorsGogglesItem(final Properties properties) {
		super(properties.humanoidArmor(AeroArmorMaterials.AVIATORS_GOGGLES, ArmorType.HELMET));
		GogglesItem.addIsWearingPredicate(player -> AeroItems.AVIATORS_GOGGLES.isIn(player.getItemBySlot(EquipmentSlot.HEAD)));
	}
}
