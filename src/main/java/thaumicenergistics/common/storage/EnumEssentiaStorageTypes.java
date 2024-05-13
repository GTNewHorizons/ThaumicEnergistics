package thaumicenergistics.common.storage;

import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;

import thaumicenergistics.common.items.ItemEnum;
import thaumicenergistics.common.registries.ThEStrings;

/**
 * The types of essentia storage.
 *
 * @author Nividica
 *
 */
public enum EnumEssentiaStorageTypes {

    Type_1K(0, 1 << 10, 12, "1k", EnumRarity.common, 0.5, ThEStrings.Item_EssentiaCell_1k,
            ThEStrings.Item_StorageComponent_1k),
    Type_4K(1, 1 << 12, 12, "4k", EnumRarity.uncommon, 1.0, ThEStrings.Item_EssentiaCell_4k,
            ThEStrings.Item_StorageComponent_4k),
    Type_16K(2, 1 << 14, 12, "16k", EnumRarity.uncommon, 1.5, ThEStrings.Item_EssentiaCell_16k,
            ThEStrings.Item_StorageComponent_16k),
    Type_64K(3, 1 << 16, 12, "64k", EnumRarity.rare, 2.0, ThEStrings.Item_EssentiaCell_64k,
            ThEStrings.Item_StorageComponent_64k),
    Type_Creative(4, 0, 63, "creative", EnumRarity.epic, 0.0, ThEStrings.Item_EssentiaCell_Creative, null),
    Type_256K(5, 1 << 18, 24, "256k", EnumRarity.rare, 2.5, ThEStrings.Item_EssentiaCell_256k,
            ThEStrings.Item_StorageComponent_256k),
    Type_1024K(6, 1 << 20, 36, "1024k", EnumRarity.rare, 3.0, ThEStrings.Item_EssentiaCell_1024k,
            ThEStrings.Item_StorageComponent_1024k),
    Type_4096K(7, 1 << 22, 48, "4096k", EnumRarity.epic, 3.5, ThEStrings.Item_EssentiaCell_4096k,
            ThEStrings.Item_StorageComponent_4096k),
    Type_16384K(8, 1 << 24, 60, "16384k", EnumRarity.epic, 4.0, ThEStrings.Item_EssentiaCell_16384k,
            ThEStrings.Item_StorageComponent_16384k),
    Type_QUANTUM(9, Integer.MAX_VALUE / 16, 1, "quantum", EnumRarity.epic, 8.0, ThEStrings.Item_EssentiaCell_Quantum,
            null),
    Type_SINGULARITY(10, Long.MAX_VALUE / 4, 1, "singularity", EnumRarity.epic, 16.0,
            ThEStrings.Item_EssentiaCell_Singularity, null);

    /**
     * Array of values whose index matches the types index.
     */
    public static final EnumEssentiaStorageTypes fromIndex[];

    /**
     * This is to ensure that the index can be independent of the ordinal. Since the data is saved based on index, not
     * ordinal.
     */
    static {
        // Setup the array
        fromIndex = new EnumEssentiaStorageTypes[11];
        fromIndex[Type_1K.index] = Type_1K;
        fromIndex[Type_4K.index] = Type_4K;
        fromIndex[Type_16K.index] = Type_16K;
        fromIndex[Type_64K.index] = Type_64K;
        fromIndex[Type_Creative.index] = Type_Creative;
        fromIndex[Type_256K.index] = Type_256K;
        fromIndex[Type_1024K.index] = Type_1024K;
        fromIndex[Type_4096K.index] = Type_4096K;
        fromIndex[Type_16384K.index] = Type_16384K;
        fromIndex[Type_QUANTUM.index] = Type_QUANTUM;
        fromIndex[Type_SINGULARITY.index] = Type_SINGULARITY;
    }

    /**
     * Index of the type.
     */
    public final int index;

    /**
     * Displayable suffix of the type.
     */
    public final String suffix;

    /**
     * Capacity of the type, in bytes.
     */
    public final long capacity;

    /**
     * Rarity class of the type.
     */
    public final EnumRarity rarity;

    /**
     * Maximum number of stored types.
     */
    public final int maxStoredTypes;

    /**
     * Amount of power drained while the cell is active.
     */
    public final double idleAEPowerDrain;

    /**
     * Name of the cell for this type.
     */
    public final ThEStrings cellName;

    /**
     * Name of the component for this type. The creative cell does not have a component.
     */
    public final ThEStrings componentName;

    private EnumEssentiaStorageTypes(final int index, final long capacity, final int maxStoredTypes,
            final String suffix, final EnumRarity rarity, final double aeDrain, final ThEStrings cellName,
            final ThEStrings componentName) {
        this.index = index;
        this.capacity = capacity;
        this.suffix = suffix;
        this.rarity = rarity;
        this.maxStoredTypes = maxStoredTypes;
        this.idleAEPowerDrain = aeDrain;
        this.cellName = cellName;
        this.componentName = componentName;
    }

    /**
     * Returns an empty cell for this type.
     *
     * @return
     */
    public ItemStack getCell() {
        return ItemEnum.ESSENTIA_CELL.getDMGStack(this.index);
    }

    /**
     * Returns a storage component for this type. The creative type has no component, null is returned.
     *
     * @param stackSize
     * @return
     */
    public ItemStack getComponent(final int stackSize) {
        if (this == Type_Creative) {
            return null;
        }
        return ItemEnum.STORAGE_COMPONENT.getDMGStack(this.index, stackSize);
    }
}
