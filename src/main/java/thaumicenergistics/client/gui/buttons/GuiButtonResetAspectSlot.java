package thaumicenergistics.client.gui.buttons;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumicenergistics.client.textures.AEStateIconsEnum;
import thaumicenergistics.common.registries.ThEStrings;

/**
 * Displays reset button icon.
 *
 * @author MCTBL
 *
 */
@SideOnly(Side.CLIENT)
public class GuiButtonResetAspectSlot extends ThEStateButton {

    public GuiButtonResetAspectSlot(int ID, int xPosition, int yPosition, int buttonWidth, int buttonHeight) {
        // Call super
        super(
                ID,
                xPosition,
                yPosition,
                buttonWidth,
                buttonHeight,
                AEStateIconsEnum.RESET_BUTTON,
                0,
                0,
                AEStateIconsEnum.REGULAR_BUTTON);
    }

    @Override
    public void getTooltip(List<String> tooltip) {
        this.addAboutToTooltip(
                tooltip,
                ThEStrings.TooltipButton_Reset_Aspect.getLocalized(),
                ThEStrings.TooltipButton_Reset_Aspect_Description.getLocalized());
    }

}
