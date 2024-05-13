package thaumicenergistics.api;

import javax.annotation.Nonnull;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumicenergistics.api.storage.IAspectStorage;

/**
 * Defines what items and tile entities ThaumicEnergistics is allowed to interact with.
 *
 * @author Nividica
 *
 */
public interface IThETransportPermissions {

    /**
     * Adds a tile entity to both inject & extract whitelists. The tile must implement the interface
     * {@link IAspectContainer}<br>
     * Capacity is required to function properly
     *
     * @param tileClass
     * @param capacity
     * @return True if added to the lists, False if not.
     */
    <T extends TileEntity & IAspectContainer> boolean addAspectContainerTileToBothPermissions(
            @Nonnull Class<T> tileClass, int capacity);

    /**
     * Adds a tile entity to both inject & extract whitelists. The tile must implement the interface
     * {@link IAspectStorage}
     *
     * @param tileClass
     * @return True if added to the lists, False if not.
     */
    <T extends TileEntity & IAspectStorage> boolean addAspectStorageTileToBothPermissions(@Nonnull Class<T> tileClass);

    /**
     * Adds a tile entity to the extract whitelist. The tile must implement the interface {@link IAspectContainer}<br>
     * Note: Capacity can be 0 if the tile doesn't truely 'contain' essentia.
     *
     * @param tileClass
     * @param capacity
     * @return True if added to the list or already present, False otherwise.
     */
    <T extends TileEntity & IAspectContainer> boolean addAspectContainerTileToExtractPermissions(
            @Nonnull Class<T> tileClass, int capacity);

    /**
     * Adds a tile entity to the extract whitelist. The tile must implement the interface {@link IAspectStorage}
     *
     * @param tileClass
     * @return True if added to the list or already present, False otherwise.
     */
    <T extends TileEntity & IAspectStorage> boolean addAspectStorageTileToExtractPermissions(
            @Nonnull Class<T> tileClass);

    /**
     * Adds a tile entity to the inject whitelist. The tile must implement the interface {@link IAspectContainer}<br>
     * Capacity is required to function properly.
     *
     * @param tileClass
     * @param capacity
     * @return True if added to the list, False if not.
     */
    <T extends TileEntity & IAspectContainer> boolean addAspectContainerTileToInjectPermissions(
            @Nonnull Class<T> tileClass, int capacity);

    /**
     * Adds a tile entity to the inject whitelist. The tile must implement the interface {@link IAspectStorage}
     *
     * @param tileClass
     * @return True if added to the list, False if not.
     */
    <T extends TileEntity & IAspectStorage> boolean addAspectStorageTileToInjectPermissions(
            @Nonnull Class<T> tileClass);

    /**
     * Adds an item to the whitelist that must match the specified damage value.
     *
     * @param itemClass
     * @param capacity
     * @param damageValue
     * @param canHoldPartialAmount
     */
    void addEssentiaContainerItemToTransportPermissions(@Nonnull Class<? extends IEssentiaContainerItem> itemClass,
            int capacity, int damageValue, boolean canHoldPartialAmount);

    /**
     * Adds the specified item to the whitelist.
     *
     * @param containerItem
     * @param capacity
     * @param canHoldPartialAmount
     */
    void addEssentiaContainerItemToTransportPermissions(@Nonnull ItemStack containerItem, int capacity,
            boolean canHoldPartialAmount);

    /**
     * Checks if the container can be extracted from
     *
     * @param container
     * @return
     */
    boolean canExtractFromAspectContainerTile(@Nonnull IAspectContainer container);

    /**
     * Checks if the container can be injected into
     *
     * @param container
     * @return
     */
    boolean canInjectToAspectContainerTile(@Nonnull IAspectContainer container);

    /**
     * Returns the registered capacity of the specified container.
     *
     * @param container
     * @return Registered capacity, or -1 if not registered.
     */
    int getAspectContainerTileCapacity(@Nonnull IAspectContainer container);

    /**
     * Returns whether the capacity of aspect container is shared among all aspects.
     * 
     * @param container
     * @return True if container shares capacity among all aspects; False if doesn't.
     */
    boolean doesAspectContainerTileShareCapacity(@Nonnull IAspectContainer container);

    /**
     * Gets the information about the container as it was registered to the whitelist.
     *
     * @param itemClass
     * @param damageValue
     * @return Info if was registered, null otherwise.
     */
    IThEEssentiaContainerPermission getEssentiaContainerInfo(@Nonnull Class<? extends Item> itemClass, int damageValue);
}
