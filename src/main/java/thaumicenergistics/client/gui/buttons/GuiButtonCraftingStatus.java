package thaumicenergistics.client.gui.buttons;

import java.util.List;

import appeng.core.localization.GuiText;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumicenergistics.client.textures.AEStateIconsEnum;

/**
 * Button for crafting status.
 *
 * @author MCTBL
 *
 */
@SideOnly(Side.CLIENT)
public class GuiButtonCraftingStatus extends ThEStateButton {

    public GuiButtonCraftingStatus(final int ID, final int xPosition, final int yPosition, final int buttonWidth,
            final int buttonHeight) {
        super(ID, xPosition, yPosition, buttonWidth, buttonHeight, null, 0, 0, AEStateIconsEnum.CRAFTING_STATUS);
    }

    @Override
    public void getTooltip(List<String> tooltip) {
        this.addAboutToTooltip(tooltip, GuiText.CraftingStatus.getLocal());
    }

}
