package thaumicenergistics.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import thaumicenergistics.client.gui.abstraction.ThEBaseGui;

/**
 * ThE's scroll bar
 * 
 * @author MCTBL
 */
public class ThEScrollbar {

    private int txtShiftX = 232;
    private int txtShiftY = 0;

    private int displayX = 0;
    private int displayY = 0;
    private int width = 12;
    private int height = 16;
    private int pageSize = 1;

    private int maxScroll = 0;
    private int minScroll = 0;
    private int currentScroll = 0;
    private boolean visible = true;

    private ResourceLocation texture;

    public ThEScrollbar() {
        this.texture = new ResourceLocation("minecraft", "textures/gui/container/creative_inventory/tabs.png");
    }

    public ThEScrollbar(final ResourceLocation texture, final int shiftX, final int shiftY) {
        this.texture = texture;
        this.txtShiftX = shiftX;
        this.txtShiftY = shiftY;
    }

    public void draw(final ThEBaseGui g) {
        if (!visible) {
            return;
        }
        Minecraft.getMinecraft().renderEngine.bindTexture(this.texture);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        if (this.getRange() == 0) {
            g.drawTexturedModalRect(this.displayX, this.displayY, txtShiftX + this.width, txtShiftY, this.width, 15);
        } else {
            final int offset = (this.currentScroll - this.minScroll) * (this.height - 15) / this.getRange();
            g.drawTexturedModalRect(this.displayX, offset + this.displayY, txtShiftX, txtShiftY, this.width, 15);
        }
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    private int getRange() {
        return this.maxScroll - this.minScroll;
    }

    public int getLeft() {
        return this.displayX;
    }

    public ThEScrollbar setLeft(final int v) {
        this.displayX = v;
        return this;
    }

    public int getTop() {
        return this.displayY;
    }

    public ThEScrollbar setTop(final int v) {
        this.displayY = v;
        return this;
    }

    public int getWidth() {
        return this.width;
    }

    public ThEScrollbar setWidth(final int v) {
        this.width = v;
        return this;
    }

    public int getHeight() {
        return this.height;
    }

    public ThEScrollbar setHeight(final int v) {
        this.height = v;
        return this;
    }

    public void setRange(final int min, final int max, final int pageSize) {
        this.minScroll = min;
        this.maxScroll = max;
        this.pageSize = pageSize;

        if (this.minScroll > this.maxScroll) {
            this.maxScroll = this.minScroll;
        }

        this.applyRange();
    }

    private void applyRange() {
        this.currentScroll = Math.max(Math.min(this.currentScroll, this.maxScroll), this.minScroll);
    }

    public int getCurrentScroll() {
        return this.currentScroll;
    }

    public void setCurrentScroll(final int currentScroll) {
        this.currentScroll = Math.max(Math.min(currentScroll, this.maxScroll), this.minScroll);
    }

    public boolean contains(final int x, final int y) {
        return x >= this.displayX && y >= this.displayY
                && x <= this.displayX + this.width
                && y <= this.displayY + this.height;
    }

    public void click(final ThEBaseGui theBaseGui, final int x, final int y) {
        if (this.getRange() == 0) {
            return;
        }

        if (this.contains(x, y)) {
            this.currentScroll = (y - this.displayY);
            this.currentScroll = this.minScroll + ((this.currentScroll * 2 * this.getRange() / this.height));
            this.currentScroll = (this.currentScroll + 1) >> 1;
            this.applyRange();
        }
    }

    public void wheel(int delta) {
        delta = Math.max(Math.min(-delta, 1), -1);
        this.currentScroll += delta * this.pageSize;
        this.applyRange();
    }
}
