package thaumicenergistics.common.features;

import net.minecraft.item.ItemStack;

import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumicenergistics.api.IThEConfig;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.common.blocks.BlockEnum;
import thaumicenergistics.common.registries.FeatureRegistry;
import thaumicenergistics.common.registries.RecipeRegistry;
import thaumicenergistics.common.registries.ResearchRegistry;
import thaumicenergistics.common.registries.ResearchRegistry.ResearchTypes;

/**
 * {@link TileAdvancedInfusionProvider} feature.
 * 
 * @author MCTBL
 *
 */
public class FeatureAdvancedInfusionProvider extends ThEThaumcraftResearchFeature {

    public FeatureAdvancedInfusionProvider() {
        super(ResearchTypes.ADVANCED_INFUSION_PROVIDER.getKey());
    }

    @Override
    protected ThEThaumcraftResearchFeature getParentFeature() {
        return FeatureRegistry.instance().featureInfusionProvider;
    }

    @Override
    protected void registerPseudoParents() {}

    @Override
    protected void registerResearch() {
        AspectList advancedInfusionProviderList = new AspectList();
        advancedInfusionProviderList.add(Aspect.MECHANISM, 1);
        advancedInfusionProviderList.add(Aspect.MAGIC, 1);
        advancedInfusionProviderList.add(Aspect.EXCHANGE, 1);
        advancedInfusionProviderList.add(Aspect.MOTION, 1);
        advancedInfusionProviderList.add(Aspect.TOOL, 1);
        advancedInfusionProviderList.add(Aspect.MIND, 1);
        advancedInfusionProviderList.add(Aspect.SENSES, 1);

        ItemStack advancedInfusionProviderIcon = new ItemStack(BlockEnum.ADVANCED_INFUSION_PROVIDER.getBlock(), 1);

        ResearchPage[] advancedInfusionProviderPages = new ResearchPage[] {
                new ResearchPage(ResearchTypes.ADVANCED_INFUSION_PROVIDER.getPageName(1)),
                new ResearchPage(RecipeRegistry.BLOCK_ADVANCED_INFUSION_PROVIDER) };

        // Create the infusion provider research
        ResearchTypes.ADVANCED_INFUSION_PROVIDER.createResearchItem(
                advancedInfusionProviderList,
                ResearchRegistry.COMPLEXITY_LARGE,
                advancedInfusionProviderIcon,
                advancedInfusionProviderPages);
        ResearchTypes.ADVANCED_INFUSION_PROVIDER.researchItem.setParents(ResearchTypes.INFUSION_PROVIDER.getKey());

        ResearchTypes.ADVANCED_INFUSION_PROVIDER.researchItem.registerResearchItem();
    }

    @Override
    protected boolean checkConfigs(IThEConfig theConfig) {
        return true;
    }

    @Override
    protected Object[] getItemReqs(CommonDependantItems cdi) {
        return null;
    }

    @Override
    protected void registerCrafting(CommonDependantItems cdi) {

        ItemStack DiffusionCore = ThEApi.instance().items().DiffusionCore.getStack();
        ItemStack InfusionProvider = ThEApi.instance().blocks().InfusionProvider.getStack();
        ItemStack AdvancedInfusionProvider = ThEApi.instance().blocks().AdvancedInfusionProvider.getStack();

        // Set required aspects for infusion provider
        AspectList advancedInfusionProviderList = new AspectList();
        advancedInfusionProviderList.add(Aspect.MECHANISM, 64);
        advancedInfusionProviderList.add(Aspect.MAGIC, 64);
        advancedInfusionProviderList.add(Aspect.EXCHANGE, 64);
        advancedInfusionProviderList.add(Aspect.MIND, 64);
        advancedInfusionProviderList.add(Aspect.GREED, 64);

        ItemStack[] advancedInfusionProviderRecipeItems = { cdi.PrimalCharm, cdi.BallanceShard, DiffusionCore,
                cdi.BallanceShard, cdi.PrimalCharm, cdi.BallanceShard, DiffusionCore, cdi.BallanceShard };
        RecipeRegistry.BLOCK_ADVANCED_INFUSION_PROVIDER = ThaumcraftApi.addInfusionCraftingRecipe(
                this.researchKey,
                AdvancedInfusionProvider,
                10,
                advancedInfusionProviderList,
                InfusionProvider,
                advancedInfusionProviderRecipeItems);

    }

}
