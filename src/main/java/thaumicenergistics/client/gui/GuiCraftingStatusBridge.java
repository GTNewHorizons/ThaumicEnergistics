package thaumicenergistics.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import appeng.client.gui.implementations.GuiCraftingStatus;
import appeng.client.gui.widgets.GuiTabButton;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumicenergistics.api.grid.ICraftingIssuerHost;

@SideOnly(Side.CLIENT)
public class GuiCraftingStatusBridge extends GuiCraftingStatus {

    private GuiTabButton btnOriginalGui;

    /**
     * Player using this GUI
     */
    protected EntityPlayer player;

    /**
     * The thing that issued the crafting request.
     */
    protected ICraftingIssuerHost host;

    public GuiCraftingStatusBridge(final EntityPlayer player, final ICraftingIssuerHost craftingHost) {
        // Call super
        super(player.inventory, craftingHost);

        // Set the player
        this.player = player;

        // Set the host
        this.host = craftingHost;
    }

    @Override
    public void initGui() {
        super.initGui();

        ItemStack icon = host.getIcon();
        this.buttonList.add(
                this.btnOriginalGui = new GuiTabButton(
                        this.guiLeft + 213,
                        this.guiTop - 4,
                        icon,
                        icon.getDisplayName(),
                        itemRender));
        this.btnOriginalGui.setHideEdge(13);
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        super.actionPerformed(btn);
        if (btn == this.btnOriginalGui) {
            host.launchGUI(player);
        }
    }
}
