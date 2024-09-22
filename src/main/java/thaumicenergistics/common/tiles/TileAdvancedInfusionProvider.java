package thaumicenergistics.common.tiles;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

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

    public List<TileInfusionMatrix> matrixs = new ArrayList<>();
    private List<TileInfusionMatrix> waitToRemoveMatrix = new ArrayList<>();

    private int tickCounter = 50;

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
        if (this.matrixs.isEmpty()) {
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

        // If allready bind infusion matrix or worldObj is null
        if ((this.matrixs != null && !this.matrixs.isEmpty()) || this.worldObj == null) {
            // Do nothing
            return;
        }

        int x = this.xCoord;
        int y = this.yCoord;
        int z = this.zCoord;

        for (int dx = -12; dx <= 12; dx++) {
            for (int dy = -5; dy <= 5; dy++) {
                for (int dz = -12; dz <= 12; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    this.bindMatrixs(x + dx, y + dy, z + dz);
                }
            }
        }
    }

    public void bindMatrixs(final int x, final int y, final int z) {
        if (this.worldObj != null && this.worldObj.getTileEntity(x, y, z) instanceof TileInfusionMatrix tim) {
            this.matrixs.add(tim);

            this.markForUpdate();
            this.saveChanges();
        }
    }

    public void unbindMatrixs() {
        this.matrixs.clear();

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
    @TileEvent(TileEventType.WORLD_NBT_WRITE)
    public void onSaveNBT(final NBTTagCompound data) {
        super.onSaveNBT(data);
    }

    @Override
    @TileEvent(TileEventType.WORLD_NBT_READ)
    public void onLoadNBT(final NBTTagCompound data) {
        super.onLoadNBT(data);
    }

    @Override
    public void addWailaInformation(List<String> tooltip) {
        super.addWailaInformation(tooltip);
        if (this.matrixs == null || (this.matrixs != null && this.matrixs.isEmpty())) {
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
                            this.matrixs.size()));
        }
    }

    @TileEvent(TileEventType.TICK)
    public void onTick() {
        // Try binding matrix every 5s
        if (++tickCounter >= 100 && this.matrixs.isEmpty() && this.isActive) {
            this.searchMatrix();

            tickCounter = 0;
        }

        if (!this.matrixs.isEmpty() && this.isActive) {
            for (TileInfusionMatrix matrix : this.matrixs) {
                if (matrix == null) {
                    this.waitToRemoveMatrix.add(matrix);
                } else {
                    this.grabAllAspects(matrix);
                }
            }
        }

        if (!this.waitToRemoveMatrix.isEmpty()) {
            this.matrixs.removeAll(this.waitToRemoveMatrix);
            this.waitToRemoveMatrix.clear();
            this.markForUpdate();
            this.saveChanges();
        }
    }

}
