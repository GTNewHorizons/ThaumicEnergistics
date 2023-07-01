package thaumicenergistics.common.integration;

import appeng.api.AEApi;
import thaumicenergistics.common.items.ItemCraftingAspect;

public class ModuleAe2 {

    private ModuleAe2() {}

    static void init() {
        AEApi.instance().registries().itemDisplay().blacklistItemDisplay(ItemCraftingAspect.class);
    }
}
