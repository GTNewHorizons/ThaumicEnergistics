package thaumicenergistics.common.network.packet.server;

import net.minecraft.client.Minecraft;

import io.netty.buffer.ByteBuf;
import thaumicenergistics.common.container.ContainerDistillationPatternEncoder;
import thaumicenergistics.common.network.NetworkHandler;
import thaumicenergistics.common.tiles.TileDistillationPatternEncoder;

/**
 * {@link TileDistillationPatternEncoder} server-bound packet.
 *
 * @author Nividica
 *
 */
public class Packet_S_DistillationEncoder extends ThEServerPacket {

    private static final byte MODE_ENCODE = 1;
    private static final byte MODE_RESETASPECT = 2;

    public static void sendEncodePattern() {
        // Create a new packet
        Packet_S_DistillationEncoder packet = new Packet_S_DistillationEncoder();

        // Set the player
        packet.player = Minecraft.getMinecraft().thePlayer;

        // Set the mode
        packet.mode = MODE_ENCODE;

        // Send it
        NetworkHandler.sendPacketToServer(packet);
    }

    public static void sendResetAspect() {
        // Create a new packet
        Packet_S_DistillationEncoder packet = new Packet_S_DistillationEncoder();

        // Set the player
        packet.player = Minecraft.getMinecraft().thePlayer;

        // Set the mode
        packet.mode = MODE_RESETASPECT;

        // Send it
        NetworkHandler.sendPacketToServer(packet);
    }

    @Override
    protected void readData(final ByteBuf stream) {}

    @Override
    protected void writeData(final ByteBuf stream) {}

    @Override
    public void execute() {
        // Sanity check
        if (this.mode == MODE_ENCODE) {
            // Get the players open container
            if (this.player.openContainer instanceof ContainerDistillationPatternEncoder container) {
                // Send the encode
                container.encodePattern(this.player);
            }
        } else if (this.mode == MODE_RESETASPECT) {
            // Get the players open container
            if (this.player.openContainer instanceof ContainerDistillationPatternEncoder container) {
                // Send the reset
                container.scanSourceItem();
            }
        }

    }
}
