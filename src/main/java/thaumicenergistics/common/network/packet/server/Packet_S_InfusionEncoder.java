package thaumicenergistics.common.network.packet.server;

import net.minecraft.entity.player.EntityPlayer;

import io.netty.buffer.ByteBuf;
import thaumicenergistics.common.container.ContainerInfusionEncoder;
import thaumicenergistics.common.network.NetworkHandler;
import thaumicenergistics.common.tiles.TileInfusionPatternEncoder;

/**
 * {@link TileInfusionPatternEncoder} server-bound packet.
 *
 * @author MCTBL
 *
 */
public class Packet_S_InfusionEncoder extends ThEServerPacket {

    private static final byte MODE_ENCODE = 1;
    private static final byte MODE_RESET_SLOT = 2;
    private static final byte MODE_SCORLL_BAR_ROLL = 3;

    private int currentPage;
    private boolean isShift;

    public static void sendEncodePattern(final EntityPlayer player, final boolean isShift) {
        // Create a new packet
        Packet_S_InfusionEncoder packet = new Packet_S_InfusionEncoder();

        // Set the player
        packet.player = player;

        // Set the mode
        packet.mode = MODE_ENCODE;

        packet.isShift = isShift;

        // Send it
        NetworkHandler.sendPacketToServer(packet);
    }

    public static void sendResetSlot(final EntityPlayer player) {
        // Create a new packet
        Packet_S_InfusionEncoder packet = new Packet_S_InfusionEncoder();

        // Set the player
        packet.player = player;

        // Set the mode
        packet.mode = MODE_RESET_SLOT;

        // Send it
        NetworkHandler.sendPacketToServer(packet);
    }

    public static void sendRollScorllBar(final EntityPlayer player, final int currentPage) {
        // Create a new packet
        Packet_S_InfusionEncoder packet = new Packet_S_InfusionEncoder();

        // Set the player
        packet.player = player;

        // Set the mode
        packet.mode = MODE_SCORLL_BAR_ROLL;

        packet.currentPage = currentPage;

        // Send it
        NetworkHandler.sendPacketToServer(packet);
    }

    @Override
    protected void readData(final ByteBuf stream) {
        if (this.mode == MODE_SCORLL_BAR_ROLL) {
            this.currentPage = stream.readInt();
        } else if (this.mode == MODE_ENCODE) {
            this.isShift = stream.readBoolean();
        }
    }

    @Override
    protected void writeData(final ByteBuf stream) {
        if (this.mode == MODE_SCORLL_BAR_ROLL) {
            stream.writeInt(this.currentPage);
        } else if (this.mode == MODE_ENCODE) {
            stream.writeBoolean(this.isShift);
        }
    }

    @Override
    public void execute() {
        // Get the players open container
        if (this.player.openContainer instanceof ContainerInfusionEncoder container) {
            // Sanity check
            if (this.mode == MODE_ENCODE) {
                // Send the encode
                container.encodePattern(this.isShift);
            } else if (this.mode == MODE_RESET_SLOT) {
                // Send the reset
                container.clearSlots();
            } else if (this.mode == MODE_SCORLL_BAR_ROLL) {
                // Rolling
                container.setActivePage(this.currentPage);
            }
        }
    }

}
