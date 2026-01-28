package thaumicenergistics.client.gui;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;

import org.lwjgl.opengl.GL11;

import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.helpers.InventoryAction;
import thaumcraft.client.lib.UtilsFX;
import thaumicenergistics.client.gui.buttons.GuiButtonClearCraftingGrid;
import thaumicenergistics.client.gui.buttons.GuiButtonSwapArmor;
import thaumicenergistics.client.textures.GuiTextureManager;
import thaumicenergistics.common.container.ContainerPartArcaneCraftingTerminal;
import thaumicenergistics.common.container.slot.SlotArcaneCraftingResult;
import thaumicenergistics.common.network.packet.server.Packet_S_ArcaneCraftingTerminal;
import thaumicenergistics.common.parts.PartArcaneCraftingTerminal;
import thaumicenergistics.common.utils.ThEUtils;

public class GuiArcaneCraftingTerminal extends GuiMEMonitorable {

    private static final int GUI_WIDTH = 230;
    private static final int GUI_HEIGHT = 243;
    private static final int GUI_VIEW_CELL_TEXTURE_WIDTH = 35;
    private static final int GUI_MAIN_BODY_WIDTH = GUI_WIDTH - GUI_VIEW_CELL_TEXTURE_WIDTH;
    private static final int GUI_UPPER_TEXTURE_HEIGHT = 17 + 18 * 3;
    private static final int PLAYER_INVENTORY_HEIGHT = 96;
    private static final int GUI_TEXTURE_LOWER_HEIGHT = GUI_HEIGHT - GUI_UPPER_TEXTURE_HEIGHT;

    private static final int BUTTON_CLEAR_GRID_POS_X = 98;
    private static final int BUTTON_CLEAR_GRID_POS_Y = 18;
    private static final int BUTTON_SWAP_ARMOR_POS_X = 26;
    private static final int BUTTON_SWAP_ARMOR_POS_Y = 41;

    private GuiButtonClearCraftingGrid buttonClearCraftingGrid;
    private GuiButtonSwapArmor buttonSwapArmor;

    public GuiArcaneCraftingTerminal(InventoryPlayer inventoryPlayer, PartArcaneCraftingTerminal terminal) {
        super(inventoryPlayer, terminal, new ContainerPartArcaneCraftingTerminal(inventoryPlayer, terminal));

        this.setReservedSpace(GUI_TEXTURE_LOWER_HEIGHT - PLAYER_INVENTORY_HEIGHT);
    }

    private int getTopHeight() {
        return 17 + this.rows * 18;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        super.initGui();

        this.buttonClearCraftingGrid = new GuiButtonClearCraftingGrid(
                0,
                this.guiLeft + BUTTON_CLEAR_GRID_POS_X,
                this.guiTop + BUTTON_CLEAR_GRID_POS_Y + this.getTopHeight(),
                8,
                8,
                true);
        this.buttonList.add(this.buttonClearCraftingGrid);

        this.buttonSwapArmor = new GuiButtonSwapArmor(
                1,
                this.guiLeft + BUTTON_SWAP_ARMOR_POS_X,
                this.guiTop + BUTTON_SWAP_ARMOR_POS_Y + this.getTopHeight(),
                8,
                8);
        this.buttonList.add(this.buttonSwapArmor);
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.mc.renderEngine.bindTexture(GuiTextureManager.ARCANE_CRAFTING_TERMINAL.getTexture());

        final int x_width = GUI_MAIN_BODY_WIDTH;

        // Draw the upper portion: Label, Search, First row
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, x_width, 18);

        for (int i = 0; i < this.rows; i++) {
            this.drawTexturedModalRect(offsetX, offsetY + 18 + i * 18, 0, 18, x_width, 18);
        }

        // Draw the lower portion, bottom two rows, crafting grid, player inventory
        this.drawTexturedModalRect(
                this.guiLeft,
                this.guiTop + this.getTopHeight(),
                0,
                GUI_UPPER_TEXTURE_HEIGHT,
                GUI_MAIN_BODY_WIDTH,
                GUI_TEXTURE_LOWER_HEIGHT);

        // draw view cells background
        this.drawTexturedModalRect(offsetX + x_width, offsetY, x_width, 0, 32, 104);

        this.searchField.drawTextBox();

        this.updateViewCells();
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        super.drawFG(offsetX, offsetY, mouseX, mouseY);

        this.drawAspects(offsetX, offsetY);
    }

    private static final int ASPECT_COST_POS_Y = 19;
    private static final int ASPECT_COST_POS_X = 136;
    private static final int ASPECT_COST_SPACING = 18;
    private static final double ASPECT_COST_BLINK_SPEED = 0.5D;
    private static final float ASPECT_COST_MIN_ALPHA = 0.25F;
    private static final float ASPECT_COST_MAX_ALPHA = 0.75F;

    private void drawAspects(int offsetX, int offsetY) {
        List<ContainerPartArcaneCraftingTerminal.ArcaneCrafingCost> aspectCosts = ((ContainerPartArcaneCraftingTerminal) this.inventorySlots)
                .getAspectCosts();
        if (aspectCosts.isEmpty()) return;

        int posY = ASPECT_COST_POS_Y + this.getTopHeight();
        int column = 0;

        // Draw each primal
        for (ContainerPartArcaneCraftingTerminal.ArcaneCrafingCost cost : aspectCosts) {
            // Set the alpha to full
            float alpha = 1.0F;

            // Do we have enough vis for this aspect?
            if (!cost.hasEnoughVis) {
                // Ping-pong the alpha
                alpha = ThEUtils
                        .pingPongFromTime(ASPECT_COST_BLINK_SPEED, ASPECT_COST_MIN_ALPHA, ASPECT_COST_MAX_ALPHA);
            }

            // Calculate X position
            int posX = ASPECT_COST_POS_X + (column * ASPECT_COST_SPACING);

            // Draw the aspect icon
            UtilsFX.drawTag(
                    posX,
                    posY,
                    cost.primal,
                    cost.visCost,
                    0,
                    this.zLevel,
                    GL11.GL_ONE_MINUS_SRC_ALPHA,
                    alpha,
                    false);

            // Should we move to the next row?
            if (++column == 2) {
                // Reset column
                column = 0;

                // Increment Y
                posY += ASPECT_COST_SPACING;
            }
        }
    }

    @Override
    protected void handleMouseClick(Slot slot, int index, int p_146984_3_, int mouseButton) {
        if (slot instanceof SlotArcaneCraftingResult) {
            if (slot.getStack() == null) return;

            InventoryAction action;
            if (isShiftKeyDown()) {
                action = InventoryAction.CRAFT_SHIFT;
            } else {
                action = InventoryAction.CRAFT_ITEM;
            }

            final PacketInventoryAction p = new PacketInventoryAction(action, index, 0);
            NetworkHandler.instance.sendToServer(p);
            return;
        }

        super.handleMouseClick(slot, index, p_146984_3_, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton btn) {
        if (btn == this.buttonClearCraftingGrid) {
            Packet_S_ArcaneCraftingTerminal.sendClearGrid(this.mc.thePlayer);
        } else if (btn == this.buttonSwapArmor) {
            Packet_S_ArcaneCraftingTerminal.sendSwapArmor(this.mc.thePlayer);
        } else {
            super.actionPerformed(btn);
        }
    }
}
