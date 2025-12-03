package thaumicenergistics.mixins.late.thaumcraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import thaumcraft.client.renderers.entity.RenderGolemBase;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumicenergistics.common.integration.tc.GolemHooks;
import thaumicenergistics.mixins.interfaces.EntityGolemBaseExt;

@Mixin(RenderGolemBase.class)
public class MixinRenderGolemBase {

    @Inject(method = "render", at = @At("RETURN"), remap = false)
    private void onRender(EntityGolemBase e, double x, double y, double z, float par8, float partialTicks,
            CallbackInfo ci) {
        GolemHooks.hook_RenderGolem(e, ((EntityGolemBaseExt) e).thenergistic$getHandlers(), x, y, z, partialTicks);
    }
}
