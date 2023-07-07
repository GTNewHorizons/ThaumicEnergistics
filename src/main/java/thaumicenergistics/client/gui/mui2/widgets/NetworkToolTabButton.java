package thaumicenergistics.client.gui.mui2.widgets;

import codechicken.lib.gui.GuiDraw;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.PageButton;
import com.cleanroommc.modularui.widgets.PagedWidget;
import thaumicenergistics.client.gui.mui2.helper.MuiNetworkTool;

public class NetworkToolTabButton extends PageButton {
    private ItemSlot slot;

    public NetworkToolTabButton(int index, PagedWidget.Controller controller, ItemSlot slot) {
        super(index, controller);
        this.slot = slot;
    }

    @Override
    public void drawBackground(GuiContext context) {
        super.drawBackground(context);
        MuiNetworkTool.NETWORK_TOOL_BG.draw(context, 2, 2, 11, 11);
    }

    @Override
    public void drawForeground(GuiContext context) {
        if (isHovering()) {
            Area area = slot.getArea();
            GuiDraw.drawRect(area.x, area.y, area.width, area.height, Color.argb(1.0f, 1.0f, 0.5f, 0.5f));
        }
        super.drawForeground(context);
    }

}
