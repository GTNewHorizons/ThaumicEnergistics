package thaumicenergistics.client.gui.mui2.widgets;

import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.widgets.ItemSlot;
import org.jetbrains.annotations.NotNull;

public class LockedItemSlot extends ItemSlot {

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        return Result.SUCCESS;
    }

    @Override
    public boolean onMouseRelease(int mouseButton) {
        return true;
    }

    @Override
    public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
        return false;
    }

    @Override
    public void onMouseDrag(int mouseButton, long timeSinceClick) {}
}