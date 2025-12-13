package thaumicenergistics.common.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.items.contents.CellConfig;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.common.storage.AEEssentiaStack;

public class EssentiaCellConfig extends CellConfig {

    private static final String NBT_PARTITION_KEY = "Partitions";
    private static final String NBT_PARTITION_COUNT_KEY = "PartitionCount";
    private static final String NBT_PARTITION_NUMBER_KEY = "Partition#";

    public EssentiaCellConfig(ItemStack is) {
        super(is);
    }

    @Override
    public void readFromNBT(NBTTagCompound data, String name) {
        NBTTagCompound tag = this.is.getTagCompound();

        // Migrate from old data
        if (tag.hasKey(NBT_PARTITION_KEY)) {
            // Get the partition tag
            NBTTagCompound partitionData = tag.getCompoundTag(NBT_PARTITION_KEY);

            // Get the partition count
            int partitionCount = partitionData.getInteger(NBT_PARTITION_COUNT_KEY);

            // Read the partition list
            Aspect partitionAspect;
            for (int i = 0; i < partitionCount; i++) {
                // Read the aspect tag
                String aspectTag = partitionData.getString(NBT_PARTITION_NUMBER_KEY + i);

                // Skip if empty tag
                if (aspectTag.isEmpty()) {
                    continue;
                }

                // Get the aspect
                partitionAspect = Aspect.aspects.get(aspectTag);

                if (partitionAspect != null) {
                    // Add the aspect
                    this.putAEStackInSlot(i, new AEEssentiaStack(partitionAspect));
                }
            }

            return;
        }

        super.readFromNBT(data, name);
    }
}
