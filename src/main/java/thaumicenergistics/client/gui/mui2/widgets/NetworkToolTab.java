package thaumicenergistics.client.gui.mui2.widgets;

import com.cleanroommc.modularui.widget.ParentWidget;

public class NetworkToolTab extends ParentWidget<NetworkToolTab> {
    private int slot;
    /**
     * @param networkToolSlot the slot index of the associated network tool.
     */
    public NetworkToolTab(int networkToolSlot) {
        this.slot = networkToolSlot;
    }

    public int getSlot() {
        return slot;
    }
}
