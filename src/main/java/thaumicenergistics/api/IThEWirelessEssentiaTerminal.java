package thaumicenergistics.api;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.api.features.INetworkEncodable;
import appeng.api.implementations.items.IAEItemPowerStorage;

@Deprecated
public interface IThEWirelessEssentiaTerminal extends INetworkEncodable, IAEItemPowerStorage {

    @Deprecated
    @Nonnull
    NBTTagCompound getWETerminalTag(@Nonnull ItemStack terminalItemstack);
}
