package thaumicenergistics.implementaion;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;

import appeng.api.AEApi;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumicenergistics.api.IThEInteractionHelper;
import thaumicenergistics.api.entities.IGolemHookHandler;
import thaumicenergistics.common.container.ContainerPartArcaneCraftingTerminal;
import thaumicenergistics.common.integration.tc.GolemHooks;
import thaumicenergistics.common.network.packet.server.Packet_S_ArcaneCraftingTerminal;
import thaumicenergistics.common.utils.ThELog;

/**
 * Implements {@link IThEInteractionHelper}
 */
public class ThEInteractionHelper implements IThEInteractionHelper {

    @Override
    public void registerGolemHookHandler(final IGolemHookHandler handler) {
        try {
            GolemHooks.registerHandler(handler);
        } catch (Exception e) {
            ThELog.warning("Caught Exception During API call to registerGolemHookHandler");
            ThELog.warning(e.toString());
            e.printStackTrace();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setArcaneCraftingTerminalRecipe(final ItemStack[] itemsVanilla) {
        try {
            // Get the player
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;

            // Is the player looking at an ACT?
            if (!(player.openContainer instanceof ContainerPartArcaneCraftingTerminal)) {
                return;
            }

            boolean hasItems = false;

            // Ensure the input items array is the correct size
            if ((itemsVanilla == null) || (itemsVanilla.length != 9)) {
                return;
            }

            // Create the AE items array
            IAEItemStack[] items = new IAEItemStack[9];

            // Get the items and convert them to their AE counterparts.
            for (int slotIndex = 0; slotIndex < 9; ++slotIndex) {
                if (itemsVanilla[slotIndex] != null) {
                    items[slotIndex] = AEApi.instance().storage().createItemStack(itemsVanilla[slotIndex]);
                    hasItems = true;
                }
            }

            // Send the list to the server
            if (hasItems) {
                Packet_S_ArcaneCraftingTerminal.sendSetCrafting_NEI(player, items);
            }
        } catch (Exception e) {
            ThELog.warning("Caught Exception During API call to setArcaneCraftingTerminalRecipe");
            ThELog.warning(e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public void openWirelessTerminalGui(final EntityPlayer player) {
        // Valid player?
        if ((player == null) || (player instanceof FakePlayer)) {
            return;
        }

        // Ignored client side
        if (Platform.isClient()) {
            return;
        }

        // Get the item the player is holding.
        ItemStack wirelessTerminal = player.getHeldItem();

        AEApi.instance().registries().wireless().openWirelessTerminalGui(wirelessTerminal, player.worldObj, player);
    }
}
