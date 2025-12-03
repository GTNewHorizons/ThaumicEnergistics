package thaumicenergistics.mixins;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;

import cpw.mods.fml.relauncher.FMLLaunchHandler;

@LateMixin
public class ThaumicEnergisticsLateMixinLoader implements ILateMixinLoader {

    @Override
    public String getMixinConfig() {
        return "mixins.thaumicenergistics.late.json";
    }

    @Nonnull
    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        List<String> mixins = new ArrayList<>();
        if (FMLLaunchHandler.side().isClient()) {
            mixins.add("thaumcraft.MixinRenderGolemBase");
        }
        mixins.add("thaumcraft.MixinAspect");
        mixins.add("thaumcraft.MixinEntityGolemBase");
        mixins.add("thaumcraft.MixinItemGolemBell");
        mixins.add("thaumcraft.MixinItemGolemPlacer");
        return mixins;
    }
}
