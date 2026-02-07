package thaumicenergistics.client.gui.widget;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;

import appeng.helpers.ICustomButtonDataObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import thaumicenergistics.client.gui.buttons.GuiButtonAllowVoid;
import thaumicenergistics.common.network.NetworkHandler;
import thaumicenergistics.common.network.packet.server.Packet_S_ExportBusVoidButtonUpdate;

public class ExportBusVoidButtonObject implements ICustomButtonDataObject {

    private static final int ALLOW_VOID_BUTTON_ID = 1;

    private boolean isVoidAllowed;
    private GuiButtonAllowVoid voidModeButton;

    @SideOnly(Side.CLIENT)
    @Override
    public void initCustomButtons(int guiLeft, int guiTop, int xSize, int ySize, int xOffset, int yOffset,
            List<GuiButton> buttonList) {
        this.voidModeButton = new GuiButtonAllowVoid(ALLOW_VOID_BUTTON_ID, guiLeft - 19, guiTop + yOffset);
        buttonList.add(this.voidModeButton);

        Packet_S_ExportBusVoidButtonUpdate packet = new Packet_S_ExportBusVoidButtonUpdate(true);
        packet.player = Minecraft.getMinecraft().thePlayer;
        NetworkHandler.sendPacketToServer(packet);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean actionPerformedCustomButtons(GuiButton btn) {
        if (btn == voidModeButton) {
            Packet_S_ExportBusVoidButtonUpdate packet = new Packet_S_ExportBusVoidButtonUpdate(false);
            packet.player = Minecraft.getMinecraft().thePlayer;
            NetworkHandler.sendPacketToServer(packet);
            return true;
        }
        return false;
    }

    @Override
    public void readData(NBTTagCompound tag) {

    }

    @Override
    public void writeData(NBTTagCompound tag) {

    }

    @Override
    public void writeByte(ByteBuf buf) {
        buf.writeBoolean(this.isVoidAllowed);
    }

    @Override
    public void readByte(ByteBuf buf) {
        this.voidModeButton.setIsVoidAllowed(buf.readBoolean());
    }

    public void setVoidAllowed(boolean isVoidAllowed) {
        this.isVoidAllowed = isVoidAllowed;
    }
}
