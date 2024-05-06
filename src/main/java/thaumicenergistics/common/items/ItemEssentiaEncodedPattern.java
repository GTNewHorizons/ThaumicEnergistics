package thaumicenergistics.common.items;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.items.misc.ItemEncodedPattern;
import thaumicenergistics.common.ThaumicEnergistics;
import thaumicenergistics.common.registries.ThEStrings;
import thaumicenergistics.common.utils.EssentiaPatternDetails;

/**
 * Item Essentia Encoded Pattern
 * 
 * @author MCTBL
 */
public class ItemEssentiaEncodedPattern extends ItemEncodedPattern {

    @Override
    public ICraftingPatternDetails getPatternForItem(final ItemStack is, final World w) {
        EssentiaPatternDetails pattern = new EssentiaPatternDetails(is);
        return pattern.readFromStack() ? pattern : null;
    }

    @Override
    public String getUnlocalizedName() {
        return ThEStrings.Item_Essentia_Encoded_Pattern.getUnlocalized();
    }

    @Override
    public String getUnlocalizedName(final ItemStack itemStack) {
        return this.getUnlocalizedName();
    }

    @Override
    public void registerIcons(final IIconRegister register) {
        this.itemIcon = register.registerIcon(ThaumicEnergistics.MOD_ID + ":essentia.encoded.pattern");
    }

}
