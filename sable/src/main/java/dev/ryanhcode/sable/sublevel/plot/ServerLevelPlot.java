package dev.ryanhcode.sable.sublevel.plot;

import com.mojang.serialization.Codec;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.block.BlockSubLevelLiftProvider;
import dev.ryanhcode.sable.api.entity.EntitySubLevelUtil;
import dev.ryanhcode.sable.api.sublevel.KinematicContraption;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import dev.ryanhcode.sable.compatibility.LightEngineCompat;
import dev.ryanhcode.sable.index.SableTags;
import dev.ryanhcode.sable.mixinterface.plot.serialization.LevelChunkTicksExtension;
import dev.ryanhcode.sable.platform.SableChunkEventPlatform;
import dev.ryanhcode.sable.platform.SablePlotPlatform;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.SavedTick;

import java.util.*;
import java.util.stream.Stream;

/**
 * An allocated & reserved space in a level belonging to a {@link SubLevel}, holding its own chunk grid.
 */
public class ServerLevelPlot extends LevelPlot {
    protected static final int DATA_VERSION = 1;
    // PORT-NOTE(mc26.1): PalettedContainer codecs take a Strategy (top-level class) instead of an IdMap +
    // nested Strategy constant; tick lists serialize through SavedTick codecs (LevelChunkTicks.save/load are gone).
    private static final Strategy<BlockState> BLOCK_STATE_STRATEGY = Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY);
    private static final Codec<PalettedContainer<BlockState>> BLOCK_STATE_CODEC = PalettedContainer.codecRW(
            BlockState.CODEC, BLOCK_STATE_STRATEGY, Blocks.AIR.defaultBlockState()
    );
    private static final Codec<List<SavedTick<Block>>> BLOCK_TICKS_CODEC = SavedTick.codec(BuiltInRegistries.BLOCK.byNameCodec()).listOf();
    private static final Codec<List<SavedTick<Fluid>>> FLUID_TICKS_CODEC = SavedTick.codec(BuiltInRegistries.FLUID.byNameCodec()).listOf();

    /**
     * The light engine for this plot
     */
    protected final LevelLightEngine lightEngine;

    /**
     * All kinematic contraption children
     */
    private final Set<KinematicContraption> contraptions = new ReferenceOpenHashSet<>();

    /**
     * All LiftProviders within this plot.
     */
    private final Long2ObjectMap<BlockSubLevelLiftProvider.LiftProviderContext> liftProviders = new Long2ObjectOpenHashMap<>();

    /**
     * Creates a new plot at the given plot coordinate.
     *
     * @param plotContainer the parent plot container of this level plot
     * @param x             the global X coordinate of the plot, in units of {@code 1 << logSize} chunks
     * @param z             the global Z coordinate of the plot, in units of {@code 1 << logSize} chunks
     * @param logSize       the log_2 of the side length of a plot
     * @param subLevel      the sub-level using this plot
     */
    public ServerLevelPlot(final SubLevelContainer plotContainer, final int x, final int z, final int logSize, final ServerSubLevel subLevel) {
        super(plotContainer, x, z, logSize, subLevel);

        final Level level = subLevel.getLevel();
        final LevelLightEngine parentLightEngine = level.getLightEngine();
        final ChunkSource chunkSource = level.getChunkSource();
        this.lightEngine = new LevelLightEngine(
                chunkSource,
                LightEngineCompat.hasBlockLight(parentLightEngine),
                LightEngineCompat.hasSkyLight(parentLightEngine)
        );
    }

    /**
     * Adds a kinematic contraption to this plot
     */
    public void addContraption(final KinematicContraption contraption) {
        this.contraptions.add(contraption);
    }

    /**
     * Removes a kinematic contraption from this plot
     */
    public void removeContraption(final KinematicContraption contraption) {
        this.contraptions.remove(contraption);
    }

    /**
     * @return all kinematic contraption children
     */
    public Collection<KinematicContraption> getContraptions() {
        return this.contraptions;
    }

    /**
     * Logs loading errors for a plot chunk section
     */
    private static void logLoadingErrors(final ChunkPos chunkPos, final int y, final String errorText) {
        Sable.LOGGER.error("Recoverable errors when loading plot section [{}, {}, {}]: {}", chunkPos.x, y, chunkPos.z, errorText);
    }

    /**
     * Ticks this plot, running lighting updates
     */
    @Override
    public void tick() {
        do {
            this.lightEngine.runLightUpdates();
        } while (this.lightEngine.hasLightWork());

        this.contraptions.removeIf(contraption -> !contraption.sable$isValid());
    }

    /**
     * @return the light engine responsible for lighting this plot
     */
    @Override
    public LevelLightEngine getLightEngine() {
        return this.lightEngine;
    }

    @Override
    protected void onRemoveChunkHolder(final LevelChunk levelChunk) {
        final ChunkPos pos = levelChunk.getPos();
        final ServerLevel serverLevel = this.getSubLevel().getLevel();

        if (serverLevel.getChunkSource() instanceof final ServerChunkCache cache) {
            cache.chunkMap.updatingChunkMap.remove(pos.toLong());
            cache.chunkMap.modified = true;
        }

        levelChunk.setLoaded(false);

        // This clears block entities in the chunk and notifies them that the
        // chunk was unloaded rather than deleting the entities.
        serverLevel.unload(levelChunk);

        if (LightEngineCompat.usesScalableLux()) {
            LightEngineCompat.removeChunk(this.lightEngine, pos);
        } else {
            this.lightEngine.retainData(pos, false);
            this.lightEngine.setLightEnabled(pos, false);
            for (int idx = this.lightEngine.getMinLightSection(); idx < this.lightEngine.getMaxLightSection(); idx++) {
                this.lightEngine.queueSectionData(LightLayer.BLOCK, SectionPos.of(pos, idx), null);
                this.lightEngine.queueSectionData(LightLayer.SKY, SectionPos.of(pos, idx), null);
            }

            for (int idx = serverLevel.getMinSectionY(); idx < serverLevel.getMaxSectionY(); idx++) {
                this.lightEngine.updateSectionStatus(SectionPos.of(pos, idx), true);
            }
        }

        serverLevel.entityManager.updateChunkStatus(pos, FullChunkStatus.INACCESSIBLE);
    }

    public void setBiome(final ResourceKey<Biome> biome) {
        this.biome = biome;
    }

    private void initializeLight(final LevelChunk chunk) {
        final LevelChunkSection[] alevelchunksection = chunk.getSections();
        final Level level = chunk.getLevel();
        final ChunkPos pos = chunk.getPos();
        final LevelLightEngine lightEngine = this.lightEngine;

        for (int i = 0; i < chunk.getSectionsCount(); i++) {
            final LevelChunkSection levelchunksection = alevelchunksection[i];
            if (!levelchunksection.hasOnlyAir()) {
                this.lightEngine.updateSectionStatus(SectionPos.of(pos, level.getSectionYFromSectionIndex(i)), false);
            }
        }

        lightEngine.setLightEnabled(pos, chunk.isLightCorrect());
        lightEngine.retainData(pos, false);
    }

    private void correctLight(final LevelChunk chunk) {
        if (chunk.isLightCorrect()) {
            return;
        }

        this.lightEngine.propagateLightSources(chunk.getPos());
        chunk.setLightCorrect(true);
    }

    private void lightChunk(final LevelChunk chunk) {
        if (LightEngineCompat.usesScalableLux()) {
            LightEngineCompat.lightChunk(this.lightEngine, chunk);
            return;
        }

        chunk.initializeLightSources();
        this.initializeLight(chunk);
        this.correctLight(chunk);
    }

    /**
     * Sets a chunk at the local position in the plot
     *
     * @param localChunkPos      the local chunk position in the plot
     * @param holder             the chunk holder to add
     * @param initializeLighting whether to initialize lighting for the chunk
     */
    @Override
    public void addChunkHolder(final ChunkPos localChunkPos, final PlotChunkHolder holder, final boolean initializeLighting) {
        final ServerLevel level = this.getSubLevel().getLevel();

        final ChunkPos globalChunkPos = this.toGlobal(localChunkPos);
        final LevelChunk chunk = holder.getChunk();

        // Update the chunk map if one exists
        final ServerChunkCache cache = level.getChunkSource();
        cache.chunkMap.updatingChunkMap.put(globalChunkPos.toLong(), holder);
        cache.chunkMap.modified = true;

        super.addChunkHolder(localChunkPos, holder, initializeLighting);

        chunk.setLightCorrect(false);

        // light chunk
        if (initializeLighting) {
            this.lightChunk(chunk);
        }

        chunk.setFullStatus(holder::getFullStatus);
        chunk.runPostLoad();
        chunk.setLoaded(true);
        chunk.registerAllBlockEntitiesAfterLevelLoad();
        chunk.registerTickContainerInLevel(level);

        level.entityManager.updateChunkStatus(chunk.getPos(), FullChunkStatus.ENTITY_TICKING);
        level.getChunkSource().chunkMap.onFullChunkStatusChange(globalChunkPos, FullChunkStatus.ENTITY_TICKING);

        do {
            this.lightEngine.runLightUpdates();
        } while (this.lightEngine.hasLightWork());

        final Iterable<ServerPlayer> players = this.container.getPlayersTracking(globalChunkPos);

        for (final ServerPlayer player : players) {
            SubLevelPlayerChunkSender.sendChunk(player.connection::send, this.lightEngine, chunk);
            SubLevelPlayerChunkSender.sendChunkPoiData(level, chunk);
        }
    }


    /**
     * Deletes all entities in the plot
     */
    public void kickAllEntities() {
        final ServerSubLevel subLevel = this.getSubLevel();
        final PersistentEntitySectionManager<Entity> manager = subLevel.getLevel().entityManager;
        for (final PlotChunkHolder chunk : this.getLoadedChunks()) {
            final Stream<EntitySection<Entity>> sections = manager.sectionStorage.getExistingSectionsInChunk(chunk.getPos().toLong());

            for (final EntitySection<Entity> section : sections.toList()) {
                final List<Entity> entities = section.getEntities().toList();

                for (final Entity entity : entities) {
                    if (entity.getType().is(SableTags.DESTROY_WITH_SUB_LEVEL)) {
                        entity.remove(Entity.RemovalReason.KILLED);
                    } else {
                        EntitySubLevelUtil.kickEntity(subLevel, entity);
                        final ServerLevel level = subLevel.getLevel();

                        entity.levelCallback.onRemove(Entity.RemovalReason.CHANGED_DIMENSION);
                        level.addDuringTeleport(entity);
                    }

                    section.remove(entity);
                }
            }
        }
    }

    /**
     * Destroys all blocks within the plot
     */
    public void destroyAllBlocks() {
        if (this.localBounds == null || this.localBounds == BoundingBox3i.EMPTY) {
            return;
        }

        final Level level = this.getSubLevel().getLevel();
        final BoundingBox3i bounds = this.localBounds;

        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
            for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                    final BlockPos pos = new BlockPos(x, y, z);

                    level.destroyBlock(pos, true);
                }
            }
        }
    }

    /**
     * Adds a new, empty chunk at the given global chunk position.
     * Does not initialize light, as this chunk is expected to be populated and for light to be initialized afterwards.
     */
    private void newNonLitChunk(final ChunkPos pos) {
        final Level level = this.container.getLevel();

        final int sectionCount = level.getSectionsCount();
        final LevelChunkSection[] sections = new LevelChunkSection[sectionCount];

        for (int i = 0; i < sectionCount; ++i) {
            // PORT-NOTE(mc26.1): empty sections are created from the level's PalettedContainerFactory now.
            sections[i] = new LevelChunkSection(level.palettedContainerFactory());
        }

        final LevelChunk chunk = new LevelChunk(level, pos, UpgradeData.EMPTY, new LevelChunkTicks<>(), new LevelChunkTicks<>(), 0L, sections, null, null);
        this.newChunk(pos, chunk, false);
    }

    /**
     * Serializes this plot & all loaded chunks to an NBT tag
     */
    public CompoundTag save() {
        final CompoundTag tag = new CompoundTag();
        tag.putInt("plot_x", this.plotPos.x - this.container.getOrigin().x());
        tag.putInt("plot_z", this.plotPos.z - this.container.getOrigin().y);
        tag.putInt("log_size", this.logSize);
        tag.putString("biome", this.biome.location().toString());
        tag.putInt("data_version", DATA_VERSION);

        final ServerLevel level = this.getSubLevel().getLevel();

        final CompoundTag chunks = new CompoundTag();
        for (final PlotChunkHolder chunkHolder : this.getLoadedChunks()) {
            final ChunkPos global = chunkHolder.getPos();
            final ChunkPos local = this.toLocal(global);
            final LevelChunk chunk = chunkHolder.getChunk();

            final CompoundTag chunkTag = new CompoundTag();
            final CompoundTag sectionsTag = new CompoundTag();

            for (int idx = 0; idx < chunk.getSectionsCount(); idx++) {
                final LevelChunkSection section = chunk.getSection(idx);

                if (section.hasOnlyAir()) {
                    continue;
                }

                final CompoundTag sectionTag = new CompoundTag();
                sectionTag.put("block_states", BLOCK_STATE_CODEC.encodeStart(NbtOps.INSTANCE, section.getStates()).getOrThrow());

                final SectionPos sectionPos = SectionPos.of(global, level.getSectionYFromSectionIndex(idx));
                final DataLayer blockLight = this.lightEngine.getLayerListener(LightLayer.BLOCK).getDataLayerData(sectionPos);
                final DataLayer skyLight = this.lightEngine.getLayerListener(LightLayer.SKY).getDataLayerData(sectionPos);

                if (blockLight != null && !blockLight.isEmpty()) {
                    sectionTag.putByteArray("BlockLight", blockLight.getData());
                }

                if (skyLight != null && !skyLight.isEmpty()) {
                    sectionTag.putByteArray("SkyLight", skyLight.getData());
                }

                sectionsTag.put(String.valueOf(idx), sectionTag);
            }
            chunkTag.put("sections", sectionsTag);

            tag.putBoolean("isLightOn", chunk.isLightCorrect());

            final ListTag blockEntitiesTag = new ListTag();

            for (final BlockPos blockPos : chunk.getBlockEntitiesPos()) {
                final CompoundTag blockEntityNBT = chunk.getBlockEntityNbtForSaving(blockPos, level.registryAccess());

                if (blockEntityNBT != null) {
                    blockEntitiesTag.add(blockEntityNBT);
                }
            }

            chunkTag.put("block_entities", blockEntitiesTag);

            // PORT-NOTE(mc26.1): getTicksForSerialization(long) returns PackedTicks (lists of SavedTick);
            // serialized via the SavedTick codecs like vanilla SerializableChunkData.
            final ChunkAccess.PackedTicks ticksToSave = chunk.getTicksForSerialization(level.getGameTime());
            chunkTag.store("block_ticks", BLOCK_TICKS_CODEC, ticksToSave.blocks());
            chunkTag.store("fluid_ticks", FLUID_TICKS_CODEC, ticksToSave.fluids());

            final CompoundTag heightMapsTag = new CompoundTag();

            for (final Map.Entry<Heightmap.Types, Heightmap> entry : chunk.getHeightmaps()) {
                if (chunk.getPersistedStatus().heightmapsAfter().contains(entry.getKey())) {
                    heightMapsTag.put(entry.getKey().getSerializationKey(), new LongArrayTag(entry.getValue().getRawData()));
                }
            }

            chunkTag.put("heightmaps", heightMapsTag);

            SablePlotPlatform.INSTANCE.writeLightData(tag, level.registryAccess(), chunk);
            SablePlotPlatform.INSTANCE.writeChunkAttachments(tag, level.registryAccess(), chunk);

            chunks.put(String.valueOf(ChunkPos.asLong(local.x, local.z)), chunkTag);
        }

        tag.put("chunks", chunks);
        return tag;
    }

    /**
     * Deserializes a plot from an NBT tag
     */
    public void load(final CompoundTag tag) {
        final int logSize = tag.getIntOr("log_size", 0);
        if (logSize != this.logSize) {
            throw new IllegalArgumentException("Log size mismatch");
        }

        final int dataVersion = tag.getIntOr("data_version", 0);
        if (dataVersion < 0 || dataVersion > DATA_VERSION) {
            throw new IllegalArgumentException("Unsupported version: " + dataVersion);
        }

        final ServerSubLevel subLevel = this.getSubLevel();
        final ServerLevel level = subLevel.getLevel();

        if (tag.contains("biome")) {
            final ResourceLocation location = ResourceLocation.tryParse(tag.getStringOr("biome", ""));

            if (location != null) {
                this.biome = ResourceKey.create(Registries.BIOME, location);
            }
        }

        final CompoundTag chunks = tag.getCompoundOrEmpty("chunks");
        for (final String key : chunks.keySet()) {
            final long chunkPos = Long.parseLong(key);

            final int x = ChunkPos.getX(chunkPos);
            final int z = ChunkPos.getZ(chunkPos);
            final ChunkPos local = new ChunkPos(x, z);
            final ChunkPos global = this.toGlobal(local);

            final CompoundTag chunkTag = chunks.getCompoundOrEmpty(key);
            final CompoundTag sectionsTag = chunkTag.getCompoundOrEmpty("sections");

            this.newNonLitChunk(global);
            final LevelChunk chunk = this.getChunk(local);

            boolean hasLit = false;
            for (final String sectionKey : sectionsTag.keySet()) {
                final int yIndex = Integer.parseInt(sectionKey);


                final LevelChunkSection[] sections = chunk.getSections();

                final PalettedContainer<BlockState> palettedContainer;
                final CompoundTag sectionTag = sectionsTag.getCompoundOrEmpty(sectionKey);

                palettedContainer = BLOCK_STATE_CODEC.parse(NbtOps.INSTANCE, sectionTag.getCompoundOrEmpty("block_states"))
                        .promotePartial(string -> logLoadingErrors(new ChunkPos(chunkPos), chunk.getSectionYFromSectionIndex(yIndex), string))
                        .getOrThrow(SerializableChunkData.ChunkReadException::new);

                final Registry<Biome> biomeRegistry = level.registryAccess().lookupOrThrow(Registries.BIOME);
                final PalettedContainer<Holder<Biome>> biomeContainer = new PalettedContainer<>(biomeRegistry.getOrThrow(this.biome), level.palettedContainerFactory().biomeStrategy());

                sections[yIndex] = new LevelChunkSection(palettedContainer, biomeContainer);

                final SectionPos sectionPos = SectionPos.of(global, level.getSectionYFromSectionIndex(yIndex));

                final boolean hasBlockLight =
                        LightEngineCompat.hasBlockLight(this.lightEngine) && sectionTag.contains("BlockLight");
                final boolean hasSkyLight =
                        LightEngineCompat.hasSkyLight(this.lightEngine)
                                && level.dimensionType().hasSkyLight()
                                && sectionTag.contains("SkyLight");
                if (hasBlockLight || hasSkyLight) {
                    if (!hasLit) {
                        this.lightEngine.retainData(global, true);
                        hasLit = true;
                    }

                    if (hasBlockLight) {
                        this.lightEngine.queueSectionData(LightLayer.BLOCK, sectionPos, sectionTag.getByteArray("BlockLight").map(DataLayer::new).orElse(null));
                    }

                    if (hasSkyLight) {
                        this.lightEngine.queueSectionData(LightLayer.SKY, sectionPos, sectionTag.getByteArray("SkyLight").map(DataLayer::new).orElse(null));
                    }
                }
            }

            if (dataVersion >= 0) {
                // PORT-NOTE(mc26.1): LevelChunkTicks.load is gone; ticks parse through SavedTick codecs
                // (filtered to the chunk) and feed the LevelChunkTicks(List) ctor, like SerializableChunkData.
                final LevelChunkTicks<Block> blockTicks = new LevelChunkTicks<>(
                        SavedTick.filterTickListForChunk(chunkTag.read("block_ticks", BLOCK_TICKS_CODEC).orElse(List.of()), global)
                );
                final LevelChunkTicks<Fluid> fluidTicks = new LevelChunkTicks<>(
                        SavedTick.filterTickListForChunk(chunkTag.read("fluid_ticks", FLUID_TICKS_CODEC).orElse(List.of()), global)
                );

                //noinspection unchecked
                ((LevelChunkTicksExtension<Block>) chunk.getBlockTicks()).sable$copy(blockTicks);
                //noinspection unchecked
                ((LevelChunkTicksExtension<Fluid>) chunk.getFluidTicks()).sable$copy(fluidTicks);

                final CompoundTag heightMapsTag = chunkTag.getCompoundOrEmpty("heightmaps");
                final EnumSet<Heightmap.Types> enumset = EnumSet.noneOf(Heightmap.Types.class);

                for (final Heightmap.Types heightMapType : chunk.getPersistedStatus().heightmapsAfter()) {
                    final String heightMapKey = heightMapType.getSerializationKey();
                    final Optional<long[]> heightmapData = heightMapsTag.getLongArray(heightMapKey);
                    if (heightmapData.isPresent()) {
                        chunk.setHeightmap(heightMapType, heightmapData.get());
                    } else {
                        enumset.add(heightMapType);
                    }
                }

                Heightmap.primeHeightmaps(chunk, enumset);

                SablePlotPlatform.INSTANCE.readLightData(chunkTag, level.registryAccess(), chunk);

                chunk.setLightCorrect(chunkTag.getBooleanOr("isLightOn", false));
            }

            // Setup lighting
            this.lightChunk(chunk);

            SablePlotPlatform.INSTANCE.readChunkAttachments(chunkTag, level.registryAccess(), chunk);

            final ListTag blockEntitiesTag = chunkTag.getListOrEmpty("block_entities");

            // Add block entities
            for (int i = 0; i < blockEntitiesTag.size(); i++) {
                final CompoundTag blockEntityTag = blockEntitiesTag.getCompoundOrEmpty(i);
                final boolean keepBlockEntityPacked = blockEntityTag.getBooleanOr("keepPacked", false);

                if (keepBlockEntityPacked) {
                    chunk.setBlockEntityNbt(blockEntityTag);
                } else {
                    // PORT-NOTE(mc26.1): getPosFromTag now resolves against the owning chunk's position.
                    final BlockPos blockPos = BlockEntity.getPosFromTag(chunk.getPos(), blockEntityTag);
                    final BlockEntity blockEntity = BlockEntity.loadStatic(blockPos, chunk.getBlockState(blockPos), blockEntityTag, level.registryAccess());
                    if (blockEntity != null) {
                        chunk.setBlockEntity(blockEntity);
                    }
                }
            }

            chunk.registerAllBlockEntitiesAfterLevelLoad();
            level.startTickingChunk(chunk);

            SablePlotPlatform.INSTANCE.postLoad(chunkTag, chunk);
            SableChunkEventPlatform.INSTANCE.onPlotChunkLoaded(chunk);
        }

        // Before we send the chunks, let's ensure our lighting data is complete
        do {
            this.lightEngine.runLightUpdates();
        } while (this.lightEngine.hasLightWork());

        final SubLevelPhysicsSystem physicsSystem = ((ServerSubLevelContainer) this.container).physicsSystem();

        final BlockPos.MutableBlockPos globalBlockPos = new BlockPos.MutableBlockPos();

        // go through them all again
        for (final String key : chunks.keySet()) {
            final long chunkPos = Long.parseLong(key);

            final int x = ChunkPos.getX(chunkPos);
            final int z = ChunkPos.getZ(chunkPos);
            final ChunkPos local = new ChunkPos(x, z);
            final ChunkPos global = this.toGlobal(local);

            final PlotChunkHolder chunkHolder = this.getChunkHolder(local);
            final LevelChunk chunk = this.getChunk(local);
            final LevelChunkSection[] levelChunkSections = chunk.getSections();

            final Iterable<ServerPlayer> players = this.container.getPlayersTracking(global);
            for (final ServerPlayer player : players) {
                SubLevelPlayerChunkSender.sendChunk(player.connection::send, this.lightEngine, chunk);
                SubLevelPlayerChunkSender.sendChunkPoiData(level, chunk);
            }

            for (int i = 0; i < chunk.getSectionsCount(); i++) {
                final LevelChunkSection section = levelChunkSections[i];
                if (!section.hasOnlyAir()) {
                    final int sectionY = chunk.getSectionYFromSectionIndex(i);
                    final int chunkMinX = global.getMinBlockX();
                    final int chunkMinY = sectionY << 4;
                    final int chunkMinZ = global.getMinBlockZ();

                    final boolean expandPlotBackup = this.expandPlotIfNecessary;

                    // We don't want to expand the plot while loading it
                    this.expandPlotIfNecessary = false;

                    final BlockState airState = Blocks.AIR.defaultBlockState();
                    for (int xOff = 0; xOff < 16; xOff++) {
                        for (int yOff = 0; yOff < 16; yOff++) {
                            for (int zOff = 0; zOff < 16; zOff++) {
                                final BlockState state = section.getBlockState(xOff, yOff, zOff);

                                if (!state.isAir()) {
                                    globalBlockPos.set(xOff + chunkMinX, yOff + chunkMinY, zOff + chunkMinZ);
                                    final BlockPos immutable = globalBlockPos.immutable();

                                    chunkHolder.handleBlockChange(xOff, chunkMinY + yOff, zOff, airState, state);
                                    subLevel.getHeatMapManager().onSolidAdded(immutable);
                                    subLevel.getFloatingBlockController().queueAddFloatingBlock(state, immutable);
                                    physicsSystem.updateMassDataFromBlockChange(subLevel, globalBlockPos, airState, state, false);
                                    this.onBlockChange(immutable, state);
                                }
                            }
                        }
                    }

                    // upload
                    this.expandPlotIfNecessary = expandPlotBackup;
                }
            }
        }

        this.updateBoundingBox();
        subLevel.updateMergedMassData(1.0f);
        physicsSystem.getPipeline().onStatsChanged(subLevel);

        for (final String key : chunks.keySet()) {
            final long chunkPos = Long.parseLong(key);

            final int x = ChunkPos.getX(chunkPos);
            final int z = ChunkPos.getZ(chunkPos);
            final ChunkPos local = new ChunkPos(x, z);
            final ChunkPos global = this.toGlobal(local);

            final LevelChunk chunk = this.getChunk(local);
            final LevelChunkSection[] levelChunkSections = chunk.getSections();

            for (int i = 0; i < chunk.getSectionsCount(); i++) {
                final LevelChunkSection section = levelChunkSections[i];
                if (!section.hasOnlyAir()) {
                    final int sectionY = chunk.getSectionYFromSectionIndex(i);
                    physicsSystem.getTicketManager().addTicketForSection(level, SectionPos.of(global.x, sectionY, global.z));
                    physicsSystem.getPipeline().handleChunkSectionAddition(section, global.x, sectionY, global.z, true);
                }
            }
        }

        subLevel.updateMergedMassData(1.0f);
        physicsSystem.getPipeline().onStatsChanged(subLevel);

    }

    /**
     * Handles a change in block-state in the plot at global block position x, y, z.
     *
     * @param state the new block-state
     */
    @Override
    public void onBlockChange(final BlockPos pos, final BlockState state) {
        super.onBlockChange(pos, state);

        this.liftProviders.remove(pos.asLong());

        if (state.getBlock() instanceof final BlockSubLevelLiftProvider prov) {
            // PORT-NOTE(mc26.1): Direction.getUnitVec3i() was renamed getUnitVec3i().
            this.liftProviders.put(pos.asLong(), new BlockSubLevelLiftProvider.LiftProviderContext(pos, state, Vec3.atLowerCornerOf(prov.sable$getNormal(state).getUnitVec3i())));
        }
    }

    /**
     * Gets all lift providers
     */
    public ObjectCollection<BlockSubLevelLiftProvider.LiftProviderContext> getLiftProviders() {
        return this.liftProviders.values();
    }

    /**
     * @return the sub-level using this plot.
     */
    @Override
    public ServerSubLevel getSubLevel() {
        return (ServerSubLevel) super.getSubLevel();
    }
}
