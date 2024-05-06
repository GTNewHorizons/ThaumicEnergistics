package thaumicenergistics.common.utils;

import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.helpers.PatternHelper;
import appeng.util.item.AEItemStack;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.common.items.ItemEssentiaEncodedPattern;

/**
 * Detail for {@link ItemEssentiaEncodedPattern}
 * 
 * @author MCTBL
 * 
 */
public class EssentiaPatternDetails implements ICraftingPatternDetails, Comparable<EssentiaPatternDetails> {

    private final ItemStack patternStack;
    private IAEItemStack patternStackAe;
    private IAEItemStack[] inputs = null, inputsCond = null, outputs = null, outputsCond = null;
    private int priority = 0;

    /**
     * NBT Keys
     */
    private static final String NBTKEY_AE_IN = "in", NBTKEY_AE_OUT = "out", NBTKEY_AE_CAN_SUB = "substitute";

    public EssentiaPatternDetails(ItemStack is) {
        this.patternStack = Objects.requireNonNull(is);
        syncStackAndStackAE();
    }

    public EssentiaPatternDetails() {
        this(ThEApi.instance().items().EssentiaEncodedPattern.getStacks(1));
    }

    @Override
    public int compareTo(EssentiaPatternDetails o) {
        return Integer.compare(o.priority, this.priority);
    }

    @Override
    public ItemStack getPattern() {
        return patternStack;
    }

    @Override
    public boolean isValidItemForSlot(int slotIndex, ItemStack itemStack, World world) {
        throw new IllegalStateException("Not a crafting recipe!");
    }

    @Override
    public boolean isCraftable() {
        return false;
    }

    @Override
    public IAEItemStack[] getInputs() {
        return checkInitialized(inputs);
    }

    @Override
    public IAEItemStack[] getCondensedInputs() {
        return checkInitialized(inputsCond);
    }

    @Override
    public IAEItemStack[] getCondensedOutputs() {
        return checkInitialized(outputsCond);
    }

    @Override
    public IAEItemStack[] getOutputs() {
        return checkInitialized(outputs);
    }

    @Override
    public boolean canSubstitute() {
        return false;
    }

    @Override
    public ItemStack getOutput(InventoryCrafting craftingInv, World world) {
        throw new IllegalStateException("Not a crafting recipe!");
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean setInputs(IAEItemStack[] inputs) {
        IAEItemStack[] condensed = condenseStacks(inputs);
        if (condensed.length == 0) {
            return false;
        }
        this.inputs = inputs;
        this.inputsCond = condensed;
        return true;
    }

    public boolean setOutputs(IAEItemStack[] outputs) {
        IAEItemStack[] condensed = condenseStacks(outputs);
        if (condensed.length == 0) {
            return false;
        }
        this.outputs = outputs;
        this.outputsCond = condensed;
        return true;
    }

    public boolean setInputAndOutput(IAEItemStack[] inputs, IAEItemStack[] outputs) {
        return setInputs(inputs) && setOutputs(outputs);
    }

    public boolean readFromStack() {
        if (!patternStack.hasTagCompound()) {
            return false;
        }
        NBTTagCompound tag = Objects.requireNonNull(patternStack.getTagCompound());
        // may be possible to enter a partially-correct state if setInputs succeeds but
        // setOutputs failed
        // but outside code should treat it as completely incorrect and not attempt to
        // make calls
        return setInputs(readStackArray(tag.getTagList(NBTKEY_AE_IN, Constants.NBT.TAG_COMPOUND)))
                && setOutputs(readStackArray(tag.getTagList(NBTKEY_AE_OUT, Constants.NBT.TAG_COMPOUND)));
    }

    public static IAEItemStack[] readStackArray(NBTTagList listTag) {
        // see note at top of class
        IAEItemStack[] stacks = new IAEItemStack[listTag.tagCount()];
        for (int i = 0; i < stacks.length; i++) {
            stacks[i] = AEItemStack.loadItemStackFromNBT(listTag.getCompoundTagAt(i));
        }
        return stacks;
    }

    public ItemStack writeToStack() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag(NBTKEY_AE_IN, writeStackArray(checkInitialized(inputs)));
        tag.setTag(NBTKEY_AE_OUT, writeStackArray(checkInitialized(outputs)));
        tag.setBoolean(NBTKEY_AE_CAN_SUB, this.canBeSubstitute());
        patternStack.setTagCompound(tag);
        syncStackAndStackAE();
        return patternStack;
    }

    private static <T> T checkInitialized(@Nullable T value) {
        if (value == null) {
            throw new IllegalStateException("Pattern is not initialized!");
        }
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EssentiaPatternDetails
                && patternStackAe.equals(((EssentiaPatternDetails) obj).patternStackAe);
    }

    private static IAEItemStack[] condenseStacks(IAEItemStack[] stacks) {
        return PatternHelper.convertToCondensedList(stacks);
    }

    public static NBTTagList writeStackArray(IAEItemStack[] stacks) {
        NBTTagList listTag = new NBTTagList();
        for (IAEItemStack stack : stacks) {
            // see note at top of class
            NBTTagCompound stackTag = new NBTTagCompound();
            if (stack != null) stack.writeToNBT(stackTag);
            listTag.appendTag(stackTag);
        }
        return listTag;
    }

    /**
     * Sync patternStackAe and patternStack
     */
    private void syncStackAndStackAE() {
        this.patternStackAe = AEItemStack.create(this.patternStack);
    }
}
