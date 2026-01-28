package thaumicenergistics.common.items;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.input.Keyboard;

import it.unimi.dsi.fastutil.objects.ObjectLongImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectLongPair;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumicenergistics.api.items.IRestrictedEssentiaContainerItem;
import thaumicenergistics.common.registries.ThEStrings;
import thaumicenergistics.common.tiles.TileEssentiaVibrationChamber;
import thaumicenergistics.common.tiles.abstraction.TileEVCBase;

/**
 * {@link TileEssentiaVibrationChamber} item.
 *
 * @author Nividica
 *
 */
public class ItemBlockEssentiaVibrationChamber extends ItemBlock implements IRestrictedEssentiaContainerItem {

    private static final String NBTKEY_ASPECT_TAG = "AspectTag", NBTKEY_ASPECT_AMOUNT = "Amount";

    public ItemBlockEssentiaVibrationChamber(final Block block) {
        super(block);
    }

    /**
     * Gets the aspect stack from the EVC item.
     */
    private ObjectLongPair<Aspect> getStoredAspectStack(final ItemStack evcStack) {
        // Get the tag
        NBTTagCompound data = evcStack.getTagCompound();
        if (data != null && data.hasKey(TileEVCBase.NBTKEY_STORED)) {
            NBTTagCompound storedTag = data.getCompoundTag(TileEVCBase.NBTKEY_STORED);
            Aspect aspect = Aspect.aspects.get(storedTag.getString(NBTKEY_ASPECT_TAG));
            long amount = storedTag.getLong(NBTKEY_ASPECT_AMOUNT);
            if (aspect != null || amount > 0) {
                return new ObjectLongImmutablePair<>(aspect, amount);
            }
        }

        return null;
    }

    private void setStoredAspectStack(final ItemStack evcStack, final Aspect aspect, final long amount) {
        // Get the tag
        NBTTagCompound data = evcStack.getTagCompound();
        if (data == null) {
            data = new NBTTagCompound();
        }

        // Is the stack empty?
        if (aspect == null || amount <= 0) {
            // Remove the stored data
            data.removeTag(TileEVCBase.NBTKEY_STORED);
        } else {
            // Create the subtag
            NBTTagCompound storedTag = new NBTTagCompound();

            storedTag.setString(NBTKEY_ASPECT_TAG, aspect.getTag());
            storedTag.setLong(NBTKEY_ASPECT_AMOUNT, amount);

            // Write the stored tag
            data.setTag(TileEVCBase.NBTKEY_STORED, storedTag);
        }

        // Is the data tag empty?
        if (data.hasNoTags()) {
            // Set to null
            data = null;
        }

        // Set tag
        evcStack.setTagCompound(data);
    }

    @Override
    public boolean acceptsAspect(final Aspect aspect) {
        return TileEVCBase.acceptsAspect(aspect);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addInformation(final ItemStack evcStack, final EntityPlayer player,
            @SuppressWarnings("rawtypes") final List displayList, final boolean advancedItemTooltips) {
        // Ignore stacks without a tag
        if (!evcStack.hasTagCompound()) {
            return;
        }

        // Is shift being held?
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || (Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))) {

            // Load the stack
            ObjectLongPair<Aspect> storedEssentia = this.getStoredAspectStack(evcStack);

            // Add stored info
            if (storedEssentia != null) {
                displayList.add(String.format("%s x %d", storedEssentia.left().getName(), storedEssentia.rightLong()));
            }
        } else {
            // Let the user know they can hold shift
            displayList.add(
                    EnumChatFormatting.WHITE.toString() + EnumChatFormatting.ITALIC.toString()
                            + ThEStrings.Tooltip_ItemStackDetails.getLocalized());
        }
    }

    @Override
    public AspectList getAspects(final ItemStack evcStack) {
        AspectList list = new AspectList();

        // Ignore stacks without a tag
        if (evcStack.hasTagCompound()) {
            // Get the stored aspect
            ObjectLongPair<Aspect> stored = this.getStoredAspectStack(evcStack);
            if (stored != null) {
                // Add it
                list.add(stored.left(), (int) stored.rightLong());
            }
        }

        return list;
    }

    @Override
    public void setAspects(final ItemStack evcStack, final AspectList list) {
        if (list.size() > 0) {
            Aspect aspect = list.getAspects()[0];

            this.setStoredAspectStack(evcStack, aspect, list.getAmount(aspect));
        } else {
            this.setStoredAspectStack(evcStack, null, 0);
        }
    }
}
