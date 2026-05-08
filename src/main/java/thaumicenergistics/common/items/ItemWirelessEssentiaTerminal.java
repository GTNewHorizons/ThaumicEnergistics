package thaumicenergistics.common.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import thaumicenergistics.common.registries.ThEStrings;

// Note to fix inconsistent hierarchy: Include the COFHCore & IC2 Api's into
// build path
/**
 * Provides wireless access to networked essentia.
 *
 * @author Nividica
 *
 */
public class ItemWirelessEssentiaTerminal extends Item {

    public ItemWirelessEssentiaTerminal() {
        super();
    }

    @Override
    public String getUnlocalizedName() {
        return ThEStrings.Item_WirelessEssentiaTerminal.getUnlocalized();
    }

    @Override
    public String getUnlocalizedName(final ItemStack itemStack) {
        return ThEStrings.Item_WirelessEssentiaTerminal.getUnlocalized();
    }
}
