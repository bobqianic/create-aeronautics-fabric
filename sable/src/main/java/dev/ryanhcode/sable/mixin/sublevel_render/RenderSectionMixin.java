package dev.ryanhcode.sable.mixin.sublevel_render;

import dev.ryanhcode.sable.mixinterface.sublevel_render.vanilla.RenderSectionExtension;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

/**
 * Notifies sub-level render data when one of its sections is marked dirty.
 */
@Mixin(SectionRenderDispatcher.RenderSection.class)
public class RenderSectionMixin implements RenderSectionExtension {

    @Shadow
    private boolean dirty;

    @Unique
    private Set<DirtyListener> sable$listeners;
    @Unique
    private boolean sable$listening = true;

    @Inject(method = "setDirty", at = @At("HEAD"))
    public void setDirty(final boolean playerChanged, final CallbackInfo ci) {
        if (this.sable$listening && !this.dirty && this.sable$listeners != null) {
            // mc26.1: Veil's render-thread executor replaced with the client executor.
            Minecraft.getInstance().execute(() -> {
                for (final DirtyListener listener : this.sable$listeners) {
                    listener.markDirty((SectionRenderDispatcher.RenderSection) (Object) this);
                }
            });
        }
    }

    // PORT-NOTE(mc26.1): the getDistToPlayerSqr() @Overwrite is gone — the
    // method no longer exists; section build priority now flows through
    // CompileTaskDynamicQueue. Far-away plot sections may compile at slightly
    // wrong priority until this is re-implemented against the new queue.

    @Override
    public void sable$addDirtyListener(final DirtyListener listener) {
        if (this.sable$listeners == null) {
            this.sable$listeners = new ObjectArraySet<>();
        }
        this.sable$listeners.add(listener);
    }

    @Override
    public void sable$setListening(final boolean listening) {
        this.sable$listening = listening;
    }
}
