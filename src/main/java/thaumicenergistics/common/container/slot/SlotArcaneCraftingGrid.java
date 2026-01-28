package thaumicenergistics.common.container.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import appeng.container.slot.AppEngSlot;
import thaumicenergistics.common.container.ContainerPartArcaneCraftingTerminal;

public class SlotArcaneCraftingGrid extends AppEngSlot {

    private final ContainerPartArcaneCraftingTerminal container;

    public SlotArcaneCraftingGrid(ContainerPartArcaneCraftingTerminal container, IInventory craftinInventory, int idx,
            int x, int y) {
        super(craftinInventory, idx, x, y);

        this.container = container;
    }

    @Override
    public void clearStack() {
        super.clearStack();
        this.container.onCraftMatrixChanged(null);
    }

    @Override
    public void putStack(ItemStack par1ItemStack) {
        super.putStack(par1ItemStack);
        this.container.onCraftMatrixChanged(null);
    }

    @Override
    public ItemStack decrStackSize(int p_75209_1_) {
        final ItemStack is = super.decrStackSize(p_75209_1_);
        this.container.onCraftMatrixChanged(null);
        return is;
    }
}
