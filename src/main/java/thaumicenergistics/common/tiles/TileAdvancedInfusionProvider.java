package thaumicenergistics.common.tiles;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectSource;
import thaumcraft.common.tiles.TileInfusionMatrix;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.common.registries.ThEStrings;
import thaumicenergistics.implementaion.ThEMultiCraftingTracker;

/**
 * Upgrade version of {@link TileInfusionProvider} works like intercepter and old provider can auto order essential it
 * need
 * 
 * @author MCTBL
 * 
 */
public class TileAdvancedInfusionProvider extends TileInfusionProvider implements IAspectSource {

    public List<TileInfusionMatrix> matrices = new ArrayList<>();
    private List<TileInfusionMatrix> matricesToRemove = new ArrayList<>();

    public static final int HORIZONTAL_RADIUS = 12;
    public static final int VERTICAL_RADIUS = 5;
    private int currentZ = -HORIZONTAL_RADIUS;

    public TileAdvancedInfusionProvider() {
        super();
        this.craftingTracker = new ThEMultiCraftingTracker(this, 16);
    }

    /**
     * How much power does this require just to be active?
     */
    @Override
    protected double getIdlePowerusage() {
        return 50d;
    }

    @Override
    protected ItemStack getItemFromTile(final Object obj) {
        // Return the itemstack the visually represents this tile
        return ThEApi.instance().blocks().AdvancedInfusionProvider.getStack();
    }

    @Override
    public boolean takeFromContainer(final Aspect tag, final int amount) {
        // Not in advanced mode
        if (this.matrices.isEmpty()) {
            // Can we extract the essentia from the network?
            if (this.extractEssentiaFromNetwork(tag, amount, true) == amount) {
                // Show partical FX
                this.doParticalFX(tag.getColor());

                return true;
            } else {
                this.orderSomeEssentia(tag);
            }
        }
        return false;
    }

    public void searchMatrix() {

        // If worldObj is null
        if (this.worldObj == null) {
            // Do nothing
            return;
        }

        for (int dx = -HORIZONTAL_RADIUS; dx <= HORIZONTAL_RADIUS; dx++) {
            for (int dy = -VERTICAL_RADIUS; dy <= VERTICAL_RADIUS; dy++) {
                if (dx == 0 && dy == 0 && currentZ == 0) continue;
                this.bindMatrices(this.xCoord + dx, this.yCoord + dy, this.zCoord + this.currentZ);
            }
        }

        if (++this.currentZ > HORIZONTAL_RADIUS) {
            this.currentZ = -HORIZONTAL_RADIUS;
        }
    }

    public void bindMatrices(final int x, final int y, final int z) {
        if (this.worldObj != null && this.worldObj.getTileEntity(x, y, z) instanceof TileInfusionMatrix tim) {
            if (this.matrices.contains(tim)) return;
            this.matrices.add(tim);

            this.markForUpdate();
            this.saveChanges();
        }
    }

    public void unbindMatrices() {
        this.matrices.clear();

        this.markForUpdate();
        this.saveChanges();
    }

    public void grabAllAspects(final TileInfusionMatrix matrix) {
        if (matrix.getAspects().size() != 0) {

            AspectList aspectList = matrix.getAspects().copy();
            for (Aspect aspect : aspectList.getAspects()) {
                if (aspect != null) {
                    int needAspectsAmount = aspectList.getAmount(aspect);
                    int extractAmount = this.extractEssentiaFromNetwork(aspect, needAspectsAmount, false);

                    if (needAspectsAmount != extractAmount) {
                        this.orderSomeEssentia(aspect, needAspectsAmount - extractAmount);
                    }

                    // Try to remove essentia from network and render runes
                    if (extractAmount > 0) {
                        this.doParticalFX(aspect.getColor());
                        matrix.getAspects().remove(aspect, extractAmount);
                    }
                }
            }
        }
    }

    @Override
    public void addWailaInformation(List<String> tooltip) {
        super.addWailaInformation(tooltip);
        if (this.matrices.isEmpty()) {
            tooltip.add(
                    ThEStrings.Tooltip_AdvancedInfusionProviderWorkingMode.getLocalized() + ":"
                            + ThEStrings.Tooltip_AdvancedInfusionProviderNormalMode.getLocalized());
        } else {
            tooltip.add(
                    ThEStrings.Tooltip_AdvancedInfusionProviderWorkingMode.getLocalized() + ":"
                            + ThEStrings.Tooltip_AdvancedInfusionProviderAdvancedMode.getLocalized());
            tooltip.add(
                    String.format(
                            ThEStrings.Tooltip_AdvancedInfusionProviderTotalBind.getLocalized(),
                            this.matrices.size()));
        }
    }

    @TileEvent(TileEventType.TICK)
    public void onTick() {
        // Try binding matrix when is active
        if (this.isActive) {
            this.searchMatrix();
        }

        if (!this.matrices.isEmpty() && this.isActive) {
            for (TileInfusionMatrix matrix : this.matrices) {
                if (matrix == null || matrix.getWorldObj().isAirBlock(matrix.xCoord, matrix.yCoord, matrix.zCoord)) {
                    this.matricesToRemove.add(matrix);
                } else {
                    this.grabAllAspects(matrix);
                }
            }
        }

        if (!this.matricesToRemove.isEmpty()) {
            this.matrices.removeAll(this.matricesToRemove);
            this.matricesToRemove.clear();
            this.markForUpdate();
            this.saveChanges();
        }
    }

}
