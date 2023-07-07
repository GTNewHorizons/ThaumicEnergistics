package thaumicenergistics.client.gui.mui2.widgets;

import com.cleanroommc.modularui.utils.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.opengl.GL11;

import com.cleanroommc.modularui.api.sync.SyncHandler;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.widget.Widget;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.lib.UtilsFX;
import thaumicenergistics.client.gui.mui2.sync.AspectCostSH;

@SideOnly(Side.CLIENT)
public class AspectWidget extends Widget<AspectWidget> {

    /* Constants */

    /**
     * Rate at which to blink the aspect if there is not enough in the wand.
     */
    public static final double ASPECT_COST_BLINK_SPEED = 0.5D;

    /**
     * Minimum transparency of the aspect if there is not enough in the wand.
     */
    public static final float ASPECT_COST_MIN_ALPHA = 0.25F;

    /**
     * Minimum transparency of the aspect if there is not enough in the wand.
     */
    public static final float ASPECT_COST_MAX_ALPHA = 0.75F;

    protected Aspect aspect;
    protected AspectCostSH syncHandler;

    public AspectWidget(Aspect aspect) {
        this.aspect = aspect;
    }

    public AspectWidget aspect(Aspect aspect) {
        this.aspect = aspect;
        return this;
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        if (syncHandler instanceof AspectCostSH isync) {
            this.syncHandler = isync;
            return true;
        }
        return false;
    }

    @Override
    public AspectCostSH getSyncHandler() {
        return syncHandler;
    }

    @Override
    public void drawForeground(GuiContext context) {
        super.drawForeground(context);
    }

    @Override
    public void draw(GuiContext context) {
        if (syncHandler == null || this.aspect == null) return;
        // Front
        float alpha = 1.0f;
        float amount = syncHandler.getVisStored() / 10;
        if (!syncHandler.hasEnoughVis()) {
            alpha = pingPongFromTime(
                    ASPECT_COST_BLINK_SPEED,
                    ASPECT_COST_MIN_ALPHA,
                    ASPECT_COST_MAX_ALPHA);
        }
        UtilsFX.drawTag(1, 1, aspect, amount, 0, 0, GL11.GL_ONE_MINUS_SRC_ALPHA, alpha, false);
    }

    /**
     * Ping pongs a value back and forth from min -> max -> min. The base speed of this effect is 1 second per
     * transition, 2 seconds total.
     *
     * @param speedReduction The higher this value, the slower the effect. The smaller this value, the faster the
     *                       effect. PingPong time (1) = 2 Seconds; (0.5) = 1 Second; (2) = 4 Seconds;
     * @param minValue
     * @param maxValue
     * @return
     */
    public static float pingPongFromTime(double speedReduction, final float minValue, final float maxValue) {
        // Sanity check for situations like pingPongFromTime( ?, 1.0F, 1.0F )
        if (minValue == maxValue) {
            return minValue;
        }

        // Bounds check speed reduction
        if (speedReduction <= 0) {
            speedReduction = Float.MIN_VALUE;
        }

        // Get the time modulated to 2000, then reduced
        float time = (float) ((System.currentTimeMillis() / speedReduction) % 2000.F);

        // Offset by -1000 and take the abs
        time = Math.abs(time - 1000.0F);

        // Convert time to a percentage
        float timePercentage = time / 1000.0F;

        // Get the position in the range we are now at
        float rangePercentage = (maxValue - minValue) * timePercentage;

        // Add the range position back to min
        return minValue + rangePercentage;
    }

    public static void drawVisText(String text, Color bColor, Color fColor, float scale) {
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        // Part 1: Background
        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, scale);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int sw = fr.getStringWidth(text);
        // so... you are drawing 3x3 grid of strings
        fr.drawString(text, 32 - sw - 1, 32 - fr.FONT_HEIGHT - 1, bColor.normal);
        fr.drawString(text, 32 - sw + 1, 32 - fr.FONT_HEIGHT - 1, bColor.normal);
        fr.drawString(text, 32 - sw - 1, 32 - fr.FONT_HEIGHT + 1, bColor.normal);
        fr.drawString(text, 32 - sw + 1, 32 - fr.FONT_HEIGHT + 1, bColor.normal);
        fr.drawString(text, 32 - sw - 1, 32 - fr.FONT_HEIGHT, bColor.normal);
        fr.drawString(text, 32 - sw + 1, 32 - fr.FONT_HEIGHT, bColor.normal);
        fr.drawString(text, 32 - sw, 32 - fr.FONT_HEIGHT, fColor.normal);
        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_DEPTH_TEST);

    }
}
