package thaumicenergistics.common.storage;

import net.minecraft.nbt.NBTTagCompound;

import appeng.api.storage.data.IAEStackType;
import appeng.api.storage.data.IItemList;

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
    public IItemList<AEEssentiaStack> createList() {
        return new EssentiaList();
    }
}
