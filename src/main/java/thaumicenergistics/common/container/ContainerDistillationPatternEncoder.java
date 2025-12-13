package thaumicenergistics.common.container;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ICrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.api.storage.StorageName;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.container.AEBaseContainer;
import appeng.container.interfaces.IVirtualSlotHolder;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotFakeTypeOnly;
import appeng.container.slot.SlotRestrictedInput;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketVirtualSlot;
import appeng.helpers.PatternHelper;
import appeng.helpers.UltimatePatternHelper;
import appeng.items.misc.ItemEncodedPattern;
import appeng.items.misc.ItemEncodedUltimatePattern;
import appeng.tile.inventory.IAEStackInventory;
import appeng.util.item.AEItemStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.research.ScanManager;
import thaumicenergistics.common.network.packet.client.Packet_C_DistillationEncoder;
import thaumicenergistics.common.storage.AEEssentiaStack;
import thaumicenergistics.common.tiles.TileDistillationPatternEncoder;

/**
 * {@link TileDistillationPatternEncoder} container.
 *
 * @author Nividica
 *
 */
public class ContainerDistillationPatternEncoder extends AEBaseContainer implements IVirtualSlotHolder {

    /**
     * Position of the source item slot
     */
    public static final int SLOT_SOURCE_ITEM_POS_X = 15, SLOT_SOURCE_ITEM_POS_Y = 69;

    /**
     * Position of the blank patterns.
     */
    private static final int SLOT_PATTERNS_BLANK_POS_X = 146, SLOT_PATTERNS_BLANK_POS_Y = 60;

    /**
     * Position of the encoded pattern.
     */
    private static final int SLOT_PATTERN_ENCODED_POS_X = 146, SLOT_PATTERN_ENCODED_POS_Y = 96;

    /**
     * Host encoder.
     */
    private final TileDistillationPatternEncoder encoder;

    public final SlotFake slotSourceItem;
    private final SlotRestrictedInput slotPatternsBlank;
    private final SlotRestrictedInput slotPatternEncoded;

    /**
     * Cached versions of the source and pattern.
     */
    private ItemStack cachedSource, cachedPattern;

    public ContainerDistillationPatternEncoder(final EntityPlayer player, final World world, final int x, final int y,
            final int z) {
        super(player.inventory, world.getTileEntity(x, y, z));

        // Get the encoder
        this.encoder = (TileDistillationPatternEncoder) this.getTileEntity();

        // Add the source item slot
        this.slotSourceItem = new SlotFakeTypeOnly(
                this.encoder,
                TileDistillationPatternEncoder.SLOT_SOURCE_ITEM,
                ContainerDistillationPatternEncoder.SLOT_SOURCE_ITEM_POS_X,
                ContainerDistillationPatternEncoder.SLOT_SOURCE_ITEM_POS_Y);
        this.addSlotToContainer(this.slotSourceItem);

        // Add blank pattern slot
        this.slotPatternsBlank = new SlotRestrictedInput(
                SlotRestrictedInput.PlacableItemType.BLANK_PATTERN,
                this.encoder,
                TileDistillationPatternEncoder.SLOT_BLANK_PATTERNS,
                ContainerDistillationPatternEncoder.SLOT_PATTERNS_BLANK_POS_X,
                ContainerDistillationPatternEncoder.SLOT_PATTERNS_BLANK_POS_Y,
                this.getInventoryPlayer());
        this.addSlotToContainer(this.slotPatternsBlank);

        // Add encoded pattern slot
        this.slotPatternEncoded = new SlotRestrictedInput(
                SlotRestrictedInput.PlacableItemType.ENCODED_PATTERN,
                this.encoder,
                TileDistillationPatternEncoder.SLOT_ENCODED_PATTERN,
                ContainerDistillationPatternEncoder.SLOT_PATTERN_ENCODED_POS_X,
                ContainerDistillationPatternEncoder.SLOT_PATTERN_ENCODED_POS_Y,
                this.getInventoryPlayer());
        this.addSlotToContainer(this.slotPatternEncoded);

        // Bind to the players inventory
        this.bindPlayerInventory(player.inventory, 0, 152);

        this.cachedSource = this.slotSourceItem.getStack();
        this.cachedPattern = this.slotPatternEncoded.getStack();
    }

    @Override
    public void addCraftingToCrafters(ICrafting p_75132_1_) {
        super.addCraftingToCrafters(p_75132_1_);
        if (!this.encoder.getWorldObj().isRemote && this.cachedSource != null) {
            int hash = ScanManager.generateItemHash(this.cachedSource.getItem(), this.cachedSource.getItemDamage());

            // Get the list of scanned objects
            List<String> list = Thaumcraft.proxy.getScannedObjects()
                    .get(this.getInventoryPlayer().player.getCommandSenderName());

            // Has the player scanned the item?
            boolean playerScanned = list != null && (list.contains("@" + hash) || list.contains("#" + hash));

            if (!playerScanned) {
                Packet_C_DistillationEncoder.sendUnknownAspect(this.getInventoryPlayer().player);
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (!this.getTileEntity().getWorldObj().isRemote) {
            if (this.slotPatternEncoded.getStack() != this.cachedPattern) {
                // Load the pattern
                this.loadPattern();
            }
            if (!ItemStack.areItemStacksEqual(this.cachedSource, this.slotSourceItem.getStack())) {
                // Scan the source item
                this.scanSourceItem();
            }
        }
    }

    /**
     * Clears all aspect slots.
     */
    private void clearAspectSlots() {
        for (int i = 0; i < this.encoder.aspectsInventory.getSizeInventory(); i++) {
            this.encoder.aspectsInventory.putAEStackInSlot(0, null);
        }
        if (!this.encoder.getWorldObj().isRemote) {
            this.syncAspectSlots();
        }
    }

    /**
     * Attempts to load a pattern in the encoded slot.
     */
    private void loadPattern() {
        // Read the pattern
        this.cachedPattern = this.slotPatternEncoded.getStack();

        if (cachedPattern == null || !(cachedPattern.getItem() instanceof ItemEncodedUltimatePattern)
                || !cachedPattern.hasTagCompound()) {
            return;
        }

        NBTTagCompound encodedValue = this.cachedPattern.getTagCompound();
        final IAEStack<?>[] inItems;
        final IAEStack<?>[] outItems;

        if (this.cachedPattern.getItem() instanceof ItemEncodedPattern) {
            inItems = PatternHelper.loadIAEItemStackFromNBT(encodedValue.getTagList("in", 10), true, null);
            outItems = PatternHelper.loadIAEItemStackFromNBT(encodedValue.getTagList("out", 10), true, null);
        } else {
            inItems = UltimatePatternHelper.loadIAEStackFromNBT(encodedValue.getTagList("in", 10), true, null);
            outItems = UltimatePatternHelper.loadIAEStackFromNBT(encodedValue.getTagList("out", 10), true, null);
        }

        if (inItems == null || inItems.length == 0) {
            return;
        }

        IAEItemStack input = null;
        for (IAEStack<?> stack : inItems) {
            if (stack == null) continue;
            if (stack instanceof IAEItemStack ais) {
                if (input != null) return;
                input = ais;
            } else {
                return;
            }
        }

        for (IAEStack<?> stack : outItems) {
            if (stack == null) continue;
            if (!(stack instanceof AEEssentiaStack)) {
                return;
            }
        }

        if (input == null) return;

        this.slotSourceItem.putStack(input.getItemStack());
        this.cachedSource = input.getItemStack();

        for (int index = 0; index < outItems.length; index++) {
            this.encoder.aspectsInventory.putAEStackInSlot(index, outItems[index]);
        }

        this.syncAspectSlots();
        Packet_C_DistillationEncoder.sendChangeSrcItem(this.getInventoryPlayer().player);
    }

    /**
     * Called when the source item has changed.
     */
    public void scanSourceItem() {
        this.cachedSource = this.slotSourceItem.getStack();

        // Clear aspect slots
        this.clearAspectSlots();

        if (this.cachedSource == null) {
            Packet_C_DistillationEncoder.sendChangeSrcItem(this.getInventoryPlayer().player);
            return;
        }

        // Get the aspects
        AspectList itemAspects = ThaumcraftApiHelper.getObjectAspects(this.cachedSource);
        itemAspects = ThaumcraftApiHelper.getBonusObjectTags(this.cachedSource, itemAspects);

        // Does the item have any aspects?
        if (itemAspects == null || itemAspects.size() == 0) {
            Packet_C_DistillationEncoder.sendChangeSrcItem(this.getInventoryPlayer().player);
            return;
        }

        // Generate hash
        int hash = ScanManager.generateItemHash(this.cachedSource.getItem(), this.cachedSource.getItemDamage());

        // Get the list of scanned objects
        List<String> list = Thaumcraft.proxy.getScannedObjects()
                .get(this.getInventoryPlayer().player.getCommandSenderName());

        // Has the player scanned the item?
        boolean playerScanned = ((list != null) && ((list.contains("@" + hash)) || (list.contains("#" + hash))));
        if (playerScanned) {
            // Get sorted
            Aspect[] sortedAspects = itemAspects.getAspectsSortedAmount();

            // Set number to display
            for (int i = 0; i < this.encoder.aspectsInventory.getSizeInventory(); i++) {
                if (i < sortedAspects.length) {
                    this.encoder.aspectsInventory.putAEStackInSlot(i, new AEEssentiaStack(sortedAspects[i]));
                } else {
                    this.encoder.aspectsInventory.putAEStackInSlot(i, null);
                }
            }
        } else {
            for (int i = 0; i < this.encoder.aspectsInventory.getSizeInventory(); i++) {
                this.encoder.aspectsInventory.putAEStackInSlot(i, null);
            }
        }

        this.syncAspectSlots();

        Packet_C_DistillationEncoder.sendChangeSrcItem(this.getInventoryPlayer().player);
    }

    @Override
    public boolean canInteractWith(final EntityPlayer player) {
        if (this.encoder != null) {
            return this.encoder.isUseableByPlayer(player);
        }
        return false;
    }

    /**
     * Called when a pattern is to be encoded.
     */
    public void encodePattern(EntityPlayer player) {
        if (!this.slotSourceItem.getHasStack()) {
            return;
        }

        if (!this.slotPatternEncoded.getHasStack() && !this.slotPatternsBlank.getHasStack()) {
            return;
        }

        ArrayList<AEEssentiaStack> outputStacks = new ArrayList<>();
        for (int i = 0; i < this.encoder.aspectsInventory.getSizeInventory(); i++) {
            AEEssentiaStack stack = (AEEssentiaStack) this.encoder.aspectsInventory.getAEStackInSlot(i);
            if (stack != null) {
                outputStacks.add(stack);
            }
        }

        if (outputStacks.isEmpty()) {
            return;
        }

        final NBTTagCompound encodedValue = new NBTTagCompound();
        final NBTTagList tagIn = new NBTTagList();
        final NBTTagList tagOut = new NBTTagList();

        tagIn.appendTag(AEItemStack.create(this.slotSourceItem.getStack()).toNBTGeneric());
        for (final IAEStack<?> i : outputStacks) {
            tagOut.appendTag(i != null ? i.toNBTGeneric() : new NBTTagCompound());
        }

        encodedValue.setTag("in", tagIn);
        encodedValue.setTag("out", tagOut);
        encodedValue.setString("author", player.getCommandSenderName());

        ItemStack pattern = AEApi.instance().definitions().items().encodedUltimatePattern().maybeStack(1).orNull();
        assert pattern != null;

        pattern.setTagCompound(encodedValue);

        if (!this.slotPatternEncoded.getHasStack()) {
            this.slotPatternsBlank.decrStackSize(1);
        }

        // Set the pattern slot
        this.cachedPattern = pattern;
        this.slotPatternEncoded.putStack(pattern);
    }

    public IAEStackInventory getAspectsInventory() {
        return this.encoder.aspectsInventory;
    }

    @Override
    public void receiveSlotStacks(StorageName invName, Int2ObjectMap<IAEStack<?>> slotStacks) {
        for (var entry : slotStacks.int2ObjectEntrySet()) {
            this.encoder.aspectsInventory.putAEStackInSlot(entry.getIntKey(), entry.getValue());
        }
        this.syncAspectSlots();
    }

    private void syncAspectSlots() {
        final Int2ObjectMap<IAEStack<?>> slotStacks = new Int2ObjectOpenHashMap<>();
        for (int i = 0; i < this.encoder.aspectsInventory.getSizeInventory(); i++) {
            slotStacks.put(i, this.encoder.aspectsInventory.getAEStackInSlot(i));
        }

        for (Object crafter : this.crafters) {
            if (crafter instanceof EntityPlayerMP playerMP) {
                NetworkHandler.instance.sendTo(
                        new PacketVirtualSlot(this.encoder.aspectsInventory.getStorageName(), slotStacks),
                        playerMP);
            }
        }
    }
}
