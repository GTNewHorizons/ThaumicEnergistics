package thaumicenergistics.client.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.container.PrimaryGui;
import appeng.helpers.IPrimaryGuiIconProvider;
import appeng.parts.AEBasePart;
import thaumicenergistics.common.ThEGuiHandler;

public class ThEPrimaryGui extends PrimaryGui {

    AEBasePart part;

    public ThEPrimaryGui(AEBasePart part) {
        super(
                null,
                part instanceof IPrimaryGuiIconProvider provider ? provider.getPrimaryGuiIcon() : null,
                null,
                ForgeDirection.UNKNOWN);
        this.part = part;
    }

    @Override
    public void open(EntityPlayer player) {

        if (this.part != null) {
            TileEntity tile = this.part.getTile();
            ThEGuiHandler.launchGui(this.part, player, tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord);
        }
    }
}
