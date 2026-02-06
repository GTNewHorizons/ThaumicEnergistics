package thaumicenergistics.common.parts;

import net.minecraft.item.ItemStack;

import thaumcraft.api.aspects.Aspect;

public class PartCreativeVisInterface extends PartVisInterface {

    public PartCreativeVisInterface(final ItemStack is) {
        super(is);
    }

    @Override
    protected int consumeVisFromVisNetwork(Aspect digiVisAspect, int amount) {
        return amount;
    }
}
