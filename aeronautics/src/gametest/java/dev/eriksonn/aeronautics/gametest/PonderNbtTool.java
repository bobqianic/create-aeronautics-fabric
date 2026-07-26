package dev.eriksonn.aeronautics.gametest;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/** Small raw-NBT diagnostic used while migrating Ponder fixtures between Minecraft versions. */
public final class PonderNbtTool {
    private PonderNbtTool() {
    }

    public static void main(final String[] args) throws Exception {
        if (args.length == 0) {
            throw new IllegalArgumentException("Expected at least one Ponder .nbt path");
        }

        if ("migrate-static".equals(args[0])) {
            if (args.length < 3) {
                throw new IllegalArgumentException("Expected migrate-static <output> <ponder-root>...");
            }

            final Path output = Path.of(args[1]).toAbsolutePath().normalize();
            for (int index = 2; index < args.length; index++) {
                migrateRoot(Path.of(args[index]).toAbsolutePath().normalize(), output);
            }
            return;
        }

        if ("merge-runtime".equals(args[0])) {
            if (args.length < 4) {
                throw new IllegalArgumentException(
                        "Expected merge-runtime <output> <captured-root> <ponder-root>..."
                );
            }

            final Path output = Path.of(args[1]).toAbsolutePath().normalize();
            final Path captured = Path.of(args[2]).toAbsolutePath().normalize();
            for (int index = 3; index < args.length; index++) {
                mergeRuntimeRoot(
                        Path.of(args[index]).toAbsolutePath().normalize(),
                        captured,
                        output
                );
            }
            return;
        }

        if ("compare-runtime".equals(args[0])) {
            if (args.length < 3) {
                throw new IllegalArgumentException("Expected compare-runtime <captured-root> <ponder-root>...");
            }

            final Path captured = Path.of(args[1]).toAbsolutePath().normalize();
            for (int index = 2; index < args.length; index++) {
                compareRuntimeRoot(Path.of(args[index]).toAbsolutePath().normalize(), captured);
            }
            return;
        }

        if ("write-runtime-overrides".equals(args[0])) {
            if (args.length < 5) {
                throw new IllegalArgumentException(
                        "Expected write-runtime-overrides <output> <seed> <captured-root> <ponder-root>..."
                );
            }

            final Path output = Path.of(args[1]).toAbsolutePath().normalize();
            final Path seed = Path.of(args[2]).toAbsolutePath().normalize();
            final Path captured = Path.of(args[3]).toAbsolutePath().normalize();
            final CompoundTag overrides = Files.isRegularFile(seed)
                    ? TagParser.parseCompoundFully(Files.readString(seed, StandardCharsets.UTF_8))
                    : new CompoundTag();
            normalizeRuntimeOverrides(overrides);
            for (int index = 4; index < args.length; index++) {
                collectRuntimeOverrides(Path.of(args[index]).toAbsolutePath().normalize(), captured, overrides);
            }
            Files.createDirectories(output.getParent());
            Files.writeString(output, overrides.toString(), StandardCharsets.UTF_8);
            System.out.printf("Wrote %s runtime override sets to %s%n", overrides.size(), output);
            return;
        }

        if ("inspect-position".equals(args[0])) {
            if (args.length != 5) {
                throw new IllegalArgumentException("Expected inspect-position <file> <x> <y> <z>");
            }
            inspectPosition(
                    Path.of(args[1]),
                    Integer.parseInt(args[2]),
                    Integer.parseInt(args[3]),
                    Integer.parseInt(args[4])
            );
            return;
        }

        for (final String argument : args) {
            dump(Path.of(argument));
        }
    }

    private static void migrateRoot(final Path sourceRoot, final Path outputRoot) throws Exception {
        final String namespace = sourceRoot.getParent().getFileName().toString();
        int changed = 0;
        int total = 0;

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            for (final Path source : paths.filter(path -> path.toString().endsWith(".nbt")).sorted().toList()) {
                total++;
                final CompoundTag structure = NbtIo.readCompressed(source, NbtAccounter.unlimitedHeap());
                final CompoundTag original = structure.copy();
                migrateStaticData(structure);

                final Path relative = sourceRoot.relativize(source);
                final Path target = outputRoot.resolve(namespace).resolve(relative).normalize();
                if (!target.startsWith(outputRoot)) {
                    throw new IllegalArgumentException("Ponder output escaped migration directory: " + target);
                }

                Files.createDirectories(target.getParent());
                NbtIo.writeCompressed(structure, target);
                if (!structure.equals(original)) {
                    changed++;
                    System.out.printf("migrated %s:%s%n", namespace, relative.toString().replace('\\', '/'));
                }
            }
        }

        System.out.printf("%s: wrote %s fixtures (%s changed) to %s%n", namespace, total, changed, outputRoot);
    }

    private static void mergeRuntimeRoot(
            final Path referenceRoot,
            final Path capturedRoot,
            final Path outputRoot
    ) throws Exception {
        final String namespace = referenceRoot.getParent().getFileName().toString();
        final Path capturedNamespace = capturedRoot.resolve(namespace).normalize();
        int total = 0;
        int replaced = 0;
        int added = 0;
        int removed = 0;
        int unavailable = 0;

        try (Stream<Path> paths = Files.walk(referenceRoot)) {
            for (final Path referencePath
                    : paths.filter(path -> path.toString().endsWith(".nbt")).sorted().toList()) {
                total++;
                final Path relative = referenceRoot.relativize(referencePath);
                final Path capturedPath = capturedNamespace.resolve(relative).normalize();
                if (!capturedPath.startsWith(capturedNamespace) || !Files.isRegularFile(capturedPath)) {
                    throw new IllegalArgumentException("Missing captured Ponder fixture: " + capturedPath);
                }

                final CompoundTag reference = NbtIo.readCompressed(referencePath, NbtAccounter.unlimitedHeap());
                final CompoundTag captured = NbtIo.readCompressed(capturedPath, NbtAccounter.unlimitedHeap());
                migrateStaticData(reference);

                final ListTag referencePalette = reference.getListOrEmpty("palette");
                final ListTag capturedPalette = captured.getListOrEmpty("palette");
                final ListTag referenceBlocks = reference.getListOrEmpty("blocks");
                final Map<String, CompoundTag> capturedBlocks = blocksByPosition(captured.getListOrEmpty("blocks"));

                for (int index = 0; index < referenceBlocks.size(); index++) {
                    final CompoundTag referenceBlock = referenceBlocks.getCompoundOrEmpty(index);
                    final String position = referenceBlock.getListOrEmpty("pos").toString();
                    final CompoundTag capturedBlock = capturedBlocks.get(position);
                    final CompoundTag referenceNbt = referenceBlock.getCompound("nbt").orElse(null);

                    if (capturedBlock == null
                            || !blockName(referencePalette, referenceBlock)
                            .equals(blockName(capturedPalette, capturedBlock))) {
                        if (referenceNbt != null) {
                            unavailable++;
                            System.out.printf(
                                    "kept unmatched block entity %s:%s at %s (%s -> %s)%n",
                                    namespace,
                                    relative.toString().replace('\\', '/'),
                                    position,
                                    blockName(referencePalette, referenceBlock),
                                    capturedBlock == null ? "<absent>" : blockName(capturedPalette, capturedBlock)
                            );
                        }
                        continue;
                    }

                    final CompoundTag capturedNbt = capturedBlock.getCompound("nbt").orElse(null);
                    if (capturedNbt == null) {
                        if (referenceNbt != null) {
                            referenceBlock.remove("nbt");
                            removed++;
                        }
                    } else {
                        referenceBlock.put("nbt", capturedNbt.copy());
                        if (referenceNbt == null) {
                            added++;
                        } else if (!referenceNbt.equals(capturedNbt)) {
                            replaced++;
                        }
                    }
                }

                reference.putInt(
                        "DataVersion",
                        captured.getIntOr("DataVersion", reference.getIntOr("DataVersion", -1))
                );

                final Path target = outputRoot.resolve(namespace).resolve(relative).normalize();
                if (!target.startsWith(outputRoot)) {
                    throw new IllegalArgumentException("Ponder output escaped migration directory: " + target);
                }
                Files.createDirectories(target.getParent());
                NbtIo.writeCompressed(reference, target);
            }
        }

        System.out.printf(
                "%s: merged %s fixtures (%s replaced, %s added, %s removed, %s unmatched block entities) to %s%n",
                namespace,
                total,
                replaced,
                added,
                removed,
                unavailable,
                outputRoot
        );
    }

    private static Map<String, CompoundTag> blocksByPosition(final ListTag blocks) {
        final Map<String, CompoundTag> byPosition = new HashMap<>();
        for (int index = 0; index < blocks.size(); index++) {
            final CompoundTag block = blocks.getCompoundOrEmpty(index);
            final String position = block.getListOrEmpty("pos").toString();
            final CompoundTag duplicate = byPosition.put(position, block);
            if (duplicate != null) {
                throw new IllegalArgumentException("Captured structure has duplicate block position " + position);
            }
        }
        return byPosition;
    }

    private static void compareRuntimeRoot(final Path referenceRoot, final Path capturedRoot) throws Exception {
        final String namespace = referenceRoot.getParent().getFileName().toString();
        final Path capturedNamespace = capturedRoot.resolve(namespace).normalize();
        int differences = 0;

        try (Stream<Path> paths = Files.walk(referenceRoot)) {
            for (final Path referencePath
                    : paths.filter(path -> path.toString().endsWith(".nbt")).sorted().toList()) {
                final Path relative = referenceRoot.relativize(referencePath);
                final Path capturedPath = capturedNamespace.resolve(relative).normalize();
                if (!Files.isRegularFile(capturedPath)) {
                    throw new IllegalArgumentException("Missing captured Ponder fixture: " + capturedPath);
                }

                final CompoundTag reference = NbtIo.readCompressed(referencePath, NbtAccounter.unlimitedHeap());
                final CompoundTag captured = NbtIo.readCompressed(capturedPath, NbtAccounter.unlimitedHeap());
                final ListTag referencePalette = reference.getListOrEmpty("palette");
                final ListTag capturedPalette = captured.getListOrEmpty("palette");
                final Map<String, CompoundTag> capturedBlocks = blocksByPosition(captured.getListOrEmpty("blocks"));

                for (int index = 0; index < reference.getListOrEmpty("blocks").size(); index++) {
                    final CompoundTag referenceBlock = reference.getListOrEmpty("blocks").getCompoundOrEmpty(index);
                    final CompoundTag referenceState = blockState(referencePalette, referenceBlock);
                    if ("minecraft:air".equals(referenceState.getStringOr("Name", ""))) {
                        continue;
                    }

                    final String position = referenceBlock.getListOrEmpty("pos").toString();
                    final CompoundTag capturedBlock = capturedBlocks.get(position);
                    final CompoundTag capturedState = capturedBlock == null
                            ? new CompoundTag()
                            : blockState(capturedPalette, capturedBlock);
                    if (statesDifferForPlacement(referenceState, capturedState)) {
                        differences++;
                        System.out.printf(
                                "%s:%s %s expected=%s actual=%s%n",
                                namespace,
                                relative.toString().replace('\\', '/'),
                                position,
                                referenceState,
                                capturedBlock == null ? "<absent>" : capturedState
                        );
                    }
                }
            }
        }

        System.out.printf("%s: %s runtime state differences%n", namespace, differences);
    }

    private static void collectRuntimeOverrides(
            final Path referenceRoot,
            final Path capturedRoot,
            final CompoundTag overrides
    ) throws Exception {
        final String namespace = referenceRoot.getParent().getFileName().toString();
        final Path capturedNamespace = capturedRoot.resolve(namespace).normalize();

        try (Stream<Path> paths = Files.walk(referenceRoot)) {
            for (final Path referencePath
                    : paths.filter(path -> path.toString().endsWith(".nbt")).sorted().toList()) {
                final Path relative = referenceRoot.relativize(referencePath);
                final Path capturedPath = capturedNamespace.resolve(relative).normalize();
                if (!Files.isRegularFile(capturedPath)) {
                    throw new IllegalArgumentException("Missing captured Ponder fixture: " + capturedPath);
                }

                final CompoundTag reference = NbtIo.readCompressed(referencePath, NbtAccounter.unlimitedHeap());
                final CompoundTag captured = NbtIo.readCompressed(capturedPath, NbtAccounter.unlimitedHeap());
                final ListTag referencePalette = reference.getListOrEmpty("palette");
                final ListTag capturedPalette = captured.getListOrEmpty("palette");
                final Map<String, CompoundTag> capturedBlocks = blocksByPosition(captured.getListOrEmpty("blocks"));
                final String fixturePath = relative.toString()
                        .replace('\\', '/')
                        .replaceFirst("\\.nbt$", "");
                final String fixtureId = namespace + ":ponder/" + fixturePath;
                final ListTag fixtureOverrides;
                if (overrides.contains(fixtureId)) {
                    fixtureOverrides = overrides.getListOrEmpty(fixtureId);
                } else {
                    fixtureOverrides = new ListTag();
                    overrides.put(fixtureId, fixtureOverrides);
                }

                for (int index = 0; index < reference.getListOrEmpty("blocks").size(); index++) {
                    final CompoundTag referenceBlock = reference.getListOrEmpty("blocks").getCompoundOrEmpty(index);
                    final CompoundTag referenceState = blockState(referencePalette, referenceBlock);
                    if ("minecraft:air".equals(referenceState.getStringOr("Name", ""))) {
                        continue;
                    }

                    final String position = referenceBlock.getListOrEmpty("pos").toString();
                    final CompoundTag capturedBlock = capturedBlocks.get(position);
                    final CompoundTag capturedState;
                    if (capturedBlock == null) {
                        capturedState = new CompoundTag();
                        capturedState.putString("Name", "minecraft:air");
                    } else {
                        capturedState = blockState(capturedPalette, capturedBlock);
                    }

                    if (statesDifferForPlacement(referenceState, capturedState)) {
                        CompoundTag override = findRuntimeOverride(fixtureOverrides, position);
                        if (override == null) {
                            override = new CompoundTag();
                            override.put("Pos", referenceBlock.getListOrEmpty("pos").copy());
                            override.put("Reference", referenceState.copy());
                            override.put("PlacedStates", new ListTag());
                            fixtureOverrides.add(override);
                        } else if (!override.getCompoundOrEmpty("Reference").equals(referenceState)) {
                            throw new IllegalArgumentException(
                                    "Runtime override is stale for %s at %s".formatted(fixtureId, position)
                            );
                        }
                        addPlacedState(override.getListOrEmpty("PlacedStates"), capturedState);
                    }
                }

                if (fixtureOverrides.isEmpty()) {
                    overrides.remove(fixtureId);
                }
            }
        }
    }

    private static void normalizeRuntimeOverrides(final CompoundTag overrides) {
        for (final String fixtureId : Set.copyOf(overrides.keySet())) {
            final ListTag fixtureOverrides = overrides.getListOrEmpty(fixtureId);
            for (int index = 0; index < fixtureOverrides.size(); index++) {
                final CompoundTag override = fixtureOverrides.getCompoundOrEmpty(index);
                if (!override.contains("PlacedStates")) {
                    final ListTag placedStates = new ListTag();
                    override.getCompound("Placed").ifPresent(placed -> placedStates.add(placed.copy()));
                    override.remove("Placed");
                    override.put("PlacedStates", placedStates);
                }
            }
        }
    }

    private static CompoundTag findRuntimeOverride(final ListTag overrides, final String position) {
        for (int index = 0; index < overrides.size(); index++) {
            final CompoundTag override = overrides.getCompoundOrEmpty(index);
            if (position.equals(override.getListOrEmpty("Pos").toString())) {
                return override;
            }
        }
        return null;
    }

    private static void addPlacedState(final ListTag placedStates, final CompoundTag state) {
        for (int index = 0; index < placedStates.size(); index++) {
            if (placedStates.getCompoundOrEmpty(index).equals(state)) {
                return;
            }
        }
        placedStates.add(state.copy());
    }

    private static boolean statesDifferForPlacement(
            final CompoundTag referenceState,
            final CompoundTag capturedState
    ) {
        if (!referenceState.getStringOr("Name", "").equals(capturedState.getStringOr("Name", ""))) {
            return true;
        }

        final CompoundTag referenceProperties = referenceState.getCompoundOrEmpty("Properties");
        final CompoundTag capturedProperties = capturedState.getCompoundOrEmpty("Properties");
        for (final String property : referenceProperties.keySet()) {
            if (!referenceProperties.getStringOr(property, "")
                    .equals(capturedProperties.getStringOr(property, ""))) {
                return true;
            }
        }
        return false;
    }

    private static String blockName(final ListTag palette, final CompoundTag block) {
        return blockState(palette, block).getStringOr("Name", "");
    }

    private static CompoundTag blockState(final ListTag palette, final CompoundTag block) {
        final int state = block.getIntOr("state", -1);
        if (state < 0 || state >= palette.size()) {
            final CompoundTag invalid = new CompoundTag();
            invalid.putString("Name", "<invalid-state-%s>".formatted(state));
            return invalid;
        }
        return palette.getCompoundOrEmpty(state);
    }

    private static void migrateStaticData(final CompoundTag structure) {
        final ListTag palette = structure.getListOrEmpty("palette");
        final Set<Integer> removedPhantomStates = new java.util.HashSet<>();

        for (int index = 0; index < palette.size(); index++) {
            final CompoundTag state = palette.getCompoundOrEmpty(index);
            final String blockName = state.getStringOr("Name", "");

            switch (blockName) {
                case "simulated:stirling_engine" -> state.putString("Name", "simulated:red_portable_engine");
                case "simulated:wooden_wing" -> {
                    state.putString("Name", "simulated:white_symmetric_sail");
                    migrateSymmetricSailProperties(state);
                }
                case "simulated:phantom_sail" -> {
                    state.putString("Name", "minecraft:air");
                    state.remove("Properties");
                    removedPhantomStates.add(index);
                }
                case "simulated:white_symmetric_sail" -> migrateSymmetricSailProperties(state);
                case "simulated:altitude_sensor" -> {
                    final CompoundTag properties = state.getCompoundOrEmpty("Properties");
                    properties.remove("powered");
                }
                default -> {
                }
            }
        }

        final ListTag blocks = structure.getListOrEmpty("blocks");
        for (int index = 0; index < blocks.size(); index++) {
            final CompoundTag block = blocks.getCompoundOrEmpty(index);
            if (removedPhantomStates.contains(block.getIntOr("state", -1))) {
                block.remove("nbt");
                continue;
            }

            block.getCompound("nbt").ifPresent(nbt -> {
                final String blockEntityName = nbt.getStringOr("id", "");
                switch (blockEntityName) {
                    case "create:simple_kinetic" -> nbt.putString("id", "create:bracketed_kinetic");
                    case "simulated:red_portable_engine", "simulated:stirling_engine" ->
                            nbt.putString("id", "simulated:portable_engine");
                    case "simulated:analog_transmission" -> nbt.putString("id", "simulated:simple");
                    default -> {
                    }
                }
                migrateLegacyBlockEntityData(nbt);
            });
        }

        final ListTag entities = structure.getListOrEmpty("entities");
        for (int index = entities.size() - 1; index >= 0; index--) {
            final CompoundTag entity = entities.getCompoundOrEmpty(index);
            final CompoundTag entityData = entity.getCompoundOrEmpty("nbt");
            if (entityData.getStringOr("id", "").isBlank()) {
                entities.remove(index);
                continue;
            }

            migrateEntityData(entityData);
        }
    }

    private static void migrateEntityData(final CompoundTag entityData) {
        if ("create:super_glue".equals(entityData.getStringOr("id", ""))
                && !entityData.contains("Box")) {
            final ListTag from = entityData.getListOrEmpty("From");
            final ListTag to = entityData.getListOrEmpty("To");
            if (from.size() == 3 && to.size() == 3) {
                final ListTag box = new ListTag();
                for (int index = 0; index < 3; index++) {
                    box.add(from.get(index).copy());
                }
                for (int index = 0; index < 3; index++) {
                    box.add(to.get(index).copy());
                }
                entityData.put("Box", box);
                entityData.remove("From");
                entityData.remove("To");
            }
        }

        final ListTag passengers = entityData.getListOrEmpty("Passengers");
        for (int index = 0; index < passengers.size(); index++) {
            final CompoundTag passenger = passengers.getCompoundOrEmpty(index);
            if ("minecraft:parrot".equals(passenger.getStringOr("id", ""))) {
                // Legacy Forge entity payloads contain empty ItemStacks and obsolete attributes
                // that make the entire passenger fail to decode on modern vanilla. A parrot's
                // only fixture-specific data here is its colour variant; the seat supplies its
                // position after mounting.
                final CompoundTag migratedParrot = new CompoundTag();
                migratedParrot.putString("id", "minecraft:parrot");
                migratedParrot.putInt("Variant", passenger.getIntOr("Variant", 0));
                migratedParrot.putBoolean("PersistenceRequired", true);
                passengers.set(index, migratedParrot);
            } else {
                migrateEntityData(passenger);
            }
        }
    }

    private static void migrateSymmetricSailProperties(final CompoundTag state) {
        final CompoundTag oldProperties = state.getCompoundOrEmpty("Properties");
        final String facing = oldProperties.getStringOr("facing", "up").toLowerCase(Locale.ROOT);
        final String axis = switch (facing) {
            case "east", "west" -> "x";
            case "north", "south" -> "z";
            default -> "y";
        };
        final CompoundTag properties = new CompoundTag();
        properties.putString("axis", axis);
        state.put("Properties", properties);
    }

    private static void migrateLegacyBlockEntityData(final CompoundTag blockEntityData) {
        migrateLegacyBlockEntityDataRecursively(blockEntityData);
    }

    private static void migrateLegacyBlockEntityDataRecursively(final CompoundTag data) {
        for (final String key : Set.copyOf(data.keySet())) {
            if ("OpenEnd".equals(key)) {
                // Create's pipe-flow cache is transient and its serialized schema changed. It is
                // rebuilt from the placed pipe network, so carrying the legacy payload is harmful.
                data.remove(key);
                continue;
            }

            final Tag value = data.get(key);
            if (value instanceof CompoundTag compound) {
                final var x = compound.getInt("X");
                final var y = compound.getInt("Y");
                final var z = compound.getInt("Z");
                if (x.isPresent() && y.isPresent() && z.isPresent()) {
                    data.putIntArray(key, new int[]{x.get(), y.get(), z.get()});
                } else {
                    migrateLegacyBlockEntityDataRecursively(compound);
                }
            } else if (value instanceof ListTag list) {
                for (int index = 0; index < list.size(); index++) {
                    if (list.get(index) instanceof CompoundTag compound) {
                        migrateLegacyBlockEntityDataRecursively(compound);
                    }
                }
            }
        }

        for (final String enumKey : List.of("Casing", "Dye")) {
            data.getString(enumKey).ifPresent(value -> data.putString(enumKey, value.toLowerCase(Locale.ROOT)));
        }
    }

    private static void dump(final Path path) throws Exception {
        final CompoundTag structure = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
        final ListTag palette = structure.getListOrEmpty("palette");
        final ListTag blocks = structure.getListOrEmpty("blocks");
        final ListTag entities = structure.getListOrEmpty("entities");

        System.out.printf("%n=== %s ===%n", path);
        System.out.printf("size=%s palette=%s blocks=%s entities=%s DataVersion=%s%n",
                structure.getListOrEmpty("size"),
                palette.size(),
                blocks.size(),
                entities.size(),
                structure.getIntOr("DataVersion", -1));

        System.out.println("-- palette --");
        for (int index = 0; index < palette.size(); index++) {
            System.out.printf("[%s] %s%n", index, palette.getCompoundOrEmpty(index));
        }

        System.out.println("-- block entities --");
        for (int index = 0; index < blocks.size(); index++) {
            final CompoundTag block = blocks.getCompoundOrEmpty(index);
            if (!block.contains("nbt")) {
                continue;
            }

            final int stateIndex = block.getIntOr("state", -1);
            final CompoundTag state = stateIndex >= 0 && stateIndex < palette.size()
                    ? palette.getCompoundOrEmpty(stateIndex)
                    : new CompoundTag();
            System.out.printf("block[%s] pos=%s state=%s nbt=%s%n",
                    index,
                    block.getListOrEmpty("pos"),
                    state,
                    block.getCompoundOrEmpty("nbt"));
        }

        System.out.println("-- entities --");
        for (int index = 0; index < entities.size(); index++) {
            final CompoundTag entity = entities.getCompoundOrEmpty(index);
            final ListTag entityPos = entity.getListOrEmpty("blockPos");
            CompoundTag stateAtEntity = new CompoundTag();
            for (int blockIndex = 0; blockIndex < blocks.size(); blockIndex++) {
                final CompoundTag block = blocks.getCompoundOrEmpty(blockIndex);
                if (!block.getListOrEmpty("pos").equals(entityPos)) {
                    continue;
                }

                final int stateIndex = block.getIntOr("state", -1);
                if (stateIndex >= 0 && stateIndex < palette.size()) {
                    stateAtEntity = palette.getCompoundOrEmpty(stateIndex);
                }
                break;
            }
            System.out.printf("entity[%s] stateAtBlockPos=%s %s%n", index, stateAtEntity, entity);
        }
    }

    private static void inspectPosition(final Path path, final int x, final int y, final int z) throws Exception {
        final CompoundTag structure = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
        final ListTag palette = structure.getListOrEmpty("palette");
        final ListTag blocks = structure.getListOrEmpty("blocks");
        final String requestedPosition = "[%s,%s,%s]".formatted(x, y, z);
        final Set<String> nearbyPositions = new java.util.HashSet<>();
        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetY = -1; offsetY <= 1; offsetY++) {
                for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                    nearbyPositions.add("[%s,%s,%s]".formatted(x + offsetX, y + offsetY, z + offsetZ));
                }
            }
        }

        System.out.printf("%n=== %s at %s ===%n", path, requestedPosition);
        boolean foundRequestedPosition = false;
        for (int index = 0; index < blocks.size(); index++) {
            final CompoundTag block = blocks.getCompoundOrEmpty(index);
            final String position = block.getListOrEmpty("pos").toString();
            if (!nearbyPositions.contains(position)) {
                continue;
            }

            final int stateIndex = block.getIntOr("state", -1);
            final CompoundTag state = stateIndex >= 0 && stateIndex < palette.size()
                    ? palette.getCompoundOrEmpty(stateIndex)
                    : new CompoundTag();
            System.out.printf("block[%s] pos=%s state=%s nbt=%s%n",
                    index, position, state, block.getCompoundOrEmpty("nbt"));
            foundRequestedPosition |= requestedPosition.equals(position);
        }

        if (!foundRequestedPosition) {
            System.out.println("No explicitly stored block at requested position");
        }
    }
}
