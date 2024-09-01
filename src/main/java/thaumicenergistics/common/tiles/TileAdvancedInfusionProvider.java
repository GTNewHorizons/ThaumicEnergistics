package thaumicenergistics.common.tiles;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectSource;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.tiles.TileInfusionMatrix;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.common.registries.ThEStrings;
import thaumicenergistics.common.tiles.abstraction.TileProviderBase;

public class TileAdvancedInfusionProvider extends TileProviderBase implements IAspectSource {

    private static final String NBT_MATRIX_X = "MatrixX", NBT_MATRIX_Y = "MatrixY", NBT_MATRIX_Z = "MatrixZ",
            NBT_HAS_ADD_STABILITY = "HasAddStability";

    public TileInfusionMatrix matrix = null;

    private Integer matrixX = null, matrixY = null, matrixZ = null;

    public boolean hasAddStability = false;

    /**
     * Shows runes on the infusion provider.
     *
     * @param aspectColor
     */
    private void doParticalFX(final int aspectColor) {
        // Convert each color to percentage
        float red = (aspectColor & 0xFF0000) / (float) 0xFF0000;
        float green = (aspectColor & 0x00FF00) / (float) 0x00FF00;
        float blue = (aspectColor & 0x0000FF) / (float) 0x0000FF;

        // Add particles
        for (int i = 0; i < 5; i++) {
            Thaumcraft.proxy
                    .blockRunes(this.worldObj, this.xCoord, this.yCoord, this.zCoord, red, green, blue, 15, -0.1F);
        }
        for (int i = 0; i < 5; i++) {
            Thaumcraft.proxy
                    .blockRunes(this.worldObj, this.xCoord, this.yCoord, this.zCoord, red, green, blue, 15, 0.1F);
        }
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
        if (this.matrix == null) {
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

    @Override
    public AspectList getAspects() {
        return null;
    }

    @Override
    public void setAspects(AspectList var1) {}

    @Override
    public boolean doesContainerAccept(Aspect var1) {
        return false;
    }

    @Override
    public int addToContainer(Aspect var1, int var2) {
        return 0;
    }

    @Override
    public boolean takeFromContainer(AspectList var1) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect var1, int var2) {
        return false;
    }

    @Override
    public boolean doesContainerContain(AspectList var1) {
        return false;
    }

    @Override
    public int containerContains(Aspect var1) {
        return 0;
    }

    public void searchMatrix() {
        if (this.matrix == null && this.worldObj != null) {
            int x = this.xCoord;
            int y = this.yCoord;
            int z = this.zCoord;
            for (int dx = -4; dx <= 4; dx++) {
                for (int dy = -4; dy <= 4; dy++) {
                    for (int dz = -4; dz <= 4; dz++) {
                        if (this.bindMatrix(x + dx, y + dy, z + dz)) {
                            this.matrixX = x + dx;
                            this.matrixY = y + dy;
                            this.matrixZ = z + dz;
                            return;
                        }
                    }
                }
            }
        }
    }

    public boolean bindMatrix(final int x, final int y, final int z) {
        if (this.worldObj != null && this.worldObj.getTileEntity(x, y, z) instanceof TileInfusionMatrix tim) {
            this.matrix = tim;

            this.markForUpdate();
            this.saveChanges();
            return true;
        } else {
            return false;
        }
    }

    public boolean bindMatrix() {
        return this.bindMatrix(this.matrixX, this.matrixY, this.matrixZ);
    }

    public void grabAllAspects() {
        if (this.matrix.getAspects().size() != 0) {
            AspectList aspectList = this.matrix.getAspects().copy();
            for (Aspect aspect : aspectList.getAspects()) {
                if (aspect != null) {
                    int needAspectsAmount = aspectList.getAmount(aspect);
                    int extractAmount = this.extractEssentiaFromNetwork(aspect, needAspectsAmount, false);

                    if (needAspectsAmount != extractAmount) {
                        this.orderSomeEssentia(aspect, needAspectsAmount - extractAmount);
                    }
                    this.doParticalFX(aspect.getColor());
                    matrix.getAspects().remove(aspect, extractAmount);
                }
            }
        }
    }

    @Override
    @TileEvent(TileEventType.WORLD_NBT_WRITE)
    public void onSaveNBT(final NBTTagCompound data) {
        if (this.matrix != null) {
            data.setInteger(NBT_MATRIX_X, this.matrix.xCoord);
            data.setInteger(NBT_MATRIX_Y, this.matrix.yCoord);
            data.setInteger(NBT_MATRIX_Z, this.matrix.zCoord);
        }
        data.setBoolean(NBT_HAS_ADD_STABILITY, this.hasAddStability);
        super.onSaveNBT(data);
    }

    @Override
    @TileEvent(TileEventType.WORLD_NBT_READ)
    public void onLoadNBT(final NBTTagCompound data) {
        if (data.hasKey(NBT_MATRIX_X) && data.hasKey(NBT_MATRIX_Y) && data.hasKey(NBT_MATRIX_Z)) {
            this.matrixX = data.getInteger(NBT_MATRIX_X);
            this.matrixY = data.getInteger(NBT_MATRIX_Y);
            this.matrixZ = data.getInteger(NBT_MATRIX_Z);
        }
        if (data.hasKey(NBT_HAS_ADD_STABILITY)) {
            this.hasAddStability = data.getBoolean(NBT_HAS_ADD_STABILITY);
        }
        super.onLoadNBT(data);
    }

    @Override
    public void addWailaInformation(List<String> tooltip) {
        super.addWailaInformation(tooltip);
        if (this.matrix == null) {
            tooltip.add(
                    ThEStrings.Tooltip_AdvancedInfusionProviderWorkingMode.getLocalized() + ":"
                            + ThEStrings.Tooltip_AdvancedInfusionProviderNormalMode.getLocalized());
        } else {
            tooltip.add(
                    ThEStrings.Tooltip_AdvancedInfusionProviderWorkingMode.getLocalized() + ":"
                            + ThEStrings.Tooltip_AdvancedInfusionProviderAdvancedMode.getLocalized());
            tooltip.add(
                    String.format(
                            ThEStrings.Tooltip_AdvancedInfusionProviderBindTo.getLocalized(),
                            this.matrixX,
                            this.matrixY,
                            this.matrixZ));
        }
    }

    @TileEvent(TileEventType.TICK)
    public void onTick() {
        if (this.matrix == null && this.isActive) {
            if (this.matrixX == null || this.matrixY == null || this.matrixZ == null) {
                this.searchMatrix();
            } else {
                this.bindMatrix();
            }
        }
        if (this.isActive && this.matrix != null) {
            this.grabAllAspects();
        }
    }

}
