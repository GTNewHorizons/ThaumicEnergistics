package thaumicenergistics.common.tiles.abstraction;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.Nullable;

import appeng.api.config.Actionable;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectIntMutablePair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectSource;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.api.tiles.IEssentiaTransportWithSimulate;
import thaumicenergistics.common.utils.EffectiveSide;

/**
 * Essentia Vibration Chamber Base Handles most of the mod-interface functionality.
 *
 * @author Nividica
 *
 */
public abstract class TileEVCBase extends AENetworkTile implements IEssentiaTransportWithSimulate, IAspectSource {

    /**
     * NBT Key for the stored aspect stack.
     */
    public static final String NBTKEY_STORED = "StoredEssentia";

    /**
     * The maximum amount of stored essentia.
     */
    public static final int MAX_ESSENTIA_STORED = 64;

    /**
     * Maximum reciprocal
     */
    public static final float MAX_ESSENTIA_STORED_RECIPROCAL = 1.0f / MAX_ESSENTIA_STORED;

    /**
     * Stored Essentia
     */
    @Nullable
    protected ObjectIntPair<Aspect> storedEssentia = null;

    /**
     * @return true if the EVC accepts the specified aspect.
     */
    public static boolean acceptsAspect(final Aspect aspect) {
        return aspect == Aspect.FIRE || aspect == Aspect.ENERGY;
    }

    /**
     * Add essentia to the EVC.
     *
     * @param aspect
     * @param amount
     * @param mode
     * @return
     */
    protected abstract int addEssentia(final Aspect aspect, final int amount, final Actionable mode);

    @Override
    protected ItemStack getItemFromTile(final Object obj) {
        // Return the itemstack that visually represents this tile
        return ThEApi.instance().blocks().EssentiaVibrationChamber.getStack();
    }

    protected abstract void NBTRead(NBTTagCompound data);

    protected abstract void NBTWrite(NBTTagCompound data);

    @SideOnly(Side.CLIENT)
    protected abstract void networkRead(ByteBuf stream);

    protected abstract void networkWrite(ByteBuf stream);

    @Override
    public int addEssentia(final Aspect aspect, final int amount, final ForgeDirection side) {
        return this.addEssentia(aspect, amount, Actionable.MODULATE);
    }

    @Override
    public int addEssentia(final Aspect aspect, final int amount, final ForgeDirection side, final Actionable mode) {
        return this.addEssentia(aspect, amount, mode);
    }

    @Override
    public int addToContainer(final Aspect aspect, final int amount) {
        return this.addEssentia(aspect, amount, Actionable.MODULATE);
    }

    @Override
    public boolean canInputFrom(final ForgeDirection side) {
        return (side != this.getForward());
    }

    /**
     * Can not output.
     */
    @Override
    public boolean canOutputTo(final ForgeDirection side) {
        return false;
    }

    @Override
    public int containerContains(final Aspect aspect) {
        int storedAmount = 0;

        // Is the aspect stored?
        if (this.storedEssentia != null && this.storedEssentia.left() == aspect) {
            storedAmount = this.storedEssentia.rightInt();
        }

        return storedAmount;
    }

    @Override
    public boolean doesContainerAccept(final Aspect aspect) {
        // Is there stored essentia?
        if (this.storedEssentia != null) {
            // Match to stored essentia
            return aspect == this.storedEssentia.left();
        }

        // Nothing is stored, accepts ignis or potentia
        return TileEVCBase.acceptsAspect(aspect);
    }

    @Deprecated
    @Override
    public boolean doesContainerContain(final AspectList aspectList) {
        // Is there not stored essentia?
        if (this.storedEssentia == null) {
            return false;
        }

        return aspectList.aspects.containsKey(this.storedEssentia.left());
    }

    @Override
    public boolean doesContainerContainAmount(final Aspect aspect, final int amount) {
        // Does the stored essentia match the aspect?
        if (this.storedEssentia == null || this.storedEssentia.left() != aspect) {
            // Does not match
            return false;
        }

        return this.storedEssentia.rightInt() >= amount;
    }

    @Override
    public AspectList getAspects() {
        // Create a new list
        AspectList aspectList = new AspectList();

        // Is there stored essentia?
        if (this.storedEssentia != null) {
            // Add the essentia aspect and amount
            aspectList.add(this.storedEssentia.left(), this.storedEssentia.rightInt());
        }

        return aspectList;
    }

    @Override
    public AECableType getCableConnectionType(final ForgeDirection dir) {
        return AECableType.COVERED;
    }

    @Override
    public int getEssentiaAmount(final ForgeDirection side) {
        return (this.storedEssentia != null ? this.storedEssentia.rightInt() : 0);
    }

    @Override
    public Aspect getEssentiaType(final ForgeDirection side) {
        return (this.storedEssentia != null ? this.storedEssentia.left() : null);
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    /**
     * Can not output.
     */
    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public int getSuctionAmount(final ForgeDirection side) {
        // Suction is based on how full the chamber is, as it fills up suction drops

        // Get how much is stored
        float stored = (this.storedEssentia == null ? 0.0f : this.storedEssentia.rightInt());
        if (stored == MAX_ESSENTIA_STORED) {
            return 0;
        }

        // Calculate the ratio, minimum of 25%, and multiply against maximum suction
        return (int) (128 * (1.0f - ((stored * 0.75f) * MAX_ESSENTIA_STORED_RECIPROCAL)));
    }

    @Override
    public Aspect getSuctionType(final ForgeDirection side) {
        // Is there anything stored?
        if (this.storedEssentia != null) {
            // Suction type must match what is stored
            return this.storedEssentia.left();
        }

        // Rotate into Potentia?
        if ((MinecraftServer.getServer().getTickCounter() % 200) > 100) {
            // Set to Potentia
            return Aspect.ENERGY;
        }

        // Default to Ignis
        return Aspect.FIRE;
    }

    @Override
    public boolean isConnectable(final ForgeDirection side) {
        return (side != this.getForward());
    }

    private static final String NBTKEY_ASPECT_TAG = "AspectTag", NBTKEY_ASPECT_AMOUNT = "Amount";

    @TileEvent(TileEventType.WORLD_NBT_READ)
    public final void onNBTLoad(final NBTTagCompound data) {
        if (data.hasKey(TileEVCBase.NBTKEY_STORED)) {
            NBTTagCompound storedTag = data.getCompoundTag(TileEVCBase.NBTKEY_STORED);
            Aspect aspect = Aspect.aspects.get(storedTag.getString(NBTKEY_ASPECT_TAG));
            int amount = (int) storedTag.getLong(NBTKEY_ASPECT_AMOUNT);
            if (aspect != null && amount > 0) {
                this.storedEssentia = new ObjectIntMutablePair<>(aspect, amount);
            }
        }

        // Call sub
        this.NBTRead(data);
    }

    @TileEvent(TileEventType.WORLD_NBT_WRITE)
    public final void onNBTSave(final NBTTagCompound data) {
        // Save storage
        if (this.storedEssentia != null) {
            NBTTagCompound stack = new NBTTagCompound();
            stack.setString(NBTKEY_ASPECT_TAG, this.storedEssentia.left().getTag());
            stack.setLong(NBTKEY_ASPECT_AMOUNT, this.storedEssentia.rightInt());

            // Write into data
            data.setTag(TileEVCBase.NBTKEY_STORED, stack);
        }

        // Call sub
        this.NBTWrite(data);
    }

    @TileEvent(TileEventType.NETWORK_READ)
    @SideOnly(Side.CLIENT)
    public final boolean onNetworkRead(final ByteBuf stream) {
        // Anything stored?
        if (stream.readBoolean()) {
            if (this.storedEssentia == null) {
                this.storedEssentia = new ObjectIntMutablePair<>(
                        Aspect.aspects.get(ByteBufUtils.readUTF8String(stream)),
                        stream.readInt());
            } else {
                this.storedEssentia.left(Aspect.aspects.get(ByteBufUtils.readUTF8String(stream)));
                this.storedEssentia.right(stream.readInt());
            }
        } else {
            // Null out the stack
            this.storedEssentia = null;
        }

        // Call sub
        this.networkRead(stream);

        return true;
    }

    @TileEvent(TileEventType.NETWORK_WRITE)
    public final void onNetworkWrite(final ByteBuf stream) throws IOException {
        // Is there anything stored?
        boolean hasStored = this.storedEssentia != null;

        // Write stored
        stream.writeBoolean(hasStored);
        if (hasStored) {
            // Write the stack
            ByteBufUtils.writeUTF8String(stream, this.storedEssentia.left().getTag());
            stream.writeInt(this.storedEssentia.rightInt());
        }

        // Call sub
        this.networkWrite(stream);
    }

    /**
     * Sets up the chamber
     *
     * @return
     */
    @Override
    public void onReady() {
        // Call super
        super.onReady();

        // Ignored on client side
        if (EffectiveSide.isServerSide()) {
            // Set idle power usage to zero
            this.getProxy().setIdlePowerUsage(0.0D);
        }
    }

    /**
     * Full block, not extension needed.
     */
    @Override
    public boolean renderExtendedTube() {
        return false;
    }

    @Override
    public void setAspects(final AspectList aspectList) {
        // Ignored
    }

    /**
     * Sets the owner of this tile.
     *
     * @param player
     */
    public void setOwner(final EntityPlayer player) {
        this.getProxy().setOwner(player);
    }

    @Override
    public void setSuction(final Aspect aspect, final int amount) {
        // Ignored
    }

    /**
     * Can not output.
     */
    @Override
    public int takeEssentia(final Aspect aspect, final int amount, final ForgeDirection side) {
        return 0;
    }

    /**
     * Can not output.
     */
    @Override
    public boolean takeFromContainer(final Aspect aspect, final int amount) {
        return false;
    }

    /**
     * Can not output.
     */
    @Deprecated
    @Override
    public boolean takeFromContainer(final AspectList arg0) {
        return false;
    }
}
