package thaumicenergistics.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import appeng.client.gui.implementations.GuiCraftAmount;
import appeng.client.gui.widgets.GuiTabButton;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumicenergistics.api.grid.ICraftingIssuerHost;
import thaumicenergistics.common.network.packet.server.Packet_S_ConfirmCraftingJob;

/**
 * Bridges the AE2 CraftAmount GUI and the ThE API
 *
 * @author Nividica
 *
 */
@SideOnly(Side.CLIENT)
public class GuiCraftAmountBridge extends GuiCraftAmount {

    /**
     * The thing that issued the crafting request.
     */
    protected ICraftingIssuerHost host;

    /**
     * Button that returns to the terminal's GUI.
     */
    protected GuiTabButton buttonReturnToTerminalHost;

    /**
     * The player that is using the GUI.
     */
    protected EntityPlayer player;

    public GuiCraftAmountBridge(final EntityPlayer player, final ICraftingIssuerHost craftingHost) {
        // Call super
        super(player.inventory, craftingHost);

        // Set the host
        this.host = craftingHost;

        // Set the player
        this.player = player;
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        if (btn == this.buttonReturnToTerminalHost) {
            // Change back to host GUI
            this.host.launchGUI(this.player);
        } else if (btn == this.nextBtn) {
            try {
                // Parse the amount
                long amount = Long.parseLong(this.amountTextField.getText());

                // Ask server to show confirm gui
                Packet_S_ConfirmCraftingJob.sendConfirmAutoCraft(this.player, amount, isShiftKeyDown());
            } catch (final NumberFormatException e) {
                // Reset amount to 1
                this.amountTextField.setText("1");
            }
        } else {
            // Call super
            super.actionPerformed(btn);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initGui() {
        // Call super
        super.initGui();

        // Get the icon of the host
        ItemStack myIcon = this.host.getIcon();

        // Create the return button
        this.buttonReturnToTerminalHost = new GuiTabButton(
                this.guiLeft + 154,
                this.guiTop,
                myIcon,
                myIcon.getDisplayName(),
                itemRender);
        this.buttonList.add(this.buttonReturnToTerminalHost);
    }
}
