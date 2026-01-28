package thaumicenergistics.common.inventory;

import appeng.tile.inventory.IAEStackInventory;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.common.storage.AEEssentiaStack;

public class CreativeEssentiaCellConfig extends IAEStackInventory {

    public CreativeEssentiaCellConfig() {
        super(null, Aspect.aspects.size());

        int i = 0;
        for (Aspect aspect : Aspect.aspects.values()) {
            this.putAEStackInSlot(i, new AEEssentiaStack(aspect));
            i++;
        }
    }

    @Override
    public void markDirty() {}
}
