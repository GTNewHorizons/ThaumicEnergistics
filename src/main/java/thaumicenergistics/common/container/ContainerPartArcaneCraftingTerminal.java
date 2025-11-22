package thaumicenergistics.common.container;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.ContainerNull;
import appeng.container.PrimaryGui;
import appeng.container.implementations.ContainerMEMonitorable;
import appeng.container.slot.AppEngSlot;
import appeng.core.AELog;
import appeng.helpers.InventoryAction;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.InventoryAdaptor;
import appeng.util.inv.AdaptorPlayerHand;
import appeng.util.item.AEItemStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumicenergistics.client.gui.ThEPrimaryGui;
import thaumicenergistics.common.container.slot.SlotArcaneCraftingGrid;
import thaumicenergistics.common.container.slot.SlotArcaneCraftingResult;
import thaumicenergistics.common.container.slot.SlotArmor;
import thaumicenergistics.common.container.slot.SlotWand;
import thaumicenergistics.common.integration.tc.ArcaneRecipeHelper;
import thaumicenergistics.common.network.packet.client.Packet_C_ArcaneCraftingTerminal;
import thaumicenergistics.common.network.packet.client.Packet_C_UpdatePlayerArmor;
import thaumicenergistics.common.parts.PartArcaneCraftingTerminal;

public class ContainerPartArcaneCraftingTerminal extends ContainerMEMonitorable {

    public static class ArcaneCrafingCost {

        /**
         * How much vis does the recipe require?
         */
        public final float visCost;

        /**
         * Which aspect?
         */
        public final Aspect primal;

        /**
         * Do we have enough of this aspect in the wand to perform the craft?
         */
        public final boolean hasEnoughVis;

        public ArcaneCrafingCost(final float visCost, final Aspect primal, final boolean hasEnough) {
            // Round to 1 decimal place
            this.visCost = Math.round(visCost * 10.0F) / 10.0F;

            this.primal = primal;

            this.hasEnoughVis = hasEnough;
        }
    }

    public static final int CRAFTING_SLOT_X_POS = 44;
    public static final int CRAFTING_SLOT_Y_POS = -72;
    private static final int WAND_SLOT_XPOS = 116, WAND_SLOT_YPOS = -72;
    private static final int ARMOR_SLOT_X_POS = 9, ARMOR_SLOT_Y_POS = -81;

    public final PartArcaneCraftingTerminal terminal;
    private final PlayerSource playerSource;

    private final IInventory resultInventory;
    private AspectList requiredAspects;
    private final List<ArcaneCrafingCost> craftingCost = new ArrayList<ArcaneCrafingCost>();

    private final SlotWand wandSlot;
    private final SlotArcaneCraftingResult resultSlot;
    private final SlotArmor[] armorSlots = new SlotArmor[4];

    public ContainerPartArcaneCraftingTerminal(InventoryPlayer ip, PartArcaneCraftingTerminal terminal) {
        super(ip, terminal, false);

        this.terminal = terminal;
        this.playerSource = new PlayerSource(ip.player, terminal);

        SlotWand wandSlot = new SlotWand(this, terminal.wandInventory, 0, WAND_SLOT_XPOS, WAND_SLOT_YPOS);
        this.wandSlot = wandSlot;
        this.addSlotToContainer(wandSlot);

        this.resultInventory = new AppEngInternalInventory(null, 1);
        this.resultSlot = new SlotArcaneCraftingResult(ip.player, this.resultInventory, 0, 116, -36);
        this.addSlotToContainer(this.resultSlot);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                final int slotIndex = (row * 3) + column;

                final SlotArcaneCraftingGrid slot = new SlotArcaneCraftingGrid(
                        this,
                        terminal.craftingGridInventory,
                        slotIndex,
                        CRAFTING_SLOT_X_POS + (column * 18),
                        CRAFTING_SLOT_Y_POS + (row * 18));
                this.addSlotToContainer(slot);
            }
        }

        for (int armorIndex = 0; armorIndex < 4; armorIndex++) {
            SlotArmor armorSlot = new SlotArmor(
                    terminal.armorInventory,
                    armorIndex,
                    ARMOR_SLOT_X_POS,
                    ARMOR_SLOT_Y_POS + armorIndex * 18,
                    armorIndex,
                    false);
            this.armorSlots[armorIndex] = armorSlot;
            this.addSlotToContainer(armorSlot);
        }

        this.bindPlayerInventory(ip, 1, 0);

        if (!ip.player.worldObj.isRemote) {
            this.terminal.registerContainer(this);
        }

        this.onCraftMatrixChanged(null);
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        if (!player.worldObj.isRemote) {
            this.terminal.removeContainer(this);
        }
    }

    @Override
    public void doAction(EntityPlayerMP player, InventoryAction action, int slot, long id) {
        if (action == InventoryAction.CRAFT_ITEM) {
            this.craftOnce(player);
            return;
        } else if (action == InventoryAction.CRAFT_SHIFT) {
            this.craftStack(player);
            return;
        }

        super.doAction(player, action, slot, id);
    }

    private ItemStack validateWandVisAmount(final IArcaneRecipe forRecipe) {
        boolean hasAll = true;
        AspectList wandAspectList = null;
        ItemWandCasting wandItem = null;

        // Get the cost
        this.requiredAspects = ArcaneRecipeHelper.INSTANCE
                .getRecipeAspectCost(this.terminal.craftingGridInventory, 0, 9, forRecipe);

        // Ensure there is a cost
        if (this.requiredAspects == null) {
            return null;
        }

        // Cache the recipes aspects
        Aspect[] recipeAspects = this.requiredAspects.getAspects();

        ItemStack wand = this.terminal.wandInventory.getStackInSlot(0);

        // Do we have a wand?
        if (wand != null) {
            // Get the wand item
            wandItem = ((ItemWandCasting) wand.getItem());
            assert wandItem != null;

            // Cache the wand's aspect list
            wandAspectList = wandItem.getAllVis(wand);
        }

        // Check the wand amounts vs recipe aspects
        for (Aspect currentAspect : recipeAspects) {
            // Get the base required vis
            int baseVis = this.requiredAspects.getAmount(currentAspect);

            // Get the adjusted amount
            int requiredVis = baseVis * 100;

            // Assume we do not have enough
            boolean hasEnough = false;

            // Do we have a wand?
            if ((wandItem != null) && (wandAspectList != null)) {
                // Adjust the required amount by the wand modifier
                requiredVis = (int) (requiredVis
                        * wandItem.getConsumptionModifier(wand, this.getInventoryPlayer().player, currentAspect, true));

                // Does the wand not have enough of vis of this aspect?
                hasEnough = (wandAspectList.getAmount(currentAspect) >= requiredVis);
            }

            if (!hasEnough) {
                // Mark that we do not have enough vis to complete crafting
                hasAll = false;
            }

            // Add to the cost list
            this.craftingCost.add(new ArcaneCrafingCost(requiredVis / 100.0F, currentAspect, hasEnough));
        }

        // Did we have all the vis required?
        if (hasAll) {
            // Get the result of the recipe.
            return ArcaneRecipeHelper.INSTANCE.getRecipeOutput(this.terminal.craftingGridInventory, 0, 9, forRecipe);
        }

        return null;
    }

    private ItemStack findMatchingArcaneResult() {
        ItemStack arcaneResult = null;

        // Is there a matching recipe?
        IArcaneRecipe matchingRecipe = ArcaneRecipeHelper.INSTANCE
                .findMatchingArcaneResult(this.terminal.craftingGridInventory, 0, 9, this.getInventoryPlayer().player);

        if (matchingRecipe != null) {
            // Found a match, validate it.
            arcaneResult = this.validateWandVisAmount(matchingRecipe);
        }

        // Return the result
        return arcaneResult;
    }

    private ItemStack findMatchingRegularResult() {
        InventoryCrafting craftingInventory = new InventoryCrafting(new ContainerNull(), 3, 3);
        for (int slotIndex = 0; slotIndex < 9; slotIndex++) {
            // Set the slot
            craftingInventory
                    .setInventorySlotContents(slotIndex, this.terminal.craftingGridInventory.getStackInSlot(slotIndex));
        }

        return CraftingManager.getInstance()
                .findMatchingRecipe(craftingInventory, this.terminal.getTile().getWorldObj());
    }

    @Override
    public void onCraftMatrixChanged(IInventory p_75130_1_) {
        this.requiredAspects = null;
        this.craftingCost.clear();

        ItemStack result = this.findMatchingRegularResult();

        if (result == null) {
            result = this.findMatchingArcaneResult();
        }

        this.resultInventory.setInventorySlotContents(0, result);
    }

    /**
     * Attempts to extract an item from the network. Used when crafting to replenish the crafting grid.
     */
    public ItemStack requestCraftingReplenishment(final ItemStack itemStack) {
        IMEMonitor<IAEItemStack> monitor = this.getItemMonitor();
        if (monitor == null) {
            return null;
        }

        // Create the AE request stack
        IAEItemStack requestStack = AEApi.instance().storage().createItemStack(itemStack);

        // Set the request amount to one
        requestStack.setStackSize(1);

        // Attempt an extraction
        IAEItemStack replenishmentAE = monitor.extractItems(requestStack, Actionable.MODULATE, this.playerSource);

        // Did we get a replenishment?
        if (replenishmentAE != null) {
            return replenishmentAE.getItemStack();
        }

        // Did not get a replenishment, search for items that match.

        // Get a list of all items in the ME network
        IItemList<IAEItemStack> networkItems = monitor.getStorageList();

        // Search all items
        for (IAEItemStack potentialMatch : networkItems) {
            // Does the request match?
            if (requestStack.isSameType(potentialMatch) || requestStack.sameOre(potentialMatch)) {
                // Found a match
                requestStack = potentialMatch.copy();

                // Set the request amount to one
                requestStack.setStackSize(1);

                // Attempt an extraction
                replenishmentAE = monitor.extractItems(requestStack, Actionable.MODULATE, this.playerSource);

                // Did we get a replenishment?
                if ((replenishmentAE != null) && (replenishmentAE.getStackSize() > 0)) {
                    return replenishmentAE.getItemStack();
                }
            }
        }

        // No matches at all :(
        return null;
    }

    private void craftOnce(EntityPlayerMP player) {
        AdaptorPlayerHand adaptor = new AdaptorPlayerHand(player);
        ItemStack leftover = adaptor.simulateAdd(this.resultSlot.getStack());
        if (leftover != null) return;

        adaptor.addItems(this.resultSlot.getStack());
        this.consumeIngredients(player);
        this.updateHeld(player);

        this.detectAndSendChanges();
    }

    private void craftStack(EntityPlayer player) {
        InventoryAdaptor adaptor = InventoryAdaptor.getAdaptor(player, null);
        int maxTimesToCraft = (int) Math.floor(
                (double) this.resultSlot.getStack().getMaxStackSize() / (double) this.resultSlot.getStack().stackSize);

        ItemStack simStack = this.resultSlot.getStack().copy();
        simStack.stackSize *= maxTimesToCraft;
        ItemStack simResult = adaptor.simulateAdd(simStack);
        if (simResult != null) {
            maxTimesToCraft = (int) Math
                    .floor((double) simResult.stackSize / (double) this.resultSlot.getStack().stackSize);
        }

        if (maxTimesToCraft == 0) return;

        for (int i = 0; i < maxTimesToCraft; i++) {
            adaptor.addItems(this.resultSlot.getStack());
            this.consumeIngredients(player);
            if (this.resultSlot.getStack() == null) break;
        }

        this.detectAndSendChanges();
    }

    public void consumeIngredients(final EntityPlayer player) {
        if (player.worldObj.isRemote) return;

        if ((this.requiredAspects != null)) {
            // Consume wand vis
            ItemStack wand = this.terminal.wandInventory.getStackInSlot(0);
            if (wand != null && wand.getItem() instanceof ItemWandCasting wandItem) {
                wandItem.consumeAllVisCrafting(wand, player, this.requiredAspects, true);
            } else {
                AELog.error("Failed consume vis from the wand");
                return;
            }
        }

        // Loop over all crafting slots
        for (int slotIndex = 0; slotIndex < this.terminal.craftingGridInventory.getSizeInventory(); slotIndex++) {
            // Get the itemstack in this slot
            ItemStack slotStack = this.terminal.craftingGridInventory.getStackInSlot(slotIndex);

            // Is there a stack?
            if (slotStack == null) {
                // Next
                continue;
            }

            // Get the container item
            ItemStack slotContainerStack = Objects.requireNonNull(slotStack.getItem()).getContainerItem(slotStack);
            // Does the item have a container?
            if (slotContainerStack != null) {

                if (slotContainerStack.isItemStackDamageable()
                        && slotContainerStack.getItemDamage() >= slotContainerStack.getMaxDamage()) {
                    MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, slotContainerStack));
                } else {
                    // Did we not kill the container item?
                    /*
                     * Should the item stay in the crafting grid, or if it is supposed to go back to the players
                     * inventory but can't?
                     */
                    if (!slotStack.getItem().doesContainerItemLeaveCraftingGrid(slotStack)
                            || !player.inventory.addItemStackToInventory(slotContainerStack)) {
                        // Place it back in the grid
                        this.terminal.craftingGridInventory.setInventorySlotContents(slotIndex, slotContainerStack);
                        continue;
                    }
                }
            }

            // If decrementing it would result in it being empty, ask the ME system for a replenishment.
            if (slotStack.stackSize == 1) {
                // First check if we can replenish it from the ME network
                ItemStack replenishment = this.requestCraftingReplenishment(slotStack);

                // Did we get a replenishment?
                if (replenishment != null) {
                    // Did the item change?
                    if (!ItemStack.areItemStacksEqual(replenishment, slotStack)) {
                        // Set the slot contents to the replenishment
                        this.terminal.craftingGridInventory.setInventorySlotContents(slotIndex, replenishment);
                    }
                    continue;
                }
            }

            // Decrement the stack
            this.terminal.craftingGridInventory.decrStackSize(slotIndex, 1);
        }
        this.onCraftMatrixChanged(null);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer p, int idx) {
        if (p.worldObj.isRemote) {
            return null;
        }

        final AppEngSlot clickSlot = (AppEngSlot) this.inventorySlots.get(idx); // require AE SLots!

        if (clickSlot != null && clickSlot.getHasStack() && clickSlot.isPlayerSide()) {
            ItemStack tis = clickSlot.getStack();

            boolean merged = false;
            if (!this.wandSlot.getHasStack() && this.wandSlot.isItemValid(tis)) {
                merged = this.mergeItemStack(tis, this.wandSlot.slotNumber, this.wandSlot.slotNumber + 1, false);
            } else {
                for (SlotArmor slot : this.armorSlots) {
                    if (!slot.getHasStack() && slot.isItemValid(tis)) {
                        merged = this.mergeItemStack(tis, slot.slotNumber, slot.slotNumber + 1, false);
                        break;
                    }
                }
            }

            if (merged) {
                if (tis.stackSize == 0) {
                    clickSlot.putStack(null);
                } else {
                    clickSlot.onSlotChanged();
                }
                this.detectAndSendChanges();
                return null;
            }
        }

        return super.transferStackInSlot(p, idx);
    }

    public List<ArcaneCrafingCost> getAspectCosts() {
        return this.craftingCost;
    }

    @Override
    public PrimaryGui getPrimaryGui() {
        return new ThEPrimaryGui(this.terminal);
    }

    public void clearCraftingGrid() {
        IMEMonitor<IAEItemStack> monitor = this.terminal.getItemInventory();
        for (int i = 0; i < this.terminal.craftingGridInventory.getSizeInventory(); i++) {
            ItemStack slotStack = this.terminal.craftingGridInventory.getStackInSlot(i);
            if (slotStack == null) continue;

            IAEItemStack leftOver = monitor
                    .injectItems(AEItemStack.create(slotStack), Actionable.MODULATE, this.playerSource);
            if (leftOver != null) {
                slotStack.stackSize = (int) leftOver.getStackSize();
            } else {
                this.terminal.craftingGridInventory.setInventorySlotContents(i, null);
            }
        }

        this.detectAndSendChanges();
        this.onCraftMatrixChanged(null);
    }

    public void swapArmor(EntityPlayer player) {

        Packet_C_UpdatePlayerArmor.send(player, this.terminal.armorInventory);

        for (int armorSlot = 0; armorSlot < 4; ++armorSlot) {
            // Get the stored armor
            ItemStack storedArmor = this.terminal.armorInventory.getStackInSlot(armorSlot);

            // Get the player armor
            ItemStack playerArmor = player.inventory.armorInventory[3 - armorSlot];

            // Swap
            player.inventoryContainer.putStackInSlot(5 + armorSlot, storedArmor);
            this.terminal.armorInventory.setInventorySlotContents(armorSlot, playerArmor);
        }

        this.detectAndSendChanges();

        Packet_C_ArcaneCraftingTerminal.updateAspectCost(player);
    }

    public void updateVisCost() {
        this.craftingCost.clear();
        this.findMatchingArcaneResult();
    }
}
