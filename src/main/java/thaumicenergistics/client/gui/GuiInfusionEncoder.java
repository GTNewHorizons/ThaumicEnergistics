package thaumicenergistics.client.gui;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.world.World;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import appeng.container.guisync.GuiSync;
import appeng.core.localization.ButtonToolTips;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumicenergistics.api.storage.IInventoryUpdateReceiver;
import thaumicenergistics.client.gui.abstraction.ThEBaseGui;
import thaumicenergistics.client.gui.buttons.GuiButtonClearCraftingGrid;
import thaumicenergistics.client.gui.buttons.GuiButtonEncodePattern;
import thaumicenergistics.client.gui.widget.ThEScrollbar;
import thaumicenergistics.client.textures.AEStateIconsEnum;
import thaumicenergistics.client.textures.GuiTextureManager;
import thaumicenergistics.common.blocks.BlockInfusionEncoder;
import thaumicenergistics.common.container.ContainerInfusionEncoder;
import thaumicenergistics.common.network.packet.server.Packet_S_InfusionEncoder;
import thaumicenergistics.common.registries.ThEStrings;

/**
 * {@link BlockInfusionEncoder}'s GUI
 * 
 * @author MCTBL
 */
@SideOnly(Side.CLIENT)
public class GuiInfusionEncoder extends ThEBaseGui implements IInventoryUpdateReceiver {

    /**
     * Gui size.
     */
    private static final int GUI_WIDTH = 176, GUI_HEIGHT = 234;

    /**
     * Position of the title string.
     */
    private static final int TITLE_POS_X = 6, TITLE_POS_Y = 6;

    /**
     * Position of the scroll bar.
     */
    private static final int SCROLLBAR_HEIGHT = 106, SCROLLBAR_WIDTH = 7, SCROLLBAR_X = 121, SCROLLBAR_Y = 25;
    private static final int SCROLLBAR_X_BIAS = 242, SCROLLBAR_Y_BIAS = 0;

    /**
     * Position of the fake slots.
     */
    private static final int FIRST_FAKE_SLOT_X = 10, FIRST_FAKE_SLOT_Y = 25;

    /*
     * Position of the buttons.
     */
    private static final int RESET_BUTTON_X = 132, RESET_BUTTON_Y = 26;
    private static final int ENCODE_BUTTON_X = 153, ENCODE_BUTTON_Y = 71;

    /**
     * Title of the gui.
     */
    private final String title;

    private final ThEScrollbar scrollBar = new ThEScrollbar(
            GuiTextureManager.INFUSION_ENCODER.getTexture(),
            GuiInfusionEncoder.SCROLLBAR_X_BIAS,
            GuiInfusionEncoder.SCROLLBAR_Y_BIAS);

    /**
     * The encode button.
     */
    private GuiButtonEncodePattern buttonEncode;

    /**
     * The reset button.
     */
    private GuiButtonClearCraftingGrid buttonReset;

    /**
     * The GUI's container.
     */
    private final ContainerInfusionEncoder deContainer;

    @GuiSync(1)
    public int activePage;

    public GuiInfusionEncoder(final EntityPlayer player, final World world, final int x, final int y, final int z) {
        super(new ContainerInfusionEncoder(player, world, x, y, z));
        this.title = ThEStrings.Block_InfusionEncoder.getLocalized();

        // Set the GUI size
        this.xSize = GuiInfusionEncoder.GUI_WIDTH;
        this.ySize = GuiInfusionEncoder.GUI_HEIGHT;

        // Init the scroll bar
        scrollBar.setHeight(GuiInfusionEncoder.SCROLLBAR_HEIGHT).setWidth(GuiInfusionEncoder.SCROLLBAR_WIDTH);
        scrollBar.setLeft(GuiInfusionEncoder.SCROLLBAR_X).setTop(GuiInfusionEncoder.SCROLLBAR_Y);
        scrollBar.setRange(0, ContainerInfusionEncoder.PAGE_COUNT - 1, 1);
        this.activePage = this.scrollBar.getCurrentScroll();

        // get container
        this.deContainer = (ContainerInfusionEncoder) this.inventorySlots;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        // Full white
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        // Bind the encoder gui texture
        Minecraft.getMinecraft().renderEngine.bindTexture(GuiTextureManager.INFUSION_ENCODER.getTexture());

        // Draw the gui texture
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);

    }

    @Override
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY) {
        // Draw the title
        this.fontRendererObj.drawString(this.title, GuiInfusionEncoder.TITLE_POS_X, GuiInfusionEncoder.TITLE_POS_Y, 0);

        this.scrollBar.draw(this);

    }

    @SuppressWarnings("unchecked")
    @Override
    public void initGui() {
        super.initGui();

        // Create the encode button
        this.buttonEncode = new GuiButtonEncodePattern(
                0,
                GuiInfusionEncoder.ENCODE_BUTTON_X + this.guiLeft,
                GuiInfusionEncoder.ENCODE_BUTTON_Y + this.guiTop,
                AEStateIconsEnum.STANDARD_ICON_SIZE,
                AEStateIconsEnum.STANDARD_ICON_SIZE);
        this.buttonList.add(this.buttonEncode);

        // Create the reset aspect button
        this.buttonReset = new GuiButtonClearCraftingGrid(
                1,
                GuiInfusionEncoder.RESET_BUTTON_X + this.guiLeft,
                GuiInfusionEncoder.RESET_BUTTON_Y + this.guiTop,
                AEStateIconsEnum.STANDARD_ICON_SIZE,
                AEStateIconsEnum.STANDARD_ICON_SIZE,
                true) {

            @Override
            public void getTooltip(final List<String> tooltip) {
                this.addAboutToTooltip(tooltip, ButtonToolTips.Clear.getLocal(), "");
            }
        };
        this.buttonList.add(this.buttonReset);
    }

    @Override
    protected void mouseClicked(final int xCoord, final int yCoord, final int btn) {
        final int currentScroll = this.scrollBar.getCurrentScroll();
        this.scrollBar.click(this, xCoord - this.guiLeft, yCoord - this.guiTop);
        super.mouseClicked(xCoord, yCoord, btn);

        if (currentScroll != this.scrollBar.getCurrentScroll()) {
            changeActivePage();
        }
    }

    @Override
    protected void mouseClickMove(final int x, final int y, final int c, final long d) {
        final int currentScroll = this.scrollBar.getCurrentScroll();
        this.scrollBar.click(this, x - this.guiLeft, y - this.guiTop);
        super.mouseClickMove(x, y, c, d);

        if (currentScroll != this.scrollBar.getCurrentScroll()) {
            changeActivePage();
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        final int wheel = Mouse.getEventDWheel();

        if (wheel != 0) {
            final int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
            final int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight;
            if (this.scrollBar.contains(x - this.guiLeft, y - this.guiTop)
                    || (this.isMouseInFakeSlot(x - this.guiLeft, y - this.guiTop))) {
                final int currentScroll = this.scrollBar.getCurrentScroll();
                this.scrollBar.wheel(wheel);

                if (currentScroll != this.scrollBar.getCurrentScroll()) {
                    changeActivePage();
                }
            }
        }
    }

    private boolean isMouseInFakeSlot(final int x, final int y) {
        if (x > GuiInfusionEncoder.FIRST_FAKE_SLOT_X && x < GuiInfusionEncoder.FIRST_FAKE_SLOT_X + 18 * 6
                && y > GuiInfusionEncoder.FIRST_FAKE_SLOT_Y
                && y < GuiInfusionEncoder.FIRST_FAKE_SLOT_Y + 18 * 6) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onButtonClicked(final GuiButton button, final int mouseButton) {
        // Which button
        if (button == this.buttonEncode) {
            // Ask server to encode the pattern
            Packet_S_InfusionEncoder.sendEncodePattern(this.deContainer.getPlayer(), isShiftKeyDown());
        } else if (button == this.buttonReset) {
            // Ask server to clear the slots
            Packet_S_InfusionEncoder.sendResetSlot(this.deContainer.getPlayer());
        }
    }

    private void changeActivePage() {
        // Ask server to roll
        Packet_S_InfusionEncoder.sendRollScorllBar(this.deContainer.getPlayer(), this.scrollBar.getCurrentScroll());
    }

    @Override
    public void onInventoryChanged(IInventory sourceInventory) {

    }
}
