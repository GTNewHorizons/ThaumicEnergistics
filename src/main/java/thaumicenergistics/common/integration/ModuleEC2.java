package thaumicenergistics.common.integration;

import cpw.mods.fml.common.Optional;
import extracells.api.ECApi;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.common.fluids.GaseousEssentia;

/**
 * Extra Cells 2 integration.
 *
 * @author Nividica
 *
 */
public class ModuleEC2 {

    private ModuleEC2() {}

    @Optional.Method(modid = "extracells")
    static void init() {
        // Is blacklisting enabled?
        if (ThEApi.instance().config().blacklistEssentiaFluidInExtraCells()) {
            ECApi.instance().addFluidToShowBlacklist(GaseousEssentia.class);
            ECApi.instance().addFluidToStorageBlacklist(GaseousEssentia.class);
        }
    }
}
