package dev.eriksonn.aeronautics.api.levitite_blend_crystallization;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class CrystallizationWorldSaveData extends SavedData {
	public static final String ID = "aeronautics_levitite_data";
	
	Level level;
	
	private CompoundTag save() {
		final CompoundTag tag = new CompoundTag();
		ListTag list = new ListTag();
		LevititeCrystallizerManager.saveData(list, level);
		tag.put("Levitite Manager Data", list);
		
		return tag;
	}

	public static CrystallizationWorldSaveData load(ServerLevel level, CompoundTag tag) {
		CrystallizationWorldSaveData data = new CrystallizationWorldSaveData();
		data.level = level;

		LevititeCrystallizerManager.loadData(tag, level);

		return data;
	}

	public static CrystallizationWorldSaveData get(ServerLevel level) {
		CrystallizationWorldSaveData data = level.getChunkSource().getDataStorage().computeIfAbsent(
				new SavedDataType<>(
						ID,
						CrystallizationWorldSaveData::new,
						CompoundTag.CODEC.xmap(tag -> load(level, tag), CrystallizationWorldSaveData::save),
						DataFixTypes.SAVED_DATA_COMMAND_STORAGE));

		data.level = level;
		return data;
	}
}
