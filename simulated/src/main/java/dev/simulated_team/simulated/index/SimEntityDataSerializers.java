package dev.simulated_team.simulated.index;

import dev.simulated_team.simulated.service.SimEntityDataSerialization;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.phys.Vec3;

public class SimEntityDataSerializers {

    public static final EntityDataSerializer<Vec3> VEC3 = new EntityDataSerializer<>() {
        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, Vec3> codec() {
            return new StreamCodec<>() {
                @Override
                public Vec3 decode(final RegistryFriendlyByteBuf object) {
                    return object.readVec3();
                }

                @Override
                public void encode(final RegistryFriendlyByteBuf object, final Vec3 object2) {
                    object.writeVec3(object2);
                }
            };
        }

        @Override
        public Vec3 copy(final Vec3 object) {
            return new Vec3(object.x, object.y, object.z);
        }
    };

    public static void register() {
        SimEntityDataSerialization.INSTANCE.registerDataSerializer("vec3", VEC3);
    }
}
