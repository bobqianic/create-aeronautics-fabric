package dev.ryanhcode.offroad.index;

import com.zurrtum.create.api.contraption.ContraptionType;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.content.contraptions.Contraption;
import dev.ryanhcode.offroad.Offroad;
import dev.ryanhcode.offroad.content.contraptions.borehead_contraption.BoreheadBearingContraption;
import net.minecraft.core.Registry;

import java.util.function.Supplier;

public class OffroadContraptionTypes {

    public static final ContraptionType BOREHEAD_CONTRAPTION_TYPE = register("borehead_contraption", BoreheadBearingContraption::new);

    private static ContraptionType register(String name, Supplier<? extends Contraption> factory) {
        return Registry.register(CreateRegistries.CONTRAPTION_TYPE, Offroad.path(name), new ContraptionType(factory));
    }

    public static void init() {}

}
