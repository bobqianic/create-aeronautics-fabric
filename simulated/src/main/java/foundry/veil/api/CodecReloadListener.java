package foundry.veil.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public abstract class CodecReloadListener<T> extends SimplePreparableReloadListener<Map<ResourceLocation, T>> {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final Codec<T> codec;
    protected final FileToIdConverter converter;
    private final HolderLookup.Provider registries;

    protected CodecReloadListener(final Codec<T> codec, final FileToIdConverter converter) {
        this(codec, converter, null);
    }

    protected CodecReloadListener(final Codec<T> codec, final FileToIdConverter converter,
                                  @Nullable final HolderLookup.Provider registries) {
        this.codec = codec;
        this.converter = converter;
        this.registries = registries;
    }

    @Override
    protected @NotNull Map<ResourceLocation, T> prepare(final ResourceManager resourceManager,
                                                        final ProfilerFiller profilerFiller) {
        final Map<ResourceLocation, T> data = new HashMap<>();
        final DynamicOps<JsonElement> ops = this.registries == null
                ? JsonOps.INSTANCE
                : RegistryOps.create(JsonOps.INSTANCE, this.registries);

        for (final Map.Entry<ResourceLocation, Resource> entry : this.converter.listMatchingResources(resourceManager).entrySet()) {
            final ResourceLocation location = entry.getKey();
            final ResourceLocation id = this.converter.fileToId(location);
            try (Reader reader = entry.getValue().openAsReader()) {
                final DataResult<T> result = this.codec.parse(ops, JsonParser.parseReader(reader));
                if (result.error().isPresent()) {
                    throw new JsonSyntaxException(result.error().get().message());
                }
                if (data.put(id, result.result().orElseThrow()) != null) {
                    throw new IllegalStateException("Duplicate data file with ID " + id);
                }
            } catch (final Exception exception) {
                LOGGER.error("Couldn't parse data file {} from {}", id, location, exception);
            }
        }
        return data;
    }
}
