package thaumicenergistics.common.integration;

import cpw.mods.fml.common.Loader;

public enum ModsList {

    THAUMCRAFT_NEI_PLUGIN("thaumcraftneiplugin"),
    ASPECT_RECIPE_INDEX("aspectrecipeindex");

    private final String modID;
    private Boolean isLoaded;

    ModsList(String modID) {
        this.modID = modID;
    }

    public boolean isLoaded() {
        if (isLoaded == null) {
            isLoaded = Loader.isModLoaded(modID);
        }
        return isLoaded;
    }

    public String modID() {
        return modID;
    }
}
