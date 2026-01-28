package thaumicenergistics.common.network.packet.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import io.netty.buffer.ByteBuf;
import thaumicenergistics.client.gui.GuiDistillationPatternEncoder;
import thaumicenergistics.common.network.NetworkHandler;

public class Packet_C_DistillationEncoder extends ThEClientPacket {

    private static final byte MODE_CHANGE_SRC = 1;
    private static final byte MODE_UNKNOWN_ASPECT = 2;

    public static void sendChangeSrcItem(final EntityPlayer player) {
        Packet_C_DistillationEncoder packet = new Packet_C_DistillationEncoder();

        packet.player = player;
        packet.mode = MODE_CHANGE_SRC;

        NetworkHandler.sendPacketToClient(packet);
    }

    public static void sendUnknownAspect(final EntityPlayer player) {
        Packet_C_DistillationEncoder packet = new Packet_C_DistillationEncoder();

        packet.player = player;
        packet.mode = MODE_UNKNOWN_ASPECT;

        NetworkHandler.sendPacketToClient(packet);
    }

    @Override
    protected void readData(ByteBuf stream) {}

    @Override
    protected void writeData(ByteBuf stream) {}

    @Override
    protected void wrappedExecute() {
        if (Minecraft.getMinecraft().currentScreen instanceof GuiDistillationPatternEncoder gui) {
            switch (mode) {
                case MODE_CHANGE_SRC -> gui.setChangedSrcItem();
                case MODE_UNKNOWN_ASPECT -> gui.setUnknownAspect();
            }
        }
    }
}
