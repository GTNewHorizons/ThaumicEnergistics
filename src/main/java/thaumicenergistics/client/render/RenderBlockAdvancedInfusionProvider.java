package thaumicenergistics.client.render;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumicenergistics.client.textures.BlockTextureManager;
import thaumicenergistics.common.registries.Renderers;
import thaumicenergistics.common.tiles.TileInfusionProvider;

/**
 * Renders the {@link TileInfusionProvider}
 *
 * @author MCTBL
 *
 */
@SideOnly(Side.CLIENT)
public class RenderBlockAdvancedInfusionProvider extends RenderBlockProviderBase {

    public RenderBlockAdvancedInfusionProvider() {
        super(BlockTextureManager.ADVANCED_INFUSION_PROVIDER);
    }

    @Override
    public int getRenderId() {
        return Renderers.AdvancedInfusionProviderRenderID;
    }
}
