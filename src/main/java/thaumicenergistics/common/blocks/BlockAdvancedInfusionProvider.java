package thaumicenergistics.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumicenergistics.client.textures.BlockTextureManager;
import thaumicenergistics.common.ThEGuiHandler;
import thaumicenergistics.common.integration.IWailaSource;
import thaumicenergistics.common.registries.Renderers;
import thaumicenergistics.common.tiles.TileAdvancedInfusionProvider;
import thaumicenergistics.common.tiles.TileInfusionProvider;
import thaumicenergistics.common.utils.ThELog;

/**
 * {@link TileAdvancedInfusionProvider} block.
 *
 * @author MCTBL
 *
 */
public class BlockAdvancedInfusionProvider extends AbstractBlockProviderBase{
	
    public BlockAdvancedInfusionProvider() {
        // Call super with material machine (iron)
        super(Material.iron);

        // Basic hardness
        this.setHardness(1.0f);

        // Sound of metal
        this.setStepSound(Block.soundTypeMetal);
    }
    
    /**
     * Called when the provider is right-clicked
     *
     * @param world
     * @param x
     * @param y
     * @param z
     * @param player
     * @return
     */
    @Override
    protected boolean onBlockActivated(final World world, final int x, final int y, final int z,
            final EntityPlayer player) {
        ItemStack heldItem = player.getHeldItem();
		ThELog.info("item is %s", heldItem);
		ThELog.info("%s", heldItem.getDisplayName());
		String[] split = heldItem.getDisplayName().split(",");
		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof TileAdvancedInfusionProvider taip) {
			taip.setUpMatrixCoordinate(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
		}

        return false;
    }

    @Override
    public TileEntity createNewTileEntity(final World world, final int metaData) {
        // Create a new provider tile, passing the side to attach to
    	TileAdvancedInfusionProvider tile = new TileAdvancedInfusionProvider();
    	
    	tile.setupProvider(metaData);
    	
        // Return the tile
        return tile;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(final int side, final int metaData) {
        return BlockTextureManager.ADVANCED_INFUSION_PROVIDER.getTextures()[1];
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getRenderType() {
        // Provide our custom ID
        return Renderers.AdvancedInfusionProviderRenderID;
    }

    @Override
    public String getUnlocalizedName() {
        return BlockEnum.ADVANCED_INFUSION_PROVIDER.getUnlocalizedName();
    }

    @Override
    public void onNeighborBlockChange(final World world, final int x, final int y, final int z, final Block neighbor) {
        // Get tile entity
        TileEntity tileProvider = world.getTileEntity(x, y, z);
        if (tileProvider instanceof TileAdvancedInfusionProvider tP) {
            // Inform our tile entity a neighbor has changed
            tP.checkGridConnectionColor();
        }
    }
}
