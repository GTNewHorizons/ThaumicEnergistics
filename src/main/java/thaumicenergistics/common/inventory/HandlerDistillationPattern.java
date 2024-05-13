package thaumicenergistics.common.inventory;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;

import appeng.items.misc.ItemEncodedPattern;
import thaumicenergistics.common.items.ItemCraftingAspect;

/**
 * Handles {@link ItemEncodedPattern} items with a distillation encodeing.
 *
 * @author Nividica
 *
 */
public class HandlerDistillationPattern {

    /**
     * NBT Keys
     */
    private static final String NBTKEY_AE_IN = "in", NBTKEY_AE_OUT = "out", NBTKEY_AE_ISCRAFTING = "crafting",
            NBTKEY_AE_CAN_SUB = "substitute";

    /**
     * Output of the pattern. Must be ItemCraftingAspect patterns.
     */
    protected ItemStack[] output = null;

    /**
     * Input of the pattern.
     */
    protected ItemStack input = null;

    public void encodePattern(final ItemStack pattern) {
        // Valid pattern?
        if (pattern == null) {
            return;
        }

        // Check the input & output
        if ((this.input == null) || (this.output == null)) {
            // No input or output
            return;
        }

        // Create a new tag
        NBTTagCompound data = new NBTTagCompound();

        // Write the input
        NBTTagList inTags = new NBTTagList();
        inTags.appendTag(this.input.writeToNBT(new NBTTagCompound()));

        // Write the outputs
        NBTTagList outTags = new NBTTagList();
        for (ItemStack is : this.output) {
            outTags.appendTag(createItemTag(is));
        }

        // Write the basics
        data.setBoolean(NBTKEY_AE_CAN_SUB, false);
        data.setBoolean(NBTKEY_AE_ISCRAFTING, false);

        // Write the lists
        data.setTag(NBTKEY_AE_IN, inTags);
        data.setTag(NBTKEY_AE_OUT, outTags);

        // Save into the item
        pattern.setTagCompound(data);
    }

    /**
     * Returns the input of the pattern. May be null.
     *
     * @return
     */
    public ItemStack getInput() {
        return this.input;
    }

    /**
     * Returns the output of the pattern. May be null.
     *
     * @return
     */
    public ItemStack[] getOutput() {
        return this.output;
    }

    /**
     * Returns true if the current items are valid.
     *
     * @return
     */
    public boolean isValid() {
        return ((this.output != null) && (this.input != null));
    }

    /**
     * Loads the values from the pattern
     *
     * @param pattern
     */
    public void readPattern(final ItemStack pattern) {
        // Reset
        this.reset();

        // Valid pattern?
        if (pattern == null) {
            return;
        }

        if (!pattern.hasTagCompound()) {
            // Nothing to load
            return;
        }

        // Get the NBT tag
        NBTTagCompound data = pattern.getTagCompound();

        // Get the input and output list
        NBTTagList inTags = data.getTagList(NBTKEY_AE_IN, NBT.TAG_COMPOUND);
        NBTTagList outTags = data.getTagList(NBTKEY_AE_OUT, NBT.TAG_COMPOUND);

        // Empty check
        if ((outTags.tagCount() < 1) || (inTags.tagCount() < 1)) {
            // Nothing to load.
            return;
        }

        // Read the input and output
        ArrayList<ItemStack> outputArray = new ArrayList<>();
        for (int index = 0; index < outTags.tagCount(); index++) {
            outputArray.add(ItemStack.loadItemStackFromNBT(outTags.getCompoundTagAt(index)));
        }
        this.setOutputItems(outputArray.toArray(new ItemStack[0]));
        this.setInputItem(ItemStack.loadItemStackFromNBT(inTags.getCompoundTagAt(0)));

        // Null check
        if ((this.input == null) || (this.output == null)) {
            this.reset();
            return;
        }

        return;
    }

    /**
     * Resets the helper.
     */
    public void reset() {
        this.output = null;
        this.input = null;
    }

    /**
     * Sets the input.
     *
     * @param inputItem
     */
    public void setInputItem(final ItemStack inputItem) {
        this.input = inputItem.copy();
        this.input.stackSize = 1;
    }

    /**
     * Sets the input and output items. Returns if the recipe is valid or not.
     *
     * @param inputItem
     * @param outputItem
     * @return
     */
    public boolean setPatternItems(final ItemStack inputItem, final ItemStack outputItem) {
        this.setInputItem(inputItem);
        this.setOutputItems(new ItemStack[] { outputItem });
        return this.isValid();
    }

    /**
     * Sets the output. Must be a crafting aspect.
     *
     * @param outputItem
     */
    public void setOutputItems(final ItemStack[] outputItems) {
        ArrayList<ItemStack> outputArray = new ArrayList<>();
        for (ItemStack is : outputItems) {
            // Not valid crafting aspect?
            if ((is != null) && !(is.getItem() instanceof ItemCraftingAspect)) {
                this.output = null;
                return;
            }
            // Aspect null?
            else if (ItemCraftingAspect.getAspect(is) == null) {
                this.output = null;
                return;
            } else {
                outputArray.add(is);
            }
        }
        this.output = outputArray.toArray(new ItemStack[0]);
    }

    /**
     * Sets the input and output items. Returns if the recipe is valid or not.
     *
     * @param inputItem
     * @param outputItems
     * @return
     */
    public boolean setPatternItems(final ItemStack inputItem, final ItemStack[] outputItems) {
        this.setInputItem(inputItem);
        this.setOutputItems(outputItems);
        return this.isValid();
    }

    /**
     * From itemstack to NBT Compound
     * 
     * @param ItemStack
     * @return NBTTagCompound
     */
    private NBTBase createItemTag(final ItemStack i) {
        final NBTTagCompound c = new NBTTagCompound();

        if (i != null) {
            i.writeToNBT(c);
            c.setInteger("Count", i.stackSize);
        }

        return c;
    }
}
