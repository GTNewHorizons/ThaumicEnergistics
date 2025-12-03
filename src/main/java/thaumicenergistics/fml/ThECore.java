package thaumicenergistics.fml;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;

/**
 * Thaumic Energistics Core Mod.
 *
 * @author Nividica
 *
 */
@MCVersion("1.7.10")
public class ThECore implements IFMLLoadingPlugin {

    /**
     * Set to true if any classes required for golem hooks can not be transformed.
     */
    public static boolean golemHooksTransformFailed = false;

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[] { "thaumicenergistics.fml.ASMTransformer" };
    }

    @Override
    public String getModContainerClass() {
        return "thaumicenergistics.fml.ThaumicCoreModContainer";
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(final Map<String, Object> data) {}
}
