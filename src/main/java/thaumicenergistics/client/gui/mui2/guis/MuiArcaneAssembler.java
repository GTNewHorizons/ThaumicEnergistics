package thaumicenergistics.client.gui.mui2.guis;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.sync.DoubleSyncHandler;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.PagedWidget;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Column;

import com.cleanroommc.modularui.widgets.layout.Row;
import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.client.gui.mui2.helper.MuiItemSlotIcons;
import thaumicenergistics.client.gui.mui2.helper.MuiNetworkTool;
import thaumicenergistics.client.gui.mui2.widgets.IconItemSlot;
import thaumicenergistics.client.gui.mui2.widgets.NetworkToolTab;
import thaumicenergistics.client.gui.mui2.widgets.NetworkToolTabButton;
import thaumicenergistics.client.gui.mui2.widgets.PartialAspectWidget;
import thaumicenergistics.client.textures.GuiTextureManager;
import thaumicenergistics.common.registries.ThEStrings;
import thaumicenergistics.common.tiles.TileArcaneAssembler;

import java.util.List;

/**
 * Modular UI 2: Arcane Assembler
 * @author firenoo
 */
public final class MuiArcaneAssembler {

    // spotless:off
    static final UITexture MAIN_BG = UITexture.builder()
            .location(GuiTextureManager.ARCANE_ASSEMBLER.getTexture())
            .imageSize(256, 256)
            .uv(0, 0, 176, 197)
            .build();
    static final UITexture UPGRADES_BG = UITexture.builder()
            .location(GuiTextureManager.ARCANE_ASSEMBLER.getTexture())
            .imageSize(256, 256)
            .uv(179, 0, 55, 104)
            .build();
    static final UITexture CRAFT_PROGRESS_BG = UITexture.builder()
            .location(GuiTextureManager.ARCANE_ASSEMBLER.getTexture())
            .imageSize(256, 256)
            .uv(0, 197, 4, 16)
            .build();
    // spotless:on

    private MuiArcaneAssembler() {}

    /* Gui constants */

    /** xPos of the leftmost aspect indicator */
    public static final int ASPECT_BASE_X = 22;
    /** yPos of the leftmost aspect indicator */
    public static final int ASPECT_BASE_Y = 87;
    /** xPos of left upgrade slot column */
    public static final int UPGRADE_LEFT_X = 7;
    /** yPos of left upgrade slot column */
    public static final int UPGRADE_LEFT_Y = 7;
    /** xPos of right upgrade slot column */
    public static final int UPGRADE_RIGHT_X = UPGRADE_LEFT_X + 5;
    /** yPos of right upgrade slot column (incl. kcore) */
    public static final int UPGRADE_RIGHT_Y = UPGRADE_LEFT_Y;
    /** Number of Kcore pattern slots */
    public static final int NUM_PATTERN_SLOTS = 21;

    /* SyncHandler constants */

    public static final int SYNC_ASPECT_AIR = 0;
    public static final int SYNC_ASPECT_FIRE = 1;
    public static final int SYNC_ASPECT_WATER = 2;
    public static final int SYNC_ASPECT_EARTH = 3;
    public static final int SYNC_ASPECT_ORDER = 4;
    public static final int SYNC_ASPECT_ENTROPY = 5;
    public static final int SYNC_CRAFT_PROGRESS = 6;
    public static final String SYNC_KCORE_SLOT = "kcore";
    public static final String SYNC_UPGRADE_SLOTS = "upgrades";
    public static final String SYNC_ARMOR_SLOTS = "armors";
    public static final String SYNC_PATTERN_SLOTS = "patterns";
    public static final String SYNC_RESULT_SLOT = "result";


    public static ModularPanel createGui(GuiContext context, TileArcaneAssembler ass, EntityPlayer player) {
        ModularPanel topPanel = new ModularPanel(context);
        Column mainWindow = new Column();
        Column upgradeWindow = new Column();
        SlotGroupWidget playerInventory = MuiNetworkTool.createPlayerInventory(player)
                .background(IDrawable.EMPTY);
        // Spotless makes this look awful
        // spotless:off

        PartialAspectWidget aerIndicator = new PartialAspectWidget(Aspect.AIR);
        aerIndicator.setSynced(SYNC_ASPECT_AIR)
                .size(16, 16)
                .pos(ASPECT_BASE_X + 20 * 1, ASPECT_BASE_Y);

        PartialAspectWidget waterIndicator = new PartialAspectWidget(Aspect.WATER);
        waterIndicator.setSynced(SYNC_ASPECT_WATER)
                .size(16, 16)
                .pos(ASPECT_BASE_X + 20 * 2, ASPECT_BASE_Y);

        PartialAspectWidget fireIndicator = new PartialAspectWidget(Aspect.FIRE);
        fireIndicator.setSynced(SYNC_ASPECT_FIRE)
                .size(16, 16)
                .pos(ASPECT_BASE_X + 20 * 3, ASPECT_BASE_Y);

        PartialAspectWidget earthIndicator = new PartialAspectWidget(Aspect.EARTH);
        earthIndicator.setSynced(SYNC_ASPECT_EARTH)
                .size(16, 16)
                .pos(ASPECT_BASE_X + 20 * 4, ASPECT_BASE_Y);

        PartialAspectWidget orderIndicator = new PartialAspectWidget(Aspect.ORDER);
        orderIndicator.setSynced(SYNC_ASPECT_ORDER)
                .size(16, 16)
                .pos(ASPECT_BASE_X + 20 * 5, ASPECT_BASE_Y);

        PartialAspectWidget entropyIndicator = new PartialAspectWidget(Aspect.ENTROPY);
        entropyIndicator.setSynced(SYNC_ASPECT_ENTROPY)
                .size(16, 16)
                .pos(ASPECT_BASE_X + 20 * 6, ASPECT_BASE_Y);

        mainWindow.background(MAIN_BG).size(176, 197).pos(0, 0)
                // Title
                .child(new TextWidget(IKey.str(ThEStrings.Block_ArcaneAssembler.getLocalized()))
                        .pos(6, 5))
                // Inventory
                .child(playerInventory)
                // Pattern Slots
                .child(SlotGroupWidget.builder()
                        .matrix("IIIIIII", "IIIIIII", "IIIIIII")
                        .key('I', index -> new ItemSlot().background(IDrawable.EMPTY))
                        .synced(SYNC_PATTERN_SLOTS)
                        .build()
                        .pos(25, 24))
                // Crafting Result Slot
                .child(new ItemSlot()
                        .setSynced(SYNC_RESULT_SLOT)
                        .pos(13, 86)
                        .background(IDrawable.EMPTY))
                // Crafting Progress Slot
                .child(new CraftProgress())
                // Aspect status indicators
                .child(aerIndicator)
                .child(waterIndicator)
                .child(fireIndicator)
                .child(earthIndicator)
                .child(orderIndicator)
                .child(entropyIndicator);

        SlotGroupWidget armorSlots = SlotGroupWidget.builder()
                .matrix("I", "I", "I", "I")
                .key('I', index ->
                        new IconItemSlot(MuiItemSlotIcons.ARMOR_SLOT_BG[index], 1, 1f).background(IDrawable.EMPTY))
                .synced(SYNC_ARMOR_SLOTS)
                .build()
                .pos(UPGRADE_RIGHT_X + 18, UPGRADE_RIGHT_Y + 18);

        // Upgrade side panel
        upgradeWindow.background(UPGRADES_BG).left(179).size(55, 104)
                // Knowledge core slot
                .child(new ItemSlot()
                        .setSynced(SYNC_KCORE_SLOT)
                        .pos(UPGRADE_LEFT_X, UPGRADE_LEFT_Y)
                        .background(IDrawable.EMPTY))
                // Upgrade Inventory
                .child(SlotGroupWidget.builder()
                        .matrix("I", "I", "I", "I")
                        .key('I', index ->
                                new IconItemSlot(MuiItemSlotIcons.UPGRADE_SLOT_BG, 1, 0.5f).background(IDrawable.EMPTY))
                        .synced(SYNC_UPGRADE_SLOTS)
                        .build()
                        .pos(UPGRADE_LEFT_X, UPGRADE_LEFT_Y + 18))
                // Actual armor slots
                .child(armorSlots);

        PagedWidget.Controller controller = new PagedWidget.Controller();
        PagedWidget netToolPanel = MuiNetworkTool.createNetworkToolWidget(controller, ass.getLocation(), player);
        if (netToolPanel != null) {
            netToolPanel.pos(179, 129);
            List<NetworkToolTab> tabPages = (List<NetworkToolTab>) netToolPanel.getPages();
            int numPages = Math.min(tabPages.size(), 4);
            Row tabs = new Row();
            for (int i = 0; i < numPages; ++i) {
                ItemSlot slot = (ItemSlot) playerInventory.getChildren().get(tabPages.get(i).getSlot());
                tabs.child(new NetworkToolTabButton(i, controller, slot)
                        .tab(GuiTextures.TAB_TOP, 0)
                        .size(16, 16));
            }
            context.addNEIExclusionArea(tabs);
            tabs.pos(179 + 2, 129 - 12)
                    .coverChildren();
            topPanel.child(netToolPanel)
                    .child(tabs);
        }
        // Top level configuration
        topPanel.background(IDrawable.EMPTY)
                .coverChildren()
                .align(Alignment.Center);

        topPanel.child(mainWindow)
                .child(upgradeWindow);
        // spotless:on
        return topPanel;
    }

    private static class CraftProgress extends ProgressWidget {

        public CraftProgress() {
            direction(ProgressWidget.Direction.UP).texture(null, CRAFT_PROGRESS_BG, 16).pos(32, 87).size(4, 16)
                    .setSynced(SYNC_CRAFT_PROGRESS);
            tooltip(
                    tooltip -> tooltip.addLine(
                            IKey.dynamic(() -> String.format("Progress: %.2f%%", this.getCurrentProgress() * 100))));
            progress(() -> ((DoubleSyncHandler) getSyncHandler()).getDoubleValue());
        }
    }
}
