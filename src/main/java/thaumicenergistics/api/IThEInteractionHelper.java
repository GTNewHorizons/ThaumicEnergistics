package thaumicenergistics.api;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumicenergistics.api.entities.IGolemHookHandler;

/**
 * Contains miscellaneous functionality intended to help other moders interact with ThE.
 *
 * @author Nividica
 *
 */
public interface IThEInteractionHelper {

    /**
     * Registers a handler to receive golem events.
     *
     * @param handler
     */
    void registerGolemHookHandler(@Nonnull IGolemHookHandler handler);

    /**
     * Attempts to set the Arcane Crafting Terminals recipe to the items specified for the current player.<br>
     * Call is ignored if player does not have an A.C.T. GUI open.<br>
     * The items array should be of size 9. Items will be placed in the crafting grid according to index where
     * <ul>
     * <li>0 = Top-Left</li>
     * <li>1 = Top-Middle</li>
     * <li>2 = Top-Right</li>
     * <li>etc</li>
     * </ul>
     * Null items are allowed.
     */
    @SideOnly(Side.CLIENT)
    void setArcaneCraftingTerminalRecipe(@Nonnull ItemStack[] items);

    @Deprecated
    void openWirelessTerminalGui(@Nonnull EntityPlayer player);
}
