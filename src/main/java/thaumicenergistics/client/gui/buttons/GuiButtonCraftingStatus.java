package thaumicenergistics.client.gui.buttons;

import java.util.List;

import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

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
        super(ID, xPosition, yPosition, buttonWidth, buttonHeight, AEStateIconsEnum.CRAFT_ONLY, 0, 0, null);
    }

    @Override
    public void drawButton(final Minecraft minecraftInstance, final int x, final int y) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.drawIcon(
                minecraftInstance,
                AEStateIconsEnum.TAB_BUTTON_EDGELESS,
                this.xPosition,
                this.yPosition,
                AEStateIconsEnum.TAB_BUTTON_EDGELESS.getWidth(),
                AEStateIconsEnum.TAB_BUTTON_EDGELESS.getHeight());
        if (this.stateIcon != null) {
            this.drawIcon(minecraftInstance, this.stateIcon, this.xPosition, this.yPosition, this.width, this.height);
        }
    }

    @Override
    public void getTooltip(List<String> tooltip) {
        this.addAboutToTooltip(tooltip, GuiText.CraftingStatus.getLocal());
    }

}
