package thaumicenergistics.common.network.packet.server;

import net.minecraft.entity.player.EntityPlayer;

import appeng.api.storage.data.IAEItemStack;
import io.netty.buffer.ByteBuf;
import thaumicenergistics.common.container.ContainerPartArcaneCraftingTerminal;
import thaumicenergistics.common.network.NetworkHandler;
import thaumicenergistics.common.network.ThEBasePacket;
import thaumicenergistics.common.parts.PartArcaneCraftingTerminal;

/**
 * {@link PartArcaneCraftingTerminal} server-bound packet.
 *
 * @author Nividica
 *
 */
public class Packet_S_ArcaneCraftingTerminal extends ThEServerPacket {

    private static final byte MODE_REQUEST_CLEAR_GRID = 4;
    private static final byte MODE_REQUEST_SET_GRID = 7;
    private static final byte MODE_REQUEST_SWAP_ARMOR = 9;

    private static final int ITEM_GRID_SIZE = 9;

    /**
     * Items to set the crafting grid to.
     */
    private IAEItemStack[] gridItems;

    /**
     * Creates the packet
     *
     * @param player
     * @param mode
     * @return
     */
    private static Packet_S_ArcaneCraftingTerminal newPacket(final EntityPlayer player, final byte mode) {
        // Create the packet
        Packet_S_ArcaneCraftingTerminal packet = new Packet_S_ArcaneCraftingTerminal();

        // Set the player & mode
        packet.player = player;
        packet.mode = mode;

        return packet;
    }

    /**
     * Create a packet to request that the crafting grid be cleared. the ME network. Use only when needed.
     *
     * @param player
     */
    public static void sendClearGrid(final EntityPlayer player) {
        Packet_S_ArcaneCraftingTerminal packet = newPacket(player, MODE_REQUEST_CLEAR_GRID);

        // Send it
        NetworkHandler.sendPacketToServer(packet);
    }

    /**
     * Create a packet to request that the crafting grid be set to these items.
     *
     * @param player
     * @param items  Must be at least length of 9
     * @return
     */
    public static void sendSetCrafting_NEI(final EntityPlayer player, final IAEItemStack[] items) {
        Packet_S_ArcaneCraftingTerminal packet = newPacket(player, MODE_REQUEST_SET_GRID);

        // Set the items
        packet.gridItems = items;

        // Send it
        NetworkHandler.sendPacketToServer(packet);
    }

    public static void sendSwapArmor(final EntityPlayer player) {
        Packet_S_ArcaneCraftingTerminal packet = newPacket(player, MODE_REQUEST_SWAP_ARMOR);

        // Send it
        NetworkHandler.sendPacketToServer(packet);
    }

    @Override
    public void execute() {
        if (this.player.openContainer instanceof ContainerPartArcaneCraftingTerminal container) {
            switch (this.mode) {
                case MODE_REQUEST_CLEAR_GRID -> container.clearCraftingGrid();
                case MODE_REQUEST_SWAP_ARMOR -> container.swapArmor(this.player);
            }
        }
    }

    @Override
    public void readData(final ByteBuf stream) {
        if (this.mode == Packet_S_ArcaneCraftingTerminal.MODE_REQUEST_SET_GRID) {// Init the items
            this.gridItems = new IAEItemStack[Packet_S_ArcaneCraftingTerminal.ITEM_GRID_SIZE];

            // Read the items
            for (int slotIndex = 0; slotIndex < 9; slotIndex++) {
                // Do we have an item to read?
                if (stream.readBoolean()) {
                    // Set the item
                    this.gridItems[slotIndex] = ThEBasePacket.readAEItemStack(stream);
                }
            }
        }
    }

    @Override
    public void writeData(final ByteBuf stream) {
        if (this.mode == Packet_S_ArcaneCraftingTerminal.MODE_REQUEST_SET_GRID) {// Write each non-null item
            for (int slotIndex = 0; slotIndex < Packet_S_ArcaneCraftingTerminal.ITEM_GRID_SIZE; slotIndex++) {
                // Get the item
                IAEItemStack slotItem = this.gridItems[slotIndex];

                // Write if the slot is not null
                boolean hasItem = slotItem != null;
                stream.writeBoolean(hasItem);

                if (hasItem) {
                    // Write the item
                    ThEBasePacket.writeAEItemStack(slotItem, stream);
                }
            }
        }
    }
}
