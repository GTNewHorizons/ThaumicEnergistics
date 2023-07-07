package thaumicenergistics.client.gui.mui2.helper;

import appeng.api.AEApi;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.api.util.DimensionalCoord;
import appeng.core.AppEng;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import com.cleanroommc.modularui.sync.ItemSlotSH;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.PageButton;
import com.cleanroommc.modularui.widgets.PagedWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thaumicenergistics.client.gui.mui2.widgets.IconItemSlot;
import thaumicenergistics.client.gui.mui2.widgets.LockedItemSlot;
import thaumicenergistics.client.gui.mui2.widgets.NetworkToolTab;
import thaumicenergistics.client.textures.GuiTextureManager;
import thaumicenergistics.common.container.slot.SlotNetworkTool;

public final class MuiNetworkTool {
    public static final int WIDTH = 68;
    public static final int HEIGHT = 68;

    /* Utility */
    private static final ItemStack NETWORK_TOOL =
            AEApi.instance().definitions().items().networkTool().maybeStack(1).get();
    private static final IGuiItem NETWORK_TOOL_ITEM = (IGuiItem) NETWORK_TOOL.getItem();
    public static final UITexture NETWORK_TOOL_SLOTS_BG = UITexture.builder()
            .location(GuiTextureManager.ARCANE_ASSEMBLER.getTexture())
            .imageSize(256, 256)
            .uv(179, 129, WIDTH, HEIGHT)
            .build();
    public static final UITexture NETWORK_TOOL_BG = UITexture.builder()
            .location(new ResourceLocation(AppEng.MOD_ID, "textures/items/ToolNetworkTool.png"))
            .imageSize(16, 16)
            .fullImage()
            .build();

    public static final String SYNC_NETWORK_TOOL_SLOT = "net_tool";
    public static final int NUM_SLOTS = 9;

    /**
     * Creates a slot group for the player inventory. This implementation locks
     * slots with a network tool (i.e. makes them inaccessible from the GUI)
     */
    public static SlotGroupWidget createPlayerInventory(EntityPlayer player) {
        SlotGroupWidget slotGroupWidget = new SlotGroupWidget();
        slotGroupWidget.flex()
                .coverChildren()
                .startDefaultMode()
                .leftRel(0.5f).bottom(7)
                .endDefaultMode();
        slotGroupWidget.debugName("player_inventory");
        String key = "player";
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack != null && NETWORK_TOOL.isItemEqual(stack)) {
                 slotGroupWidget.child(new LockedItemSlot()
                         .setSynced(key, i)
                         .pos(i * 18, 3 * 18 + 5)
                         .debugName("slot_locked_" + i));
            } else {
                slotGroupWidget.child(new ItemSlot()
                        .setSynced(key, i)
                        .pos(i * 18, 3 * 18 + 5)
                        .debugName("slot_" + i));
            }
        }
        for (int i = 0; i < 27; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i + 9);
            if (stack != null && NETWORK_TOOL.isItemEqual(stack)) {
                slotGroupWidget.child(new LockedItemSlot()
                        .setSynced(key, i + 9)
                        .pos(i % 9 * 18, i / 9 * 18)
                        .debugName("slot_locked" + (i + 9)));
            } else {
                slotGroupWidget.child(new ItemSlot()
                        .setSynced(key, i + 9)
                        .pos(i % 9 * 18, i / 9 * 18)
                        .debugName("slot_" + (i + 9)));
            }
        }
        return slotGroupWidget;
    }

    public static void addSyncHandlers(GuiSyncHandler guiSyncHandler, DimensionalCoord loc, EntityPlayer player) {
        InventoryPlayer inventory = player.inventory;
        int toolCount = 0;

        for (int i = 0; i < inventory.getSizeInventory(); ++i) {
            ItemStack stack = inventory.getStackInSlot(i);

            if (stack != null && NETWORK_TOOL.isItemEqual(stack)) {
                INetworkTool tool = (INetworkTool) NETWORK_TOOL_ITEM.getGuiObject(stack, loc.getWorld(), loc.x, loc.y, loc.z);

                guiSyncHandler.registerSlotGroup(SYNC_NETWORK_TOOL_SLOT, 3, true);
                for (int j = 0; j < tool.getSizeInventory(); ++j) {
                    guiSyncHandler.syncValue(SYNC_NETWORK_TOOL_SLOT, j + (NUM_SLOTS * toolCount),
                            new ItemSlotSH(new SlotNetworkTool(tool, j, 0, 0))
                                    .slotGroup(SYNC_NETWORK_TOOL_SLOT));
                }
                toolCount++;
            }
        }
    }
    public static PagedWidget createNetworkToolWidget(PagedWidget.Controller control, DimensionalCoord loc, EntityPlayer player) {
        InventoryPlayer inventory = player.inventory;
        PagedWidget result = new PagedWidget<>();
        result.controller(control);
        int toolCount = 0;

        if (NETWORK_TOOL == null) return null;
        for (int i = 0; i < inventory.getSizeInventory(); ++i) {
            ItemStack stack = inventory.getStackInSlot(i);

            if (stack != null && NETWORK_TOOL.isItemEqual(stack)) {
                INetworkTool tool = (INetworkTool) NETWORK_TOOL_ITEM.getGuiObject(stack, loc.getWorld(), loc.x, loc.y, loc.z);
                ParentWidget<NetworkToolTab> toolPage = new NetworkToolTab(i);
                SlotGroupWidget slots = new SlotGroupWidget();

                for (int j = 0; j < tool.getSizeInventory(); ++j) {
                    slots.child(new IconItemSlot(MuiItemSlotIcons.UPGRADE_SLOT_BG, 1, 0.5f)
                            .setSynced(SYNC_NETWORK_TOOL_SLOT, j + (toolCount * NUM_SLOTS))
                            .pos(7 + (j % 3) * 18, 7 + (j / 3) * 18));
                }
                toolCount++;

                toolPage.child(slots)
                        .background(NETWORK_TOOL_SLOTS_BG)
                        .size(WIDTH, HEIGHT);
                result.addPage(toolPage);
            }
        }
        result.coverChildren();
        return toolCount > 0 ? result : null;
    }
}
