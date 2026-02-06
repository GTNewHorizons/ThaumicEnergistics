package thaumicenergistics.client.gui.buttons;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;

import appeng.client.gui.widgets.ITooltip;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumicenergistics.client.gui.ThEGuiHelper;

/**
 * Base class for ThE buttons.
 *
 * @author Nividica
 *
 */
@SideOnly(Side.CLIENT)
public abstract class ThEGuiButtonBase extends GuiButton implements ITooltip {

    public ThEGuiButtonBase(final int ID, final int xPosition, final int yPosition, final int width, final int height,
            final String text) {
        super(ID, xPosition, yPosition, width, height, text);
    }

    /**
     * Called to get the tooltip for this button.
     *
     * @param tooltip List to add tooltip string to.
     */
    public abstract void getTooltip(final List<String> tooltip);

    /**
     * Checks if the mouse is over this button.
     *
     * @param mouseX
     * @param mouseY
     * @return
     */
    public boolean isMouseOverButton(final int mouseX, final int mouseY) {
        return ThEGuiHelper.INSTANCE
                .isPointInRegion(this.yPosition, this.xPosition, this.height, this.width, mouseX, mouseY);
    }

    @Override
    public String getMessage() {
        List<String> lines = new ArrayList<>();
        this.getTooltip(lines);
        return String.join("\n", lines);
    }

    @Override
    public int xPos() {
        return this.xPosition;
    }

    @Override
    public int yPos() {
        return this.yPosition;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public boolean isVisible() {
        List<String> lines = new ArrayList<>();
        this.getTooltip(lines);
        return !lines.isEmpty();
    }
}
