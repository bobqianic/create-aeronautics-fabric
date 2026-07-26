package dev.eriksonn.aeronautics.mixin.create_sublevel;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.zurrtum.create.content.kinetics.fan.AirCurrent;
import com.zurrtum.create.content.kinetics.fan.IAirCurrentSource;
import dev.ryanhcode.sable.api.SubLevelEntityScope;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = AirCurrent.class, remap = false)
public abstract class AirCurrentMixin {

    @Shadow
    @Final
    public IAirCurrentSource source;

    @Shadow
    protected List<Entity> caughtEntities;

    @WrapMethod(method = "tickAffectedEntities")
    private void aeronautics$useLocalEntityCoordinates(final Level world, final Operation<Void> original) {
        if (world == null) {
            original.call(world);
            return;
        }

        try (final SubLevelEntityScope scope = SubLevelEntityScope.at(world, this.source.getAirCurrentPos())) {
            scope.includeAll(this.caughtEntities);
            original.call(world);
        }
    }
}
