package thaumicenergistics.common.tiles;

import net.minecraft.item.ItemStack;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectSource;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.common.network.packet.client.Packet_R_ParticleFX;
import thaumicenergistics.common.tiles.abstraction.TileProviderBase;

/**
 * Provides essentia to devices via Infusion mechanics.
 *
 * @author Nividica
 *
 */
public class TileInfusionProvider extends TileProviderBase implements IAspectSource {

    public void doParticalFX(final int aspectColor) {
        if (!this.worldObj.isRemote) {
            Packet_R_ParticleFX.sendInfusionRunes(this, aspectColor);
        }
    }

    /**
     * How much power does this require just to be active?
     */
    @Override
    protected double getIdlePowerusage() {
        return 5.0;
    }

    @Override
    protected ItemStack getItemFromTile(final Object obj) {
        // Return the itemstack the visually represents this tile
        return ThEApi.instance().blocks().InfusionProvider.getStack();
    }

    @Override
    public int addToContainer(final Aspect tag, final int amount) {
        // Ignored
        return 0;
    }

    @Override
    public int containerContains(final Aspect tag) {
        // Ignored
        return 0;
    }

    @Override
    public boolean doesContainerAccept(final Aspect tag) {
        // Ignored
        return false;
    }

    @Deprecated
    @Override
    public boolean doesContainerContain(final AspectList ot) {
        // Ignored
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(final Aspect tag, final int amount) {
        // Ignored
        return false;
    }

    @Override
    public AspectList getAspects() {
        // Ignored
        return null;
    }

    @Override
    public void setAspects(final AspectList aspects) {
        // Ignored
    }

    @Override
    public boolean takeFromContainer(final Aspect tag, final int amount) {
        // Can we extract the essentia from the network?
        if (this.extractEssentiaFromNetwork(tag, amount, true) == amount) {
            // Show partical FX
            this.doParticalFX(tag.getColor());

            return true;
        } else {
            this.orderSomeEssentia(tag);
        }

        return false;
    }

    @Deprecated
    @Override
    public boolean takeFromContainer(final AspectList ot) {
        // Ignored
        return false;
    }
}
