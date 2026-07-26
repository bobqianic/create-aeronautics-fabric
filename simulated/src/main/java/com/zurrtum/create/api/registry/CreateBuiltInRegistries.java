package com.zurrtum.create.api.registry;

import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttributeType;
import net.minecraft.core.Registry;

/** Legacy registry name retained for code written against older Create APIs. */
public final class CreateBuiltInRegistries {
    public static final Registry<ArmInteractionPointType> ARM_INTERACTION_POINT_TYPE = CreateRegistries.ARM_INTERACTION_POINT_TYPE;
    public static final Registry<ItemAttributeType> ITEM_ATTRIBUTE_TYPE = CreateRegistries.ITEM_ATTRIBUTE_TYPE;

    private CreateBuiltInRegistries() {
    }
}
