package thaumicenergistics.common.storage;

import java.io.IOException;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.djgiannuzz.thaumcraftneiplugin.items.ItemAspect;

import appeng.api.storage.data.IAEStackType;
import appeng.api.storage.data.IItemList;
import cpw.mods.fml.common.Loader;
import io.netty.buffer.ByteBuf;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;

public class AEEssentiaStackType implements IAEStackType<AEEssentiaStack> {

    public static final AEEssentiaStackType ESSENTIA_STACK_TYPE = new AEEssentiaStackType();
    public static final String ESSENTIA_STACK_ID = "essentia";

    @Override
    public String getId() {
        return ESSENTIA_STACK_ID;
    }

    @Override
    public AEEssentiaStack loadStackFromNBT(NBTTagCompound tag) {
        return AEEssentiaStack.loadStackFromNBT(tag);
    }

    @Override
    public AEEssentiaStack loadStackFromByte(ByteBuf buffer) throws IOException {
        return AEEssentiaStack.loadEssentiaStackFromPacket(buffer);
    }

    @Override
    public IItemList<AEEssentiaStack> createList() {
        return new EssentiaList();
    }

    @Override
    public boolean isContainerItemForType(@NotNull ItemStack container) {
        return container.getItem() instanceof IEssentiaContainerItem;
    }

    @Override
    public @Nullable AEEssentiaStack getStackFromContainerItem(@NotNull ItemStack container) {
        if (container.getItem() instanceof IEssentiaContainerItem essentiaContainer) {
            AspectList list = essentiaContainer.getAspects(container);
            if (list != null) {
                Aspect aspect = list.getAspects()[0];
                if (aspect != null) {
                    return new AEEssentiaStack(aspect);
                }
            }
        }
        return null;
    }

    @Override
    public @Nullable AEEssentiaStack convertStackFromItem(@NotNull ItemStack itemStack) {
        if (Loader.isModLoaded("thaumcraftneiplugin") && itemStack.getItem() instanceof ItemAspect) {
            AspectList list = ItemAspect.getAspects(itemStack);
            if (list != null) {
                Aspect aspect = list.getAspects()[0];
                if (aspect != null) {
                    return new AEEssentiaStack(aspect);
                }
            }
        }
        return null;
    }
}
