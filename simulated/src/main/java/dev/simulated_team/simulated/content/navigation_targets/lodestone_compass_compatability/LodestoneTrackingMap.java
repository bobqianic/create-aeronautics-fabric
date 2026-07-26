package dev.simulated_team.simulated.content.navigation_targets.lodestone_compass_compatability;

import com.mojang.serialization.Codec;
import dev.simulated_team.simulated.compat.create.CommonLevelAttached;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.storage.HoldingSubLevel;
import dev.ryanhcode.sable.sublevel.storage.holding.GlobalSavedSubLevelPointer;
import dev.ryanhcode.sable.sublevel.storage.holding.SubLevelHoldingChunkMap;
import dev.ryanhcode.sable.sublevel.storage.serialization.SubLevelData;
import dev.ryanhcode.sable.sublevel.tracking_points.SubLevelTrackingPointSavedData;
import dev.ryanhcode.sable.sublevel.tracking_points.TrackingPoint;
import dev.simulated_team.simulated.network.packets.lodestone_compass.UpdateClientLodestonePositionPacket;
import foundry.veil.api.network.VeilPacketManager;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;

public class LodestoneTrackingMap extends SavedData {

	//Static members begin//
	public static final String FILE_ID = "simulated_lodestone_tracker";
	private static final Codec<List<LodestoneInformation>> INFORMATION_CODEC = LodestoneInformation.CODEC.listOf();
	private static final SavedDataType<LodestoneTrackingMap> TYPE = new SavedDataType<>(
			FILE_ID,
			context -> new LodestoneTrackingMap(context.levelOrThrow()),
			context -> INFORMATION_CODEC.fieldOf("TrackerInformation").codec().xmap(
					information -> load(context.levelOrThrow(), information),
					map -> List.copyOf(map.lodestoneInformationSet)),
			null);

	private static final CommonLevelAttached<LodestoneTrackingMap> LODESTONE_MAP = new CommonLevelAttached<>(level -> {
		if (level instanceof ServerLevel sl) {
			return sl.getDataStorage().computeIfAbsent(TYPE);
		}

		return null;
	});

	private static final Vector3dc ZERO = new Vector3d();
	private static final Vector3d DUMMY = new Vector3d();
	private static final BlockPos.MutableBlockPos DUMMY_POS = new BlockPos.MutableBlockPos();

	public static LodestoneTrackingMap getOrLoad(final Level level) {
		if (level.isClientSide()) {
			return null;
		}

		return LODESTONE_MAP.get(level);
	}

	private static LodestoneTrackingMap load(final ServerLevel level, final List<LodestoneInformation> information) {
		final LodestoneTrackingMap lodestoneMap = new LodestoneTrackingMap(level);
		lodestoneMap.lodestoneInformationSet.addAll(information);

		return lodestoneMap;
	}
	//Static members end//

	private final ObjectOpenHashSet<LodestoneInformation> lodestoneInformationSet = new ObjectOpenHashSet<>();
	private final WeakReference<ServerLevel> associatedLevel;

	public LodestoneTrackingMap(final ServerLevel level) {
		this.associatedLevel = new WeakReference<>(level);
	}

	/**
	 * Ticks the lodestone maps, removing any invalid points
	 */
	public void tick() {
		if (this.checkLevel()) {
			return;
		}

		final ServerLevel serverLevel = this.associatedLevel.get();
		final SubLevelTrackingPointSavedData data = SubLevelTrackingPointSavedData.getOrLoad(serverLevel);

		final int before = this.lodestoneInformationSet.size();
		this.checkLoadedLodestonePositions(data, serverLevel);
		if (before != this.lodestoneInformationSet.size()) {
			this.setDirty();
		}

		this.updateProjectedPositions(data, serverLevel);
	}

	private void checkLoadedLodestonePositions(final SubLevelTrackingPointSavedData data, final ServerLevel serverLevel) {
		final ObjectIterator<LodestoneInformation> lodestoneIter = this.lodestoneInformationSet.iterator();
		while (lodestoneIter.hasNext()) {
			final LodestoneInformation info = lodestoneIter.next();
			final TrackingPoint point = data.getTrackingPoint(info.id());

			if (point == null) { //if somehow the tracking point data says we don't exist, then we shouldn't
				lodestoneIter.remove();
				continue;
			}

			final Vector3d lodestonePoint = point.point();

			//make sure we don't queue any positions inside unloaded plots
			//TODO: the level is still loaded in recently removed plots, I'm unsure if this will be an issue but noting just in case
			if ((point.inSubLevel() && Sable.HELPER.getContaining(serverLevel, point.point()) == null) || !serverLevel.isLoaded(DUMMY_POS.set(lodestonePoint.x, lodestonePoint.y, lodestonePoint.z))) {
				continue;
			}

			final BlockState state = serverLevel.getBlockState(DUMMY_POS);
			if (!state.is(Blocks.LODESTONE)) {
				data.removeTrackingPoint(info.id());
				lodestoneIter.remove();
			}
		}
	}

	private void updateProjectedPositions(final SubLevelTrackingPointSavedData data, final ServerLevel serverLevel) {
		for (final LodestoneInformation info : this.lodestoneInformationSet) {
			final TrackingPoint point = data.getTrackingPoint(info.id());

			if (point != null) {
				if (point.inSubLevel()) {
					final SubLevel existing = Sable.HELPER.getContaining(serverLevel, point.point());

					if (existing != null) {
						existing.logicalPose().transformPosition(point.point(), info.projectedPos());
					} else {
						//sublevel might be in holding
						if (point.subLevelID() != null) {
							final SubLevelHoldingChunkMap holdingMap = ServerSubLevelContainer
									.getContainer(serverLevel).getHoldingChunkMap();

							final HoldingSubLevel holdingSubLevel = holdingMap
									.getHoldingSubLevel(point.subLevelID());

							//sublevel is in holding
							if (holdingSubLevel != null) {
								holdingSubLevel.data().pose()
										.transformPosition(point.point(), info.projectedPos());
							} else {//sublevel is serialzied

								//we're uncached / newly loaded
								if (info.projectedPos().equals(ZERO)) {
									final GlobalSavedSubLevelPointer lastPointer = point.lastSavedSubLevelPointer();
									if (lastPointer != null) {
										final SubLevelData subLevelData = holdingMap.getStorage().attemptLoadSubLevel(lastPointer.chunkPos(), lastPointer.local());

										if (subLevelData != null) {
											subLevelData.pose().transformPosition(point.point(), info.projectedPos());
										}
									}
								}
							}
						}
					}
				} else {
					info.projectedPos().set(point.point());
				}
			}
		}
	}

	public LodestoneInformation getInformation(final UUID id) {
		for (final LodestoneInformation info : this.lodestoneInformationSet) {
			if (info.id().equals(id)) {
				return info;
			}
		}

		return null;
	}

	/**
	 * Adds a tracking point for a lodestone based on the given position <p><b>
	 * Should ONLY be called when a position is known to be loaded <p/><b/>
	 *
	 * @param pos The position to gather tracking point information from
	 * @return The UUID of the generated tracking point
	 */
	@Nullable
	public UUID addOrGetLodestoneTrackingPoint(final BlockPos pos) {
		if (this.checkLevel()) {
			return null;
		}

		final ServerLevel sl = this.associatedLevel.get();
		final SubLevelTrackingPointSavedData savedData = SubLevelTrackingPointSavedData.getOrLoad(sl);

		UUID lodestoneID = null;

		//check all lodestone tracking positions to see if any match the given blockPos
		for (final LodestoneInformation info : this.lodestoneInformationSet) {
			final TrackingPoint point = savedData.getTrackingPoint(info.id());
			if (point != null) {
				if (point.point().sub(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, DUMMY).length() <= 0.1) { //we're the same position
					lodestoneID = info.id();
					break;
				}
			}
		}

		//there's already a lodestone in this position!
		if (lodestoneID != null) {
			return lodestoneID;
		}

		//there is not a lodestone in this position, generate a new tracking point and UUID
		return this.generateTrackingPoint(pos, sl, savedData);
	}

	/**
	 * Generates a new lodestone tracking point from the given position
	 *
	 * @param pos The position of the lodestone
	 * @param sl  The server level
	 * @return The new UUID of the lodestone tracking point.
	 */
	private @NotNull UUID generateTrackingPoint(final BlockPos pos, final ServerLevel sl, final SubLevelTrackingPointSavedData savedData) {
		final LodestoneInformation newInfo = new LodestoneInformation(UUID.randomUUID(), new Vector3d());
		final ServerSubLevel containing = (ServerSubLevel) Sable.HELPER.getContaining(sl, pos);

		final boolean sublevelExists = containing != null;
		final TrackingPoint trackingPoint = new TrackingPoint(
				sublevelExists,
				sublevelExists ? containing.getUniqueId() : null,
				sublevelExists ? containing.getLastSerializationPointer() : null,
				new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5),
				null
		);

		savedData.setTrackingPoint(newInfo.id(), trackingPoint);
		this.lodestoneInformationSet.add(newInfo);
		this.setDirty();

		return newInfo.id();
	}

	private boolean checkLevel() {
		return this.associatedLevel.get() == null;
	}

	public void sendUpdateForPlayer(final UUID trackerID, final ServerPlayer sp) {
		for (final LodestoneInformation info : this.lodestoneInformationSet) {
			if (info.id().equals(trackerID)) {
				VeilPacketManager.player(sp).sendPacket(new UpdateClientLodestonePositionPacket(info.id(), info.projectedPos()));
				break;
			}
		}
	}

}
