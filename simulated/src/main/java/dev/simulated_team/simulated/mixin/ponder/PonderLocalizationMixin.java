package dev.simulated_team.simulated.mixin.ponder;

import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.client.ponder.foundation.PonderIndex;
import com.zurrtum.create.client.ponder.foundation.registration.PonderLocalization;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(PonderLocalization.class)
public class PonderLocalizationMixin {
    @Shadow
    @Final
    public Map<ResourceLocation, String> shared;

    @Shadow
    @Final
    public Map<ResourceLocation, Couple<String>> tag;

    @Shadow
    @Final
    public Map<ResourceLocation, Map<String, String>> specific;

    @Inject(method = "getShared(Lnet/minecraft/resources/ResourceLocation;)Ljava/lang/String;", at = @At("RETURN"), cancellable = true)
    private void simulated$fallbackShared(final ResourceLocation key, final CallbackInfoReturnable<String> cir) {
        if (!simulated$isMissing(simulated$sharedKey(key))) return;

        final String fallback = this.shared.get(key);
        if (fallback != null) cir.setReturnValue(fallback);
    }

    @Inject(method = "getShared(Lnet/minecraft/resources/ResourceLocation;[Ljava/lang/Object;)Ljava/lang/String;", at = @At("RETURN"), cancellable = true)
    private void simulated$fallbackSharedWithParams(final ResourceLocation key, final Object[] params, final CallbackInfoReturnable<String> cir) {
        if (!simulated$isMissing(simulated$sharedKey(key))) return;

        final String fallback = this.shared.get(key);
        if (fallback != null) cir.setReturnValue(String.format(fallback, params));
    }

    @Inject(method = "getTagName", at = @At("RETURN"), cancellable = true)
    private void simulated$fallbackTagName(final ResourceLocation key, final CallbackInfoReturnable<String> cir) {
        if (!simulated$isMissing(simulated$tagKey(key))) return;

        final Couple<String> fallback = this.tag.get(key);
        if (fallback != null) cir.setReturnValue(fallback.getFirst());
    }

    @Inject(method = "getTagDescription", at = @At("RETURN"), cancellable = true)
    private void simulated$fallbackTagDescription(final ResourceLocation key, final CallbackInfoReturnable<String> cir) {
        if (!simulated$isMissing(simulated$tagDescriptionKey(key))) return;

        final Couple<String> fallback = this.tag.get(key);
        if (fallback != null) cir.setReturnValue(fallback.getSecond());
    }

    @Inject(method = "getSpecific(Lnet/minecraft/resources/ResourceLocation;Ljava/lang/String;)Ljava/lang/String;", at = @At("RETURN"), cancellable = true)
    private void simulated$fallbackSpecific(final ResourceLocation sceneId, final String key, final CallbackInfoReturnable<String> cir) {
        if (!simulated$isMissing(simulated$specificKey(sceneId, key))) return;

        final Map<String, String> sceneText = this.specific.get(sceneId);
        if (sceneText == null) return;

        final String fallback = sceneText.get(key);
        if (fallback != null) cir.setReturnValue(fallback);
    }

    @Inject(method = "getSpecific(Lnet/minecraft/resources/ResourceLocation;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;", at = @At("RETURN"), cancellable = true)
    private void simulated$fallbackSpecificWithParams(final ResourceLocation sceneId, final String key, final Object[] params, final CallbackInfoReturnable<String> cir) {
        if (!simulated$isMissing(simulated$specificKey(sceneId, key))) return;

        final Map<String, String> sceneText = this.specific.get(sceneId);
        if (sceneText == null) return;

        final String fallback = sceneText.get(key);
        if (fallback != null) cir.setReturnValue(String.format(fallback, params));
    }

    @Unique
    private static boolean simulated$isMissing(final String key) {
        return !PonderIndex.editingModeActive() && !I18n.exists(key);
    }

    @Unique
    private static String simulated$sharedKey(final ResourceLocation key) {
        return key.getNamespace() + ".ponder.shared." + key.getPath();
    }

    @Unique
    private static String simulated$tagKey(final ResourceLocation key) {
        return key.getNamespace() + ".ponder.tag." + key.getPath();
    }

    @Unique
    private static String simulated$tagDescriptionKey(final ResourceLocation key) {
        return simulated$tagKey(key) + ".description";
    }

    @Unique
    private static String simulated$specificKey(final ResourceLocation sceneId, final String key) {
        return sceneId.getNamespace() + ".ponder." + sceneId.getPath() + "." + key;
    }
}
