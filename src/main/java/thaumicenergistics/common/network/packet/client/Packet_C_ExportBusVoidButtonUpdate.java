package thaumicenergistics.common.network.packet.client;

import appeng.container.implementations.ContainerBusIO;
import appeng.helpers.ICustomButtonDataObject;
import io.netty.buffer.ByteBuf;
import thaumicenergistics.client.gui.widget.ExportBusVoidButtonObject;
import thaumicenergistics.common.parts.PartEssentiaExportBus;

public class Packet_C_ExportBusVoidButtonUpdate extends ThEClientPacket {

    private ICustomButtonDataObject dataObject;
    private ByteBuf buf;

    public Packet_C_ExportBusVoidButtonUpdate() {}

    public Packet_C_ExportBusVoidButtonUpdate(ICustomButtonDataObject dataObject) {
        this.dataObject = dataObject;
    }

    @Override
    protected void readData(ByteBuf stream) {
        this.buf = stream;
    }

    @Override
    protected void writeData(ByteBuf stream) {
        this.dataObject.writeByte(stream);
    }

    @Override
    protected void wrappedExecute() {
        if (this.player.openContainer instanceof ContainerBusIO container
                && container.getUpgradeable() instanceof PartEssentiaExportBus bus) {
            ExportBusVoidButtonObject object = (ExportBusVoidButtonObject) bus.getDataObject();
            object.readByte(this.buf);
        }
    }
}
