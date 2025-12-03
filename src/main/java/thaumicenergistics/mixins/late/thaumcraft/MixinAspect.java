package thaumicenergistics.mixins.late.thaumcraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.common.integration.tc.AspectHooks;

@Mixin(Aspect.class)
public class MixinAspect {

    @Inject(
            method = "<init>(Ljava/lang/String;I[Lthaumcraft/api/aspects/Aspect;Lnet/minecraft/util/ResourceLocation;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/LinkedHashMap;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                    remap = false,
                    shift = At.Shift.AFTER))
    private void injectInit(CallbackInfo ci) {
        AspectHooks.hook_AspectInit((Aspect) (Object) this);
    }
}
