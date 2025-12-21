package thaumicenergistics.common.parts;

import static thaumicenergistics.common.storage.AEEssentiaStackType.ESSENTIA_STACK_TYPE;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.StorageName;
import appeng.api.storage.data.IAEStackType;
import appeng.api.util.AEColor;
import appeng.parts.automation.UpgradeInventory;
import appeng.parts.misc.PartStorageBus;
import appeng.tile.inventory.IAEStackInventory;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.client.textures.BlockTextureManager;
import thaumicenergistics.common.storage.AEEssentiaStack;

public class PartEssentiaStorageBus extends PartStorageBus {

    public PartEssentiaStorageBus(ItemStack is) {
        super(is);
    }

    @Override
    public IAEStackType<?> getStackType() {
        return ESSENTIA_STACK_TYPE;
    }

    private static final int FILTER_SIZE = 9;
    private static final String NBT_KEY_PRIORITY = "Priority", NBT_KEY_FILTER = "FilterAspects#",
            NBT_KEY_UPGRADES = "UpgradeInventory";

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        // Call super
        super.readFromNBT(data);

        // Read the priority
        if (data.hasKey(NBT_KEY_PRIORITY)) {
            setPriority(data.getInteger(NBT_KEY_PRIORITY));
        }

        IAEStackInventory config = this.getAEInventoryByName(StorageName.NONE);
        // Read the filter list
        for (int index = 0; index < FILTER_SIZE; index++) {
            if (data.hasKey(NBT_KEY_FILTER + index)) {
                config.putAEStackInSlot(
                        index,
                        new AEEssentiaStack(Aspect.aspects.get(data.getString(NBT_KEY_FILTER + index))));
            }
        }

        // Read the upgrade inventory
        if (data.hasKey(NBT_KEY_UPGRADES)) {
            UpgradeInventory upgradeInventory = (UpgradeInventory) this.getInventoryByName("upgrades");
            upgradeInventory.readFromNBT(data, NBT_KEY_UPGRADES);
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

        IIcon side = BlockTextureManager.ESSENTIA_STORAGE_BUS.getTextures()[2];
        helper.setTexture(side, side, side, BlockTextureManager.ESSENTIA_STORAGE_BUS.getTextures()[0], side, side);

        // Face
        helper.setBounds(1.0F, 1.0F, 15.0F, 15.0F, 15.0F, 16.0F);
        helper.renderInventoryBox(renderer);

        // Mid
        helper.setBounds(4.0F, 4.0F, 14.0F, 12.0F, 12.0F, 15.0F);
        helper.renderInventoryBox(renderer);

        // Color overlay
        helper.setBounds(2.0F, 2.0F, 15.0F, 14.0F, 14.0F, 16.0F);
        helper.setInvColor(AEColor.Black.blackVariant);
        ts.setBrightness(0xF000F0);
        helper.renderInventoryFace(
                BlockTextureManager.ESSENTIA_STORAGE_BUS.getTextures()[1],
                ForgeDirection.SOUTH,
                renderer);

        // Back
        helper.setBounds(5.0F, 5.0F, 13.0F, 11.0F, 11.0F, 14.0F);
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
        Tessellator tessellator = Tessellator.instance;

        IIcon side = BlockTextureManager.ESSENTIA_STORAGE_BUS.getTextures()[2];
        helper.setTexture(side, side, side, BlockTextureManager.ESSENTIA_STORAGE_BUS.getTexture(), side, side);

        // Front (facing jar)
        helper.setBounds(1.0F, 1.0F, 15.0F, 15.0F, 15.0F, 16.0F);
        helper.renderBlock(x, y, z, renderer);

        tessellator.setColorOpaque_I(this.getHost().getColor().blackVariant);

        if (this.isActive()) {
            tessellator.setBrightness(0xD000D0);
        }

        // Mid
        helper.renderFace(
                x,
                y,
                z,
                BlockTextureManager.ESSENTIA_STORAGE_BUS.getTextures()[1],
                ForgeDirection.SOUTH,
                renderer);
        helper.setBounds(4.0F, 4.0F, 14.0F, 12.0F, 12.0F, 15.0F);
        helper.renderBlock(x, y, z, renderer);

        // Back (facing bus)
        helper.setBounds(5.0F, 5.0F, 13.0F, 11.0F, 11.0F, 14.0F);
        this.renderStaticBusLights(x, y, z, helper, renderer);
    }
}
