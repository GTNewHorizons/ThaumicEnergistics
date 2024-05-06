package thaumicenergistics.common.container;

import java.util.ArrayList;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import appeng.api.storage.data.IAEItemStack;
import appeng.container.slot.IOptionalSlotHost;
import appeng.container.slot.SlotFake;
import appeng.util.item.AEItemStack;
import thaumicenergistics.common.blocks.BlockInfusionEncoder;
import thaumicenergistics.common.container.slot.SlotRestrictive;
import thaumicenergistics.common.inventory.TheInternalInventory;
import thaumicenergistics.common.tiles.TileInfusionPatternEncoder;
import thaumicenergistics.common.utils.EffectiveSide;
import thaumicenergistics.common.utils.EssentiaPatternDetails;

/**
 * Container for {@link BlockInfusionEncoder}
 * 
 * @author MTCBL
 */
public class ContainerInfusionEncoder extends ContainerWithPlayerInventory implements IOptionalSlotHost {

    /**
     * Y position for the player and hotbar inventory.
     */
    private static final int PLAYER_INV_POSITION_Y = 152,
            HOTBAR_INV_POSITION_Y = ContainerInfusionEncoder.PLAYER_INV_POSITION_Y + 58;

    /**
     * Position of the blank patterns.
     */
    private static final int SLOT_PATTERNS_BLANK_POS_X = 153, SLOT_PATTERNS_BLANK_POS_Y = 47;

    /**
     * Position of the encoded pattern.
     */
    private static final int SLOT_PATTERN_ENCODED_POS_X = 153, SLOT_PATTERN_ENCODED_POS_Y = 93;

    /**
     * Position of the source item.
     */
    private static final int SLOT_SOURCE_ITEM_POS_X = 132, SLOT_SOURCE_ITEM_POS_Y = 47;

    /**
     * Position of the target item.
     */
    private static final int SLOT_TARGET_ITEM_POS_X = 132, SLOT_TARGET_ITEM_POS_Y = 93;

    /*
     * Crafting page parameter
     */
    public static final int PAGE_COUNT = 4, SLOTS_IN_ONE_PAGE = 36, SLOT_STACK_SIZE_LIMIT = 1000;

    /**
     * Position of the crafting slots.
     */
    private static final int FIRST_FAKE_SLOT_X = 10, FIRST_FAKE_SLOT_Y = 25,
            FAKE_SLOT_COUNT = PAGE_COUNT * SLOTS_IN_ONE_PAGE;

    /**
     * 
     */
    private static final String NBTKEY_AUTHOR = "author";

    /**
     * Slot holding the source item.
     */
    public final SlotFake slotSourceItem;

    /**
     * Slot holding the source item.
     */
    public final SlotFake slotTragetItem;

    /**
     * Slots holding the crafting source items.
     */
    public final SlotFake[] showingCraftingItems = new SlotFake[SLOTS_IN_ONE_PAGE];

    /**
     * Host encoder.
     */
    private final TileInfusionPatternEncoder encoder;

    /**
     * Blank patterns slot.
     */
    private final SlotRestrictive slotPatternsBlank;

    /**
     * Encoded pattern slot.
     */
    private final SlotRestrictive slotPatternEncoded;

    /**
     * Stores the crafting items.
     */
    private final TheInternalInventory craftingInventory;

    /**
     * Stores which page's items shows up.
     */
    private final TheInternalInventory internalInventory;

    /**
     * Default active page.
     */
    public int activePage;

    /**
     * If change page will revert, then wait to fresh.
     */
    private boolean changePage = false;

    /**
     * Hold the pattern last time encode.
     */
    private ItemStack cachedPattern;

    public ContainerInfusionEncoder(final EntityPlayer player, final World world, final int x, final int y,
            final int z) {
        super(player);
        // Get the encoder
        this.encoder = (TileInfusionPatternEncoder) world.getTileEntity(x, y, z);

        this.craftingInventory = this.encoder.getCraftingInventory();

        this.internalInventory = new TheInternalInventory(
                "showingCraftingItem",
                SLOTS_IN_ONE_PAGE,
                SLOT_STACK_SIZE_LIMIT);

        this.activePage = 0;

        // Add the source item slots
        for (int index = 0; index < ContainerInfusionEncoder.FAKE_SLOT_COUNT; index++) {
            // Create the slot and add it
            if (index < SLOTS_IN_ONE_PAGE) {
                showingCraftingItems[index] = new SlotFake(
                        this.internalInventory,
                        index,
                        FIRST_FAKE_SLOT_X + (index % 6) * 18,
                        FIRST_FAKE_SLOT_Y + (index / 6) * 18);
                showingCraftingItems[index].putStack(this.craftingInventory.getStackInSlot(index));
                this.addSlotToContainer(this.showingCraftingItems[index]);
            }
        }

        // Add the source and target item slot
        this.slotSourceItem = new SlotFake(
                encoder,
                TileInfusionPatternEncoder.SLOT_SOURCE_ITEM,
                ContainerInfusionEncoder.SLOT_SOURCE_ITEM_POS_X,
                ContainerInfusionEncoder.SLOT_SOURCE_ITEM_POS_Y);
        this.slotTragetItem = new SlotFake(
                encoder,
                TileInfusionPatternEncoder.SLOT_TARGET_ITEM,
                ContainerInfusionEncoder.SLOT_TARGET_ITEM_POS_X,
                ContainerInfusionEncoder.SLOT_TARGET_ITEM_POS_Y);
        this.addSlotToContainer(this.slotSourceItem);
        this.addSlotToContainer(this.slotTragetItem);

        // Add blank pattern slot
        this.slotPatternsBlank = new SlotRestrictive(
                this.encoder,
                TileInfusionPatternEncoder.SLOT_BLANK_PATTERNS,
                ContainerInfusionEncoder.SLOT_PATTERNS_BLANK_POS_X,
                ContainerInfusionEncoder.SLOT_PATTERNS_BLANK_POS_Y);
        this.addSlotToContainer(this.slotPatternsBlank);

        // Add encoded pattern slot
        this.slotPatternEncoded = new SlotRestrictive(
                this.encoder,
                TileInfusionPatternEncoder.SLOT_ENCODED_PATTERN,
                ContainerInfusionEncoder.SLOT_PATTERN_ENCODED_POS_X,
                ContainerInfusionEncoder.SLOT_PATTERN_ENCODED_POS_Y);
        this.addSlotToContainer(this.slotPatternEncoded);

        // Bind to the players inventory
        this.bindPlayerInventory(
                player.inventory,
                ContainerInfusionEncoder.PLAYER_INV_POSITION_Y,
                ContainerInfusionEncoder.HOTBAR_INV_POSITION_Y);

    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        // Ensure there is an encoder
        if (this.encoder != null) {
            // Ask it if this player can interact
            return this.encoder.isUseableByPlayer(player);
        }

        return false;
    }

    @Override
    public ItemStack transferStackInSlot(final EntityPlayer player, final int slotIndex) {
        if (EffectiveSide.isClientSide()) {
            // Ignored client side
            return null;
        }

        // Get the slot
        Slot clickedSlot = this.getSlotOrNull(slotIndex);

        // Slot empty?
        if ((clickedSlot == null) || !clickedSlot.getHasStack()) {
            // Done
            return null;
        }
        // Was this handled?
        if (this.handleSlotTransfer(clickedSlot)) {
            // Was the itemstack in the slot drained?
            if (clickedSlot.getHasStack() && (clickedSlot.getStack().stackSize <= 0)) {
                // Set to null
                clickedSlot.putStack(null);
            } else {
                // Let the slot know it has changed.
                clickedSlot.onSlotChanged();
            }

            // Update
            this.detectAndSendChanges();
        }

        return null;
    }

    private boolean handleSlotTransfer(final Slot clickedSlot) {
        // Get the stack
        ItemStack slotStack = clickedSlot.getStack();

        // Is the slot in the player inventory?
        if (clickedSlot.inventory == this.player.inventory) {
            // Will the blank pattern slot take this?
            if (this.slotPatternsBlank.isItemValid(slotStack)) {
                return this.mergeItemStack(
                        slotStack,
                        this.slotPatternsBlank.slotNumber,
                        this.slotPatternsBlank.slotNumber + 1,
                        false);
            }

            // Will the encoded pattern take this?
            if (this.slotPatternEncoded.isItemValid(slotStack)) {
                if (this.mergeItemStack(
                        slotStack,
                        this.slotPatternEncoded.slotNumber,
                        this.slotPatternEncoded.slotNumber + 1,
                        false)) {
                    this.changePage = true;
                    return true;
                } else {
                    return false;
                }
            }
            if (!this.slotSourceItem.getHasStack()) {
                slotStack = slotStack.copy();
                slotStack.stackSize = 1;
                this.slotSourceItem.putStack(slotStack);
                return true;
            }
            if (!this.slotTragetItem.getHasStack()) {
                slotStack = slotStack.copy();
                slotStack.stackSize = 1;
                this.slotTragetItem.putStack(slotStack);
                return true;
            }
        }

        // Pattern slot?
        if (((this.slotPatternsBlank == clickedSlot) || (this.slotPatternEncoded == clickedSlot))) {
            // Merge with the hotbar?
            if (!this.mergeSlotWithHotbarInventory(slotStack)) {
                // Merge with the player inventory
                return this.mergeSlotWithPlayerInventory(slotStack);
            }
            // Merged with hotbar
            return true;
        }

        // End
        return false;
    }

    @Override
    public ItemStack slotClick(final int slotNumber, final int buttonPressed, final int flag,
            final EntityPlayer player) {
        // If true detect will be called, and null returned.
        boolean handled = false;

        // click these two slot
        if (this.slotSourceItem.slotNumber == slotNumber || this.slotTragetItem.slotNumber == slotNumber) {
            // Get the slot
            SlotFake targetSlot = this.slotSourceItem.slotNumber == slotNumber ? this.slotSourceItem
                    : this.slotTragetItem;
            // Get the item the player is dragging
            ItemStack heldItem = player.inventory.getItemStack();

            // Set the source slot
            if (heldItem != null) {
                ItemStack copy = heldItem.copy();
                copy.stackSize = 1;
                targetSlot.putStack(copy);
            } else {
                targetSlot.putStack(null);
            }
            handled = true;
        }

        // check if is slotSourceItems
        if (!handled && slotNumber < ContainerInfusionEncoder.SLOTS_IN_ONE_PAGE && slotNumber >= 0) {
            // Get the item the player is dragging
            ItemStack heldItem = player.inventory.getItemStack();
            int slotNum = slotNumber + activePage * SLOTS_IN_ONE_PAGE;

            // Set the source slot
            if (heldItem != null) {
                ItemStack copy = heldItem.copy();

                // If player dragging item is same as the item in slot
                if (this.craftingInventory.getStackInSlot(slotNum) != null
                        && this.craftingInventory.getStackInSlot(slotNum).isItemEqual(copy)) {
                    int addNum = 0;
                    if (buttonPressed == 0) {
                        addNum = copy.stackSize;
                    } else if (buttonPressed == 1) {
                        addNum = 1;
                    }
                    // Add the number
                    copy.stackSize = this.craftingInventory.getStackInSlot(slotNum).stackSize + addNum;
                }
                this.craftingInventory.setInventorySlotContents(slotNum, copy);
            } else {
                if (buttonPressed == 0) {
                    this.craftingInventory.setInventorySlotContents(slotNum, null);
                } else if (buttonPressed == 1) {
                    if (this.craftingInventory.getStackInSlot(slotNum).stackSize != 1) {
                        this.craftingInventory.decrStackSize(slotNum, 1);
                    }
                }

            }
            this.showingCraftingItems[slotNumber].putStack(this.craftingInventory.getStackInSlot(slotNum));
            handled = true;
        }

        // Was the click handled?
        if (handled) {
            // Detect any changes
            this.detectAndSendChanges();

            // Done
            return null;
        }

        // better call sual
        return super.slotClick(slotNumber, buttonPressed, flag, player);
    }

    public void changeActivePage() {
        for (int index = 0; index < SLOTS_IN_ONE_PAGE; index++) {
            this.showingCraftingItems[index]
                    .putStack(this.craftingInventory.getStackInSlot(index + (this.activePage * SLOTS_IN_ONE_PAGE)));
        }
    }

    public void setActivePage(final int actviePage) {
        this.activePage = actviePage;
        this.changePage = true;
    }

    public void clearSlots() {
        // Clear all the slot
        this.slotSourceItem.clearStack();
        this.slotTragetItem.clearStack();

        // Clear all crafting slots
        for (int index = 0; index < FAKE_SLOT_COUNT; index++) {
            this.craftingInventory.setInventorySlotContents(index, null);
        }
        for (SlotFake slot : showingCraftingItems) {
            slot.clearStack();
        }
    }

    public void encodePattern(final boolean isShift) {
        boolean shouldTakePattern;
        // If there's no blank pattern.
        if (!this.slotPatternsBlank.getHasStack()) {
            return;
        }
        EssentiaPatternDetails patternDetail;
        // Get a new essentia pattern details.
        if (this.slotPatternEncoded.getHasStack()) {
            patternDetail = new EssentiaPatternDetails(this.slotPatternEncoded.getStack());
            shouldTakePattern = false;
        } else {
            patternDetail = new EssentiaPatternDetails();
            shouldTakePattern = true;
        }

        // Get Input and Output arrays
        ArrayList<AEItemStack> tempInputs = new ArrayList<>();
        // Check is there have source item set.
        if (this.slotSourceItem.getHasStack()) {
            tempInputs.add(AEItemStack.create(this.slotSourceItem.getStack()));
        } else {
            return;
        }

        for (int index = 0; index < FAKE_SLOT_COUNT; index++) {
            if (this.craftingInventory.getStackInSlot(index) != null) {
                tempInputs.add(AEItemStack.create(this.craftingInventory.getStackInSlot(index)));
            }
        }
        // If no inputs
        if (tempInputs.size() == 0) {
            return;
        }
        IAEItemStack[] inputs = tempInputs.stream().toArray(IAEItemStack[]::new);
        IAEItemStack[] outputs;
        // Check is there any output
        if (slotTragetItem.getHasStack()) {
            outputs = new AEItemStack[] { AEItemStack.create(slotTragetItem.getStack()) };
        } else {
            return;
        }
        // Set in and output for pattern
        patternDetail.setInputAndOutput(inputs, outputs);
        ItemStack encodedPattern = patternDetail.writeToStack();
        this.signUpPattern(encodedPattern);

        // When holding shift, try merge into backpack and hotbar
        // If can't, just put into encoded slot
        if (isShift && (this.mergeSlotWithHotbarInventory(encodedPattern)
                || this.mergeSlotWithPlayerInventory(encodedPattern))) {
            this.slotPatternEncoded.putStack(null);
        } else {
            this.slotPatternEncoded.putStack(encodedPattern);
        }
        // cache the pattern
        this.cachedPattern = encodedPattern;
        // Take one blank pattern
        if (shouldTakePattern) {
            this.slotPatternsBlank.decrStackSize(1);
        }

    }

    private void signUpPattern(ItemStack pattern) {
        if (pattern.stackTagCompound == null) {
            pattern.stackTagCompound = new NBTTagCompound();
        } else {
            pattern.stackTagCompound.setString(NBTKEY_AUTHOR, this.getPlayer().getCommandSenderName());
        }
    }

    private void readAndLoadFromPattern(ItemStack pattern) {
        this.cachedPattern = pattern;
        // clean the old contents
        this.clearSlots();
        EssentiaPatternDetails patternDetail = new EssentiaPatternDetails(pattern);
        // checkout if the pattern is valid
        if (patternDetail.readFromStack()) {
            IAEItemStack[] inputs = patternDetail.getInputs();
            IAEItemStack[] outputs = patternDetail.getOutputs();
            this.slotSourceItem.putStack(inputs[0].getItemStack());
            this.slotTragetItem.putStack(outputs[0].getItemStack());
            for (int index = 1; index < inputs.length; index++) {
                this.craftingInventory.setInventorySlotContents(index - 1, inputs[index].getItemStack());
            }
        }
    }

    /**
     * Returns the player for this container.
     *
     * @return
     */
    public EntityPlayer getPlayer() {
        return this.player;
    }

    @Override
    public boolean isSlotEnabled(int idx) {
        return true;
    }

    @Override
    public void detectAndSendChanges() {
        if (this.changePage) {
            this.changePage = false;
            this.changeActivePage();
        }
        super.detectAndSendChanges();
    }

    @Override
    protected boolean detectAndSendChangesMP(@Nonnull final EntityPlayerMP playerMP) {
        if (this.slotPatternEncoded.getHasStack() && this.slotPatternEncoded.getStack() != this.cachedPattern) {
            this.readAndLoadFromPattern(this.slotPatternEncoded.getStack());
            // update showing page
            this.changePage = true;
            return true;
        }
        return false;
    }
}
