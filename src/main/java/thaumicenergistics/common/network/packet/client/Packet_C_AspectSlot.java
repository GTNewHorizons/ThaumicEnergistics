package thaumicenergistics.common.network.packet.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.gui.IAspectSlotGui;
import thaumicenergistics.client.gui.GuiEssentiaCellWorkbench;
import thaumicenergistics.common.container.ContainerEssentiaCellWorkbench;
import thaumicenergistics.common.network.NetworkHandler;
import thaumicenergistics.common.network.ThEBasePacket;

/**
 * Aspect slot client-bound packet.
 *
 * @author Nividica
 *
 */
public class Packet_C_AspectSlot extends ThEClientPacket {

    private static final byte MODE_LIST_UPDATE = 0;

    private List<Aspect> filterAspects;

    private static final byte MODE_SLOTS_UPDATE = 1;

    private ItemStack slotStack;

    public static void setFilterList(final List<Aspect> filterAspects, final EntityPlayer player) {
        Packet_C_AspectSlot packet = new Packet_C_AspectSlot();

        // Set the player
        packet.player = player;

        // Set the mode
        packet.mode = Packet_C_AspectSlot.MODE_LIST_UPDATE;

        // Mark to use compression
        packet.useCompression = true;

        // Set the list
        packet.filterAspects = filterAspects;

        // Send it
        NetworkHandler.sendPacketToClient(packet);
    }

    public static void setUpgradeSlots(final ItemStack stack, final EntityPlayer player) {
        Packet_C_AspectSlot packet = new Packet_C_AspectSlot();

        // Set the player
        packet.player = player;

        // Set the mode
        packet.mode = Packet_C_AspectSlot.MODE_SLOTS_UPDATE;

        packet.slotStack = stack;

        // Send it
        NetworkHandler.sendPacketToClient(packet);
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected void wrappedExecute() {
        // Get the gui
        Gui gui = Minecraft.getMinecraft().currentScreen;

        // Ensure it is an IAspectSlotGui
        if (!(gui instanceof IAspectSlotGui)) {
            return;
        }

        switch (this.mode) {
            case Packet_C_AspectSlot.MODE_LIST_UPDATE:
                ((IAspectSlotGui) gui).updateAspects(this.filterAspects);
                break;
            case Packet_C_AspectSlot.MODE_SLOTS_UPDATE:
                ((ContainerEssentiaCellWorkbench) ((GuiEssentiaCellWorkbench) gui).inventorySlots)
                        .createUpgradeSlots(this.slotStack);
                break;
        }
    }

    @Override
    public void readData(final ByteBuf stream) {
        switch (this.mode) {
            case Packet_C_AspectSlot.MODE_LIST_UPDATE:
                // Read the size
                int size = stream.readInt();

                // Create the list
                this.filterAspects = new ArrayList<Aspect>(size);

                // Read each aspect
                for (int i = 0; i < size; i++) {
                    this.filterAspects.add(ThEBasePacket.readAspect(stream));
                }
                break;
            case Packet_C_AspectSlot.MODE_SLOTS_UPDATE:
                this.slotStack = ThEBasePacket.readItemstack(stream);
                break;
        }
    }

    @Override
    public void writeData(final ByteBuf stream) {
        switch (this.mode) {
            case Packet_C_AspectSlot.MODE_LIST_UPDATE:
                // Write the size of the list
                stream.writeInt(this.filterAspects.size());

                // Write each aspect
                for (int index = 0; index < this.filterAspects.size(); index++) {
                    ThEBasePacket.writeAspect(this.filterAspects.get(index), stream);
                }
                break;
            case Packet_C_AspectSlot.MODE_SLOTS_UPDATE:
                // Write each aspect
                ThEBasePacket.writeItemstack(this.slotStack, stream);
                break;
        }
    }
}
