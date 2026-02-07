package thaumicenergistics.common.items;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

import com.google.common.base.Optional;

import appeng.api.AEApi;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.util.IConfigManager;
import appeng.core.sync.GuiBridge;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.util.ConfigManager;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumicenergistics.common.ThaumicEnergistics;
import thaumicenergistics.common.registries.ThEStrings;

// Note to fix inconsistent hierarchy: Include the COFHCore & IC2 Api's into
// build path
/**
 * Provides wireless access to networked essentia.
 *
 * @author Nividica
 *
 */
public class ItemWirelessEssentiaTerminal extends AEBasePoweredItem implements IWirelessTermHandler {

    /**
     * NBT keys
     */
    private static final String NBT_AE_SOURCE_KEY = "SourceKey";

    /**
     * Amount of power the wireless terminal can store.
     */
    private static final int POWER_STORAGE = 1600000;

    /**
     * Used during power calculations.
     */
    public static double GLOBAL_POWER_MULTIPLIER = PowerMultiplier.CONFIG.multiplier;

    /**
     * Creates the wireless terminal item.
     */
    public ItemWirelessEssentiaTerminal() {
        super(POWER_STORAGE, Optional.absent());
    }

    /**
     * Gets the encryption, or source, key for the specified terminal.
     *
     * @param wirelessTerminal
     * @return
     */
    @Override
    public String getEncryptionKey(final ItemStack wirelessTerminal) {
        // Ensure the terminal has a tag
        if (wirelessTerminal.hasTagCompound()) {
            // Get the security terminal source key
            String sourceKey = wirelessTerminal.getTagCompound()
                    .getString(ItemWirelessEssentiaTerminal.NBT_AE_SOURCE_KEY);

            // Ensure the source is not empty nor null
            if ((sourceKey != null) && (!sourceKey.isEmpty())) {
                // The terminal is linked.
                return sourceKey;
            }
        }

        // Terminal is unlinked.
        return "";
    }

    @Override
    public String getUnlocalizedName() {
        return ThEStrings.Item_WirelessEssentiaTerminal.getUnlocalized();
    }

    @Override
    public String getUnlocalizedName(final ItemStack itemStack) {
        return ThEStrings.Item_WirelessEssentiaTerminal.getUnlocalized();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean isFull3D() {
        return false;
    }

    @Override
    public ItemStack onItemRightClick(final ItemStack itemStack, final World world, final EntityPlayer player) {
        if (ForgeEventFactory.onItemUseStart(player, itemStack, 1) > 0) {
            if (AEApi.instance().registries().wireless().performCheck(itemStack, player)) {
                Platform.openGUI(player, null, null, GuiBridge.GUI_ME);
            }
        }
        return itemStack;
    }

    /**
     * Registers and sets the wireless terminal icon.
     */
    @Override
    public void registerIcons(final IIconRegister iconRegister) {
        this.itemIcon = iconRegister.registerIcon(ThaumicEnergistics.MOD_ID + ":wireless.essentia.terminal");
    }

    private NBTTagCompound getOrCreateCompoundTag(final ItemStack wirelessTerminal) {
        NBTTagCompound dataTag;

        // Ensure the terminal has a tag
        if (!wirelessTerminal.hasTagCompound()) {
            // Create a new tag.
            wirelessTerminal.setTagCompound((dataTag = new NBTTagCompound()));
        } else {
            // Get the tag
            dataTag = wirelessTerminal.getTagCompound();
        }

        return dataTag;
    }

    @Override
    public void setEncryptionKey(final ItemStack wirelessTerminal, final String sourceKey, final String name) {
        // Set the key
        this.getOrCreateCompoundTag(wirelessTerminal)
                .setString(ItemWirelessEssentiaTerminal.NBT_AE_SOURCE_KEY, sourceKey);
    }

    @Override
    public boolean showDurabilityBar(final ItemStack wirelessTerminal) {
        return true;
    }

    @Override
    public boolean canHandle(ItemStack is) {
        return is != null && is.getItem() instanceof ItemWirelessEssentiaTerminal;
    }

    @Override
    public boolean usePower(EntityPlayer player, double amount, ItemStack is) {
        return this.extractAEPower(is, amount) >= amount - 0.5;
    }

    @Override
    public boolean hasPower(EntityPlayer player, double amount, ItemStack is) {
        return this.getAECurrentPower(is) >= amount;
    }

    @Override
    public boolean hasInfinityPower(ItemStack is) {
        return false;
    }

    @Override
    public boolean hasInfinityRange(ItemStack is) {
        return false;
    }

    @Override
    public IConfigManager getConfigManager(ItemStack is) {
        final ConfigManager out = new ConfigManager((manager, settingName, newValue) -> {
            final NBTTagCompound data = Platform.openNbtData(is);
            manager.writeToNBT(data);
        });

        out.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        out.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        out.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);

        out.readFromNBT((NBTTagCompound) Platform.openNbtData(is).copy());
        return out;
    }
}
