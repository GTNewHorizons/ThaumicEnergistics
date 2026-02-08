package thaumicenergistics.common.features;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import appeng.api.AEApi;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import cpw.mods.fml.common.registry.GameRegistry;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumicenergistics.api.IThEConfig;
import thaumicenergistics.api.IThEItems;
import thaumicenergistics.api.IThEParts;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.common.parts.AEPartsEnum;
import thaumicenergistics.common.parts.PartEssentiaConversionMonitor;
import thaumicenergistics.common.parts.PartEssentiaStorageMonitor;
import thaumicenergistics.common.parts.PartEssentiaTerminal;
import thaumicenergistics.common.registries.FeatureRegistry;
import thaumicenergistics.common.registries.ResearchRegistry;
import thaumicenergistics.common.registries.ResearchRegistry.ResearchTypes;

/**
 * {@link PartEssentiaTerminal}, {@link PartEssentiaStorageMonitor}, and {@link PartEssentiaConversionMonitor} feature.
 *
 * @author Nividica
 *
 */
public class FeatureEssentiaMonitoring extends ThEThaumcraftResearchFeature {

    private boolean isWirelessEnabled = false, isConversionEnabled = false;

    public FeatureEssentiaMonitoring() {
        super(ResearchTypes.ESSENTIA_TERMINAL.getKey());
    }

    @Override
    protected boolean checkConfigs(final IThEConfig theConfig) {
        this.isConversionEnabled = AEConfig.instance.isFeatureEnabled(AEFeature.PartConversionMonitor);
        this.isWirelessEnabled = AEConfig.instance.isFeatureEnabled(AEFeature.WirelessAccessTerminal)
                && theConfig.craftWirelessEssentiaTerminal();
        return true;
    }

    @Override
    protected Object[] getItemReqs(final CommonDependantItems cdi) {
        this.isWirelessEnabled &= (cdi.DenseCell != null) && (cdi.WirelessReceiver != null);

        return new Object[] { cdi.LogicProcessor, cdi.CalculationProcessor };
    }

    @Override
    protected ThEThaumcraftResearchFeature getParentFeature() {
        return FeatureRegistry.instance().featureConversionCores;
    }

    @Override
    protected void registerCrafting(final CommonDependantItems cdi) {
        // My items
        IThEItems theItems = ThEApi.instance().items();
        IThEParts theParts = ThEApi.instance().parts();
        ItemStack EssentiaTerminal = theParts.Essentia_Terminal.getStack();
        ItemStack EssentiaLevelEmitter = theParts.Essentia_LevelEmitter.getStack();
        ItemStack EssentiaStorageMonitor = theParts.Essentia_StorageMonitor.getStack();

        // Register Essentia Terminal
        GameRegistry.addRecipe(
                new ShapelessOreRecipe(
                        AEApi.instance().definitions().parts().terminal().maybeStack(1).get(),
                        EssentiaTerminal));

        // Is wireless term enabled?
        if (this.isWirelessEnabled) {
            ItemStack WirelessEssentiaTerminal = theItems.WirelessEssentiaTerminal.getStack();

            // Register Wireless Essentia Terminal
            GameRegistry.addRecipe(
                    new ShapelessOreRecipe(
                            AEApi.instance().definitions().items().wirelessTerminal().maybeStack(1).get(),
                            WirelessEssentiaTerminal));

        }

        // Register Essentia Level Emitter
        GameRegistry.addRecipe(
                new ShapelessOreRecipe(
                        AEApi.instance().definitions().parts().levelEmitter().maybeStack(1).get(),
                        EssentiaLevelEmitter));

        // Register Essentia Storage Monitor
        GameRegistry.addRecipe(
                new ShapelessOreRecipe(
                        AEApi.instance().definitions().parts().storageMonitor().maybeStack(1).get(),
                        EssentiaStorageMonitor));

        // Is conversion monitor enabled?
        if (this.isConversionEnabled) {
            ItemStack EssentiaConversionMonitor = theParts.Essentia_ConversionMonitor.getStack();

            // Register Essentia Conversion Monitor
            GameRegistry.addRecipe(
                    new ShapelessOreRecipe(
                            AEApi.instance().definitions().parts().conversionMonitor().maybeStack(1).get(),
                            EssentiaConversionMonitor));
        }
    }

    @Override
    protected void registerPseudoParents() {}

    @Override
    protected void registerResearch() {
        // Set the research aspects
        AspectList etAspectList = new AspectList();
        etAspectList.add(Aspect.EXCHANGE, 5);
        etAspectList.add(Aspect.SENSES, 5);
        etAspectList.add(Aspect.ENERGY, 3);
        etAspectList.add(Aspect.WATER, 3);

        // Set the icon
        ItemStack etIcon = AEPartsEnum.EssentiaTerminal.getStack();

        // Setup pages
        ArrayList<ResearchPage> pageList = new ArrayList<ResearchPage>();
        pageList.add(new ResearchPage(ResearchTypes.ESSENTIA_TERMINAL.getPageName(1)));
        pageList.add(new ResearchPage(ResearchTypes.ESSENTIA_TERMINAL.getPageName(2)));
        pageList.add(new ResearchPage(ResearchTypes.ESSENTIA_TERMINAL.getPageName(3)));
        pageList.add(new ResearchPage(ResearchTypes.ESSENTIA_TERMINAL.getPageName(4)));

        // Set the pages
        ResearchPage[] etPages = pageList.toArray(new ResearchPage[pageList.size()]);

        // Create the IO research
        ResearchTypes.ESSENTIA_TERMINAL
                .createResearchItem(etAspectList, ResearchRegistry.COMPLEXITY_SMALL, etIcon, etPages);
        ResearchTypes.ESSENTIA_TERMINAL.researchItem.setParents(this.getFirstValidParentKey(false));
        ResearchTypes.ESSENTIA_TERMINAL.researchItem.setConcealed();
        ResearchTypes.ESSENTIA_TERMINAL.researchItem.setSecondary();
        ResearchTypes.ESSENTIA_TERMINAL.researchItem.registerResearchItem();
    }
}
