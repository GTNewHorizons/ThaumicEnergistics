package thaumicenergistics.common.parts;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.parts.reporting.PartStorageMonitor;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.common.storage.AEEssentiaStack;

public class PartEssentiaStorageMonitor extends PartStorageMonitor {

    private static final String NBT_KEY_LOCKED = "Locked", NBT_KEY_TRACKED_ASPECT = "TrackedAspect";
    private static final String NBT_KEY_OWNER = "Owner";

    public PartEssentiaStorageMonitor(ItemStack is) {
        super(is);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        // Migrate old tags
        if (data.hasKey(NBT_KEY_TRACKED_ASPECT)) {
            Aspect trackedAspect = Aspect.getAspect(data.getString(NBT_KEY_TRACKED_ASPECT));
            data.setTag("configuredItem", (new AEEssentiaStack(trackedAspect)).toNBTGeneric());
        }
        if (data.hasKey(NBT_KEY_LOCKED)) {
            data.setBoolean("isLocked", data.getBoolean(NBT_KEY_LOCKED));
        }

        super.readFromNBT(data);
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
}
