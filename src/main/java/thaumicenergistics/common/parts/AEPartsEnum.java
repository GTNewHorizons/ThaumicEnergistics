package thaumicenergistics.common.parts;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import appeng.api.config.Upgrades;
import appeng.api.parts.IPart;
import thaumicenergistics.common.ThaumicEnergistics;
import thaumicenergistics.common.items.ItemEnum;
import thaumicenergistics.common.registries.ThEStrings;

/**
 * Enumeration of all ThE cable parts.
 *
 * @author Nividica
 *
 */
public enum AEPartsEnum {

    EssentiaImportBus(ThEStrings.Part_EssentiaImportBus, PartEssentiaImportBus.class,
            ThaumicEnergistics.MOD_ID + ".group.essentia.transport", generatePair(Upgrades.CAPACITY, 2),
            generatePair(Upgrades.REDSTONE, 1), generatePair(Upgrades.SPEED, 4)),

    EssentiaLevelEmitter(ThEStrings.Part_EssentiaLevelEmitter, PartEssentiaLevelEmitter.class, true),

    EssentiaStorageBus(ThEStrings.Part_EssentiaStorageBus, PartEssentiaStorageBus.class, null,
            generatePair(Upgrades.INVERTER, 1)),

    EssentiaExportBus(ThEStrings.Part_EssentiaExportBus, PartEssentiaExportBus.class,
            ThaumicEnergistics.MOD_ID + ".group.essentia.transport", generatePair(Upgrades.CAPACITY, 2),
            generatePair(Upgrades.REDSTONE, 1), generatePair(Upgrades.SPEED, 4), generatePair(Upgrades.CRAFTING, 1)),

    EssentiaTerminal(ThEStrings.Part_EssentiaTerminal, PartEssentiaTerminal.class, true),

    ArcaneCraftingTerminal(ThEStrings.Part_ArcaneCraftingTerminal, PartArcaneCraftingTerminal.class),

    VisInterface(ThEStrings.Part_VisRelayInterface, PartVisInterface.class),

    EssentiaStorageMonitor(ThEStrings.Part_EssentiaStorageMonitor, PartEssentiaStorageMonitor.class, true),

    EssentiaConversionMonitor(ThEStrings.Part_EssentiaConversionMonitor, PartEssentiaConversionMonitor.class, true),

    CreativeVisInterface(ThEStrings.Part_CreativeVisRelayInterface, PartCreativeVisInterface.class);

    /**
     * Cached enum values
     */
    public static final AEPartsEnum[] VALUES = AEPartsEnum.values();

    private final ThEStrings unlocalizedName;

    private final Class<? extends IPart> partClass;

    private final String groupName;

    private final Map<Upgrades, Integer> upgrades = new HashMap<Upgrades, Integer>();

    private final String tooltip;

    AEPartsEnum(final ThEStrings unlocalizedName, final Class<? extends IPart> partClass, boolean deprecated) {
        this(unlocalizedName, partClass, null, deprecated ? "§4DEPRECATED!" : "");
    }

    AEPartsEnum(final ThEStrings unlocalizedName, final Class<? extends IPart> partClass) {
        this(unlocalizedName, partClass, null, "");
    }

    AEPartsEnum(final ThEStrings unlocalizedName, final Class<? extends IPart> partClass, final String groupName,
            final String tooltip) {
        // Set the localization string
        this.unlocalizedName = unlocalizedName;

        // Set the class
        this.partClass = partClass;

        // Set the group name
        this.groupName = groupName;

        this.tooltip = tooltip;
    }

    @SafeVarargs
    AEPartsEnum(final ThEStrings unlocalizedName, final Class<? extends IPart> partClass, final String groupName,
            final Pair<Upgrades, Integer>... upgrades) {
        this(unlocalizedName, partClass, groupName, "");

        for (Pair<Upgrades, Integer> pair : upgrades) {
            // Add the upgrade to the map
            this.upgrades.put(pair.getKey(), pair.getValue());
        }
    }

    private static Pair<Upgrades, Integer> generatePair(final Upgrades upgrade, final int maximum) {
        return new ImmutablePair<>(upgrade, Integer.valueOf(maximum));
    }

    /**
     * Gets an AEPart based on an item stacks damage value.
     *
     * @param itemStack
     * @return
     */
    public static AEPartsEnum getPartFromDamageValue(final ItemStack itemStack) {
        // Clamp the damage
        int clamped = MathHelper.clamp_int(itemStack.getItemDamage(), 0, AEPartsEnum.VALUES.length - 1);

        // Get the part
        return AEPartsEnum.VALUES[clamped];
    }

    public IPart createPartInstance(final ItemStack itemStack) throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        // Create a new instance of the part
        return this.partClass.getConstructor(ItemStack.class).newInstance(itemStack);
    }

    /**
     * Gets the group associated with this part.
     *
     * @return
     */
    public String getGroupName() {
        return this.groupName;
    }

    public String getLocalizedName() {
        return this.unlocalizedName.getLocalized();
    }

    /**
     * Gets the class associated with this part.
     *
     * @return
     */
    public Class<? extends IPart> getPartClass() {
        return this.partClass;
    }

    public ItemStack getStack() {
        return ItemEnum.ITEM_AEPART.getDMGStack(this.ordinal());
    }

    /**
     * Gets the unlocalized name for this part.
     *
     * @return
     */
    public String getUnlocalizedName() {
        return this.unlocalizedName.getUnlocalized();
    }

    public Map<Upgrades, Integer> getUpgrades() {
        return this.upgrades;
    }

    @Nullable
    public String getTooltip() {
        if (tooltip == null) {
            return null;
        }
        return tooltip.isEmpty() ? null : tooltip;
    }
}
