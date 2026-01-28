package thaumicenergistics.common.network.packet.client;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import thaumicenergistics.common.network.NetworkHandler;

public class Packet_C_UpdatePlayerArmor extends ThEClientPacket {

    private final ItemStack[] armors = new ItemStack[4];

    public static void send(final EntityPlayer player, IInventory armorInventory) {
        // Create the packet
        Packet_C_UpdatePlayerArmor packet = new Packet_C_UpdatePlayerArmor();

        // Set the player & mode
        packet.player = player;

        for (int i = 0; i < 4; i++) {
            packet.armors[i] = armorInventory.getStackInSlot(i);
        }

        NetworkHandler.sendPacketToClient(packet);
    }

    @Override
    protected void wrappedExecute() {
        for (int i = 0; i < 4; i++) {
            this.player.inventory.setInventorySlotContents(36 + (3 - i), armors[i]);
        }
    }

    @Override
    protected void readData(ByteBuf stream) {
        for (int i = 0; i < 4; i++) {
            this.armors[i] = ByteBufUtils.readItemStack(stream);
        }
    }

    @Override
    protected void writeData(ByteBuf stream) {
        for (int i = 0; i < 4; i++) {
            ByteBufUtils.writeItemStack(stream, this.armors[i]);
        }
    }
}
