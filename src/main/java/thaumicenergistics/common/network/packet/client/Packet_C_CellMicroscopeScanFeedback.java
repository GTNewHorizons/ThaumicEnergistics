package thaumicenergistics.common.network.packet.client;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import thaumcraft.api.research.ScanResult;
import thaumcraft.common.lib.research.ScanManager;
import thaumicenergistics.common.network.NetworkHandler;

public class Packet_C_CellMicroscopeScanFeedback extends ThEClientPacket {

    private int itemId;
    private int itemDamage;

    public static void sendScanFeedback(final EntityPlayer player, final ScanResult scanResult) {
        Packet_C_CellMicroscopeScanFeedback packet = new Packet_C_CellMicroscopeScanFeedback();

        packet.player = player;
        packet.itemId = scanResult.id;
        packet.itemDamage = scanResult.meta;

        NetworkHandler.sendPacketToClient(packet);
    }

    @Override
    protected void readData(final ByteBuf stream) {
        this.itemId = stream.readInt();
        this.itemDamage = stream.readInt();
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected void wrappedExecute() {
        ScanResult scanResult = new ScanResult((byte) 1, this.itemId, this.itemDamage, null, "");
        if (Item.getItemById(this.itemId) != null) {
            ScanManager.completeScan(this.player, scanResult, "@");
        }
    }

    @Override
    protected void writeData(final ByteBuf stream) {
        stream.writeInt(this.itemId);
        stream.writeInt(this.itemDamage);
    }
}
