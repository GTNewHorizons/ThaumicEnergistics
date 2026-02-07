package thaumicenergistics.common.inventory;

import static thaumicenergistics.common.storage.AEEssentiaStackType.ESSENTIA_STACK_TYPE;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.api.exceptions.AppEngException;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.data.IAEStackType;
import appeng.api.storage.data.IItemList;
import appeng.me.storage.CellInventory;
import thaumicenergistics.common.storage.AEEssentiaStack;

public class EssentiaCellInventory extends CellInventory<AEEssentiaStack> {

    private static final String NBT_ESSENTIA_NUMBER_KEY = "Essentia#";

    public EssentiaCellInventory(ItemStack cell, ISaveProvider provider) throws AppEngException {
        super(cell, provider);

        this.setTypeWeight(2);
    }

    @Override
    protected AEEssentiaStack readStack(NBTTagCompound tag) {
        return AEEssentiaStack.loadStackFromNBT(tag);
    }

    @Override
    protected String getStackTypeTag() {
        return "et";
    }

    @Override
    protected String getStackCountTag() {
        return "ec";
    }

    @Override
    protected void saveChanges() {
        long count = 0;

        int index = 0;
        for (AEEssentiaStack stack : this.cellStacks) {
            count += stack.getStackSize();
            NBTTagCompound stackTag = new NBTTagCompound();
            stack.writeToNBT(stackTag);
            this.tagCompound.setTag(NBT_ESSENTIA_NUMBER_KEY + index, stackTag);

            index++;
        }

        for (int i = index; i < this.storedTypes; i++) {
            this.tagCompound.removeTag(NBT_ESSENTIA_NUMBER_KEY + i);
        }

        this.storedTypes = (short) this.cellStacks.size();
        if (this.cellStacks.isEmpty()) {
            this.tagCompound.removeTag(getStackTypeTag());
        } else {
            this.tagCompound.setShort(getStackTypeTag(), this.storedTypes);
        }

        this.storedCount = count;
        if (count == 0) {
            this.tagCompound.removeTag(getStackCountTag());
        } else {
            this.tagCompound.setLong(getStackCountTag(), count);
        }

        if (this.container != null) {
            this.container.saveChanges(this);
        }
    }

    @Override
    protected void loadCellStacks() {
        long count = 0;
        for (int index = 0; index < this.getMaxTypes(); index++) {
            if (!this.tagCompound.hasKey(NBT_ESSENTIA_NUMBER_KEY + index)) continue;

            AEEssentiaStack aes = AEEssentiaStack
                    .loadStackFromNBT(this.tagCompound.getCompoundTag(NBT_ESSENTIA_NUMBER_KEY + index));
            if (aes != null) {
                this.cellStacks.add(aes);
                count += aes.getStackSize();
            }
        }
        this.tagCompound.setShort(this.getStackTypeTag(), (short) this.cellStacks.size());
        this.tagCompound.setLong(this.getStackCountTag(), count);
    }

    @Nonnull
    @Override
    public IAEStackType<?> getStackType() {
        return ESSENTIA_STACK_TYPE;
    }

    @Override
    public IItemList<AEEssentiaStack> getAvailableItems(IItemList<AEEssentiaStack> out, int iteration) {
        return super.getAvailableItems(out, iteration);
    }
}
