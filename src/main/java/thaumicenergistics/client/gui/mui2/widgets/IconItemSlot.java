package thaumicenergistics.client.gui.mui2.widgets;

import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.widgets.ItemSlot;
import org.lwjgl.opengl.GL11;

public class IconItemSlot extends ItemSlot {

    protected UITexture icon;
    protected int inset;
    protected float alpha;

    public IconItemSlot(UITexture icon, int inset, float alpha) {
        this.icon = icon;
        this.inset = inset;
        this.alpha = alpha;
    }

    @Override
    public void drawBackground(GuiContext context) {
        super.drawBackground(context);
        if (!getSlot().getHasStack()) {
            GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha);
            icon.draw(inset, inset, getArea().width - 2 * inset, getArea().height - 2 * inset);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }
}
