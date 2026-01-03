package thaumicenergistics.client.gui.buttons;

import java.util.List;

import thaumicenergistics.client.textures.AEStateIconsEnum;
import thaumicenergistics.client.textures.ThEStateIconsEnum;
import thaumicenergistics.common.registries.ThEStrings;

public class GuiButtonDumpAll extends ThEStateButton {

    private String tooltipSortBy = "";

    public GuiButtonDumpAll(final int ID, final int xPosition, final int yPosition, final int width, final int height) {
        super(
                ID,
                xPosition,
                yPosition,
                width,
                height,
                ThEStateIconsEnum.DUMP_ALL,
                0,
                0,
                AEStateIconsEnum.REGULAR_BUTTON);
    }

    @Override
    public void getTooltip(final List<String> tooltip) {
        this.addAboutToTooltip(
                tooltip,
                ThEStrings.TooltipButton_EssentiaCellDumpAll_Title.getLocalized(),
                ThEStrings.TooltipButton_EssentiaCellDumpAll.getLocalized());
    }

}
