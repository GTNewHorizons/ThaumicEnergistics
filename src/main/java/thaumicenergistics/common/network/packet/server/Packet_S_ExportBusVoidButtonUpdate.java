package thaumicenergistics.common.network.packet.server;

import appeng.container.implementations.ContainerBusIO;
import io.netty.buffer.ByteBuf;
import thaumicenergistics.client.gui.widget.ExportBusVoidButtonObject;
import thaumicenergistics.common.network.NetworkHandler;
import thaumicenergistics.common.network.packet.client.Packet_C_ExportBusVoidButtonUpdate;
import thaumicenergistics.common.parts.PartEssentiaExportBus;

public class Packet_S_ExportBusVoidButtonUpdate extends ThEServerPacket {

    private boolean onlySync;

    public Packet_S_ExportBusVoidButtonUpdate() {}

    public Packet_S_ExportBusVoidButtonUpdate(boolean onlySync) {
        this.onlySync = onlySync;
    }

    @Override
    protected void readData(ByteBuf stream) {
        this.onlySync = stream.readBoolean();
    }

    @Override
    protected void writeData(ByteBuf buf) {
        buf.writeBoolean(this.onlySync);
    }

    @Override
    public void execute() {
        if (this.player.openContainer instanceof ContainerBusIO container
                && container.getUpgradeable() instanceof PartEssentiaExportBus bus) {
            ExportBusVoidButtonObject object = (ExportBusVoidButtonObject) bus.getDataObject();
            if (!this.onlySync) {
                bus.toggleVoidAllowed();
            }
            object.setVoidAllowed(bus.getVoidAllowed());

            Packet_C_ExportBusVoidButtonUpdate packet = new Packet_C_ExportBusVoidButtonUpdate(object);
            packet.player = this.player;
            NetworkHandler.sendPacketToClient(packet);
        }
    }
}
