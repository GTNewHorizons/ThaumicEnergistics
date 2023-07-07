package thaumicenergistics.client.gui.mui2.helper;

import com.cleanroommc.modularui.drawable.UITexture;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemArmor;
import net.minecraft.util.IIcon;
import thaumicenergistics.client.textures.AEStateIconsEnum;

public final class MuiItemSlotIcons {
    // Spotless makes this look awful
    // spotless:off
    public static final UITexture[] ARMOR_SLOT_BG = new UITexture[4];

    public static final UITexture UPGRADE_SLOT_BG = UITexture.builder()
            .location(AEStateIconsEnum.AE_STATES_TEXTURE)
            .imageSize(256, 256)
            .uv(AEStateIconsEnum.UPGRADE_CARD_BACKGROUND.getU(),
                    AEStateIconsEnum.UPGRADE_CARD_BACKGROUND.getV(),
                    AEStateIconsEnum.UPGRADE_CARD_BACKGROUND.getWidth(),
                    AEStateIconsEnum.UPGRADE_CARD_BACKGROUND.getHeight())
            .build();

    static {
        IIcon iconHead = ItemArmor.func_94602_b(0);
        IIcon iconBody = ItemArmor.func_94602_b(1);
        IIcon iconLegs = ItemArmor.func_94602_b(2);
        IIcon iconShoe = ItemArmor.func_94602_b(3);
        ARMOR_SLOT_BG[0] = UITexture.builder()
                .location(Minecraft.getMinecraft().renderEngine.getResourceLocation(1))
                .fullImage()
                .build()
                .getSubArea(iconHead.getMinU(), iconHead.getMinV(), iconHead.getMaxU(), iconHead.getMaxV());
        ARMOR_SLOT_BG[1] = UITexture.builder()
                .location(Minecraft.getMinecraft().renderEngine.getResourceLocation(1))
                .fullImage()
                .build()
                .getSubArea(iconBody.getMinU(), iconBody.getMinV(), iconBody.getMaxU(), iconBody.getMaxV());
        ARMOR_SLOT_BG[2] = UITexture.builder()
                .location(Minecraft.getMinecraft().renderEngine.getResourceLocation(1))
                .fullImage()
                .build()
                .getSubArea(iconLegs.getMinU(), iconLegs.getMinV(), iconLegs.getMaxU(), iconLegs.getMaxV());
        ARMOR_SLOT_BG[3] = UITexture.builder()
                .location(Minecraft.getMinecraft().renderEngine.getResourceLocation(1))
                .fullImage()
                .build()
                .getSubArea(iconShoe.getMinU(), iconShoe.getMinV(), iconShoe.getMaxU(), iconShoe.getMaxV());
        // spotless:on
    }

}
