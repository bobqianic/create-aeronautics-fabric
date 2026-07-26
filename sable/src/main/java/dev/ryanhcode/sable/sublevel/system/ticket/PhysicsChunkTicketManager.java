package dev.ryanhcode.sable.sublevel.system.ticket;

import dev.ryanhcode.sable.SableConfig;
import dev.ryanhcode.sable.index.SableTicketTypes;
import dev.ryanhcode.sable.api.physics.PhysicsPipeline;
import dev.ryanhcode.sable.api.physics.object.ArbitraryPhysicsObject;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.companion.math.BoundingBox3dc;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.plot.LevelPlot;
import dev.ryanhcode.sable.sublevel.storage.holding.SubLevelHoldingChunkMap;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class PhysicsChunkTicketManager {

    public static final double MAX_PREDICTION_DISTANCE = 20.0;

    /**
     * The physics chunks that are currently loaded.
     */
    private final Map<SectionPos, PhysicsChunkTicket> physicsChunks = new Object2ObjectOpenHashMap<>();

    /**
     * Chunks that are force-loaded by force-loaded sub-levels inhabiting them
     */
    private final Long2ObjectMap<ObjectArraySet<InhabitedChunkTicket>> forcedInhabitedChunks = new Long2ObjectOpenHashMap<>();

    /**
     * mc26.1: vanilla tickets carry no identity payload, so exactly one vanilla
     * ticket per force-loaded chunk is tracked here; the per-sub-level
     * bookkeeping lives in {@link #forcedInhabitedChunks}.
     */
    private final Long2ObjectMap<Ticket> vanillaChunkTickets = new Long2ObjectOpenHashMap<>();

    /**
     * Updates the state of the ticket manager.
     * This will:
     * <ul>
     *     <li>Remove outdated tickets that have not been inhabited for more than 20 ticks and are not part of a plot</li>
     *     <li>Remove tickets for chunks that no longer exist</li>
     *     <li>Add new tickets for chunks that have sub-levels in them</li>
     *     <li>Remove / unload sub-levels that are in unloaded chunks</li>
     *     <li>Update the last inhabited tick for tickets that are still valid to reflect the game time</li>
     * </ul>
     *
     * @param level     the level to update the ticket manager for
     * @param container the sub-level container to update the ticket manager for
     * @param pipeline  the physics pipeline to update the ticket manager for
     */
    public void update(final ServerLevel level, final ServerSubLevelContainer container, final SubLevelPhysicsSystem system, final PhysicsPipeline pipeline, final double timeStep) {
        final SubLevelHoldingChunkMap holdingChunkMap = container.getHoldingChunkMap();
        final long gameTime = level.getGameTime();

        final Collection<ServerSubLevel> forceLoaded = container.collectForceLoadedSubLevels();

        this.expirePhysicsChunkTickets(level, pipeline, gameTime);

        if (DimensionPhysicsData.of(level).ignoreChunks()) {
            return;
        }

        this.expireForcedInhabitedChunks(level, gameTime);

        final LongOpenHashSet unloadedChunks = new LongOpenHashSet();

        final boolean cannotUnloadPlayerInhabited = SableConfig.SUB_LEVELS_WITH_PLAYERS_CANNOT_UNLOAD.getAsBoolean();

        final BoundingBox3d b = new BoundingBox3d();
        final BoundingBox3d b2 = new BoundingBox3d();
        final Vector3d velocity = new Vector3d();

        final Iterator<ArbitraryPhysicsObject> objectIter = system.getArbitraryObjects().iterator();
        arbitraryObjectLoop:
        while (objectIter.hasNext()) {
            final ArbitraryPhysicsObject arbitraryObject = objectIter.next();
            arbitraryObject.getBoundingBox(b);
            b.expand(1.0, b);

            final BoundingBox3i chunkBounds = new BoundingBox3i(
                    Mth.floor(b.minX()) >> 4,
                    Mth.floor(b.minY()) >> 4,
                    Mth.floor(b.minZ()) >> 4,
                    Mth.floor(b.maxX()) >> 4,
                    Mth.floor(b.maxY()) >> 4,
                    Mth.floor(b.maxZ()) >> 4
            );

            for (int x = chunkBounds.minX(); x <= chunkBounds.maxX(); x++) {
                for (int z = chunkBounds.minZ(); z <= chunkBounds.maxZ(); z++) {
                    final long l = ChunkPos.asLong(x, z);

                    if (!isChunkLoadedEnough(level, x, z) || unloadedChunks.contains(l)) {
                        arbitraryObject.onUnloaded(holdingChunkMap, new ChunkPos(x, z));
                        unloadedChunks.add(l);
                        objectIter.remove();
                        continue arbitraryObjectLoop;
                    }
                }
            }

            for (int x = chunkBounds.minX(); x <= chunkBounds.maxX(); x++) {
                for (int z = chunkBounds.minZ(); z <= chunkBounds.maxZ(); z++) {
                    for (int y = chunkBounds.minY(); y <= chunkBounds.maxY(); y++) {
                        final SectionPos sectionPos = SectionPos.of(x, y, z);

                        final int index = level.getSectionIndexFromSectionY(y);

                        if (index >= 0 && index < level.getSectionsCount()) {
                            this.addTicket(level, pipeline, sectionPos, x, y, z, index, gameTime);
                        }
                    }
                }
            }
        }

        subLevelLoop:
        for (int i = 0; i < container.getAllSubLevels().size(); i++) {
            final ServerSubLevel subLevel = container.getAllSubLevels().get(i);
            if (subLevel.isRemoved()) continue;
            final UUID uuid = subLevel.getUniqueId();

            b.set(subLevel.boundingBox());
            b2.set(b);

            // Only do velocity prediction if there's at-least some movement (1 m/s)
            if (subLevel.lastPose().position().distanceSquared(subLevel.logicalPose().position()) > 0.05 * 0.05) {
                system.getPipeline().getLinearVelocity(subLevel, velocity.zero()).mul(timeStep);
                b2.move(0.0, Mth.clamp(velocity.y, -MAX_PREDICTION_DISTANCE, MAX_PREDICTION_DISTANCE), 0.0);
                b.expandTo(b2);
            }

            b.expand(1.0, b);
            final BoundingBox3i chunkBounds = b.chunkBoundsFrom();

            boolean checkedPlayerInhabited = false;
            boolean playerInhabited = false;

            for (int x = chunkBounds.minX(); x <= chunkBounds.maxX(); x++) {
                for (int z = chunkBounds.minZ(); z <= chunkBounds.maxZ(); z++) {
                    final long chunkLong = ChunkPos.asLong(x, z);

                    final boolean chunkLoadedEnough = isChunkLoadedEnough(level, x, z);

                    if (forceLoaded.contains(subLevel)) {
                        this.inhabitChunk(level, uuid, gameTime, chunkLong, x, z);
                    } else {
                        if (!chunkLoadedEnough || unloadedChunks.contains(chunkLong)) {
                            // The sub-level has now entered an unloaded chunk.
                            unloadedChunks.add(chunkLong);

                            if (!checkedPlayerInhabited && cannotUnloadPlayerInhabited) {
                                playerInhabited = !level.getPlayers(player -> {
                                    final Vec3 position = player.getBoundingBox().getCenter();
                                    return b.contains(position.x, position.y, position.z);
                                }).isEmpty();
                                checkedPlayerInhabited = true;
                            }

                            if (!cannotUnloadPlayerInhabited || !playerInhabited) {
                                holdingChunkMap.moveToUnloaded(subLevel, new ChunkPos(x, z));

                                // Because we just removed this sub-level, we need to decrement the index to avoid skipping the next sub-level
                                i--;

                                continue subLevelLoop;
                            } else {
                                this.inhabitChunk(level, uuid, gameTime, chunkLong, x, z);
                            }
                        }
                    }
                }
            }

            for (int x = chunkBounds.minX(); x <= chunkBounds.maxX(); x++) {
                for (int z = chunkBounds.minZ(); z <= chunkBounds.maxZ(); z++) {
                    for (int y = chunkBounds.minY(); y <= chunkBounds.maxY(); y++) {
                        final SectionPos sectionPos = SectionPos.of(x, y, z);

                        final int index = level.getSectionIndexFromSectionY(y);

                        if (index >= 0 && index < level.getSectionsCount()) {
                            final PhysicsChunkTicket ticket = this.addTicket(level, pipeline, sectionPos, x, y, z, index, gameTime);

                            if (SubLevelPhysicsSystem.USE_TICKETS_FOR_QUERIES) {
                                ticket.residentSubLevels().add(subLevel);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Marks a chunk as sub-level inhabited and force-loads it as a result
     */
    private void inhabitChunk(final ServerLevel level,
                              final UUID subLevelId,
                              final long gameTime,
                              final long chunkLong,
                              final int x,
                              final int z) {
        ObjectArraySet<InhabitedChunkTicket> set = this.forcedInhabitedChunks.get(chunkLong);

        if (set == null) {
            this.forcedInhabitedChunks.put(chunkLong, set = new ObjectArraySet<>(1));
        }

        final InhabitedChunkTicket newSableTicket = new InhabitedChunkTicket(subLevelId, gameTime);

        if (set.add(newSableTicket)) {
            // One vanilla ticket per chunk; per-sub-level identity is tracked in the set.
            if (!this.vanillaChunkTickets.containsKey(chunkLong)) {
                final Ticket vanillaTicket = new Ticket(SableTicketTypes.SUB_LEVEL_LOADED, ChunkLevel.ENTITY_TICKING_LEVEL);
                this.vanillaChunkTickets.put(chunkLong, vanillaTicket);
                level.getChunkSource().addTicket(vanillaTicket, new ChunkPos(x, z));
            }

            level.getChunk(x, z, ChunkStatus.FULL, true);
        } else {
            boolean any = false;

            for (final InhabitedChunkTicket ticket : set) {
               if (ticket.equals(newSableTicket)) {
                   ticket.setLastInhabitedTick(gameTime);
                   any = true;
                   break;
               }
            }

            if (!any) {
                throw new RuntimeException("Chunk ticket state management has gone horribly wrong.");
            }
        }
    }

    private void expirePhysicsChunkTickets(final ServerLevel level, final PhysicsPipeline pipeline, final long gameTime) {
        final Iterator<Map.Entry<SectionPos, PhysicsChunkTicket>> chunkTicketIter = this.physicsChunks.entrySet().iterator();

        while (chunkTicketIter.hasNext()) {
            final Map.Entry<SectionPos, PhysicsChunkTicket> entry = chunkTicketIter.next();
            final SectionPos sectionPos = entry.getKey();
            final PhysicsChunkTicket ticket = entry.getValue();

            final LevelPlot plot = SubLevelContainer.getContainer(level).getPlot(sectionPos.chunk());

            final boolean outdated = ticket.lastInhabitedTick() < gameTime - 20 && plot == null;
            final boolean noLongerExistent = !isChunkLoadedEnough(level, sectionPos.x(), sectionPos.z());
            if (outdated || noLongerExistent) {
                pipeline.handleChunkSectionRemoval(sectionPos.x(), sectionPos.y(), sectionPos.z());
                chunkTicketIter.remove();
            } else {
                if (SubLevelPhysicsSystem.USE_TICKETS_FOR_QUERIES && ticket.residentSubLevels() != null) {
                    if (!ticket.residentSubLevels().isEmpty())
                        ticket.residentSubLevels().clear();
                }
            }
        }
    }

    private void expireForcedInhabitedChunks(final ServerLevel level, final long gameTime) {
        final ObjectIterator<Long2ObjectMap.Entry<ObjectArraySet<InhabitedChunkTicket>>> forcedChunkIter = this.forcedInhabitedChunks.long2ObjectEntrySet().iterator();

        while (forcedChunkIter.hasNext()) {
            final Long2ObjectMap.Entry<ObjectArraySet<InhabitedChunkTicket>> entry = forcedChunkIter.next();

            final long chunkLong = entry.getLongKey();
            final ObjectArraySet<InhabitedChunkTicket> set = entry.getValue();
            set.removeIf(ticket -> ticket.lastInhabitedTick() < gameTime - 20);

            if (set.isEmpty()) {
                final Ticket vanillaTicket = this.vanillaChunkTickets.remove(chunkLong);
                if (vanillaTicket != null) {
                    level.getChunkSource().removeTicketWithRadius(vanillaTicket.getType(), new ChunkPos(chunkLong), 0);
                }
                forcedChunkIter.remove();
            }
        }
    }

    private @NotNull PhysicsChunkTicket addTicket(final Level level,
                                                  final PhysicsPipeline pipeline,
                                                  final SectionPos sectionPos,
                                                  final int x,
                                                  final int y,
                                                  final int z,
                                                  final int index,
                                                  final long gameTime) {
        PhysicsChunkTicket existingTicket = this.physicsChunks.get(sectionPos);
        if (existingTicket == null) {
            final LevelChunk chunk = level.getChunk(x, z);

            pipeline.handleChunkSectionAddition(chunk.getSection(index), x, y, z, false);

            final Collection<SubLevel> residents = SubLevelPhysicsSystem.USE_TICKETS_FOR_QUERIES ? new ObjectArraySet<>(SubLevelPhysicsSystem.DEFAULT_RESIDENT_CAPACITY) : null;
            final PhysicsChunkTicket newTicket = new PhysicsChunkTicket(sectionPos, gameTime, residents);
            this.physicsChunks.put(sectionPos, newTicket);

            existingTicket = newTicket;
        }

        existingTicket.setLastInhabitedTick(gameTime);
        return existingTicket;
    }

    /**
     * Adds a chunk section if it is not currently tracked, and a ticket does not currently exist.
     * This will notify the given pipeline of the addition, and add a new ticket for the section and current game time.
     *
     * @param level      the server level to add the section to
     * @param section    the section to add
     * @param sectionPos the position of the section
     * @param pipeline   the physics pipeline to notify of the addition
     */
    public void addSectionIfNotTracked(final ServerLevel level, final LevelChunkSection section, final SectionPos sectionPos, final PhysicsPipeline pipeline) {
        if (!this.physicsChunks.containsKey(sectionPos)) {
            pipeline.handleChunkSectionAddition(section, sectionPos.x(), sectionPos.y(), sectionPos.z(), false);

            final PhysicsChunkTicket ticket = new PhysicsChunkTicket(sectionPos, level.getGameTime(), null);
            this.physicsChunks.put(sectionPos, ticket);
        }
    }

    public void addTicketForSection(final ServerLevel level, final SectionPos sectionPos) {
        final PhysicsChunkTicket ticket = new PhysicsChunkTicket(sectionPos, level.getGameTime(), null);
        this.physicsChunks.put(sectionPos, ticket);
    }

    /**
     * Queries all sub-levels that intersect with the given bounds.
     * This will iterate through all chunk sections in the bounds, and return all sub-levels that are tracked as residents.
     *
     * @param bounds the bounding box to query for intersecting sub-levels
     * @return an iterable of sub-levels that intersect with the given bounds
     */
    public Iterable<SubLevel> queryIntersecting(final BoundingBox3dc bounds) {
        if (!SubLevelPhysicsSystem.USE_TICKETS_FOR_QUERIES) {
            throw new IllegalStateException("Cannot query intersecting sub-levels when tickets are not used for queries.");
        }

        final ObjectList<SubLevel> intersecting = new ObjectArrayList<>(16);

        final BoundingBox3i chunkBounds = bounds.chunkBoundsFrom();

        for (int x = chunkBounds.minX(); x <= chunkBounds.maxX(); x++) {
            for (int z = chunkBounds.minZ(); z <= chunkBounds.maxZ(); z++) {
                for (int y = chunkBounds.minY(); y <= chunkBounds.maxY(); y++) {
                    final SectionPos sectionPos = SectionPos.of(x, y, z);

                    final PhysicsChunkTicket ticket = this.physicsChunks.get(sectionPos);
                    if (ticket != null) {
                        final Collection<SubLevel> residents = ticket.residentSubLevels();

                        if (residents == null) {
                            continue;
                        }

                        for (final SubLevel subLevel : residents) {
                            if (!subLevel.boundingBox().intersects(bounds)) {
                                continue;
                            }

                            intersecting.add(subLevel);
                        }
                    }
                }
            }
        }
        return intersecting;
    }

    /**
     * Checks if an arbitrary physics object would be loaded
     */
    public boolean wouldBeLoaded(final Level level, final ArbitraryPhysicsObject object) {
        final BoundingBox3d b = new BoundingBox3d();
        object.getBoundingBox(b);
        b.expand(1.0, b);

        final BoundingBox3i chunkBounds = b.chunkBoundsFrom();

        for (int x = chunkBounds.minX(); x <= chunkBounds.maxX(); x++) {
            for (int z = chunkBounds.minZ(); z <= chunkBounds.maxZ(); z++) {
                if (!isChunkLoadedEnough((ServerLevel) level, x, z)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * @return if a chunk is considered loaded enough to contain and tick sub-levels
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isChunkLoadedEnough(final ServerLevel level, final int x, final int z) {
        final ServerSubLevelContainer container = SubLevelContainer.getContainer(level);

        if (container != null && container.inBounds(x, z)) {
            return true;
        }

        final DistanceManager distanceManager = level.getChunkSource().chunkMap.getDistanceManager();
        return distanceManager.inBlockTickingRange(ChunkPos.asLong(x, z));
    }
}
