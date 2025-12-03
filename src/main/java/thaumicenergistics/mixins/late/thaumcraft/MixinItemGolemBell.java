package thaumicenergistics.mixins.late.thaumcraft;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.entities.golems.ItemGolemBell;
import thaumicenergistics.common.integration.tc.GolemHooks;
import thaumicenergistics.mixins.interfaces.EntityGolemBaseExt;

@Mixin(ItemGolemBell.class)
public class MixinItemGolemBell {

    @Inject(
            method = "onLeftClickEntity",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;<init>(Lnet/minecraft/item/Item;II)V",
                    shift = At.Shift.BY,
                    by = 2,
                    ordinal = 1))
    private void injectOnLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity,
            CallbackInfoReturnable<Boolean> cir, @Local(name = "dropped") ItemStack dropped) {
        GolemHooks.hook_Bell_OnLeftClickGolem(
                (EntityGolemBase) entity,
                dropped,
                player,
                ((EntityGolemBaseExt) entity).thenergistic$getHandlers());
    }
}
