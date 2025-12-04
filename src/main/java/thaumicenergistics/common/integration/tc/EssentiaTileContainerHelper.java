package thaumicenergistics.common.integration.tc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import appeng.api.config.Actionable;
import it.unimi.dsi.fastutil.objects.ObjectLongImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectLongPair;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.common.tiles.TileAlchemyFurnaceAdvancedNozzle;
import thaumcraft.common.tiles.TileAlembic;
import thaumcraft.common.tiles.TileCentrifuge;
import thaumcraft.common.tiles.TileEssentiaReservoir;
import thaumcraft.common.tiles.TileJarFillable;
import thaumcraft.common.tiles.TileJarFillableVoid;
import thaumcraft.common.tiles.TileTubeBuffer;
import thaumicenergistics.api.IThETransportPermissions;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.common.fluids.GaseousEssentia;
import thaumicenergistics.common.storage.AEEssentiaStack;
import thaumicenergistics.common.tiles.TileEssentiaVibrationChamber;
import thaumicenergistics.common.tiles.abstraction.TileEVCBase;

/**
 * Helper class for working with Thaumcraft TileEntity essentia containers.
 *
 * @author Nividica
 *
 */
public final class EssentiaTileContainerHelper {

    /**
     * Singleton
     */
    public static final EssentiaTileContainerHelper INSTANCE = new EssentiaTileContainerHelper();

    /**
     * Cache the permission class
     */
    public final IThETransportPermissions perms = ThEApi.instance().transportPermissions();

    /**
     * Extracts essentia from a container based on the specified fluid stack type and amount.
     *
     * @param container
     * @param request
     * @param mode
     * @return
     */
    public FluidStack extractFromContainer(final IAspectContainer container, final FluidStack request,
            final Actionable mode) {
        // Ensure there is a request
        if ((request == null) || (request.getFluid() == null) || (request.amount == 0)) {
            // No request
            return null;
        }

        // Get the fluid
        Fluid fluid = request.getFluid();

        // Ensure it is a gas
        if (!(fluid instanceof GaseousEssentia)) {
            // Not a gas
            return null;
        }

        // Get the gas's aspect
        Aspect gasAspect = ((GaseousEssentia) fluid).getAspect();

        // Get the amount to extract
        long amountToDrain_EU = EssentiaConversionHelper.INSTANCE.convertFluidAmountToEssentiaAmount(request.amount);

        // Extract
        long extractedAmount_EU = this.extractFromContainer(container, (int) amountToDrain_EU, gasAspect, mode);

        // Was any extracted?
        if (extractedAmount_EU <= 0) {
            // None extracted
            return null;
        }

        // Return the extracted amount
        return new FluidStack(
                fluid,
                (int) EssentiaConversionHelper.INSTANCE.convertEssentiaAmountToFluidAmount(extractedAmount_EU));
    }

    /**
     * Extracts the specified aspect and amount from the container.
     *
     * @param container
     * @param amountToDrain
     * @param aspectToDrain
     * @param mode
     * @return The amount extracted.
     */
    public long extractFromContainer(final IAspectContainer container, int amountToDrain, final Aspect aspectToDrain,
            final Actionable mode) {

        // Is the request empty?
        if (amountToDrain == 0) {
            // Empty request
            return 0;
        }

        // Is the container whitelisted?
        if (!this.perms.canExtractFromAspectContainerTile(container)) {
            // Not whitelisted
            return 0;
        }

        // Get how much is in the container
        int containerAmount = container.getAspects().getAmount(aspectToDrain);

        // Is the container empty?
        if (containerAmount == 0) {
            // Empty container, or does not contain the wanted aspect
            return 0;
        }
        // Is the drain for more than is in the container?
        if (amountToDrain > containerAmount) {
            amountToDrain = containerAmount;
        }

        // Are we really draining, or just simulating?
        if (mode == Actionable.MODULATE) {
            if (!container.takeFromContainer(aspectToDrain, amountToDrain)) return 0;
        }

        // Return how much was drained
        return amountToDrain;
    }

    @Nullable
    public Aspect getAspectInContainer(final IAspectContainer container) {
        if (container == null) {
            return null;
        }

        // Get the list of aspects in the container
        AspectList aspectList = container.getAspects();

        if (aspectList == null) {
            return null;
        }

        return aspectList.getAspectsSortedAmount()[0];
    }

    @Nullable
    public ObjectLongPair<Aspect> getAspectStackFromContainer(final IAspectContainer container) {
        // Ensure we have a container
        if (container == null) {
            return null;
        }

        // Get the list of aspects in the container
        AspectList aspectList = container.getAspects();

        if (aspectList == null) {
            return null;
        }

        Aspect aspect = aspectList.getAspectsSortedAmount()[0];
        if (aspect == null) {
            return null;
        }

        return new ObjectLongImmutablePair<>(aspect, aspectList.getAmount(aspect));
    }

    public List<AEEssentiaStack> getEssentiaStacksFromContainer(final IAspectContainer container) {
        if (container == null) {
            return Collections.emptyList();
        }

        // Get the list of aspects in the container
        AspectList aspectList = container.getAspects();

        if (aspectList == null) {
            return Collections.emptyList();
        }

        List<AEEssentiaStack> stacks = new ArrayList<>();

        for (Entry<Aspect, Integer> entry : aspectList.aspects.entrySet()) {
            if ((entry != null) && (entry.getValue() != 0)) {
                stacks.add(new AEEssentiaStack(entry.getKey(), entry.getValue()));
            }
        }

        return stacks;
    }

    /**
     * Returns the capacity of the specified container.
     *
     * @param container
     * @return Capacity or -1 if unknown capacity.
     */
    public int getContainerCapacity(final IAspectContainer container) {
        return this.perms.getAspectContainerTileCapacity(container);
    }

    public int getContainerStoredAmount(final IAspectContainer container, final Aspect aspect) {
        if (!this.perms.doesAspectContainerTileShareCapacity(container)) {
            AspectList storedAspects = container.getAspects();
            return storedAspects != null ? storedAspects.getAmount(aspect) : 0;
        }

        AspectList aspectList = container.getAspects();

        if (aspectList == null) {
            return 0;
        }

        int stored = 0;

        for (Integer amount : aspectList.aspects.values()) {
            stored += amount;
        }

        return stored;
    }

    /**
     * Attempts to inject essentia into the container. Returns the amount that was injected.
     *
     * @param container
     * @param amountToFill
     * @param aspectToFill
     * @param mode
     * @return
     */
    public long injectEssentiaIntoContainer(final IAspectContainer container, int amountToFill,
            final Aspect aspectToFill, final Actionable mode) {
        // Is the container whitelisted?
        if (!this.perms.canInjectToAspectContainerTile(container)) {
            // Not whitelisted
            return 0;
        }

        // Get the aspect in the container
        Aspect storedEssentia = this.getAspectInContainer(container);

        // Match types on jars
        if (storedEssentia != null && container instanceof TileJarFillable) {
            // Do the aspects match?
            if (aspectToFill != storedEssentia) {
                // Aspects do not match;
                return 0;
            }
        } else if (!(container.doesContainerAccept(aspectToFill))) {
            // Container will not accept this aspect
            return 0;
        }

        // Get how much the container can hold
        int containerCurrentCapacity = this.getContainerCapacity(container)
                - this.getContainerStoredAmount(container, aspectToFill);

        // Is there more to fill than the container will hold?
        if (amountToFill > containerCurrentCapacity) {
            amountToFill = containerCurrentCapacity;
        }

        // Are we really filling, or simulating?
        if (mode == Actionable.MODULATE) {
            // Attempt to inject the full amount
            int remaining = container.addToContainer(aspectToFill, amountToFill);

            // Subtract any that could not be injected
            amountToFill -= remaining;
        }

        return amountToFill;
    }

    /**
     * Setup the standard white list
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void registerDefaultContainers() {
        // Alembic
        this.perms.addAspectContainerTileToExtractPermissions(TileAlembic.class, 32);

        // Centrifuge
        this.perms.addAspectContainerTileToExtractPermissions(TileCentrifuge.class, 0);

        // Jars
        this.perms.addAspectContainerTileToBothPermissions(TileJarFillable.class, 64);
        this.perms.addAspectContainerTileToBothPermissions(TileJarFillableVoid.class, 64);

        // Essentia buffer
        this.perms.addAspectContainerTileToExtractPermissions(TileTubeBuffer.class, 8);
        this.perms.addAspectContainerTileToInjectPermissions(TileTubeBuffer.class, 8);

        // Essentia reservoir
        this.perms.addAspectContainerTileToBothPermissions(TileEssentiaReservoir.class, 256);

        // Advanced alchemical furnace
        this.perms.addAspectContainerTileToExtractPermissions(TileAlchemyFurnaceAdvancedNozzle.class, 0);

        // Essentia vibration chamber
        this.perms.addAspectContainerTileToInjectPermissions(
                TileEssentiaVibrationChamber.class,
                TileEVCBase.MAX_ESSENTIA_STORED);

        try {
            Class c = Class.forName("flaxbeard.thaumicexploration.tile.TileEntityTrashJar");
            this.perms.addAspectContainerTileToInjectPermissions(c, 64);
            c = Class.forName("flaxbeard.thaumicexploration.tile.TileEntityBoundJar");
            this.perms.addAspectContainerTileToBothPermissions(c, 64);
        } catch (Exception ignored) {}
        try {
            Class c = Class.forName("makeo.gadomancy.common.blocks.tiles.TileRemoteJar");
            this.perms.addAspectContainerTileToBothPermissions(c, 64);
            c = Class.forName("makeo.gadomancy.common.blocks.tiles.TileStickyJar");
            this.perms.addAspectContainerTileToBothPermissions(c, 64);
        } catch (Exception ignored) {}
        try {
            Class c = Class.forName("makeo.gadomancy.common.blocks.tiles.TileEssentiaCompressor");
            this.perms.addAspectStorageTileToBothPermissions(c);
        } catch (Exception ignored) {}
        try {
            // Kekztech jars finally have a namespace :)
            Class c = Class.forName("kekztech.common.tileentities.TileEntityIchorJar");
            this.perms.addAspectContainerTileToBothPermissions(c, 4096);
            c = Class.forName("kekztech.common.tileentities.TileEntityIchorVoidJar");
            this.perms.addAspectContainerTileToBothPermissions(c, 4096);
            c = Class.forName("kekztech.common.tileentities.TileEntityThaumiumReinforcedJar");
            this.perms.addAspectContainerTileToBothPermissions(c, 256);
            c = Class.forName("kekztech.common.tileentities.TileEntityThaumiumReinforcedVoidJar");
            this.perms.addAspectContainerTileToBothPermissions(c, 256);
        } catch (Exception ignored) {}
        try {
            Class c = Class.forName("tuhljin.automagy.tiles.TileEntityJarCreative");
            this.perms.addAspectContainerTileToBothPermissions(c, 1 << 31);
        } catch (Exception ignored) {}
    }
}
