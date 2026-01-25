package thaumicenergistics.common.container.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import appeng.container.slot.AppEngSlot;
import thaumicenergistics.common.container.ContainerPartArcaneCraftingTerminal;
import thaumicenergistics.common.utils.ThEUtils;

public class SlotWand extends AppEngSlot {

    private final ContainerPartArcaneCraftingTerminal container;

    public SlotWand(ContainerPartArcaneCraftingTerminal container, IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);

        this.container = container;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return ThEUtils.isItemValidWand(stack, false);
    }

    @Override
    public void onSlotChanged() {
        container.onCraftMatrixChanged(null);
        super.onSlotChanged();
    }
}
