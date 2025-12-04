package thaumicenergistics.common.integration.tc;

import thaumicenergistics.common.ThaumicEnergistics;

/**
 * Aids in converting essentia to and from a fluid.
 *
 * @author Nividica
 *
 */
public final class EssentiaConversionHelper {

    /**
     * Singleton
     */
    public static final EssentiaConversionHelper INSTANCE = new EssentiaConversionHelper();

    /**
     * Private constructor
     */
    private EssentiaConversionHelper() {}

    /**
     * Converts an essentia amount into a fluid amount(mb).
     *
     * @param essentiaAmount
     * @return
     */
    public long convertEssentiaAmountToFluidAmount(final long essentiaAmount) {
        return essentiaAmount * ThaumicEnergistics.config.conversionMultiplier();
    }

    /**
     * Converts a fluid amount(mb) into an essentia amount.
     *
     * @param fluidAmount
     * @return
     */
    public long convertFluidAmountToEssentiaAmount(final long fluidAmount) {
        return fluidAmount / ThaumicEnergistics.config.conversionMultiplier();
    }
}
