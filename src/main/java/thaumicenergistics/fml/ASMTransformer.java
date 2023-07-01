package thaumicenergistics.fml;

import java.util.HashMap;

import net.minecraft.launchwrapper.IClassTransformer;

import thaumicenergistics.fml.classtransformers.ae.ClassTransformer_CraftingTreeProcess;
import thaumicenergistics.fml.classtransformers.tc.ClassTransformer_Aspect;
import thaumicenergistics.fml.classtransformers.tc.ClassTransformer_EntityGolemBase;
import thaumicenergistics.fml.classtransformers.tc.ClassTransformer_ItemGolemBell;
import thaumicenergistics.fml.classtransformers.tc.ClassTransformer_ItemGolemPlacer;
import thaumicenergistics.fml.classtransformers.tc.ClassTransformer_RenderGolemBase;

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
        this.addTransformer(new ClassTransformer_Aspect());
        this.addTransformer(new ClassTransformer_RenderGolemBase());
        this.addTransformer(new ClassTransformer_ItemGolemPlacer());
        this.addTransformer(new ClassTransformer_ItemGolemBell());
        this.addTransformer(new ClassTransformer_EntityGolemBase());

        // Add AE transformers
        this.addTransformer(new ClassTransformer_CraftingTreeProcess());
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
