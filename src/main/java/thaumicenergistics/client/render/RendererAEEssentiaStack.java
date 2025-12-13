package thaumicenergistics.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumcraft.client.lib.UtilsFX;
import thaumicenergistics.common.storage.AEEssentiaStack;

public class RendererAEEssentiaStack {

    private static final ResourceLocation UNKNOWN_TEXTURE = new ResourceLocation(
            "thaumcraft",
            "textures/aspects/_unknown.png");

    @SideOnly(Side.CLIENT)
    public static void drawInGui(AEEssentiaStack stack, Minecraft mc, int x, int y) {
        if (stack.hasPlayerDiscovered(mc.thePlayer)) {
            // Draw the aspect
            UtilsFX.drawTag(x, y, stack.getAspect(), 0, 0, 0);
        }
        // Draw the question mark
        else {
            // Bind the Thaumcraft question mark texture
            mc.renderEngine.bindTexture(UNKNOWN_TEXTURE);

            int color = stack.getAspect().getColor();
            GL11.glColor4ub(
                    (byte) ((color >> 8) & 0xff),
                    (byte) ((color >> 16) & 0xff),
                    (byte) ((color >> 24) & 0xff),
                    (byte) 255);

            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            // Ask Thaumcraft to draw the question texture
            UtilsFX.drawTexturedQuadFull(x, y, 0);

            // Disable blending
            GL11.glDisable(GL11.GL_BLEND);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void drawOnBlockFace(AEEssentiaStack stack) {
        GL11.glPushMatrix();

        GL11.glTranslatef(0, -0.04F, 0);
        GL11.glScalef(1.0f / 42.0f, 1.0f / 42.0f, 1.0f / 42.0f);
        GL11.glTranslated(-8.0, -10.2, -10.6);

        UtilsFX.drawTag(0, 0, stack.getAspect(), 0, 0, 0);

        GL11.glPopMatrix();
    }
}
