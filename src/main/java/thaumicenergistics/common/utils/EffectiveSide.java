package thaumicenergistics.common.utils;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

/**
 * Which side, server vs client, is the code on?
 *
 * @author Nividica
 *
 */
public final class EffectiveSide {

    /**
     * True if the thread executing this code is client side.
     */
    public static boolean isClientSide() {
        return FMLCommonHandler.instance().getEffectiveSide().isClient();
    }

    /**
     * True if the thread executing this code is server side.
     */
    public static boolean isServerSide() {
        return FMLCommonHandler.instance().getEffectiveSide().isServer();
    }

    /**
     * Returns the effective side for the context in the game.
     */
    public static Side side() {
        return FMLCommonHandler.instance().getEffectiveSide();
    }
}
