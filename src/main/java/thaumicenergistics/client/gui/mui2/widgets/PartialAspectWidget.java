package thaumicenergistics.client.gui.mui2.widgets;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.Tooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;

import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;

import com.cleanroommc.modularui.drawable.TextRenderer;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.NumberFormat;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.common.tiles.TileArcaneAssembler;

import java.util.function.Consumer;

public class PartialAspectWidget extends AspectWidget {

    public PartialAspectWidget(Aspect aspect) {
        super(aspect);
        final String visdiscount = StatCollector.translateToLocal("tc.visdiscount");
        tooltipBuilder(tooltip -> {
            tooltip.addLine(IKey.str(EnumChatFormatting.AQUA + aspect.getName()));
            tooltip.setHasSpaceAfterFirstLine(true);
            tooltip.addLine(IKey.dynamic(() -> String.format(
                    EnumChatFormatting.ITALIC + EnumChatFormatting.DARK_PURPLE.toString() + "%s: %s%%",
                    visdiscount,
                    NumberFormat.format((1 - syncHandler.getVisDiscount()) * 100), 2)));
            if (syncHandler != null) {
                float visCost = syncHandler.getVisCost() / 10.0f;
                if (visCost > 0.0f && !syncHandler.hasEnoughVis()) {
                    tooltip.addLine(IKey.dynamic(() -> EnumChatFormatting.RED + NumberFormat.format(visCost)));
                }
            }
            tooltip.setUpdateTooltipEveryTick(true);
        });
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void draw(GuiContext context) {
        final double width = getArea().width;
        final double height = getArea().height;
        final double proportion = 1 - syncHandler.getVisStored() / TileArcaneAssembler.MAX_STORED_CVIS;
        Minecraft mc = context.mc;
        int aspectColor = aspect.getColor();
        int red = (aspectColor >> 16) & 0xFF;
        int green = (aspectColor >> 8) & 0xFF;
        int blue = (aspectColor) & 0xFF;
        Color textColor = Color.WHITE;
        Color bTextColor = Color.BLACK;
        float alpha = 1.0f;
        if (!syncHandler.hasEnoughVis()) {
            alpha = AspectWidget.pingPongFromTime(
                    AspectWidget.ASPECT_COST_BLINK_SPEED,
                    AspectWidget.ASPECT_COST_MIN_ALPHA,
                    AspectWidget.ASPECT_COST_MAX_ALPHA);
        }
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.003921569F);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glPushMatrix();
        mc.renderEngine.bindTexture(aspect.getImage());
        GL11.glColor4f(red / 255.0f, green / 255.0f, blue / 255.0f, alpha);

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(0.0, height, 0.0, 0.0, 1.0);
        tessellator.addVertexWithUV(width, height, 0.0, 1.0, 1.0);
        tessellator.addVertexWithUV(width, proportion * height, 0.0, 1.0, proportion);
        tessellator.addVertexWithUV(0.0, proportion * height, 0.0, 0.0, proportion);
        tessellator.draw();

        GL11.glColor4f(0.1f, 0.1f, 0.1f, alpha);
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(0.0, proportion * height, 0.0, 0.0, proportion);
        tessellator.addVertexWithUV(width, proportion * height, 0.0, 1.0, proportion);
        tessellator.addVertexWithUV(width, 0.0, 0.0, 1.0, 0.0);
        tessellator.addVertexWithUV(0.0, 0.0, 0.0, 0.0, 0.0);
        tessellator.draw();
        GL11.glPopMatrix();
        // Text
        String formatted = NumberFormat.format(syncHandler.getVisStored() / 10.0f, 2);
        drawVisText(formatted, bTextColor, textColor, 0.5f);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1f);
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }
}
