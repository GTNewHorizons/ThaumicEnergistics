package thaumicenergistics.common.storage;

import static thaumicenergistics.common.storage.AEEssentiaStackType.ESSENTIA_STACK_TYPE;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IExternalStorageHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEStackType;
import thaumcraft.api.aspects.IAspectContainer;
import thaumicenergistics.common.inventory.MEMonitorEssentiaHandler;

public class EssentiaExternalStorageHandler implements IExternalStorageHandler {

    @Override
    public boolean canHandle(TileEntity te, ForgeDirection d, IAEStackType<?> type, BaseActionSource mySrc) {
        return type == ESSENTIA_STACK_TYPE && te instanceof IAspectContainer;
    }

    @Override
    @SuppressWarnings({ "rawtypes" })
    public IMEInventory getInventory(TileEntity te, ForgeDirection d, IAEStackType<?> type, BaseActionSource src) {
        return new MEMonitorEssentiaHandler((IAspectContainer) te);
    }
}
