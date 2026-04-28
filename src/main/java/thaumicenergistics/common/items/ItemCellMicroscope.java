package thaumicenergistics.common.items;

import static appeng.util.item.AEItemStackType.ITEM_STACK_TYPE;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.IterationCounter;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumcraft.api.research.ScanResult;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.items.relics.ItemThaumometer;
import thaumcraft.common.lib.research.ScanManager;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumicenergistics.common.network.packet.client.Packet_C_CellMicroscopeScanFeedback;
import thaumicenergistics.common.registries.ThEStrings;

public class ItemCellMicroscope extends ItemThaumometer {

    private static final String TAG_SCAN_TYPE = "scanType";
    private static final String TAG_SCAN_ENTITY = "scanEntity";
    private static final String TAG_SCAN_X = "scanX";
    private static final String TAG_SCAN_Y = "scanY";
    private static final String TAG_SCAN_Z = "scanZ";

    private static final byte TARGET_NONE = 0;
    private static final byte TARGET_ENTITY_ITEM = 1;
    private static final byte TARGET_CELL_CONTAINER = 2;

    public ItemCellMicroscope() {
        super();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister ir) {
        this.icon = ir.registerIcon("thaumcraft:blank");
    }

    @Override
    public String getUnlocalizedName() {
        return ThEStrings.Item_Cell_Microscope.getUnlocalized();
    }

    @Override
    public String getUnlocalizedName(final ItemStack itemStack) {
        return this.getUnlocalizedName();
    }

    @Override
    public int getMaxItemUseDuration(ItemStack itemstack) {
        return 50;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer p) {
        ScanTarget target = this.findScanTarget(world, p);

        if (world.isRemote) {
            this.playScanEffects(world, target, 0);
        } else if (target.isValid()) {
            this.writeScanTarget(stack, target);
        } else {
            this.clearScanTarget(stack);
        }

        p.setItemInUse(stack, this.getMaxItemUseDuration(stack));
        return stack;
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityPlayer p, int count) {
        ScanTarget target = this.findScanTarget(p.worldObj, p);

        if (p.worldObj.isRemote) {
            this.playScanEffects(p.worldObj, target, count);
            return;
        }

        if (!this.matchesStoredScanTarget(stack, target)) {
            this.clearScanTarget(stack);
            p.stopUsingItem();
            return;
        }

        if (count <= 5) {
            p.stopUsingItem();
            this.clearScanTarget(stack);
            this.doScan(p, target);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void doScan(EntityPlayer p, ScanTarget target) {
        if (target.type == TARGET_ENTITY_ITEM && target.entityItem != null) {
            this.doCellScan(p, target.entityItem.getEntityItem());
        } else if (target.type == TARGET_CELL_CONTAINER && target.tile instanceof ICellContainer) {
            List<IMEInventoryHandler> cellArray = ((ICellContainer) target.tile).getCellArray(ITEM_STACK_TYPE);
            for (IMEInventoryHandler cell : cellArray) {
                this.doInventoryScan(p, cell);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void doCellScan(EntityPlayer p, ItemStack cell) {
        IMEInventory<IAEItemStack> inv = AEApi.instance().registries().cell()
                .getCellInventory(cell, null, ITEM_STACK_TYPE);

        this.doInventoryScan(p, inv);
    }

    private void doInventoryScan(EntityPlayer p, IMEInventory<IAEItemStack> inv) {
        if (inv == null) return;

        IItemList<IAEItemStack> itemList = inv
                .getAvailableItems(AEApi.instance().storage().createItemList(), IterationCounter.fetchNewId());

        for (final IAEItemStack i : itemList) {
            ScanResult sr = new ScanResult((byte) 1, Item.getIdFromItem(i.getItem()), i.getItemDamage(), null, "");
            if (ScanManager.isValidScanTarget(p, sr, "@")) {

                if (!ScanManager.completeScan(p, sr, "@")) {
                    // Thaumcraft syncs successful scans to the client, but not failed ones.
                    // Run the client-side scan path manually so failure hints are shown.
                    Packet_C_CellMicroscopeScanFeedback.sendScanFeedback(p, sr);
                }
            }
        }
    }

    private ScanTarget findScanTarget(World world, EntityPlayer p) {
        Entity pointedEntity = EntityUtils.getPointedEntity(p.worldObj, p, 0.5D, 10.0D, 0.0F, true);

        if (pointedEntity instanceof EntityItem entityItem) {
            if (this.isItemStorageCell(entityItem.getEntityItem())) {
                return ScanTarget.forEntityItem(entityItem);
            }
        }

        MovingObjectPosition lookingAtBlock = this.getMovingObjectPositionFromPlayer(world, p, true);
        if ((lookingAtBlock != null) && (lookingAtBlock.typeOfHit == MovingObjectType.BLOCK)) {
            TileEntity blockAtPos = world
                    .getTileEntity(lookingAtBlock.blockX, lookingAtBlock.blockY, lookingAtBlock.blockZ);

            if (blockAtPos instanceof ICellContainer) {
                return ScanTarget.forCellContainer(lookingAtBlock, blockAtPos);
            }
        }

        return ScanTarget.none();
    }

    private boolean isItemStorageCell(ItemStack stack) {
        if (stack == null) return false;

        return AEApi.instance().registries().cell().getCellInventory(stack, null, ITEM_STACK_TYPE) != null;
    }

    private void playScanEffects(World world, ScanTarget target, int count) {
        if (!target.isValid()) return;

        if (count % 2 == 0) {
            world.playSound(
                    target.effectX,
                    target.effectY,
                    target.effectZ,
                    "thaumcraft:cameraticks",
                    0.2F,
                    0.45F + ((float) count / 50) + world.rand.nextFloat() * 0.1F,
                    false);
        }

        try {
            Thaumcraft.proxy.blockRunes(
                    world,
                    target.effectX,
                    target.effectY,
                    target.effectZ,
                    0.3F,
                    0.3F,
                    0.7F + world.rand.nextFloat() * 0.7F,
                    15,
                    -0.03F);
        } catch (Exception ignored) {}
    }

    /**
     * Stores the scan target selected at right-click time so the server can verify that the player is still looking at
     * the same cell when the use action completes. These tags are temporary and are cleared when scanning stops.
     */
    private void writeScanTarget(ItemStack stack, ScanTarget target) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }

        tag.setByte(TAG_SCAN_TYPE, target.type);
        tag.setInteger(TAG_SCAN_ENTITY, target.entityId);
        tag.setInteger(TAG_SCAN_X, target.x);
        tag.setInteger(TAG_SCAN_Y, target.y);
        tag.setInteger(TAG_SCAN_Z, target.z);
    }

    /**
     * Rejects the scan if the player moved to a different entity or block during the charge-up period.
     */
    private boolean matchesStoredScanTarget(ItemStack stack, ScanTarget target) {
        if (!target.isValid() || !stack.hasTagCompound()) return false;

        NBTTagCompound tag = stack.getTagCompound();
        byte type = tag.getByte(TAG_SCAN_TYPE);
        if (type != target.type) return false;

        if (type == TARGET_ENTITY_ITEM) {
            return tag.getInteger(TAG_SCAN_ENTITY) == target.entityId;
        }

        return (tag.getInteger(TAG_SCAN_X) == target.x) && (tag.getInteger(TAG_SCAN_Y) == target.y)
                && (tag.getInteger(TAG_SCAN_Z) == target.z);
    }

    /**
     * Removes the temporary scan target after completion, cancellation, or target mismatch so the item does not keep
     * stale target data.
     */
    private void clearScanTarget(ItemStack stack) {
        if (!stack.hasTagCompound()) return;

        NBTTagCompound tag = stack.getTagCompound();
        tag.removeTag(TAG_SCAN_TYPE);
        tag.removeTag(TAG_SCAN_ENTITY);
        tag.removeTag(TAG_SCAN_X);
        tag.removeTag(TAG_SCAN_Y);
        tag.removeTag(TAG_SCAN_Z);

        if (tag.hasNoTags()) {
            stack.setTagCompound(null);
        }
    }

    public void onPlayerStoppedUsing(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer,
            int par4) {
        super.onPlayerStoppedUsing(par1ItemStack, par2World, par3EntityPlayer, par4);
        this.clearScanTarget(par1ItemStack);
    }

    private static class ScanTarget {

        private final byte type;
        private final int entityId;
        private final int x;
        private final int y;
        private final int z;
        private final double effectX;
        private final double effectY;
        private final double effectZ;
        private final EntityItem entityItem;
        private final TileEntity tile;

        private ScanTarget(byte type, int entityId, int x, int y, int z, double effectX, double effectY, double effectZ,
                EntityItem entityItem, TileEntity tile) {
            this.type = type;
            this.entityId = entityId;
            this.x = x;
            this.y = y;
            this.z = z;
            this.effectX = effectX;
            this.effectY = effectY;
            this.effectZ = effectZ;
            this.entityItem = entityItem;
            this.tile = tile;
        }

        private static ScanTarget none() {
            return new ScanTarget(TARGET_NONE, 0, 0, 0, 0, 0, 0, 0, null, null);
        }

        private static ScanTarget forEntityItem(EntityItem entityItem) {
            return new ScanTarget(
                    TARGET_ENTITY_ITEM,
                    entityItem.getEntityId(),
                    0,
                    0,
                    0,
                    entityItem.posX,
                    entityItem.posY + 0.5D,
                    entityItem.posZ,
                    entityItem,
                    null);
        }

        private static ScanTarget forCellContainer(MovingObjectPosition position, TileEntity tile) {
            return new ScanTarget(
                    TARGET_CELL_CONTAINER,
                    0,
                    position.blockX,
                    position.blockY,
                    position.blockZ,
                    position.blockX,
                    position.blockY,
                    position.blockZ,
                    null,
                    tile);
        }

        private boolean isValid() {
            return this.type != TARGET_NONE;
        }
    }
}
