package dev.simulated_team.simulated.service;

import net.minecraft.network.syncher.EntityDataSerializer;

public interface SimEntityDataSerialization {

    SimEntityDataSerialization INSTANCE = ServiceUtil.load(SimEntityDataSerialization.class);

    <A, T extends EntityDataSerializer<A>> void registerDataSerializer(final String name, final T serializer);
}
