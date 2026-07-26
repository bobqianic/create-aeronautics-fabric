package dev.eriksonn.aeronautics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.TagValueInput;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Places a Ponder structure through the real GameTest server and verifies that the port still
 * decodes and places the reference exactly. Functional tests can reuse the same generated fixtures.
 */
public abstract class PonderStructureGameTest {
    private static final String MIGRATION_OUTPUT_PROPERTY = "aeronautics.ponderMigrationOutput";
    private static final String RUNTIME_OVERRIDES_RESOURCE = "/ponder-runtime-overrides.snbt";
    private static final CompoundTag RUNTIME_OVERRIDES = loadRuntimeOverrideData();
    private static final Comparator<BlockPos> BLOCK_POSITION_ORDER = Comparator
            .comparingInt((BlockPos pos) -> pos.getY())
            .thenComparingInt(pos -> pos.getZ())
            .thenComparingInt(pos -> pos.getX());

    protected final void verifyPonderStructure(
            final GameTestHelper helper,
            final String namespace,
            final String ponderPath
    ) {
        try {
            final ResourceLocation templateId = ResourceLocation.fromNamespaceAndPath(
                    namespace,
                    "ponder/" + ponderPath
            );

            final Map<ResourceLocation, Integer> expectedEntities = validateRawFixture(
                    helper,
                    namespace,
                    ponderPath,
                    templateId
            );

            final StructureTemplate template = helper.getLevel()
                    .getStructureManager()
                    .get(templateId)
                    .orElseThrow(() -> helper.assertionException("Missing Ponder fixture %s", templateId));

            final String migrationOutput = System.getProperty(MIGRATION_OUTPUT_PROPERTY, "");
            if (!migrationOutput.isBlank()) {
                writeMigratedFixture(helper, template, namespace, ponderPath, migrationOutput);
                helper.succeed();
                return;
            }

            final StructurePlaceSettings placement = new StructurePlaceSettings();
            final Map<BlockPos, StructureTemplate.StructureBlockInfo> expectedBlocks = new TreeMap<>(
                    BLOCK_POSITION_ORDER
            );

            for (final Block block : BuiltInRegistries.BLOCK) {
                for (final StructureTemplate.StructureBlockInfo expected
                        : template.filterBlocks(BlockPos.ZERO, placement, block, true)) {
                    final StructureTemplate.StructureBlockInfo duplicate = expectedBlocks.put(expected.pos(), expected);
                    if (duplicate != null) {
                        throw helper.assertionException(
                                expected.pos(),
                                "Ponder fixture %s contains duplicate blocks at %s",
                                templateId,
                                expected.pos()
                        );
                    }
                }
            }

            if (expectedBlocks.isEmpty()) {
                throw helper.assertionException("Ponder fixture %s contains no blocks", templateId);
            }

            final Map<BlockPos, RuntimeStateOverride> runtimeOverrides = runtimeOverrides(
                    helper,
                    templateId,
                    expectedBlocks
            );

            for (final StructureTemplate.StructureBlockInfo expected : expectedBlocks.values()) {
                final BlockState expectedPlacedState = assertBlockState(
                        helper,
                        templateId,
                        expected,
                        runtimeOverrides.get(expected.pos())
                );
                assertBlockEntityData(helper, templateId, expected, expectedPlacedState);
            }

            assertEntities(helper, templateId, expectedEntities);

            helper.succeed();
        } finally {
            // Legacy Ponder payloads can be malformed enough to crash on their first server tick.
            // They have already been fully loaded and compared at this point, so retire their
            // block entities before the GameTest framework advances the shared test world.
            helper.forEveryBlockInStructure(relativePos -> {
                final BlockEntity blockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(relativePos));
                if (blockEntity != null) {
                    blockEntity.setRemoved();
                }
            });
            helper.killAllEntities();
        }
    }

    private static void writeMigratedFixture(
            final GameTestHelper helper,
            final StructureTemplate original,
            final String namespace,
            final String ponderPath,
            final String migrationOutput
    ) {
        final StructureTemplate captured = new StructureTemplate();
        captured.fillFromWorld(
                helper.getLevel(),
                helper.absolutePos(BlockPos.ZERO),
                original.getSize(),
                true,
                List.of(Blocks.AIR, Blocks.STRUCTURE_VOID)
        );

        final Path outputRoot = Path.of(migrationOutput).toAbsolutePath().normalize();
        final Path output = outputRoot.resolve(namespace).resolve(ponderPath + ".nbt").normalize();
        if (!output.startsWith(outputRoot)) {
            throw helper.assertionException("Ponder migration path escaped output directory: %s", output);
        }

        try {
            Files.createDirectories(output.getParent());
            NbtIo.writeCompressed(captured.save(new CompoundTag()), output);
        } catch (final IOException exception) {
            throw helper.assertionException(
                    "Could not write migrated Ponder fixture %s:%s: %s",
                    namespace,
                    ponderPath,
                    exception.getMessage()
            );
        }
    }

    private static Map<ResourceLocation, Integer> validateRawFixture(
            final GameTestHelper helper,
            final String namespace,
            final String ponderPath,
            final ResourceLocation templateId
    ) {
        final String resourcePath = "/data/%s/structure/ponder/%s.nbt".formatted(namespace, ponderPath);

        try (InputStream input = PonderStructureGameTest.class.getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw helper.assertionException("Missing raw Ponder fixture %s at %s", templateId, resourcePath);
            }

            final CompoundTag structure = NbtIo.readCompressed(input, NbtAccounter.unlimitedHeap());
            final ListTag palette = structure.getListOrEmpty("palette");

            if (!palette.isEmpty()) {
                validatePalette(helper, templateId, palette);
            } else {
                final ListTag palettes = structure.getListOrEmpty("palettes");
                if (palettes.isEmpty()) {
                    throw helper.assertionException("Ponder fixture %s has no block palette", templateId);
                }

                for (int index = 0; index < palettes.size(); index++) {
                    validatePalette(helper, templateId, palettes.getListOrEmpty(index));
                }
            }

            return validateEntities(helper, templateId, structure.getListOrEmpty("entities"));
        } catch (final IOException exception) {
            throw helper.assertionException(
                    "Could not read Ponder fixture %s: %s",
                    templateId,
                    exception.getMessage()
            );
        }
    }

    private static Map<ResourceLocation, Integer> validateEntities(
            final GameTestHelper helper,
            final ResourceLocation templateId,
            final ListTag entities
    ) {
        final Map<ResourceLocation, Integer> expectedTypes = new TreeMap<>();

        for (int index = 0; index < entities.size(); index++) {
            final CompoundTag entityData = entities.getCompoundOrEmpty(index).getCompoundOrEmpty("nbt");
            final String entityName = entityData.getStringOr("id", "");
            final ResourceLocation entityId = ResourceLocation.tryParse(entityName);

            if (entityId == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(entityId)) {
                throw helper.assertionException(
                        "Ponder fixture %s entity %s refers to missing entity type '%s'",
                        templateId,
                        index,
                        entityName
                );
            }

            final Entity decoded = EntityType.loadEntityRecursive(
                    entityData.copy(),
                    helper.getLevel(),
                    EntitySpawnReason.LOAD,
                    entity -> entity
            );
            if (decoded == null) {
                throw helper.assertionException(
                        "Ponder fixture %s entity %s (%s) could not be decoded",
                        templateId,
                        index,
                        entityId
                );
            }

            final int expectedPassengers = entityData.getListOrEmpty("Passengers").size();
            final int decodedPassengers = decoded.getPassengers().size();
            if (decodedPassengers != expectedPassengers) {
                throw helper.assertionException(
                        "Ponder fixture %s entity %s (%s) expected %s decoded passengers, found %s",
                        templateId,
                        index,
                        entityId,
                        expectedPassengers,
                        decodedPassengers
                );
            }

            expectedTypes.merge(entityId, 1, Integer::sum);
        }

        return expectedTypes;
    }

    private static void validatePalette(
            final GameTestHelper helper,
            final ResourceLocation templateId,
            final ListTag palette
    ) {
        for (int index = 0; index < palette.size(); index++) {
            final CompoundTag serializedState = palette.getCompoundOrEmpty(index);
            final String blockName = serializedState.getStringOr("Name", "");
            final ResourceLocation blockId = ResourceLocation.tryParse(blockName);

            if (blockId == null || !BuiltInRegistries.BLOCK.containsKey(blockId)) {
                throw helper.assertionException(
                        "Ponder fixture %s palette entry %s refers to missing block '%s'",
                        templateId,
                        index,
                        blockName
                );
            }

            final Block block = BuiltInRegistries.BLOCK.getValue(blockId);
            final CompoundTag properties = serializedState.getCompoundOrEmpty("Properties");
            for (final String propertyName : properties.keySet()) {
                final Property<?> property = block.getStateDefinition().getProperty(propertyName);
                final String propertyValue = properties.getStringOr(propertyName, "");

                if (property == null) {
                    throw helper.assertionException(
                            "Ponder fixture %s uses missing property '%s' on %s",
                            templateId,
                            propertyName,
                            blockId
                    );
                }

                if (property.getValue(propertyValue).isEmpty()) {
                    throw helper.assertionException(
                            "Ponder fixture %s uses invalid value '%s' for %s.%s",
                            templateId,
                            propertyValue,
                            blockId,
                            propertyName
                    );
                }
            }
        }
    }

    private static BlockState assertBlockState(
            final GameTestHelper helper,
            final ResourceLocation templateId,
            final StructureTemplate.StructureBlockInfo expected,
            final RuntimeStateOverride runtimeOverride
    ) {
        final BlockState actual = helper.getBlockState(expected.pos());
        if (runtimeOverride == null) {
            if (actual != expected.state()) {
                throw helper.assertionException(
                        expected.pos(),
                        "Ponder fixture %s expected %s, found %s",
                        templateId,
                        expected.state(),
                        actual
                );
            }
        } else {
            if (runtimeOverride.referenceState() != expected.state()) {
                throw helper.assertionException(
                        expected.pos(),
                        "Ponder fixture %s runtime override is stale: fixture now contains %s, override describes %s",
                        templateId,
                        expected.state(),
                        runtimeOverride.referenceState()
                );
            }
            if (actual != expected.state() && runtimeOverride.placedStates().stream().noneMatch(state -> state == actual)) {
                throw helper.assertionException(
                        expected.pos(),
                        "Ponder fixture %s expected original state %s or reviewed placed states %s, found %s",
                        templateId,
                        expected.state(),
                        runtimeOverride.placedStates(),
                        actual
                );
            }
        }
        return actual;
    }

    private static void assertBlockEntityData(
            final GameTestHelper helper,
            final ResourceLocation templateId,
            final StructureTemplate.StructureBlockInfo expected,
            final BlockState expectedPlacedState
    ) {
        if (expected.nbt() == null) {
            return;
        }

        final String expectedType = expected.nbt().getStringOr("id", "");
        if (expectedType.isEmpty()) {
            throw helper.assertionException(
                    expected.pos(),
                    "Ponder fixture %s has block-entity data without an id",
                    templateId
            );
        }

        final ResourceLocation expectedTypeId = ResourceLocation.tryParse(expectedType);
        if (expectedTypeId == null || !BuiltInRegistries.BLOCK_ENTITY_TYPE.containsKey(expectedTypeId)) {
            throw helper.assertionException(
                    expected.pos(),
                    "Ponder fixture %s refers to missing block entity type %s",
                    templateId,
                    expectedType
            );
        }

        final BlockEntityType<?> expectedBlockEntityType = BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(expectedTypeId);
        if (!expectedBlockEntityType.isValid(expected.state())) {
            throw helper.assertionException(
                    expected.pos(),
                    "Ponder fixture %s block entity %s is not valid for %s",
                    templateId,
                    expectedType,
                    expected.state()
            );
        }

        final BlockEntity decoded = expectedBlockEntityType.create(expected.pos(), expected.state());
        if (decoded == null) {
            throw helper.assertionException(
                    expected.pos(),
                    "Ponder fixture %s could not create block entity %s",
                    templateId,
                    expectedType
            );
        }
        decoded.setLevel(helper.getLevel());

        final ProblemReporter.Collector problems = new ProblemReporter.Collector(decoded.problemPath());
        decoded.loadWithComponents(TagValueInput.create(
                problems,
                helper.getLevel().registryAccess(),
                expected.nbt()
        ));
        if (!problems.isEmpty()) {
            throw helper.assertionException(
                    expected.pos(),
                    "Ponder fixture %s could not decode data for block entity %s: %s",
                    templateId,
                    expectedType,
                    problems.getTreeReport().replace(System.lineSeparator(), "; ")
            );
        }
        decoded.setRemoved();

        if (!expectedBlockEntityType.isValid(expectedPlacedState)) {
            return;
        }

        final BlockEntity actual = helper.getLevel().getBlockEntity(helper.absolutePos(expected.pos()));
        if (actual == null) {
            throw helper.assertionException(
                    expected.pos(),
                    "Ponder fixture %s expected block entity %s, but none was created",
                    templateId,
                    expectedType
            );
        }

        final ResourceLocation actualType = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(actual.getType());
        if (!expectedTypeId.equals(actualType)) {
            throw helper.assertionException(
                    expected.pos(),
                    "Ponder fixture %s expected block entity %s, found %s",
                    templateId,
                    expectedType,
                    actualType
            );
        }
    }

    private static Map<BlockPos, RuntimeStateOverride> runtimeOverrides(
            final GameTestHelper helper,
            final ResourceLocation templateId,
            final Map<BlockPos, StructureTemplate.StructureBlockInfo> expectedBlocks
    ) {
        final ListTag serializedOverrides = RUNTIME_OVERRIDES.getListOrEmpty(templateId.toString());
        final Map<BlockPos, RuntimeStateOverride> overrides = new HashMap<>();
        final var blockRegistry = helper.getLevel().registryAccess().lookupOrThrow(Registries.BLOCK);

        for (int index = 0; index < serializedOverrides.size(); index++) {
            final CompoundTag serialized = serializedOverrides.getCompoundOrEmpty(index);
            final ListTag serializedPos = serialized.getListOrEmpty("Pos");
            if (serializedPos.size() != 3) {
                throw helper.assertionException(
                        "Ponder fixture %s runtime override %s has invalid position %s",
                        templateId,
                        index,
                        serializedPos
                );
            }

            final BlockPos pos = new BlockPos(
                    serializedPos.getIntOr(0, 0),
                    serializedPos.getIntOr(1, 0),
                    serializedPos.getIntOr(2, 0)
            );
            if (!expectedBlocks.containsKey(pos)) {
                throw helper.assertionException(
                        pos,
                        "Ponder fixture %s runtime override points to a missing fixture block",
                        templateId
                );
            }

            final ListTag serializedPlacedStates = serialized.getListOrEmpty("PlacedStates");
            final List<BlockState> placedStates = new java.util.ArrayList<>();
            for (int stateIndex = 0; stateIndex < serializedPlacedStates.size(); stateIndex++) {
                placedStates.add(NbtUtils.readBlockState(
                        blockRegistry,
                        serializedPlacedStates.getCompoundOrEmpty(stateIndex)
                ));
            }
            // Keeps older generated snapshots readable during a migration run.
            serialized.getCompound("Placed").ifPresent(placed ->
                    placedStates.add(NbtUtils.readBlockState(blockRegistry, placed))
            );

            final RuntimeStateOverride override = new RuntimeStateOverride(
                    NbtUtils.readBlockState(blockRegistry, serialized.getCompoundOrEmpty("Reference")),
                    List.copyOf(placedStates)
            );
            if (overrides.put(pos, override) != null) {
                throw helper.assertionException(
                        pos,
                        "Ponder fixture %s has duplicate runtime overrides",
                        templateId
                );
            }
        }

        return overrides;
    }

    private static CompoundTag loadRuntimeOverrideData() {
        try (InputStream input = PonderStructureGameTest.class.getResourceAsStream(RUNTIME_OVERRIDES_RESOURCE)) {
            if (input == null) {
                throw new IllegalStateException("Missing " + RUNTIME_OVERRIDES_RESOURCE);
            }
            return TagParser.parseCompoundFully(new String(input.readAllBytes(), StandardCharsets.UTF_8));
        } catch (final Exception exception) {
            throw new IllegalStateException("Could not read " + RUNTIME_OVERRIDES_RESOURCE, exception);
        }
    }

    private record RuntimeStateOverride(BlockState referenceState, List<BlockState> placedStates) {
    }

    private static void assertEntities(
            final GameTestHelper helper,
            final ResourceLocation templateId,
            final Map<ResourceLocation, Integer> expectedEntities
    ) {
        for (final Map.Entry<ResourceLocation, Integer> expected : expectedEntities.entrySet()) {
            final EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getValue(expected.getKey());
            final int actualCount = helper.getEntities(entityType).size();

            if (actualCount != expected.getValue()) {
                throw helper.assertionException(
                        "Ponder fixture %s expected %s %s entities, found %s",
                        templateId,
                        expected.getValue(),
                        expected.getKey(),
                        actualCount
                );
            }
        }
    }
}
