package thaumicenergistics.common.parts;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.storage.StorageName;
import appeng.parts.automation.PartLevelEmitter;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.common.storage.AEEssentiaStack;

public class PartEssentiaLevelEmitter extends PartLevelEmitter {

    private static final String NBT_KEY_ASPECT_FILTER = "aspect";
    private static final String NBT_KEY_REDSTONE_MODE = "mode";
    private static final String NBT_KEY_WANTED_AMOUNT = "wantedAmount";

    public PartEssentiaLevelEmitter(ItemStack is) {
        super(is);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);

        if (data.hasKey(NBT_KEY_ASPECT_FILTER)) {
            this.getAEInventoryByName(StorageName.NONE).putAEStackInSlot(
                    0,
                    new AEEssentiaStack(Aspect.aspects.get(data.getString(NBT_KEY_ASPECT_FILTER))));
        }

        if (data.hasKey(NBT_KEY_REDSTONE_MODE)) {
            this.getConfigManager().putSetting(
                    Settings.REDSTONE_EMITTER,
                    data.getInteger(NBT_KEY_REDSTONE_MODE) == 1 ? RedstoneMode.LOW_SIGNAL : RedstoneMode.HIGH_SIGNAL);
        }

        if (data.hasKey(NBT_KEY_WANTED_AMOUNT)) {
            this.setReportingValue(data.getLong(NBT_KEY_WANTED_AMOUNT));
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
}
