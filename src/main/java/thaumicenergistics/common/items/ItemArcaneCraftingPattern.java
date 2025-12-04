package thaumicenergistics.common.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import thaumicenergistics.common.integration.tc.ArcaneCraftingPattern;

/**
 * This is a technical item used to serialize/deserialize Knowlegde Core crafting tasks. It cannot be crafted and should
 * be invisible to players.
 */
public class ItemArcaneCraftingPattern extends Item implements ICraftingPatternItem {

    public ItemArcaneCraftingPattern() {
        this.setUnlocalizedName("itemArcaneCraftingPattern");
    }

    @Override
    public ICraftingPatternDetails getPatternForItem(ItemStack is, World w) {
        if (is == null) return null;
        if (is.hasTagCompound()) {

            NBTTagCompound tag = is.getTagCompound();
            NBTTagCompound pattern = tag.getCompoundTag("pattern");
            if (pattern != null) return new ArcaneCraftingPattern(pattern);

        }

        return null;

    }

    public static ItemStack getItemFromPattern(ArcaneCraftingPattern arcaneCraftingPattern) {
        if (arcaneCraftingPattern == null) return null;
        ItemStack is = new ItemStack(ItemEnum.ARCANE_PATTERN.getItem());
        NBTTagCompound pattern = new NBTTagCompound();
        arcaneCraftingPattern.writeToNBT(pattern);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("pattern", pattern);
        is.setTagCompound(tag);
        return is;
    }

}
