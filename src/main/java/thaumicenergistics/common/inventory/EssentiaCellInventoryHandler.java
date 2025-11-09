package thaumicenergistics.common.inventory;

import static thaumicenergistics.common.storage.AEEssentiaStackType.ESSENTIA_STACK_TYPE;

import javax.annotation.Nonnull;

import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEStackType;
import appeng.me.storage.CellInventoryHandler;
import thaumicenergistics.common.storage.AEEssentiaStack;

public class EssentiaCellInventoryHandler extends CellInventoryHandler<AEEssentiaStack> {

    public EssentiaCellInventoryHandler(IMEInventory<AEEssentiaStack> c) {
        super(c, ESSENTIA_STACK_TYPE);
    }

    @Override
    public TYPE getCellType() {
        return TYPE.ESSENTIA;
    }

    @Nonnull
    @Override
    public IAEStackType<?> getStackType() {
        return ESSENTIA_STACK_TYPE;
    }
}
