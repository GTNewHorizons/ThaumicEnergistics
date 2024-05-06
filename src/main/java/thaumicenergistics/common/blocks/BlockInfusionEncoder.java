package thaumicenergistics.common.blocks;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumicenergistics.client.textures.BlockTextureManager;
import thaumicenergistics.common.ThEGuiHandler;
import thaumicenergistics.common.ThaumicEnergistics;
import thaumicenergistics.common.tiles.TileInfusionPatternEncoder;
import thaumicenergistics.common.utils.EffectiveSide;

/**
 * Block of Infusion Encoder
 * 
 * @author MTCBL
 */
public class BlockInfusionEncoder extends AbstractBlockAEWrenchable {

    protected BlockInfusionEncoder() {
        // Call super with material machine (iron)
        super(Material.iron);

        // Basic hardness
        this.setHardness(1.0f);

        // Sound of metal
        this.setStepSound(Block.soundTypeMetal);

        // Place in the ThE creative tab
        this.setCreativeTab(ThaumicEnergistics.ThETab);
    }

    @Override
    public TileEntity createNewTileEntity(final World world, final int metaData) {
        return new TileInfusionPatternEncoder();
    }

    @Override
    protected final boolean onBlockActivated(final World world, final int x, final int y, final int z,
            final EntityPlayer player) {
        // Launch the gui.
        ThEGuiHandler.launchGui(ThEGuiHandler.INFUSION_ENCODER, player, world, x, y, z);
        return true;
    }

    /**
     * Called when the block is broken.
     */
    @Override
    public void breakBlock(final World world, final int x, final int y, final int z, final Block block,
            final int metaData) {
        // Is this server side?
        if (EffectiveSide.isServerSide()) {
            // Get the tile
            TileEntity tile = world.getTileEntity(x, y, z);

            if (tile instanceof TileInfusionPatternEncoder) {
                // Cast
                TileInfusionPatternEncoder tileDE = (TileInfusionPatternEncoder) tile;

                // Does it have patterns?
                if (tileDE.hasPatterns()) {
                    // Get the drops
                    ArrayList<ItemStack> drops = tileDE.getDrops(new ArrayList<ItemStack>());
                    for (ItemStack drop : drops) {
                        // Spawn in the world
                        world.spawnEntityInWorld(new EntityItem(world, 0.5 + x, 0.5 + y, 0.2 + z, drop));
                    }
                }
            }
        }

        // Call super
        super.breakBlock(world, x, y, z, block, metaData);
    }

    /**
     * Gets the standard block icon.
     */
    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(final int side, final int meta) {
        // Face?
        if (side == ForgeDirection.OPPOSITES[meta]) {
            // Face texture
            return BlockTextureManager.INFUSION_ENCODER.getTextures()[1];
        }

        // Top or bottom?
        if ((ForgeDirection.VALID_DIRECTIONS[side] == ForgeDirection.UP)
                || (ForgeDirection.VALID_DIRECTIONS[side] == ForgeDirection.DOWN)) {
            // Bottom texture
            return BlockTextureManager.INFUSION_ENCODER.getTextures()[2];
        }

        // Sides
        return BlockTextureManager.INFUSION_ENCODER.getTextures()[0];
    }

    /**
     * Gets the unlocalized name of the block.
     */
    @Override
    public String getUnlocalizedName() {
        return BlockEnum.INFUSION_ENCODER.getUnlocalizedName();
    }

    @Override
    public boolean rotateBlock(final World world, final int x, final int y, final int z, final ForgeDirection side) {
        // Get and increment the meta data
        int metaData = world.getBlockMetadata(x, y, z) + 1;

        // Bounds check
        if (metaData >= ForgeDirection.VALID_DIRECTIONS.length) {
            metaData = 0;
        }

        // Set the meta data
        world.setBlockMetadataWithNotify(x, y, z, metaData, 3);

        return true;
    }

}
