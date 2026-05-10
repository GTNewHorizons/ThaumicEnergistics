package thaumicenergistics.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.core.sync.GuiBridge;
import appeng.util.Platform;
import thaumicenergistics.common.tiles.TileEssentiaCellWorkbench;

/**
 * {@link TileEssentiaCellWorkbench} block.
 *
 * @author Nividica
 *
 */
public class BlockEssentiaCellWorkbench extends AbstractBlockAEWrenchable {

    public BlockEssentiaCellWorkbench() {
        super(Material.iron);
    }

    /**
     * Called when the workbench is right-clicked
     *
     * @param world
     * @param x
     * @param y
     * @param z
     * @param player
     * @return
     */
    @Override
    protected boolean onBlockActivated(final World world, final int x, final int y, final int z, final int side,
            final EntityPlayer player) {
        final TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEssentiaCellWorkbench) {
            if (Platform.isServer()) {
                Platform.openGUI(player, te, ForgeDirection.getOrientation(side), GuiBridge.GUI_CELL_WORKBENCH);
            }
            return true;
        }

        return true;
    }

    /**
     * Called when the block is broken.
     */
    @Override
    public void breakBlock(final World world, final int x, final int y, final int z, final Block block,
            final int metaData) {
        if (!world.isRemote) {
            TileEntity tileWorkBench = world.getTileEntity(x, y, z);

            if (tileWorkBench instanceof TileEssentiaCellWorkbench workbench) {
                ItemStack cell = workbench.getInventoryByName("cell").getStackInSlot(0);
                if (cell != null) {
                    world.spawnEntityInWorld(new EntityItem(world, 0.5 + x, 0.5 + y, 0.2 + z, cell));
                }
            }
        }

        // Call super
        super.breakBlock(world, x, y, z, block, metaData);
    }

    @Override
    public TileEntity createNewTileEntity(final World world, final int metaData) {
        return new TileEssentiaCellWorkbench();
    }

    @Override
    public String getUnlocalizedName() {
        return BlockEnum.ESSENTIA_CELL_WORKBENCH.getUnlocalizedName();
    }
}
