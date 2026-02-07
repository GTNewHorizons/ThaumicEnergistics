package thaumicenergistics.common.tiles;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.misc.TileCellWorkbench;
import thaumicenergistics.common.items.ItemEssentiaCell;

/**
 * Provides {@link ItemEssentiaCell} partitioning.
 *
 * @author Nividica
 *
 */
public class TileEssentiaCellWorkbench extends TileCellWorkbench {

    private static final String OLD_NBT_KEY_CELL = "EssentiaCell";

    @TileEvent(TileEventType.WORLD_NBT_READ)
    public void readFromOldNBT_TileEssentiaCellWorkbench(NBTTagCompound data) {
        if (data.hasKey(TileEssentiaCellWorkbench.OLD_NBT_KEY_CELL)) {
            ItemStack stack = ItemStack
                    .loadItemStackFromNBT(data.getCompoundTag(TileEssentiaCellWorkbench.OLD_NBT_KEY_CELL));
            this.getInventoryByName("cell").setInventorySlotContents(0, stack);
        }
    }
}
