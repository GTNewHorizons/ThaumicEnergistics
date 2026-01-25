package thaumicenergistics.common.parts;

import java.io.IOException;
import java.lang.ref.WeakReference;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.util.AEColor;
import appeng.parts.AEBasePart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.common.tiles.TileVisRelay;
import thaumicenergistics.api.grid.IDigiVisSource;
import thaumicenergistics.client.textures.BlockTextureManager;
import thaumicenergistics.common.integration.tc.DigiVisSourceData;
import thaumicenergistics.common.integration.tc.VisProviderProxy;

/**
 * Interfaces with a {@link TileVisRelay}.
 *
 * @author Nividica
 *
 */
public class PartVisInterface extends AEBasePart implements IGridTickable, IDigiVisSource {

    /**
     * NBT key for the unique ID
     */
    private static final String NBT_KEY_UID = "uid";

    /**
     * NBT key for if the interface is a source or not.
     */
    private static final String NBT_KEY_IS_PROVIDER = "isProvider";

    /**
     * NBT key for the source of this provider.
     */
    private static final String NBT_KEY_PROVIDER_SOURCE = "linkedSource";

    /**
     * The amount of time to display the color when not receiving updates
     */
    private static final int TIME_TO_CLEAR = 500;

    /**
     * The amount of power to use per each vis of a request. The amount of vis doesn't matter.
     */
    private static final int POWER_PER_REQUESTED_VIS = 4;

    /**
     * Unique ID for this interface
     */
    private long UID;

    /**
     * The aspect color we are currently draining
     */
    private int visDrainingColor = 0;

    /**
     * The last time the color was refreshed.
     */
    private long lastColorUpdate = 0;

    /**
     * Cached reference to the relay we are facing.
     */
    private WeakReference<TileVisRelay> cachedRelay = new WeakReference<TileVisRelay>(null);

    /**
     * True if this end of the P2P is a vis provider.
     */
    private boolean isProvider = false;

    /**
     * If this end is a provider, this stores the source.
     */
    private final DigiVisSourceData visP2PSourceInfo = new DigiVisSourceData();

    /**
     * If this end is a provider, this interacts with the vis network.
     */
    private VisProviderProxy visProviderSubTile = null;

    public PartVisInterface(final ItemStack is) {
        super(is);
        this.UID = System.currentTimeMillis() ^ this.hashCode();
    }

    /**
     * How often should we tick?
     */
    @Override
    public TickingRequest getTickingRequest(final IGridNode node) {
        return new TickingRequest(30, 30, false, false);
    }

    /**
     * Called when the interface ticks
     */
    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int TicksSinceLastCall) {
        if (this.visDrainingColor != 0) {
            if ((System.currentTimeMillis() - this.lastColorUpdate) > PartVisInterface.TIME_TO_CLEAR) {
                this.setDrainColor(0);
            }
        }

        // Is the interface a provider?
        if (this.isProvider) {
            boolean hasProvider = (this.visProviderSubTile != null);

            // Validate the P2P Settings
            if (!this.isP2PSourceValid()) {
                // Invalid source, remove provider
                if (hasProvider) {
                    this.visProviderSubTile.invalidate();
                    this.visProviderSubTile = null;
                }
            } else {
                boolean hasRelay = (this.getRelay() != null);

                // Invalid: Can't have a provider without a relay
                if (!hasRelay && hasProvider) {
                    // Remove the provider
                    this.visProviderSubTile.invalidate();
                    this.visProviderSubTile = null;
                }
                // Has relay but no provider
                else if (hasRelay && !hasProvider) {
                    // Create the provider
                    this.visProviderSubTile = new VisProviderProxy(this);

                    // Register the provider
                    VisNetHandler.addSource(this.getTile().getWorldObj(), this.visProviderSubTile);
                } else if (hasProvider) {
                    this.visProviderSubTile.updateEntity();
                }
            }
        }

        return TickRateModulation.SAME;
    }

    /**
     * Requests that the interface drain vis from the relay
     */
    protected int consumeVisFromVisNetwork(final Aspect digiVisAspect, final int amount) {
        // Get the relay
        TileVisRelay visRelay = this.getRelay();

        // Ensure there is a relay
        if (visRelay == null) {
            return 0;
        }

        // Get the power grid
        IGrid grid = this.getGrid();
        if (grid == null) return 0;

        IEnergyGrid eGrid = grid.getCache(IEnergyGrid.class);
        if (eGrid == null) return 0;

        // Simulate a power drain
        double drainedPower = eGrid
                .extractAEPower(PartVisInterface.POWER_PER_REQUESTED_VIS, Actionable.SIMULATE, PowerMultiplier.CONFIG);

        // Ensure we got the power we need
        if (drainedPower < PartVisInterface.POWER_PER_REQUESTED_VIS) {
            return 0;
        }

        // Ask it for vis
        int amountReceived = visRelay.consumeVis(digiVisAspect, amount);

        // Did we get any vis?
        if (amountReceived > 0) {
            // Drain the power
            eGrid.extractAEPower(PartVisInterface.POWER_PER_REQUESTED_VIS, Actionable.MODULATE, PowerMultiplier.CONFIG);
        }

        // Return the amount we received
        return amountReceived;
    }

    /**
     * Verifies that a p2p source is valid
     */
    private boolean isP2PSourceValid() {
        // Is there anything even linked to?
        if (!this.visP2PSourceInfo.hasSourceData()) {
            return false;
        }

        // Get the source
        IDigiVisSource p2pSource = this.visP2PSourceInfo.tryGetSource(this.getGrid());

        // There must be source data
        if (p2pSource == null) {
            return false;
        }

        // Source must be a vis interface
        if (!(p2pSource instanceof PartVisInterface partVisInterface)) {
            return false;
        }

        // Can't link to self
        if (this.equals(p2pSource)) {
            return false;
        }

        // Source must not be a provider
        if (partVisInterface.isVisProvider()) {
            return false;
        }

        // Source seems valid
        return true;
    }

    /**
     * Sets the color we are draining.
     */
    private void setDrainColor(final int color) {

        // Are we setting the color?
        if (color != 0) {
            // Does it match what we already have?
            if (color == this.visDrainingColor) {
                // Set the update time
                this.lastColorUpdate = System.currentTimeMillis();

                return;
            }

            // Has the alloted time passed for a change?
            if ((System.currentTimeMillis() - this.lastColorUpdate) <= (PartVisInterface.TIME_TO_CLEAR / 2)) {
                return;
            }

            // Set the update time
            this.lastColorUpdate = System.currentTimeMillis();
        }

        // Set the color
        this.visDrainingColor = color;

        // Update
        this.saveChanges();
    }

    private void setIsVisProvider(final boolean isProviding) {

        // Is the interface to be a provider?
        if (!isProviding) {
            // Clear the source info
            this.visP2PSourceInfo.clearData();

            // Null the subtile
            if (this.visProviderSubTile != null) {
                this.visProviderSubTile.invalidate();
                this.visProviderSubTile = null;
            }
        }

        this.isProvider = isProviding;
    }

    /**
     * Drains vis from either the vis relay network, or from the p2p source.
     */
    @Override
    public int consumeVis(final @NotNull Aspect digiVisAspect, final int amount) {
        // Ensure the interface is active
        if (!this.isActive()) {
            return 0;
        }

        int amountReceived = 0;

        // Is the interface a provider?
        if (this.isProvider) {
            if (this.isP2PSourceValid()) {
                // Get the p2p source
                IDigiVisSource source = this.visP2PSourceInfo.tryGetSource(this.getGrid());

                // Ask the source for vis
                amountReceived = source.consumeVis(digiVisAspect, amount);
            }
        } else {
            amountReceived = this.consumeVisFromVisNetwork(digiVisAspect, amount);
        }

        // Was any vis received?
        if (amountReceived > 0) {
            // Set the color
            this.setDrainColor(digiVisAspect.getColor());
        }

        return amountReceived;
    }

    private TileEntity getFacingTile() {
        TileEntity hostTile = this.getTile();
        if (hostTile == null) return null;

        // Get the world
        World world = hostTile.getWorldObj();
        if (world == null) // may happen during unload
            return null;

        // Get our location
        int x = hostTile.xCoord;
        int y = hostTile.yCoord;
        int z = hostTile.zCoord;

        // Get the tile entity we are facing
        return world.getTileEntity(x + this.side.offsetX, y + this.side.offsetY, z + this.side.offsetZ);
    }

    public TileVisRelay getRelay() {
        // Get the cached relay
        TileVisRelay tVR = this.cachedRelay.get();

        // Is there a cached relay?
        if (tVR != null
                && tVR == this.getHost().getTile().getWorldObj().getTileEntity(tVR.xCoord, tVR.yCoord, tVR.zCoord)) {
            // Ensure it is still there
            return tVR;
        }

        // Get the tile we are facing
        TileEntity facingTile = this.getFacingTile();

        // Is it a relay?
        if (facingTile instanceof TileVisRelay) {
            // Get the relay
            tVR = (TileVisRelay) facingTile;

            // Is it facing the same direction as we are?
            if (tVR.orientation == this.getSide().ordinal()) {
                // Set the cache
                this.cachedRelay = new WeakReference<>(tVR);

                // Return it
                return tVR;
            }
        }

        return null;
    }

    /**
     * @return the unique ID for this interface
     */
    @Override
    public long getUID() {
        return this.UID;
    }

    public Boolean isVisProvider() {
        return this.isProvider;
    }

    @Override
    public @Nullable IGrid getGrid() {
        IGridNode gridNode = this.getGridNode();
        if (gridNode == null) return null;
        return gridNode.getGrid();
    }

    @Override
    public boolean isActive() {
        IGridNode gridNode = this.getGridNode();
        if (gridNode == null) return false;
        return gridNode.isActive();
    }

    @Override
    protected boolean useMemoryCard(EntityPlayer player) {
        final ItemStack hand = player.inventory.getCurrentItem();
        if (hand == null || !(hand.getItem() instanceof IMemoryCard memoryCard)) return false;

        if (ForgeEventFactory.onItemUseStart(player, hand, 1) <= 0) return false;

        if (player.isSneaking()) {
            // Create the info data
            DigiVisSourceData data = new DigiVisSourceData(this);

            // Write into the memory card
            memoryCard.setMemoryCardContents(hand, DigiVisSourceData.SOURCE_UNLOC_NAME, data.writeToNBT());

            // Notify the user
            memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_SAVED);

            // Mark that we are not a provider
            this.setIsVisProvider(false);

            // Mark for save
            this.saveChanges();

            return true;
        } else {
            final String storedName = memoryCard.getSettingsName(hand);
            if (DigiVisSourceData.SOURCE_UNLOC_NAME.equals(storedName)) {
                NBTTagCompound data = memoryCard.getData(hand);

                // Load the info
                this.visP2PSourceInfo.readFromNBT(data);

                // Ensure there is valid data
                if (this.visP2PSourceInfo.hasSourceData()) {
                    // Can the link be established?
                    if (!this.isP2PSourceValid()) {
                        // Unable to link
                        memoryCard.notifyUser(player, MemoryCardMessages.INVALID_MACHINE);

                        // Clear the source data
                        this.visP2PSourceInfo.clearData();
                    } else {
                        // Mark that we are now a provider
                        this.setIsVisProvider(true);

                        // Inform the user
                        memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_LOADED);
                    }
                }

                // Mark for a save
                this.saveChanges();
            } else if ("gui.appliedenergistics2.Blank".equals(storedName) && this.isProvider) {
                this.setIsVisProvider(false);

                // Inform the user
                memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_CLEARED);

                // Mark for save
                this.saveChanges();
            }
            return true;
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);

        data.setLong(PartVisInterface.NBT_KEY_UID, this.UID);

        if (this.isProvider) {
            // Write provider status
            data.setBoolean(PartVisInterface.NBT_KEY_IS_PROVIDER, this.isProvider);

            // Write source data
            this.visP2PSourceInfo.writeToNBT(data, PartVisInterface.NBT_KEY_PROVIDER_SOURCE);
        }
    }

    /**
     * Reads the interface data from the tag
     */
    @Override
    public void readFromNBT(final NBTTagCompound data) {
        // Call super
        super.readFromNBT(data);

        // Does it contain the UID?
        if (data.hasKey(PartVisInterface.NBT_KEY_UID)) {
            // Read the UID
            this.UID = data.getLong(PartVisInterface.NBT_KEY_UID);
        }

        // Is there provider data?
        if (data.hasKey(PartVisInterface.NBT_KEY_IS_PROVIDER)) {
            // Set provider status
            this.isProvider = data.getBoolean(PartVisInterface.NBT_KEY_IS_PROVIDER);

            // Read source information
            if (data.hasKey(PartVisInterface.NBT_KEY_PROVIDER_SOURCE)) {
                this.visP2PSourceInfo.readFromNBT(data, PartVisInterface.NBT_KEY_PROVIDER_SOURCE);
            }
        }
    }

    private static final String NBT_KEY_OWNER = "Owner";

    @Override
    public void addToWorld() {
        super.addToWorld();

        // For back compatibility
        NBTTagCompound data = this.getItemStack().getTagCompound();
        if (data != null && data.hasKey(NBT_KEY_OWNER)) {
            int ownerID = data.getInteger(NBT_KEY_OWNER);
            this.getProxy().getNode().setPlayerID(ownerID);
        }
    }

    /**
     * Sends data to the client
     */
    @Override
    public void writeToStream(final ByteBuf data) throws IOException {
        // Call super
        super.writeToStream(data);

        // Write the drain color
        data.writeInt(this.visDrainingColor);
    }

    /**
     * Reads server-sent data
     */
    @Override
    public boolean readFromStream(final ByteBuf data) throws IOException {
        boolean redraw = false;

        // Call super
        redraw |= super.readFromStream(data);

        // Cache old color
        int oldColor = this.visDrainingColor;

        // Read the drain color
        this.visDrainingColor = data.readInt();

        // Redraw if colors changed
        redraw |= (this.visDrainingColor != oldColor);

        return redraw;
    }

    /**
     * How far to extend the cable.
     */
    @Override
    public int cableConnectionRenderTo() {
        return 2;
    }

    /**
     * Hit boxes.
     */
    @Override
    public void getBoxes(final IPartCollisionHelper helper) {
        // Face
        helper.addBox(6.0F, 6.0F, 15.0F, 10.0F, 10.0F, 16.0F);

        // Mid
        helper.addBox(4.0D, 4.0D, 14.0D, 12.0D, 12.0D, 15.0D);

        // Back
        helper.addBox(5.0D, 5.0D, 13.0D, 11.0D, 11.0D, 14.0D);
    }

    @Override
    public IIcon getBreakingTexture() {
        return BlockTextureManager.VIS_RELAY_INTERFACE.getTextures()[0];
    }

    /**
     * Produces a small amount of light.
     */
    @Override
    public int getLightLevel() {
        return 8;
    }

    @SideOnly(Side.CLIENT)
    private static void renderInventoryBusLights(final IPartRenderHelper helper, final RenderBlocks renderer) {
        // Set color to white
        helper.setInvColor(0xFFFFFF);

        IIcon busColorTexture = BlockTextureManager.BUS_COLOR.getTextures()[0];

        IIcon sideTexture = BlockTextureManager.BUS_COLOR.getTextures()[2];

        helper.setTexture(busColorTexture, busColorTexture, sideTexture, sideTexture, busColorTexture, busColorTexture);

        // Rend the box
        helper.renderInventoryBox(renderer);

        // Set the brightness
        Tessellator.instance.setBrightness(0xD000D0);

        helper.setInvColor(AEColor.Transparent.blackVariant);

        IIcon lightTexture = BlockTextureManager.BUS_COLOR.getTextures()[1];

        // Render the lights
        helper.renderInventoryFace(lightTexture, ForgeDirection.UP, renderer);
        helper.renderInventoryFace(lightTexture, ForgeDirection.DOWN, renderer);
        helper.renderInventoryFace(lightTexture, ForgeDirection.NORTH, renderer);
        helper.renderInventoryFace(lightTexture, ForgeDirection.EAST, renderer);
        helper.renderInventoryFace(lightTexture, ForgeDirection.SOUTH, renderer);
        helper.renderInventoryFace(lightTexture, ForgeDirection.WEST, renderer);
    }

    /**
     * Draws the interface in the inventory.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventory(final IPartRenderHelper helper, final RenderBlocks renderer) {
        IIcon side = BlockTextureManager.VIS_RELAY_INTERFACE.getTextures()[2];
        helper.setTexture(side, side, side, BlockTextureManager.VIS_RELAY_INTERFACE.getTexture(), side, side);

        // Face
        helper.setBounds(6.0F, 6.0F, 15.0F, 10.0F, 10.0F, 16.0F);
        helper.renderInventoryBox(renderer);

        // Mid
        helper.setBounds(4.0F, 4.0F, 14.0F, 12.0F, 12.0F, 15.0F);
        helper.renderInventoryBox(renderer);

        // Back
        helper.setBounds(5.0F, 5.0F, 13.0F, 11.0F, 11.0F, 14.0F);
        renderInventoryBusLights(helper, renderer);
    }

    @SideOnly(Side.CLIENT)
    private void renderStaticBusLights(final int x, final int y, final int z, final IPartRenderHelper helper,
            final RenderBlocks renderer) {
        IIcon busColorTexture = BlockTextureManager.BUS_COLOR.getTextures()[0];

        IIcon sideTexture = BlockTextureManager.BUS_COLOR.getTextures()[2];

        helper.setTexture(busColorTexture, busColorTexture, sideTexture, sideTexture, busColorTexture, busColorTexture);

        // Render the box
        helper.renderBlock(x, y, z, renderer);

        // Are we active?
        if (this.isActive()) {
            // Set the brightness
            Tessellator.instance.setBrightness(0xD000D0);

            // Set the color to match the cable
            Tessellator.instance.setColorOpaque_I(this.host.getColor().blackVariant);
        } else {
            // Set the color to black
            Tessellator.instance.setColorOpaque_I(0);
        }

        IIcon lightTexture = BlockTextureManager.BUS_COLOR.getTextures()[1];

        // Render the lights
        helper.renderFace(x, y, z, lightTexture, ForgeDirection.UP, renderer);
        helper.renderFace(x, y, z, lightTexture, ForgeDirection.DOWN, renderer);
        helper.renderFace(x, y, z, lightTexture, ForgeDirection.NORTH, renderer);
        helper.renderFace(x, y, z, lightTexture, ForgeDirection.EAST, renderer);
        helper.renderFace(x, y, z, lightTexture, ForgeDirection.SOUTH, renderer);
        helper.renderFace(x, y, z, lightTexture, ForgeDirection.WEST, renderer);
    }

    /**
     * Draws the interface in the world.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void renderStatic(final int x, final int y, final int z, final IPartRenderHelper helper,
            final RenderBlocks renderer) {
        Tessellator tessellator = Tessellator.instance;

        IIcon side = BlockTextureManager.VIS_RELAY_INTERFACE.getTextures()[2];
        helper.setTexture(side, side, side, BlockTextureManager.VIS_RELAY_INTERFACE.getTexture(), side, side);

        // Face
        helper.setBounds(6.0F, 6.0F, 15.0F, 10.0F, 10.0F, 16.0F);
        helper.renderBlock(x, y, z, renderer);

        // Mid
        helper.setBounds(4.0F, 4.0F, 14.0F, 12.0F, 12.0F, 15.0F);
        helper.renderBlock(x, y, z, renderer); // Mid face

        if (this.visDrainingColor != 0) {
            tessellator.setColorOpaque_I(this.visDrainingColor);
            helper.renderFace(
                    x,
                    y,
                    z,
                    BlockTextureManager.VIS_RELAY_INTERFACE.getTextures()[1],
                    ForgeDirection.SOUTH,
                    renderer);
        }

        // Back (facing bus)
        helper.setBounds(5.0F, 5.0F, 13.0F, 11.0F, 11.0F, 14.0F);
        this.renderStaticBusLights(x, y, z, helper, renderer);
    }
}
