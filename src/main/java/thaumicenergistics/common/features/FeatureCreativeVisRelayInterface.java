package thaumicenergistics.common.features;

import thaumicenergistics.common.registries.FeatureRegistry;

/**
 * Creative Vis Relay Interface, provides infinite cv
 */
public class FeatureCreativeVisRelayInterface extends FeatureVisRelayInterface {

    @Override
    protected void registerCrafting(CommonDependantItems cdi) {}

    @Override
    protected void registerPseudoParents() {}

    @Override
    protected void registerResearch() {}

    @Override
    protected ThEThaumcraftResearchFeature getParentFeature() {
        return FeatureRegistry.instance().featureVRI;
    }
}
