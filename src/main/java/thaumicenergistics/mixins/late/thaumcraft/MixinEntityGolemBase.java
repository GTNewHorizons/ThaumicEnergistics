package thaumicenergistics.mixins.late.thaumcraft;

import java.util.HashMap;

import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumicenergistics.api.entities.IGolemHookHandler;
import thaumicenergistics.common.integration.tc.GolemHooks;
import thaumicenergistics.mixins.interfaces.EntityGolemBaseExt;

@Mixin(EntityGolemBase.class)
public class MixinEntityGolemBase extends EntityGolem implements EntityGolemBaseExt {

    @Unique
    private HashMap<IGolemHookHandler, Object> thenergistic$hookHandlers;

    public MixinEntityGolemBase(World world) {
        super(world);
    }

    @Inject(method = "setupGolem", at = @At("TAIL"), remap = false)
    private void injectSetupGolem(CallbackInfoReturnable<Boolean> cir) {
        GolemHooks.hook_SetupGolem((EntityGolemBase) (Object) this, thenergistic$hookHandlers);
    }

    @Inject(method = "entityInit", at = @At("TAIL"))
    private void injectEntityInit(CallbackInfo ci) {
        thenergistic$hookHandlers = new HashMap<>();
        GolemHooks.hook_EntityInit((EntityGolemBase) (Object) this, thenergistic$hookHandlers);
    }

    @Inject(method = "writeEntityToNBT", at = @At("TAIL"))
    private void injectWriteNBT(NBTTagCompound nbt, CallbackInfo ci) {
        GolemHooks.hook_WriteEntityToNBT((EntityGolemBase) (Object) this, thenergistic$hookHandlers, nbt);
    }

    @Inject(
            method = "readEntityFromNBT",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/monster/EntityGolem;readEntityFromNBT(Lnet/minecraft/nbt/NBTTagCompound;)V",
                    shift = At.Shift.AFTER))
    private void injectReadNBT(NBTTagCompound nbt, CallbackInfo ci) {
        GolemHooks.hook_ReadEntityFromNBT((EntityGolemBase) (Object) this, thenergistic$hookHandlers, nbt);
    }

    @WrapWithCondition(
            method = "customInteraction",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/EntityPlayer;openGui(Ljava/lang/Object;ILnet/minecraft/world/World;III)V"))
    private boolean wrapCustomInteraction(EntityPlayer player, Object mod, int modGuiId, World world, int x, int y,
            int z) {
        return !GolemHooks.hook_CustomInteraction((EntityGolemBase) (Object) this, player, thenergistic$hookHandlers);
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();
        GolemHooks.hook_onEntityUpdate((EntityGolemBase) (Object) this, thenergistic$hookHandlers);
    }

    @Override
    public HashMap<IGolemHookHandler, Object> thenergistic$getHandlers() {
        return thenergistic$hookHandlers;
    }
}
