package thaumicenergistics.common.integration;

import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.tile.networking.TileCableBus;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.event.FMLInterModComms;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import thaumicenergistics.common.blocks.AbstractBlockProviderBase;
import thaumicenergistics.common.blocks.BlockArcaneAssembler;
import thaumicenergistics.common.integration.tc.DigiVisSourceData;
import thaumicenergistics.common.parts.PartArcaneCraftingTerminal;
import thaumicenergistics.common.parts.PartVisInterface;
import thaumicenergistics.common.registries.ThEStrings;
import thaumicenergistics.common.tiles.TileArcaneAssembler;
import thaumicenergistics.common.tiles.TileEssentiaVibrationChamber;

/**
 * What Am I Looking At integration.
 *
 * @author Nividica
 *
 */
public class ModuleWaila implements IWailaDataProvider {

    /**
     * Singleton
     */
    public static ModuleWaila INSTANCE;

    /**
     * NBT key holding the vis link info. Parts append the side ordinal to it.
     */
    private static final String NBT_KEY_VIS_LINK = "thaumicenergistics.vislink";

    /**
     * NBT key for the number of devices drawing from an interface.
     */
    private static final String NBT_KEY_LINKED_DEVICES = "linkedDevices";

    /**
     * NBT key for the position of the source a machine draws from.
     */
    private static final String NBT_KEY_SOURCE_POSITION = "sourcePosition";

    /**
     * Attempts to integrate with Waila
     */
    private ModuleWaila() {}

    @Optional.Method(modid = "Waila")
    static void init() {
        // Set the singleton
        ModuleWaila.INSTANCE = new ModuleWaila();

        // Register with Waila
        FMLInterModComms.sendMessage("Waila", "register", ModuleWaila.class.getCanonicalName() + ".callbackRegister");
    }

    /**
     * Called by Waila to register our hooks.
     *
     * @param registrar
     */
    @SuppressWarnings("unused")
    public static void callbackRegister(final IWailaRegistrar registrar) {
        // Register the providers
        registrar.registerBodyProvider(ModuleWaila.INSTANCE, AbstractBlockProviderBase.class);

        // Register the assembler
        registrar.registerBodyProvider(ModuleWaila.INSTANCE, BlockArcaneAssembler.class);

        // Register the vibration chamber
        registrar.registerBodyProvider(ModuleWaila.INSTANCE, TileEssentiaVibrationChamber.class);

        // Register the cable bus, so that vis links can be shown for our parts
        registrar.registerBodyProvider(ModuleWaila.INSTANCE, TileCableBus.class);

        // The vis link info is only known to the server
        registrar.registerNBTProvider(ModuleWaila.INSTANCE, TileCableBus.class);
        registrar.registerNBTProvider(ModuleWaila.INSTANCE, BlockArcaneAssembler.class);
    }

    /**
     * Writes the vis link info of a part into a new tag.
     *
     * @return The info, or null if the part has no vis link.
     */
    private static NBTTagCompound getPartVisLinkInfo(final IPart part) {
        NBTTagCompound info = new NBTTagCompound();

        if (part instanceof PartVisInterface visInterface) {
            // A provider draws from another interface, everything else draws from the vis net
            if (visInterface.isVisProvider()) {
                ModuleWaila.writeSourcePosition(info, visInterface.getP2PSourceData());
            } else {
                info.setInteger(ModuleWaila.NBT_KEY_LINKED_DEVICES, visInterface.countLinkedDevices());
            }

            return info;
        }

        if (part instanceof PartArcaneCraftingTerminal terminal) {
            ModuleWaila.writeSourcePosition(info, terminal.getVisSourceData());

            return info;
        }

        return null;
    }

    private static void writeSourcePosition(final NBTTagCompound info, final DigiVisSourceData sourceData) {
        int[] position = sourceData.getSourcePosition();

        if (position != null) {
            info.setIntArray(ModuleWaila.NBT_KEY_SOURCE_POSITION, position);
        }
    }

    /**
     * Gets the vis link info the server sent for what the player is looking at.
     *
     * @return The info, or null if there is none.
     */
    private static NBTTagCompound getVisLinkInfo(final TileEntity tileEntity, final IWailaDataAccessor accessor) {
        NBTTagCompound data = accessor.getNBTData();

        if (data == null) {
            return null;
        }

        String key = ModuleWaila.NBT_KEY_VIS_LINK;

        if (tileEntity instanceof IPartHost host) {
            MovingObjectPosition position = accessor.getPosition();

            if (position == null) {
                return null;
            }

            // Find the part the player is looking at
            SelectedPart selected = host
                    .selectPart(position.hitVec.addVector(-position.blockX, -position.blockY, -position.blockZ));

            if (selected.part == null) {
                return null;
            }

            key += selected.side.ordinal();
        }

        return data.hasKey(key) ? data.getCompoundTag(key) : null;
    }

    private static void addVisLinkInformation(final List<String> tooltip, final NBTTagCompound info) {
        if (info.hasKey(ModuleWaila.NBT_KEY_LINKED_DEVICES)) {
            tooltip.add(
                    String.format(
                            ThEStrings.Waila_VisLinkedDevices.getLocalized(),
                            info.getInteger(ModuleWaila.NBT_KEY_LINKED_DEVICES)));
            return;
        }

        if (!info.hasKey(ModuleWaila.NBT_KEY_SOURCE_POSITION)) {
            tooltip.add(ThEStrings.Waila_VisSourceNone.getLocalized());
            return;
        }

        int[] position = info.getIntArray(ModuleWaila.NBT_KEY_SOURCE_POSITION);

        tooltip.add(String.format(ThEStrings.Waila_VisSource.getLocalized(), position[0], position[1], position[2]));
    }

    @Override
    public NBTTagCompound getNBTData(final EntityPlayerMP player, final TileEntity tileEntity,
            final NBTTagCompound data, final World world, final int x, final int y, final int z) {
        if (tileEntity instanceof IPartHost host) {
            // Write the info of every part that has a vis link
            for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
                NBTTagCompound info = ModuleWaila.getPartVisLinkInfo(host.getPart(side));

                if (info != null) {
                    data.setTag(ModuleWaila.NBT_KEY_VIS_LINK + side.ordinal(), info);
                }
            }
        } else if (tileEntity instanceof TileArcaneAssembler assembler) {
            NBTTagCompound info = new NBTTagCompound();

            ModuleWaila.writeSourcePosition(info, assembler.getVisSourceData());

            data.setTag(ModuleWaila.NBT_KEY_VIS_LINK, info);
        }

        return data;
    }

    /**
     * Changes the body of the Waila message.
     */
    @Override
    public List<String> getWailaBody(final ItemStack itemStack, final List<String> tooltip,
            final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        // Get the tile entity
        TileEntity tileEntity = accessor.getTileEntity();

        // Does the tile implement the tooltip method?
        if (tileEntity instanceof IWailaSource) {
            // Add the info
            ((IWailaSource) tileEntity).addWailaInformation(tooltip);
        }

        // Is there a vis link to report?
        NBTTagCompound visLinkInfo = ModuleWaila.getVisLinkInfo(tileEntity, accessor);

        if (visLinkInfo != null) {
            ModuleWaila.addVisLinkInformation(tooltip, visLinkInfo);
        }

        return tooltip;
    }

    @Override
    public List<String> getWailaHead(final ItemStack itemStack, final List<String> currenttip,
            final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        // Ignored
        return currenttip;
    }

    @Override
    public ItemStack getWailaStack(final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        // Ignored
        return null;
    }

    @Override
    public List<String> getWailaTail(final ItemStack itemStack, final List<String> currenttip,
            final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        // Ignored
        return currenttip;
    }
}
