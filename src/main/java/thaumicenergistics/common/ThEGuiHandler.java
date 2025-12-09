package thaumicenergistics.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import appeng.parts.AEBasePart;
import cpw.mods.fml.common.network.IGuiHandler;
import thaumicenergistics.client.gui.GuiArcaneAssembler;
import thaumicenergistics.client.gui.GuiArcaneCraftingTerminal;
import thaumicenergistics.client.gui.GuiDistillationPatternEncoder;
import thaumicenergistics.client.gui.GuiEssentiaVibrationChamber;
import thaumicenergistics.client.gui.GuiKnowledgeInscriber;
import thaumicenergistics.common.container.ContainerArcaneAssembler;
import thaumicenergistics.common.container.ContainerDistillationPatternEncoder;
import thaumicenergistics.common.container.ContainerEssentiaVibrationChamber;
import thaumicenergistics.common.container.ContainerKnowledgeInscriber;
import thaumicenergistics.common.container.ContainerPartArcaneCraftingTerminal;
import thaumicenergistics.common.parts.PartArcaneCraftingTerminal;

/**
 * Handles ThE GUI launching.
 *
 * @author Nividica
 *
 */
public class ThEGuiHandler implements IGuiHandler {

    // ID's between 0 and this number indicate that they are AE parts
    private static final int DIRECTION_OFFSET = ForgeDirection.values().length;

    // ID's must increase in values of 10
    private static final int ID_STEP_VALUE = 10;

    /**
     * Singular ID of the essentia cell gui
     */
    public static final int ESSENTIA_CELL_ID = ThEGuiHandler.ID_STEP_VALUE * 1;

    /**
     * ID of the arcane assembler gui.
     */
    public static final int ARCANE_ASSEMBLER_ID = ThEGuiHandler.ID_STEP_VALUE * 5;

    /**
     * ID of the knowledge inscriber gui.
     */
    public static final int KNOWLEDGE_INSCRIBER = ThEGuiHandler.ID_STEP_VALUE * 6;

    /**
     * ID of the knowledge inscriber gui.
     */
    public static final int ESSENTIA_VIBRATION_CHAMBER = ThEGuiHandler.ID_STEP_VALUE * 7;

    /**
     * ID of the distillation encoder.
     */
    public static final int DISTILLATION_ENCODER = ThEGuiHandler.ID_STEP_VALUE * 10;

    /**
     * Gets the AE part at the specified location.
     *
     * @param tileSide
     * @param world
     * @param x
     * @param y
     * @param z
     * @return
     */
    private static IPart getPart(final ForgeDirection tileSide, final World world, final int x, final int y,
            final int z) {
        // Get the host at the specified position
        IPartHost partHost = (IPartHost) (world.getTileEntity(x, y, z));

        // Ensure we got a host
        if (partHost == null) {
            return null;
        }

        // Get the part from the host
        return (partHost.getPart(tileSide));
    }

    /**
     * Get the gui element for the AE part at the specified location
     *
     * @param tileSide
     * @param player
     * @param world
     * @param x
     * @param y
     * @param z
     * @param isServerSide
     * @return
     */
    private static Object getPartGuiElement(final ForgeDirection tileSide, final EntityPlayer player, final World world,
            final int x, final int y, final int z, final boolean isServerSide) {
        IPart ipart = ThEGuiHandler.getPart(tileSide, world, x, y, z);

        if (ipart instanceof PartArcaneCraftingTerminal arcaneCraftingTerminalNew) {
            if (isServerSide) {
                AEBaseContainer container = new ContainerPartArcaneCraftingTerminal(
                        player.inventory,
                        arcaneCraftingTerminalNew);
                ContainerOpenContext ctx = new ContainerOpenContext(arcaneCraftingTerminalNew);
                ctx.setWorld(world);
                ctx.setX(x);
                ctx.setY(y);
                ctx.setZ(z);
                ctx.setSide(tileSide);
                container.setOpenContext(ctx);
                return container;
            }
            return new GuiArcaneCraftingTerminal(player.inventory, arcaneCraftingTerminalNew);
        }

        return null;
    }

    /**
     * Launches a non AE part gui.
     *
     * @param ID
     * @param player
     * @param world
     * @param x
     * @param y
     * @param z
     */
    public static void launchGui(final int ID, final EntityPlayer player, final World world, final int x, final int y,
            final int z) {
        player.openGui(ThaumicEnergistics.INSTANCE, ID + ThEGuiHandler.DIRECTION_OFFSET, world, x, y, z);
    }

    public static void launchGui(final AEBasePart part, final EntityPlayer player, final World world, final int x,
            final int y, final int z) {
        // Ensure the player is allowed to open the gui
        // TODO: permission check
        player.openGui(ThaumicEnergistics.INSTANCE, part.getSide().ordinal(), world, x, y, z);
    }

    @Override
    public Object getClientGuiElement(int ID, final EntityPlayer player, final World world, final int x, final int y,
            final int z) {
        // Is the ID a forge direction?
        ForgeDirection side = ForgeDirection.getOrientation(ID);

        // Do we have a world and side?
        if ((world != null) && (side != ForgeDirection.UNKNOWN)) {
            // This is an AE part, get its gui
            return ThEGuiHandler.getPartGuiElement(side, player, world, x, y, z, false);
        }

        // This is not an AE part, adjust the ID
        ID -= ThEGuiHandler.DIRECTION_OFFSET;

        // Check basic ID's
        return switch (ID) {
            // Is this the arcane assembler?
            case ThEGuiHandler.ARCANE_ASSEMBLER_ID -> new GuiArcaneAssembler(player, world, x, y, z);

            // Is this the knowledge inscriber?
            case ThEGuiHandler.KNOWLEDGE_INSCRIBER -> new GuiKnowledgeInscriber(player, world, x, y, z);

            // Vibration chamber?
            case ThEGuiHandler.ESSENTIA_VIBRATION_CHAMBER -> new GuiEssentiaVibrationChamber(player, world, x, y, z);

            // Distillation encoder?
            case ThEGuiHandler.DISTILLATION_ENCODER -> new GuiDistillationPatternEncoder(player, world, x, y, z);
            default -> null;
        };

    }

    @Override
    public Object getServerGuiElement(int ID, final EntityPlayer player, final World world, final int x, final int y,
            final int z) {
        // Is the ID a forge Direction?
        ForgeDirection side = ForgeDirection.getOrientation(ID);

        // Do we have a world and side?
        if (world != null && side != ForgeDirection.UNKNOWN) {
            // This is an AE part, get its gui
            return ThEGuiHandler.getPartGuiElement(side, player, world, x, y, z, true);
        }

        // This is not an AE part, adjust the ID
        ID -= ThEGuiHandler.DIRECTION_OFFSET;

        return switch (ID) {
            // Is this the arcane assembler?
            case ThEGuiHandler.ARCANE_ASSEMBLER_ID -> new ContainerArcaneAssembler(player, world, x, y, z);

            // Is this the knowledge inscriber?
            case ThEGuiHandler.KNOWLEDGE_INSCRIBER -> new ContainerKnowledgeInscriber(player, world, x, y, z);

            // Vibration chamber?
            case ThEGuiHandler.ESSENTIA_VIBRATION_CHAMBER -> new ContainerEssentiaVibrationChamber(
                    player,
                    world,
                    x,
                    y,
                    z);

            // Distillation encoder?
            case ThEGuiHandler.DISTILLATION_ENCODER -> {
                ContainerDistillationPatternEncoder container = new ContainerDistillationPatternEncoder(
                        player,
                        world,
                        x,
                        y,
                        z);
                ContainerOpenContext ctx = new ContainerOpenContext(world.getTileEntity(x, y, z));
                ctx.setWorld(world);
                ctx.setX(x);
                ctx.setY(y);
                ctx.setZ(z);
                ctx.setSide(side);
                container.setOpenContext(ctx);
                yield container;
            }
            default -> null;
        };
    }
}
