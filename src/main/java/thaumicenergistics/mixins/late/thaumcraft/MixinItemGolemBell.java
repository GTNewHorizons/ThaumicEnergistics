package thaumicenergistics.mixins.late.thaumcraft;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.entities.golems.ItemGolemBell;
import thaumicenergistics.common.integration.tc.GolemHooks;
import thaumicenergistics.mixins.interfaces.EntityGolemBaseExt;

@Mixin(ItemGolemBell.class)
public class MixinItemGolemBell {

    @ModifyVariable(
            method = "onLeftClickEntity",
            remap = false,
            print = true,
            at = @At(value = "STORE", ordinal = 1),
            name = "dropped")
    private ItemStack injectOnLeftClickEntity(ItemStack dropped, ItemStack stack, EntityPlayer player, Entity entity) {
        GolemHooks.hook_Bell_OnLeftClickGolem(
                (EntityGolemBase) entity,
                dropped,
                player,
                ((EntityGolemBaseExt) entity).thenergistic$getHandlers());
        return dropped;
    }
}
