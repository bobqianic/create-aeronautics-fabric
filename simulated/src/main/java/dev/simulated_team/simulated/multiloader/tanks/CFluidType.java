package dev.simulated_team.simulated.multiloader.tanks;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.simulated_team.simulated.Simulated;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

/**
 * A loader-independent representation of a fluid
 */
public class CFluidType {

    public final Fluid fluid;
    DataComponentMap data;

    public CFluidType(final ResourceLocation type, @Nullable final DataComponentMap data) {
        // Old Ponder structures can contain an empty or since-removed fluid id. Treat it as an
        // empty tank; allowing null here crashes Fabric's StorageView as soon as an adjacent
        // Create pipe asks whether the tank is empty.
        this.fluid = BuiltInRegistries.FLUID.getOptional(type).orElse(Fluids.EMPTY);
        this.data = data;
    }

    public CFluidType(final Fluid type, @Nullable final DataComponentMap data) {
        this.fluid = type == null ? Fluids.EMPTY : type;
        this.data = data;
    }

    public boolean isBlank() {
        return this.equals(BLANK);
    }

    public static final CFluidType BLANK = new CFluidType(Fluids.EMPTY, null);

    public CompoundTag write() {
        final CompoundTag tag = new CompoundTag();
        tag.putString("Fluid", BuiltInRegistries.FLUID.getKey(this.fluid).toString());
        if (this.data != null) {
            final Codec<DataComponentMap> codec = DataComponentMap.CODEC;
            final DataResult<Tag> result = codec.encodeStart(NbtOps.INSTANCE, this.data);

            if (result.isError()) {
                Simulated.LOGGER.warn(result.error().get().message());
            } else {
                tag.put("data", result.result().get());
            }
        }
        return tag;
    }

    public static CFluidType read(final CompoundTag tag) {
        DataComponentMap map = null;
        if (tag.contains("data")) {
            final DataResult<Pair<DataComponentMap, Tag>> result = DataComponentMap.CODEC.decode(NbtOps.INSTANCE, tag.getCompoundOrEmpty("data"));
            if (result.isError()) {
                Simulated.LOGGER.warn(result.error().get().message());
            } else {
                map = result.result().get().getFirst();
            }
        }

        return new CFluidType(ResourceLocation.parse(tag.getStringOr("Fluid", "")), map);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof final CFluidType other) {
            // both haves tag, or both no haves tag
            if ((this.data == null) == (other.data == null)) {
                return this.fluid.isSame(other.fluid) && (this.data == null || this.data.equals(other.data));
            }
        }
        return false;
    }
}
