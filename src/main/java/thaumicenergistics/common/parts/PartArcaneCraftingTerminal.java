package thaumicenergistics.common.parts;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.util.AEColor;
import appeng.client.texture.CableBusTextures;
import appeng.parts.reporting.AbstractPartTerminal;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumicenergistics.api.grid.IDigiVisSource;
import thaumicenergistics.client.textures.BlockTextureManager;
import thaumicenergistics.common.ThEGuiHandler;
import thaumicenergistics.common.container.ContainerPartArcaneCraftingTerminal;
import thaumicenergistics.common.integration.tc.DigiVisSourceData;
import thaumicenergistics.common.utils.ThEUtils;

public class PartArcaneCraftingTerminal extends AbstractPartTerminal implements IGridTickable {

    private static final int OLD_MY_INVENTORY_SIZE = 20;
    public static final int OLD_WAND_SLOT_INDEX = 9;
    public static final int OLD_VIEW_SLOT_MIN = 11;
    public static final int OLD_VIEW_SLOT_MAX = 15;
    public static final int OLD_ARMOR_SLOT_MIN = 16;

    private static final String OLD_INVENTORY_NBT_KEY = "TEACT_Inventory";
    private static final String OLD_SLOT_NBT_KEY = "Slot#";

    private static final String VIS_INTERFACE_NBT_KEY = "VisInterface";

    public final AppEngInternalInventory craftingGridInventory = new AppEngInternalInventory(this, 9);
    public final AppEngInternalInventory wandInventory = new AppEngInternalInventory(this, 1);
    public final AppEngInternalInventory armorInventory = new AppEngInternalInventory(this, 4);

    private final DigiVisSourceData visSourceInfo = new DigiVisSourceData();
    private final List<ContainerPartArcaneCraftingTerminal> containers = new ArrayList<>();

    public PartArcaneCraftingTerminal(final ItemStack is) {
        super(is);
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(2, 20, false, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall) {
        if (!this.visSourceInfo.hasSourceData()) {
            return TickRateModulation.IDLE;
        }

        ItemStack stack = this.wandInventory.getStackInSlot(0);

        if (stack == null) {
            return TickRateModulation.IDLE;
        }

        // Get the wand
        ItemWandCasting wand = (ItemWandCasting) stack.getItem();
        assert wand != null;

        // Does the wand need vis?
        AspectList neededVis = wand.getAspectsWithRoom(stack);
        if (neededVis.size() <= 0) {
            // Wand is charged
            return TickRateModulation.IDLE;
        }

        // Get the source
        IDigiVisSource visSource = this.visSourceInfo.tryGetSource(this.getGrid());

        // Did we get an active source?
        if (visSource == null) {
            // Invalid source
            return TickRateModulation.IDLE;
        }

        boolean drained = false;
        // Request vis for each aspect that the wand needs
        for (Aspect vis : neededVis.getAspects()) {
            // Calculate the size of the request
            int amountToDrain = wand.getMaxVis(stack) - wand.getVis(stack, vis);

            // Request the vis
            int amountDrained = visSource.consumeVis(vis, amountToDrain);

            // Did we drain any?
            if (amountDrained > 0) {
                // Add to the wand
                wand.addRealVis(stack, vis, amountDrained, true);
                drained = true;
            }
        }

        if (drained) {
            for (var container : containers) {
                container.onCraftMatrixChanged(null);
            }
        }

        // Tick ASAP until the wand is charged.
        return TickRateModulation.URGENT;
    }

    public void registerContainer(ContainerPartArcaneCraftingTerminal container) {
        this.containers.add(container);
    }

    public void removeContainer(ContainerPartArcaneCraftingTerminal container) {
        this.containers.remove(container);
    }

    @Override
    public boolean useStandardMemoryCard() {
        return false;
    }

    @Override
    public boolean onPartActivate(EntityPlayer player, Vec3 pos) {
        ItemStack playerHolding = player.inventory.getCurrentItem();

        if ((playerHolding != null) && (playerHolding.getItem() instanceof IMemoryCard memoryCard)) {
            // Get the memory card

            // Get the stored name
            String settingsName = memoryCard.getSettingsName(playerHolding);

            // Does it contain the data about a vis source?
            if (settingsName.equals(DigiVisSourceData.SOURCE_UNLOC_NAME)) {
                // Get the data
                NBTTagCompound data = memoryCard.getData(playerHolding);

                // Load the info
                this.visSourceInfo.readFromNBT(data);

                // Ensure there was valid data
                if (this.visSourceInfo.hasSourceData()) {
                    // Inform the user
                    memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_LOADED);
                }

                this.saveChanges();
            }
            // Is the memory card empty?
            else if (settingsName.equals("gui.appliedenergistics2.Blank")) {
                // Clear the source info
                this.visSourceInfo.clearData();

                // Inform the user
                memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_CLEARED);

                this.saveChanges();
            }

            return true;
        }

        if (!player.isSneaking()) {
            if (Platform.isClient()) {
                return true;
            }

            TileEntity tile = this.getHost().getTile();
            ThEGuiHandler.launchGui(this, player, tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord);

            return true;
        }
        return super.onPartActivate(player, pos);
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        this.craftingGridInventory.writeToNBT(data, "craftingGrid");
        this.wandInventory.writeToNBT(data, "wand");
        this.armorInventory.writeToNBT(data, "armor");
        this.visSourceInfo.writeToNBT(data, VIS_INTERFACE_NBT_KEY);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.craftingGridInventory.readFromNBT(data, "craftingGrid");
        this.wandInventory.readFromNBT(data, "wand");
        this.armorInventory.readFromNBT(data, "armor");

        // For back compatibility
        if (data.hasKey(OLD_INVENTORY_NBT_KEY)) {
            // Get the list
            NBTTagList nbtTagList = (NBTTagList) data.getTag(OLD_INVENTORY_NBT_KEY);

            for (int listIndex = 0; listIndex < nbtTagList.tagCount(); listIndex++) {
                // Get the compound tag
                NBTTagCompound nbtCompound = nbtTagList.getCompoundTagAt(listIndex);

                int slotIndex = nbtCompound.getByte(OLD_SLOT_NBT_KEY);

                // Is it in range?
                if (slotIndex >= 0 && slotIndex < OLD_MY_INVENTORY_SIZE) {
                    ItemStack slotStack = ItemStack.loadItemStackFromNBT(nbtCompound);

                    if (slotIndex < 9) {
                        this.craftingGridInventory.setInventorySlotContents(slotIndex, slotStack);
                    } else if (slotIndex == OLD_WAND_SLOT_INDEX) {
                        // Validate the wand
                        if (!ThEUtils.isItemValidWand(slotStack, false)) {
                            // Invalid wand data
                            slotStack = null;
                        }
                        this.wandInventory.setInventorySlotContents(0, slotStack);
                    } else if (slotIndex >= OLD_VIEW_SLOT_MIN && slotIndex <= OLD_VIEW_SLOT_MAX) {
                        IInventory viewCells = this.getViewCellStorage();
                        viewCells.setInventorySlotContents(slotIndex - OLD_VIEW_SLOT_MIN, slotStack);
                    } else if (slotIndex >= OLD_ARMOR_SLOT_MIN) {
                        this.armorInventory.setInventorySlotContents(slotIndex - OLD_ARMOR_SLOT_MIN, slotStack);
                    }
                }
            }
        }

        if (data.hasKey(VIS_INTERFACE_NBT_KEY)) {
            this.visSourceInfo.readFromNBT(data, VIS_INTERFACE_NBT_KEY);
        }
    }

    private static final String NBT_KEY_OWNER = "Owner";

    @Override
    public void addToWorld() {
        super.addToWorld();

        // For back compatibility
        NBTTagCompound data = this.getItemStack().getTagCompound();
        if (data != null && data.hasKey(NBT_KEY_OWNER)) {
            int ownerID = data.getInteger(NBT_KEY_OWNER);
            this.getProxy().getNode().setPlayerID(ownerID);
        }
    }

    @Override
    public CableBusTextures getFrontBright() {
        return null;
    }

    @Override
    public CableBusTextures getFrontColored() {
        return null;
    }

    @Override
    public CableBusTextures getFrontDark() {
        return null;
    }

    @Override
    public ItemStack getPrimaryGuiIcon() {
        return this.getItemStack().copy();
    }

    @Override
    public IIcon getBreakingTexture() {
        return BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[0];
    }

    @SideOnly(Side.CLIENT)
    public static void renderInventoryBusLights(final IPartRenderHelper helper, final RenderBlocks renderer) {
        // Set color to white
        helper.setInvColor(0xFFFFFF);

        IIcon busColorTexture = BlockTextureManager.BUS_COLOR.getTextures()[0];

        IIcon sideTexture = BlockTextureManager.BUS_COLOR.getTextures()[2];

        helper.setTexture(busColorTexture, busColorTexture, sideTexture, sideTexture, busColorTexture, busColorTexture);

        // Rend the box
        helper.renderInventoryBox(renderer);

        // Set the brightness
        Tessellator.instance.setBrightness(0xD000D0);

        helper.setInvColor(AEColor.Transparent.blackVariant);

        IIcon lightTexture = BlockTextureManager.BUS_COLOR.getTextures()[1];

        // Render the lights
        helper.renderInventoryFace(lightTexture, ForgeDirection.UP, renderer);
        helper.renderInventoryFace(lightTexture, ForgeDirection.DOWN, renderer);
        helper.renderInventoryFace(lightTexture, ForgeDirection.NORTH, renderer);
        helper.renderInventoryFace(lightTexture, ForgeDirection.EAST, renderer);
        helper.renderInventoryFace(lightTexture, ForgeDirection.SOUTH, renderer);
        helper.renderInventoryFace(lightTexture, ForgeDirection.WEST, renderer);
    }

    /**
     * Renders the part while in the inventory
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventory(final IPartRenderHelper helper, final RenderBlocks renderer) {
        Tessellator ts = Tessellator.instance;

        IIcon side = BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[3];

        helper.setTexture(side, side, side, BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[0], side, side);
        helper.setBounds(2.0F, 2.0F, 14.0F, 14.0F, 14.0F, 16.0F);
        helper.renderInventoryBox(renderer);

        helper.setBounds(2.0F, 2.0F, 15.0F, 14.0F, 14.0F, 16.0F);
        ts.setColorOpaque_I(AEColor.Black.blackVariant);
        helper.renderInventoryFace(
                BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[2],
                ForgeDirection.SOUTH,
                renderer);

        ts.setColorOpaque_I(AEColor.Black.mediumVariant);
        helper.renderInventoryFace(
                BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[1],
                ForgeDirection.SOUTH,
                renderer);

        helper.setBounds(5.0F, 5.0F, 13.0F, 11.0F, 11.0F, 14.0F);
        renderInventoryBusLights(helper, renderer);
    }

    private void rotateRenderer(final RenderBlocks renderer, final boolean reset) {
        int rot = (reset ? 0 : this.getSpin());
        renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = rot;
    }

    @SideOnly(Side.CLIENT)
    public void renderStaticBusLights(final int x, final int y, final int z, final IPartRenderHelper helper,
            final RenderBlocks renderer) {
        IIcon busColorTexture = BlockTextureManager.BUS_COLOR.getTextures()[0];

        IIcon sideTexture = BlockTextureManager.BUS_COLOR.getTextures()[2];

        helper.setTexture(busColorTexture, busColorTexture, sideTexture, sideTexture, busColorTexture, busColorTexture);

        // Render the box
        helper.renderBlock(x, y, z, renderer);

        // Are we active?
        if (this.isActive()) {
            // Set the brightness
            Tessellator.instance.setBrightness(0xD000D0);

            // Set the color to match the cable
            Tessellator.instance.setColorOpaque_I(this.host.getColor().blackVariant);
        } else {
            // Set the color to black
            Tessellator.instance.setColorOpaque_I(0);
        }

        IIcon lightTexture = BlockTextureManager.BUS_COLOR.getTextures()[1];

        // Render the lights
        helper.renderFace(x, y, z, lightTexture, ForgeDirection.UP, renderer);
        helper.renderFace(x, y, z, lightTexture, ForgeDirection.DOWN, renderer);
        helper.renderFace(x, y, z, lightTexture, ForgeDirection.NORTH, renderer);
        helper.renderFace(x, y, z, lightTexture, ForgeDirection.EAST, renderer);
        helper.renderFace(x, y, z, lightTexture, ForgeDirection.SOUTH, renderer);
        helper.renderFace(x, y, z, lightTexture, ForgeDirection.WEST, renderer);
    }

    /**
     * Renders the part in the world.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void renderStatic(final int x, final int y, final int z, final IPartRenderHelper helper,
            final RenderBlocks renderer) {
        Tessellator tessellator = Tessellator.instance;

        IIcon side = BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[3];

        // Main block
        helper.setTexture(side, side, side, side, side, side);
        helper.setBounds(2.0F, 2.0F, 14.0F, 14.0F, 14.0F, 16.0F);
        helper.renderBlock(x, y, z, renderer);

        // Rotate
        this.rotateRenderer(renderer, false);

        // Face
        helper.renderFace(
                x,
                y,
                z,
                BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[0],
                ForgeDirection.SOUTH,
                renderer);

        if (this.isActive()) {
            // Set brightness
            tessellator.setBrightness(0xD000D0);

            // Draw corners
            helper.setBounds(2.0F, 2.0F, 15.0F, 14.0F, 14.0F, 16.0F);
            tessellator.setColorOpaque_I(this.getHost().getColor().blackVariant);
            helper.renderFace(
                    x,
                    y,
                    z,
                    BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[2],
                    ForgeDirection.SOUTH,
                    renderer);
            tessellator.setColorOpaque_I(this.getHost().getColor().mediumVariant);
            helper.renderFace(
                    x,
                    y,
                    z,
                    BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[1],
                    ForgeDirection.SOUTH,
                    renderer);

            // Draw crafting overlay
            tessellator.setBrightness(0xA000A0);
            tessellator.setColorOpaque_I(AEColor.Lime.blackVariant);
            helper.renderFace(
                    x,
                    y,
                    z,
                    BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[4],
                    ForgeDirection.SOUTH,
                    renderer);
        }

        // Reset rotation
        this.rotateRenderer(renderer, true);

        // Cable lights
        helper.setBounds(5.0F, 5.0F, 13.0F, 11.0F, 11.0F, 14.0F);
        this.renderStaticBusLights(x, y, z, helper, renderer);
    }
}
