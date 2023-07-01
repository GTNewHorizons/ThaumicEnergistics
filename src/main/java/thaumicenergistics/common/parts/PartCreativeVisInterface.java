package thaumicenergistics.common.parts;

import thaumcraft.api.aspects.Aspect;

public class PartCreativeVisInterface extends PartVisInterface {

    @Override
    protected int consumeVisFromVisNetwork(Aspect digiVisAspect, int amount) {
        return amount;
    }
}
