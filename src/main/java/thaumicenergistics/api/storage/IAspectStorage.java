package thaumicenergistics.api.storage;

import thaumcraft.api.aspects.IAspectContainer;

/**
 * An aspect container with defined capacity.
 */
public interface IAspectStorage extends IAspectContainer {

    /**
     * Returns the capacity of aspect container.
     *
     * @return Maximum amount of essentia this container can store.
     */
    int getContainerCapacity();

    /**
     * Determines whether the capacity of aspect container is shared among all aspects.
     *
     * @return True if this container shares its capacity among all aspects; False if this container provides the same
     *         capacity for every aspect.
     */
    boolean doesShareCapacity();
}
