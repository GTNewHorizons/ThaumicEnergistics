package thaumicenergistics.common.container.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import appeng.container.slot.AppEngCraftingSlot;

public class SlotArcaneCraftingResult extends AppEngCraftingSlot {

    public SlotArcaneCraftingResult(EntityPlayer player, IInventory resultInventory, int index, int x, int y) {
        super(player, null, resultInventory, index, x, y);
    }

    @Override
    public boolean canTakeStack(EntityPlayer par1EntityPlayer) {
        return false;
    }

    @Override
    public void onPickupFromSlot(EntityPlayer player, ItemStack is) {}
}
