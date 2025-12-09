package thaumicenergistics.common.parts;

import static thaumicenergistics.common.storage.AEEssentiaStackType.ESSENTIA_STACK_TYPE;

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
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageName;
import appeng.api.storage.data.IAEStackType;
import appeng.api.util.AEColor;
import appeng.me.GridAccessException;
import appeng.parts.automation.PartBaseImportBus;
import appeng.parts.automation.UpgradeInventory;
import appeng.tile.inventory.IAEStackInventory;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IAspectContainer;
import thaumicenergistics.client.textures.BlockTextureManager;
import thaumicenergistics.common.integration.tc.EssentiaTileContainerHelper;
import thaumicenergistics.common.registries.EnumCache;
import thaumicenergistics.common.storage.AEEssentiaStack;

/**
 * Imports essentia from {@link IAspectContainer}
 *
 * @author Nividica
 *
 */
public class PartEssentiaImportBus extends PartBaseImportBus<AEEssentiaStack> {

    public PartEssentiaImportBus(ItemStack is) {
        super(is);
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
            return w.getTileEntity(x, y, z);
        }

        return null;
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
    protected boolean importStuff(Object myTarget, AEEssentiaStack whatToImport, IMEMonitor<AEEssentiaStack> monitor,
            IEnergySource energy, FuzzyMode fzMode) {
        if (!(myTarget instanceof IAspectContainer container)) return true;

        final Aspect aspect = EssentiaTileContainerHelper.INSTANCE.getAspectInContainer(container);
        if (aspect == null || (whatToImport != null && !whatToImport.getAspect().getTag().equals(aspect.getTag())))
            return true;

        final int maxDrain = this.calculateAmountToSend();
        final long toDrainAmount = EssentiaTileContainerHelper.INSTANCE
                .extractFromContainer(container, maxDrain, aspect, Actionable.SIMULATE);

        if (toDrainAmount <= 0) {
            return true;
        }

        AEEssentiaStack aes = new AEEssentiaStack(aspect);
        aes.setStackSize(toDrainAmount);

        final AEEssentiaStack leftover = monitor.injectItems(aes, Actionable.MODULATE, this.mySrc);
        if (leftover != null && leftover.getStackSize() > 0) {
            aes.decStackSize(leftover.getStackSize());
            if (aes.getStackSize() <= 0) return true;
        }

        EssentiaTileContainerHelper.INSTANCE
                .extractFromContainer(container, (int) aes.getStackSize(), aspect, Actionable.MODULATE);

        return true;
    }

    @Override
    protected boolean doOreDict(Object myTarget, IMEMonitor<AEEssentiaStack> inv, IEnergyGrid energy,
            FuzzyMode fzMode) {
        return false;
    }

    @Override
    protected int getPowerMultiplier() {
        return 1;
    }

    @Override
    public IAEStackType<AEEssentiaStack> getStackType() {
        return ESSENTIA_STACK_TYPE;
    }

    private static final String NBT_KEY_REDSTONE_MODE = "redstoneMode";
    private static final String NBT_KEY_FILTER_NUMBER = "AspectFilter#";
    private static final String NBT_KEY_UPGRADE_INV = "upgradeInventory";
    private static final String NBT_KEY_OWNER = "Owner";
    private static final int MAX_FILTER_SIZE = 9;

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
    public void getBoxes(final IPartCollisionHelper helper) {
        // Face + Large chamber
        helper.addBox(4.0F, 4.0F, 14.0F, 12.0F, 12.0F, 16.0F);

        // Small chamber
        helper.addBox(5.0F, 5.0F, 12.0F, 11.0F, 11.0F, 14.0F);

        // Lights
        helper.addBox(6.0D, 6.0D, 11.0D, 10.0D, 10.0D, 12.0D);
    }

    @Override
    public IIcon getBreakingTexture() {
        return BlockTextureManager.ESSENTIA_IMPORT_BUS.getTextures()[2];
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
        // Get the tessellator
        Tessellator ts = Tessellator.instance;

        // Get the side texture
        IIcon busSideTexture = BlockTextureManager.ESSENTIA_IMPORT_BUS.getTextures()[3];

        helper.setTexture(
                busSideTexture,
                busSideTexture,
                busSideTexture,
                BlockTextureManager.ESSENTIA_IMPORT_BUS.getTexture(),
                busSideTexture,
                busSideTexture);

        // Face
        helper.setBounds(4.0F, 4.0F, 15.0F, 12.0F, 12.0F, 16.0F);
        helper.renderInventoryBox(renderer);

        // Set the texture to the chamber
        helper.setTexture(BlockTextureManager.ESSENTIA_IMPORT_BUS.getTextures()[2]);

        // Large chamber
        helper.setBounds(4.0F, 4.0F, 14.0F, 12.0F, 12.0F, 15.0F);
        helper.renderInventoryBox(renderer);

        // Small chamber
        helper.setBounds(5.0F, 5.0F, 13.0F, 11.0F, 11.0F, 14.0F);
        helper.renderInventoryBox(renderer);

        // Set the texture back to the side texture
        helper.setTexture(busSideTexture);

        // Small chamber back wall
        helper.setBounds(5.0F, 5.0F, 12.0F, 11.0F, 11.0F, 13.0F);
        helper.renderInventoryBox(renderer);

        // Face overlay
        helper.setBounds(4.0F, 4.0F, 15.0F, 12.0F, 12.0F, 16.0F);
        helper.setInvColor(AEColor.Black.blackVariant);
        ts.setBrightness(15728880);
        helper.renderInventoryFace(
                BlockTextureManager.ESSENTIA_IMPORT_BUS.getTextures()[1],
                ForgeDirection.SOUTH,
                renderer);

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

    @SideOnly(Side.CLIENT)
    @Override
    public void renderStatic(final int x, final int y, final int z, final IPartRenderHelper helper,
            final RenderBlocks renderer) {
        Tessellator ts = Tessellator.instance;

        IIcon busSideTexture = BlockTextureManager.ESSENTIA_IMPORT_BUS.getTextures()[3];
        helper.setTexture(
                busSideTexture,
                busSideTexture,
                busSideTexture,
                BlockTextureManager.ESSENTIA_IMPORT_BUS.getTextures()[0],
                busSideTexture,
                busSideTexture);

        // Face
        helper.setBounds(4.0F, 4.0F, 15.0F, 12.0F, 12.0F, 16.0F);
        helper.renderBlock(x, y, z, renderer);

        if (this.getHost().getColor() != AEColor.Transparent) {
            ts.setColorOpaque_I(this.getHost().getColor().blackVariant);
        } else {
            ts.setColorOpaque_I(AEColor.Black.blackVariant);
        }

        if (this.isActive()) {
            Tessellator.instance.setBrightness(0xD000D0);
        }

        // Face overlay
        helper.renderFace(
                x,
                y,
                z,
                BlockTextureManager.ESSENTIA_IMPORT_BUS.getTextures()[1],
                ForgeDirection.SOUTH,
                renderer);

        // Set the pass to alpha
        helper.renderForPass(1);

        // Set the texture to the chamber
        helper.setTexture(BlockTextureManager.ESSENTIA_IMPORT_BUS.getTextures()[2]);

        // Large chamber
        helper.setBounds(4.0F, 4.0F, 14.0F, 12.0F, 12.0F, 15.0F);
        helper.renderBlock(x, y, z, renderer);

        // Small chamber
        helper.setBounds(5.0F, 5.0F, 13.0F, 11.0F, 11.0F, 14.0F);
        helper.renderBlock(x, y, z, renderer);

        // Set the pass back to opaque
        helper.renderForPass(0);

        // Set the texture back to the side texture
        helper.setTexture(busSideTexture);

        // Small chamber back wall
        helper.setBounds(5.0F, 5.0F, 12.0F, 11.0F, 11.0F, 13.0F);
        helper.renderBlock(x, y, z, renderer);

        // Lights
        helper.setBounds(6.0F, 6.0F, 11.0F, 10.0F, 10.0F, 12.0F);
        this.renderStaticBusLights(x, y, z, helper, renderer);
    }
}
