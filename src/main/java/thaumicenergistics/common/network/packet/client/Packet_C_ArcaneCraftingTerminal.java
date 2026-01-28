package thaumicenergistics.common.network.packet.client;

import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import thaumicenergistics.common.container.ContainerPartArcaneCraftingTerminal;
import thaumicenergistics.common.network.NetworkHandler;
import thaumicenergistics.common.parts.PartArcaneCraftingTerminal;

/**
 * {@link PartArcaneCraftingTerminal} client-bound packet.
 *
 * @author Nividica
 *
 */
public class Packet_C_ArcaneCraftingTerminal extends ThEClientPacket {

    private static Packet_C_ArcaneCraftingTerminal newPacket(final EntityPlayer player) {
        // Create the packet
        Packet_C_ArcaneCraftingTerminal packet = new Packet_C_ArcaneCraftingTerminal();

        // Set the player
        packet.player = player;

        return packet;
    }

    /**
     * Forces the client to re-calculate the displayed aspect costs
     */
    public static void updateAspectCost(final EntityPlayer player) {
        // Create the packet
        Packet_C_ArcaneCraftingTerminal packet = newPacket(player);

        // Send it
        NetworkHandler.sendPacketToClient(packet);
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected void wrappedExecute() {
        if (this.player.openContainer instanceof ContainerPartArcaneCraftingTerminal container) {
            container.updateVisCost();
        }
    }

    @Override
    public void readData(final ByteBuf stream) {}

    @Override
    public void writeData(final ByteBuf stream) {}
}
