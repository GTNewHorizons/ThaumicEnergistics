package thaumicenergistics.common.parts;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.parts.reporting.PartTerminal;

/**
 * Allows a player to extract/deposit essentia from the network.
 *
 * @author Nividica
 *
 */
public class PartEssentiaTerminal extends PartTerminal {

    public PartEssentiaTerminal(ItemStack is) {
        super(is);
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
