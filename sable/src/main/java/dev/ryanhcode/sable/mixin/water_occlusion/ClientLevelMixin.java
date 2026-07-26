package dev.ryanhcode.sable.mixin.water_occlusion;

import dev.ryanhcode.sable.mixinterface.water_occlusion.WaterOcclusionContainerHolder;
import dev.ryanhcode.sable.platform.SablePlatform;
import dev.ryanhcode.sable.sublevel.water_occlusion.ClientWaterOcclusionContainer;
import dev.ryanhcode.sable.sublevel.water_occlusion.WaterOcclusionContainer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds a {@link WaterOcclusionContainer} to the server levels
 */
@Mixin(ClientLevel.class)
public class ClientLevelMixin implements WaterOcclusionContainerHolder {

    @Unique
    private final WaterOcclusionContainer<?> sable$waterOcclusionContainer = this.sable$createWaterOcclusionContainer();

    @Unique
    private WaterOcclusionContainer<?> sable$createWaterOcclusionContainer() {
        final Level self = (Level) (Object) this;
        if (SablePlatform.INSTANCE.isWrappedLevel(self)) {
            return null;
        }
        return ClientWaterOcclusionContainer.create(self);
    }

    @Override
    public WaterOcclusionContainer<?> sable$getWaterOcclusionContainer() {
        return this.sable$waterOcclusionContainer;
    }
}
