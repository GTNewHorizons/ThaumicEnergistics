package thaumicenergistics.common.parts;

import static thaumicenergistics.common.storage.AEEssentiaStackType.ESSENTIA_STACK_TYPE;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageName;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import appeng.api.util.AEColor;
import appeng.core.AELog;
import appeng.helpers.ICustomButtonDataObject;
import appeng.helpers.ICustomButtonProvider;
import appeng.me.GridAccessException;
import appeng.parts.automation.PartBaseExportBus;
import appeng.parts.automation.UpgradeInventory;
import appeng.tile.inventory.IAEStackInventory;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.common.tiles.TileJarFillableVoid;
import thaumicenergistics.client.gui.widget.ExportBusVoidButtonObject;
import thaumicenergistics.client.textures.BlockTextureManager;
import thaumicenergistics.common.integration.tc.EssentiaTileContainerHelper;
import thaumicenergistics.common.registries.EnumCache;
import thaumicenergistics.common.storage.AEEssentiaStack;

public class PartEssentiaExportBus extends PartBaseExportBus<AEEssentiaStack> implements ICustomButtonProvider {

    /**
     * If true, excess essentia will be voided when facing a void jar.
     */
    private boolean isVoidAllowed = false;
    private ICustomButtonDataObject voidButtonObject;

    public PartEssentiaExportBus(ItemStack is) {
        super(is);
        this.voidButtonObject = new ExportBusVoidButtonObject();
    }

    @Override
    public IAEStackType<AEEssentiaStack> getStackType() {
        return ESSENTIA_STACK_TYPE;
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected IMEMonitor<AEEssentiaStack> getMonitor() {
        try {
            return (IMEMonitor<AEEssentiaStack>) this.getProxy().getStorage().getMEMonitor(ESSENTIA_STACK_TYPE);
        } catch (final GridAccessException e) {
            return null;
        }
    }

    @Override
    public int calculateAmountToSend() {
        return 4 + (this.getInstalledUpgrades(Upgrades.SPEED) * 8);
    }

    @Override
    protected Object getTarget() {
        TileEntity self = this.getHost().getTile();
        final World w = self.getWorldObj();

        ForgeDirection side = this.getSide();
        int x = self.xCoord + side.offsetX;
        int y = self.yCoord + side.offsetY;
        int z = self.zCoord + side.offsetZ;

        if (w.getChunkProvider().chunkExists(x >> 4, z >> 4)) {
            TileEntity target = w.getTileEntity(x, y, z);
            if (target instanceof IAspectContainer) {
                return target;
            }
            return null;
        }

        return null;
    }

    @Override
    protected boolean canInjectStackToTarget(AEEssentiaStack aes) {
        IAspectContainer container = (IAspectContainer) this.getTarget();
        if (container == null) return false;

        long injected = EssentiaTileContainerHelper.INSTANCE
                .injectEssentiaIntoContainer(container, 1, aes.getAspect(), Actionable.SIMULATE);
        return injected > 0;
    }

    @Override
    protected void pushItemIntoTarget(IEnergyGrid energy, IMEInventory<AEEssentiaStack> monitor, AEEssentiaStack aes) {
        aes = aes.copy();
        aes.setStackSize(this.calculateAmountToSend());

        aes = monitor.extractItems(aes, Actionable.SIMULATE, this.mySrc);
        if (aes == null || aes.getStackSize() == 0) return;

        long injected = this.injectAspect(Actionable.MODULATE, aes.getAspect(), aes.getStackSize());
        aes = aes.copy();
        aes.setStackSize(injected);
        monitor.extractItems(aes, Actionable.MODULATE, this.mySrc);
    }

    @Override
    public IAEStack<?> injectCraftedItems(ICraftingLink link, IAEStack<?> items, Actionable mode) {
        AEEssentiaStack aes = (AEEssentiaStack) items;

        if (!this.proxy.isActive()) return aes;

        try {
            final IEnergyGrid energy = this.proxy.getEnergy();
            final double power = (double) aes.getStackSize() / aes.getAmountPerUnit();

            if (energy.extractAEPower(power, mode, PowerMultiplier.CONFIG) > power - 0.01) {
                long injected = this.injectAspect(mode, aes.getAspect(), aes.getStackSize());
                aes = aes.copy();
                aes.decStackSize(injected);
                return aes;
            }
        } catch (GridAccessException e) {
            AELog.debug(e);
        }
        return aes;
    }

    /**
     * @return filled amount
     */
    private long injectAspect(Actionable mode, Aspect a, long extractedAmount) {
        IAspectContainer container = (IAspectContainer) this.getTarget();
        if (container == null) return 0;

        long filledAmount;
        if (this.isVoidAllowed && (container instanceof TileJarFillableVoid)) {
            // In void mode, we don't care if the jar can hold it or not.
            filledAmount = extractedAmount;
        } else {
            // Simulate filling the container
            filledAmount = EssentiaTileContainerHelper.INSTANCE
                    .injectEssentiaIntoContainer(container, (int) extractedAmount, a, Actionable.SIMULATE);
        }

        // Was the container filled?
        if (filledAmount <= 0) {
            // Unable to inject into container
            return 0;
        }

        if (mode == Actionable.MODULATE) {
            // Fill the container
            long actualFilledAmount = EssentiaTileContainerHelper.INSTANCE
                    .injectEssentiaIntoContainer(container, (int) filledAmount, a, Actionable.MODULATE);

            // Is voiding not allowed?
            if (!this.isVoidAllowed) {
                filledAmount = actualFilledAmount;
            }
        }
        return filledAmount;
    }

    public void toggleVoidAllowed() {
        this.isVoidAllowed = !this.isVoidAllowed;
    }

    public boolean getVoidAllowed() {
        return this.isVoidAllowed;
    }

    private static final String NBT_KEY_REDSTONE_MODE = "redstoneMode";
    private static final String NBT_KEY_FILTER_NUMBER = "AspectFilter#";
    private static final String NBT_KEY_UPGRADE_INV = "upgradeInventory";
    private static final String NBT_KEY_VOID = "IsVoidAllowed";
    private static final String NBT_KEY_OWNER = "Owner";
    private static final int MAX_FILTER_SIZE = 9;

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);

        data.setBoolean(NBT_KEY_VOID, this.isVoidAllowed);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);

        // Read redstone mode
        if (data.hasKey(NBT_KEY_REDSTONE_MODE)) {
            RedstoneMode redstoneMode = EnumCache.AE_REDSTONE_MODES[data.getInteger(NBT_KEY_REDSTONE_MODE)];
            this.getConfigManager().putSetting(Settings.REDSTONE_CONTROLLED, redstoneMode);
        }

        // Read filters
        for (int index = 0; index < MAX_FILTER_SIZE; index++) {
            if (data.hasKey(NBT_KEY_FILTER_NUMBER + index)) {
                Aspect aspect = Aspect.aspects.get(data.getString(NBT_KEY_FILTER_NUMBER + index));
                if (aspect != null) {
                    IAEStackInventory config = this.getAEInventoryByName(StorageName.NONE);
                    config.putAEStackInSlot(index, new AEEssentiaStack(aspect));
                }
            }
        }

        // Read upgrade inventory
        if (data.hasKey(NBT_KEY_UPGRADE_INV)) {
            UpgradeInventory upgradeInventory = (UpgradeInventory) this.getInventoryByName("upgrades");
            upgradeInventory.readFromNBT(data, NBT_KEY_UPGRADE_INV);
        }

        // Read void
        if (data.hasKey(NBT_KEY_VOID)) {
            this.isVoidAllowed = data.getBoolean(NBT_KEY_VOID);
        }

        // Crafting only
        if (data.hasKey("isCraftingOnly")) {
            boolean isCraftingOnly = data.getBoolean("isCraftingOnly");
            this.getConfigManager().putSetting(Settings.CRAFT_ONLY, isCraftingOnly ? YesNo.YES : YesNo.NO);
        }
    }

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
    protected void doFuzzy(AEEssentiaStack aes, FuzzyMode fzMode, IEnergyGrid energy,
            IMEMonitor<AEEssentiaStack> gridInv) {

    }

    @Override
    protected void doOreDict(IEnergyGrid energy, IMEMonitor<AEEssentiaStack> gridInv) {

    }

    @Override
    public void getBoxes(final IPartCollisionHelper helper) {
        // Large chamber and back wall
        helper.addBox(4.0F, 4.0F, 12.0F, 12.0F, 12.0F, 13.5F);

        // Small chamber and front wall
        helper.addBox(5.0F, 5.0F, 13.5F, 11.0F, 11.0F, 15.0F);

        // Face
        helper.addBox(6.0F, 6.0F, 15.0F, 10.0F, 10.0F, 16.0F);
    }

    @Override
    public IIcon getBreakingTexture() {
        return BlockTextureManager.ESSENTIA_EXPORT_BUS.getTextures()[2];
    }

    @SideOnly(Side.CLIENT)
    private static void renderInventoryBusLights(final IPartRenderHelper helper, final RenderBlocks renderer) {
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

    @SideOnly(Side.CLIENT)
    @Override
    public void renderInventory(final IPartRenderHelper helper, final RenderBlocks renderer) {
        Tessellator ts = Tessellator.instance;

        IIcon busSideTexture = BlockTextureManager.ESSENTIA_EXPORT_BUS.getTextures()[3];

        // Set the texture to the side texture
        helper.setTexture(busSideTexture);

        // Large Chamber back wall
        helper.setBounds(4.0F, 4.0F, 12.0F, 12.0F, 12.0F, 12.5F);
        helper.renderInventoryBox(renderer);

        // Set the texture to the chamber
        helper.setTexture(BlockTextureManager.ESSENTIA_EXPORT_BUS.getTextures()[2]);

        // Large chamber
        helper.setBounds(4.0F, 4.0F, 12.5F, 12.0F, 12.0F, 13.5F);
        helper.renderInventoryBox(renderer);

        // Small chamber
        helper.setBounds(5.0F, 5.0F, 13.5F, 11.0F, 11.0F, 14.5F);
        helper.renderInventoryBox(renderer);

        // Set the texture back to the side texture
        helper.setTexture(busSideTexture);

        // Small chamber front wall
        helper.setBounds(5.0F, 5.0F, 14.5F, 11.0F, 11.0F, 15.0F);
        helper.renderInventoryBox(renderer);

        // Setup the face texture
        helper.setTexture(
                busSideTexture,
                busSideTexture,
                busSideTexture,
                BlockTextureManager.ESSENTIA_EXPORT_BUS.getTexture(),
                busSideTexture,
                busSideTexture);

        // Face
        helper.setBounds(6.0F, 6.0F, 15.0F, 10.0F, 10.0F, 16.0F);
        helper.renderInventoryBox(renderer);

        // Face overlay
        helper.setInvColor(AEColor.Black.blackVariant);
        ts.setBrightness(0xF000F0);
        IIcon faceOverlayTexture = BlockTextureManager.ESSENTIA_EXPORT_BUS.getTextures()[1];
        helper.renderInventoryFace(faceOverlayTexture, ForgeDirection.UP, renderer);
        helper.renderInventoryFace(faceOverlayTexture, ForgeDirection.DOWN, renderer);
        helper.renderInventoryFace(faceOverlayTexture, ForgeDirection.EAST, renderer);
        helper.renderInventoryFace(faceOverlayTexture, ForgeDirection.WEST, renderer);

        // Lights
        helper.setBounds(6.0F, 6.0F, 11.0F, 10.0F, 10.0F, 12.0F);
        renderInventoryBusLights(helper, renderer);
    }

    @SideOnly(Side.CLIENT)
    private void renderStaticBusLights(final int x, final int y, final int z, final IPartRenderHelper helper,
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

    @Override
    @SideOnly(Side.CLIENT)
    public void renderStatic(final int x, final int y, final int z, final IPartRenderHelper helper,
            final RenderBlocks renderer) {
        Tessellator ts = Tessellator.instance;

        IIcon busSideTexture = BlockTextureManager.ESSENTIA_EXPORT_BUS.getTextures()[3];

        // Set the texture to the side texture
        helper.setTexture(busSideTexture);

        // Large Chamber back wall
        helper.setBounds(4.0F, 4.0F, 12.0F, 12.0F, 12.0F, 12.5F);
        helper.renderBlock(x, y, z, renderer);

        // Set to alpha pass
        helper.renderForPass(1);

        // Set the texture to the chamber
        helper.setTexture(BlockTextureManager.ESSENTIA_EXPORT_BUS.getTextures()[2]);

        // Large chamber
        helper.setBounds(4.0F, 4.0F, 12.5F, 12.0F, 12.0F, 13.5F);
        helper.renderBlock(x, y, z, renderer);

        // Small chamber
        helper.setBounds(5.0F, 5.0F, 13.5F, 11.0F, 11.0F, 14.5F);
        helper.renderBlock(x, y, z, renderer);

        // Set back to opaque pass
        helper.renderForPass(0);

        // Set the texture back to the side texture
        helper.setTexture(busSideTexture);

        // Small chamber front wall
        helper.setBounds(5.0F, 5.0F, 14.5F, 11.0F, 11.0F, 15.0F);
        helper.renderBlock(x, y, z, renderer);

        // Setup the face texture
        helper.setTexture(
                busSideTexture,
                busSideTexture,
                busSideTexture,
                BlockTextureManager.ESSENTIA_EXPORT_BUS.getTextures()[0],
                busSideTexture,
                busSideTexture);

        // Face
        helper.setBounds(6.0F, 6.0F, 15.0F, 10.0F, 10.0F, 16.0F);
        helper.renderBlock(x, y, z, renderer);

        // Face overlay
        ts.setColorOpaque_I(this.getHost().getColor().blackVariant);

        if (this.isActive()) {
            Tessellator.instance.setBrightness(0xD000D0);
        }

        IIcon faceOverlayTexture = BlockTextureManager.ESSENTIA_EXPORT_BUS.getTextures()[1];
        helper.renderFace(x, y, z, faceOverlayTexture, ForgeDirection.UP, renderer);
        helper.renderFace(x, y, z, faceOverlayTexture, ForgeDirection.DOWN, renderer);
        helper.renderFace(x, y, z, faceOverlayTexture, ForgeDirection.EAST, renderer);
        helper.renderFace(x, y, z, faceOverlayTexture, ForgeDirection.WEST, renderer);

        // Lights
        helper.setBounds(6.0F, 6.0F, 11.0F, 10.0F, 10.0F, 12.0F);
        this.renderStaticBusLights(x, y, z, helper, renderer);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void initCustomButtons(int guiLeft, int guiTop, int xSize, int ySize, int xOffset, int yOffset,
            List<GuiButton> buttonList) {
        this.voidButtonObject.initCustomButtons(guiLeft, guiTop, xSize, ySize, xOffset, yOffset, buttonList);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean actionPerformedCustomButtons(GuiButton btn) {
        return this.voidButtonObject.actionPerformedCustomButtons(btn);
    }

    @Override
    public void writeCustomButtonData() {

    }

    @Override
    public void readCustomButtonData() {

    }

    @Override
    public ICustomButtonDataObject getDataObject() {
        return this.voidButtonObject;
    }

    @Override
    public void setDataObject(ICustomButtonDataObject dataObject) {
        this.voidButtonObject = dataObject;
    }
}
