package thaumicenergistics.common.tiles;

import java.util.List;

import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectSource;
import thaumcraft.common.tiles.TileInfusionMatrix;
import thaumicenergistics.common.registries.ThEStrings;
import thaumicenergistics.common.tiles.abstraction.TileProviderBase;
import thaumicenergistics.common.utils.ThELog;

public class TileAdvancedInfusionProvider extends TileProviderBase implements IAspectSource {
	
	private static final String NBT_MATRIX_X = "MatrixX", NBT_MATRIX_Y = "MatrixY", NBT_MATRIX_Z = "MatrixZ";

	private TileInfusionMatrix matrix = null;
	
	private Integer MatrixX = null, MatrixY = null, MatrixZ = null;

	@Override
	public AspectList getAspects() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAspects(AspectList var1) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean doesContainerAccept(Aspect var1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int addToContainer(Aspect var1, int var2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean takeFromContainer(Aspect var1, int var2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean takeFromContainer(AspectList var1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean doesContainerContainAmount(Aspect var1, int var2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean doesContainerContain(AspectList var1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int containerContains(Aspect var1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected double getIdlePowerusage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected ItemStack getItemFromTile(Object obj) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void setUpMatrixCoordinate(final int x, final int y, final int z) {
		this.MatrixX = x;
		this.MatrixY = y;
		this.MatrixZ = z;
		this.getMatrixTile();
	}
	
	public void getMatrixTile() {
		try {
			TileEntity tileEntity = worldObj.getTileEntity(MatrixX, MatrixY, MatrixZ);
			if (tileEntity != null && tileEntity instanceof TileInfusionMatrix infusionMatrix) {
				this.matrix = infusionMatrix;
			}
			this.markForUpdate();
			this.saveChanges();
			
		} catch (Exception e) {
			ThELog.error(e, "error");
		}
	}
	
	@Override
	public void onReady() {
		super.onReady();
		if(this.matrix == null && this.MatrixX != null && this.MatrixY != null && this.MatrixZ != null) {
			this.getMatrixTile();
		}
	}
	
	
	@Override
	@TileEvent(TileEventType.WORLD_NBT_WRITE)
	public void onSaveNBT(final NBTTagCompound data) {
		super.onSaveNBT(data);
		if(this.matrix != null) {
			data.setInteger(NBT_MATRIX_X, this.matrix.xCoord);
			data.setInteger(NBT_MATRIX_Y, this.matrix.yCoord);
			data.setInteger(NBT_MATRIX_Z, this.matrix.zCoord);
		}
	}
	
	@Override
	@TileEvent(TileEventType.WORLD_NBT_READ)
	public void onLoadNBT(final NBTTagCompound data) {
		super.onLoadNBT(data);
		if (data.hasKey(NBT_MATRIX_X) && data.hasKey(NBT_MATRIX_Y) && data.hasKey(NBT_MATRIX_Z)) {
			this.MatrixX = data.getInteger(NBT_MATRIX_X);
			this.MatrixY = data.getInteger(NBT_MATRIX_Y);
			this.MatrixZ = data.getInteger(NBT_MATRIX_Z);
		}
	}

	@Override
	public void addWailaInformation(List<String> tooltip) {
		super.addWailaInformation(tooltip);
		if (this.matrix == null) {
			tooltip.add(ThEStrings.Tooltip_AdvancedInfusionProviderWorkingMode.getLocalized() + ":"
					+ ThEStrings.Tooltip_AdvancedInfusionProviderNormalMode.getLocalized());
		} else {
			tooltip.add(ThEStrings.Tooltip_AdvancedInfusionProviderWorkingMode.getLocalized() + ":"
					+ ThEStrings.Tooltip_AdvancedInfusionProviderAdvancedMode.getLocalized());
		}
	}
}
