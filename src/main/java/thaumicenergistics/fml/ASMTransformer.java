package thaumicenergistics.fml;

import java.util.HashMap;

import net.minecraft.launchwrapper.IClassTransformer;

public class ASMTransformer implements IClassTransformer {

    /**
     * Map of all transformers<br>
     * Class name -> Transformer
     */
    private final HashMap<String, AClassTransformer> tranformers;

    public ASMTransformer() {
        // Create the transformers map
        this.tranformers = new HashMap<>();

        // Add Thaumcraft transformers
    }

    /**
     * Adds a transformer to the map.
     *
     * @param transformer
     */
    private void addTransformer(final AClassTransformer transformer) {
        this.tranformers.put(transformer.classCanonicalName, transformer);
    }

    @Override
    public byte[] transform(final String name, final String transformedName, final byte[] basicClass) {
        AClassTransformer transformer;
        if ((transformer = this.tranformers.getOrDefault(transformedName, null)) != null) {
            return transformer.transformClass(basicClass);
        }

        return basicClass;
    }
}
