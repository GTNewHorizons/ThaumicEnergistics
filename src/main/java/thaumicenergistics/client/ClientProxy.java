package thaumicenergistics.client;

import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import thaumicenergistics.client.render.item.ItemCellMicroscopeRenderer;
import thaumicenergistics.client.textures.BlockTextureManager;
import thaumicenergistics.common.CommonProxy;
import thaumicenergistics.common.items.ItemEnum;
import thaumicenergistics.common.registries.Renderers;

/**
 * Client side proxy.
 *
 * @author Nividica
 *
 */
public class ClientProxy extends CommonProxy {

    public ClientProxy() {
        MinecraftForge.EVENT_BUS.register(new EventHandler());
    }

    @Override
    public void registerRenderers() {
        // Register the custom block renderers
        Renderers.registerRenderers();

        // Post-release renderer
        MinecraftForgeClient.registerItemRenderer(ItemEnum.CELL_MICROSCOPE.getItem(), new ItemCellMicroscopeRenderer());
    }

    public class EventHandler {

        @SubscribeEvent
        public void registerTextures(final TextureStitchEvent.Pre event) {
            // Register all block textures
            for (BlockTextureManager texture : BlockTextureManager.ALLVALUES) {
                texture.registerTexture(event.map);
            }
        }
    }

    @Override
    public void registerItems() {
        super.registerItems();
        // hide technical item from NEI
        codechicken.nei.api.API.hideItem(ItemEnum.ARCANE_PATTERN.getStack());
    }
}
