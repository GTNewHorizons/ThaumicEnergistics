package thaumicenergistics.common.grid;

import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEItemStack;
import thaumicenergistics.common.items.ItemCraftingAspect;

class CraftingAspect_ItemWatcher implements IMEMonitorHandlerReceiver<IAEItemStack> {

    private final GridEssentiaCache gridCache;

    public CraftingAspect_ItemWatcher(final GridEssentiaCache gridCache) {
        this.gridCache = gridCache;
    }

    @Override
    public boolean isValid(final Object verificationToken) {
        return this.gridCache.internalGrid == verificationToken;
    }

    @Override
    public void onListUpdate() {
        // Ignored
    }

    @Override
    public void postChange(final IBaseMonitor<IAEItemStack> monitor, final Iterable<IAEItemStack> change,
            final BaseActionSource actionSource) {
        for (IAEItemStack stack : change) {
            // Is the stack craftable, has NBT tag, and is a crafting aspect?
            if (stack.hasTagCompound() && (stack.getItem() instanceof ItemCraftingAspect)) {
                this.gridCache.markForUpdate();

                // Remove any fake aspect stacks in the ME system immediately
                // We'll also try to remove any that snuck in another way (legacy items, etc) when updating the essentia
                // cache
                if (monitor instanceof IMEMonitor) {
                    ((IMEMonitor<IAEItemStack>) monitor)
                            .extractItems(stack, Actionable.MODULATE, new BaseActionSource());
                }
                break;
            }
        }
    }
}
