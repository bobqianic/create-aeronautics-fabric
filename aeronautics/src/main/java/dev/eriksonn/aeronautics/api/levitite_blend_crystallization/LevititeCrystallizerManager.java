package dev.eriksonn.aeronautics.api.levitite_blend_crystallization;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import java.util.*;

public class LevititeCrystallizerManager {
	private static final Map<LevelAccessor, List<LevititeBlendTicker>> tickers = new HashMap<>();
	private static final List<LevititeBlendTicker> queuedTickers = new ArrayList<>();

	public static void tick(final Level level) {
		if (tickers.containsKey(level)) {
			tickers.get(level).removeIf(LevititeBlendTicker::tick);
		}

		addQueued(level);
	}

	private static void addQueued(final Level level) {
		final CrystallizationWorldSaveData data = CrystallizationWorldSaveData.get((ServerLevel) level);

		final Set<BlockPos> tickedPositions = getTickedPositions(level);
		final List<LevititeBlendTicker> levelTickers = tickers.get(level);

		for (final LevititeBlendTicker queuedTicker : queuedTickers) {
			if (tickedPositions.contains(queuedTicker.getPos())) {
				continue;
			}

			levelTickers.add(queuedTicker);
			queuedTicker.getContext().onCrystallizationInitialize(level, queuedTicker.getPos(), queuedTicker.isDormant);
			data.setDirty();
		}

		queuedTickers.clear();
	}

	/**
	 * @usage Should only be called when adding *new* entries to the ticker group
	 */
	public static void addTicker(final Level level, final BlockPos pos, final int delay, final boolean requiresCatalyst, final boolean skipDormant, final CrystalPropagationContext context) {
		queuedTickers.add(new LevititeBlendTicker(delay, pos, level, requiresCatalyst, skipDormant, context));
	}

	public static Set<BlockPos> getTickedPositions(final Level level) {
		final Set<BlockPos> tickedPositions = new HashSet<>();

		tickers.putIfAbsent(level, new ArrayList<>());
		tickers.get(level).forEach(t -> tickedPositions.add(t.getPos()));

		return tickedPositions;
	}

	public static void saveData(final ListTag list, final Level level) {
		if (tickers.containsKey(level)) {
			for (final LevititeBlendTicker ticker : tickers.get(level)) {
				list.add(ticker.serialize());
			}
		}
	}

	public static void loadData(final CompoundTag tag, final Level level) {
		tickers.putIfAbsent(level, new ArrayList<>());

		final ListTag data = tag.getListOrEmpty("Levitite Manager Data");
		final List<LevititeBlendTicker> newTickers = new ArrayList<>();
		for (int i = 0; i < data.size(); i++) {
			newTickers.add(new LevititeBlendTicker(data.getCompoundOrEmpty(i), level));
		}

		tickers.put(level, newTickers);
	}

	public static void clearLevel(final LevelAccessor level) {
		tickers.remove(level);
	}
}
