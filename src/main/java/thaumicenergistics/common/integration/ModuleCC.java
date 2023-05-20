package thaumicenergistics.common.integration;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import thaumicenergistics.common.tiles.TileEssentiaProvider;

/**
 * Computer Craft integration
 *
 * @author Nividica
 *
 */
public class ModuleCC {

    public static class ThEPeripherals implements IPeripheralProvider {

        @Override
        public IPeripheral getPeripheral(final World world, final int x, final int y, final int z, final int side) {
            try {
                // Get the tile entity at that position
                TileEntity te = world.getTileEntity(x, y, z);

                // Is the entity an Essentia Provider?
                if (te instanceof TileEssentiaProvider) {
                    // Create the peripheral
                    return new EssentiaProviderPeripheral(world, x, y, z);
                }
            } catch (Exception e) {
                // Silently ignore
            }

            return null;
        }
    }

    private ModuleCC() {}

    /**
     * Integrates with CC
     */
    static void init() {
        // Create and register the handler
        ComputerCraftAPI.registerPeripheralProvider(new ThEPeripherals());
    }
}
