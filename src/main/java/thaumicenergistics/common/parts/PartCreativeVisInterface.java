package thaumicenergistics.common.parts;

import thaumcraft.api.aspects.Aspect;

public class PartCreativeVisInterface extends PartVisInterface {

    public PartCreativeVisInterface() {
        super(AEPartsEnum.CreativeVisInterface);
    }

    @Override
    protected int consumeVisFromVisNetwork(Aspect digiVisAspect, int amount) {
        return amount;
    }
}
