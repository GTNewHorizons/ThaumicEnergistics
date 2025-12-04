package thaumicenergistics.common.tiles;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.tile.misc.TileCellWorkbench;
import thaumicenergistics.common.items.ItemEssentiaCell;

/**
 * Provides {@link ItemEssentiaCell} partitioning.
 *
 * @author Nividica
 *
 */
public class TileEssentiaCellWorkbench extends TileCellWorkbench {

    /**
     * NBT Keys
     */
    private static String NBT_KEY_CELL = "EssentiaCell";

    @Override
    public void readFromNBT_TileCellWorkbench(NBTTagCompound data) {
        super.readFromNBT_TileCellWorkbench(data);

        if (data.hasKey(TileEssentiaCellWorkbench.NBT_KEY_CELL)) {
            this.getInventoryByName("cell").setInventorySlotContents(
                    0,
                    ItemStack.loadItemStackFromNBT(data.getCompoundTag(TileEssentiaCellWorkbench.NBT_KEY_CELL)));
        }
    }
}
