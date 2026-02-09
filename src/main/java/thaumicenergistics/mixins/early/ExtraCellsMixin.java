package thaumicenergistics.mixins.early;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import extracells.integration.Integration;

@Mixin(value = Integration.Mods.class, remap = false)
public class ExtraCellsMixin {

    @Shadow
    @Final
    private String modID;

    @ModifyReturnValue(method = "isEnabled", at = @At("RETURN"))
    private boolean isEnabled(boolean original) {
        if (this.modID.equals("thaumicenergistics")) return false;
        return original;
    }
}
