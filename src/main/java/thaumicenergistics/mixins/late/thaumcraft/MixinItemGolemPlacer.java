package thaumicenergistics.mixins.late.thaumcraft;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.entities.golems.ItemGolemPlacer;
import thaumicenergistics.common.integration.tc.GolemHooks;
import thaumicenergistics.mixins.interfaces.EntityGolemBaseExt;

@Mixin(ItemGolemPlacer.class)
public class MixinItemGolemPlacer {

    @Inject(
            method = "spawnCreature",
            at = @At(
                    value = "INVOKE",
                    target = "Lthaumcraft/common/entities/golems/EntityGolemBase;setup(I)V",
                    shift = At.Shift.BEFORE),
            remap = false)
    private void inject(World par0World, double par2, double par4, double par6, int side, ItemStack stack,
            EntityPlayer player, CallbackInfoReturnable<Boolean> cir, @Local(name = "golem") EntityGolemBase golem) {
        GolemHooks.hook_Placer_SpawnGolem(golem, stack, ((EntityGolemBaseExt) golem).thenergistic$getHandlers());
    }
}
