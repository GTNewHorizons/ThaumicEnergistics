package thaumicenergistics.common.storage;

import java.io.IOException;
import java.util.Objects;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.djgiannuzz.thaumcraftneiplugin.items.ItemAspect;

import appeng.api.storage.data.IAEStackType;
import appeng.api.storage.data.IItemList;
import cpw.mods.fml.common.Loader;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import it.unimi.dsi.fastutil.objects.ObjectLongImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectLongPair;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.common.blocks.BlockJarItem;
import thaumcraft.common.blocks.ItemJarFilled;
import thaumcraft.common.items.ItemEssence;
import thaumicenergistics.api.IThEEssentiaContainerPermission;
import thaumicenergistics.api.items.IRestrictedEssentiaContainerItem;
import thaumicenergistics.common.integration.tc.EssentiaItemContainerHelper;

public class AEEssentiaStackType implements IAEStackType<AEEssentiaStack> {

    public static final AEEssentiaStackType ESSENTIA_STACK_TYPE = new AEEssentiaStackType();
    public static final String ESSENTIA_STACK_ID = "essentia";

    @Override
    public String getId() {
        return ESSENTIA_STACK_ID;
    }

    @Override
    public AEEssentiaStack loadStackFromNBT(NBTTagCompound tag) {
        return AEEssentiaStack.loadStackFromNBT(tag);
    }

    @Override
    public AEEssentiaStack loadStackFromByte(ByteBuf buffer) throws IOException {
        return AEEssentiaStack.loadEssentiaStackFromPacket(buffer);
    }

    @Override
    public IItemList<AEEssentiaStack> createList() {
        return new EssentiaList();
    }

    @Override
    public boolean isContainerItemForType(@Nullable ItemStack container) {
        return container != null && container.getItem() instanceof IEssentiaContainerItem;
    }

    @Override
    public @Nullable AEEssentiaStack getStackFromContainerItem(@NotNull ItemStack container) {
        if (container.getItem() instanceof IEssentiaContainerItem essentiaContainer) {
            AspectList list = essentiaContainer.getAspects(container);
            if (list != null) {
                Aspect aspect = list.getAspects()[0];
                if (aspect != null) {
                    return new AEEssentiaStack(aspect);
                }
            }
        }
        return null;
    }

    @Override
    public @Nullable AEEssentiaStack convertStackFromItem(@NotNull ItemStack itemStack) {
        if (Loader.isModLoaded("thaumcraftneiplugin") && itemStack.getItem() instanceof ItemAspect) {
            AspectList list = ItemAspect.getAspects(itemStack);
            if (list != null) {
                Aspect aspect = list.getAspects()[0];
                if (aspect != null) {
                    return new AEEssentiaStack(aspect);
                }
            }
        }
        return null;
    }

    @Override
    public @NotNull ObjectLongPair<ItemStack> drainStackFromContainer(@NotNull ItemStack container,
            @NotNull AEEssentiaStack stack) {
        if (EssentiaItemContainerHelper.INSTANCE.getItemType(container)
                != EssentiaItemContainerHelper.AspectItemType.EssentiaContainer) {
            return new ObjectLongImmutablePair<>(container, 0);
        }

        Item containerItem = container.getItem();
        IThEEssentiaContainerPermission info = EssentiaItemContainerHelper.INSTANCE
                .getContainerInfo(containerItem, container.getItemDamage());
        if (info == null) {
            return new ObjectLongImmutablePair<>(container, 0);
        }

        AspectList aspectList = ((IEssentiaContainerItem) containerItem).getAspects(container);

        if (stack.getAspect() != aspectList.getAspects()[0]) {
            return new ObjectLongImmutablePair<>(container, 0);
        }

        final int containerAmountStored = aspectList.getAmount(stack.getAspect());
        if (containerAmountStored <= 0) {
            return new ObjectLongImmutablePair<>(container, 0);
        }

        int amountToDrain = (int) Math.min(stack.getStackSize(), Integer.MAX_VALUE);

        // Can this container do partial fills?
        if (!info.canHoldPartialAmount()) {
            // Ensure the request is for the containers capacity
            if (amountToDrain < info.maximumCapacity()) {
                // Can not partially drain this container
                return new ObjectLongImmutablePair<>(container, 0);
            } else if (amountToDrain > info.maximumCapacity()) {
                // Drain is to much, reduce to preset capacity
                amountToDrain = info.maximumCapacity();
            }
        } else {
            // Is the amount we want more than is in the container?
            if (amountToDrain > containerAmountStored) {
                // Adjust the amount to how much is in the container
                amountToDrain = containerAmountStored;
            }
        }

        ItemStack resultStack = null;
        if (amountToDrain == containerAmountStored || amountToDrain == info.maximumCapacity()) {
            // Is this a phial?
            if (containerItem instanceof ItemEssence) {
                // Create an empty phial for the output
                resultStack = EssentiaItemContainerHelper.INSTANCE.createEmptyPhial();
            } else if (containerItem instanceof ItemJarFilled) {
                // Was the jar labeled?
                if (EssentiaItemContainerHelper.INSTANCE.doesJarHaveLabel(container)) {
                    // Create an empty labeled jar
                    resultStack = EssentiaItemContainerHelper.INSTANCE
                            .createFilledJar(stack.getAspect(), 0, container.getItemDamage(), true);
                } else {
                    // Create an empty jar for the output
                    resultStack = EssentiaItemContainerHelper.INSTANCE.createEmptyJar(container.getItemDamage());
                }
            }
        }

        if (resultStack == null) {
            // Make a copy of the container
            resultStack = container.copy();

            // Reduce the list amount
            aspectList.remove(stack.getAspect(), amountToDrain);

            // Set the stored amount
            ((IEssentiaContainerItem) Objects.requireNonNull(resultStack.getItem()))
                    .setAspects(resultStack, aspectList);
        }

        return new ObjectLongImmutablePair<>(resultStack, amountToDrain);
    }

    @Override
    public @Nullable ItemStack clearFilledContainer(@NotNull ItemStack container) {
        return null;
    }

    @Override
    public @NotNull ObjectIntPair<ItemStack> fillContainer(@NotNull ItemStack container,
            @NotNull AEEssentiaStack stack) {
        if (EssentiaItemContainerHelper.INSTANCE.getItemType(container)
                != EssentiaItemContainerHelper.AspectItemType.EssentiaContainer) {
            return new ObjectIntImmutablePair<>(null, 0);
        }

        Item containerItem = container.getItem();
        if (containerItem == null) {
            return new ObjectIntImmutablePair<>(null, 0);
        }

        if (containerItem instanceof ItemJarFilled) {
            if (EssentiaItemContainerHelper.INSTANCE.doesJarHaveLabel(container)) {
                // Does the label match the aspect we are going to fill
                // with?
                if (stack.getAspect() != EssentiaItemContainerHelper.INSTANCE.getJarLabelAspect(container)) {
                    // Aspect does not match the jar's label
                    return new ObjectIntImmutablePair<>(null, 0);
                }
            }
        }

        if (containerItem instanceof IRestrictedEssentiaContainerItem restricted) {
            // Ensure the container accepts this essentia
            if (!restricted.acceptsAspect(stack.getAspect())) {
                // Not acceptable
                return new ObjectIntImmutablePair<>(null, 0);
            }
        }

        IThEEssentiaContainerPermission info = EssentiaItemContainerHelper.INSTANCE
                .getContainerInfo(containerItem, container.getItemDamage());
        if (info == null) {
            return new ObjectIntImmutablePair<>(null, 0);
        }

        int containerAmountStored = EssentiaItemContainerHelper.INSTANCE.getContainerStoredAmount(container);
        int remainaingStorage = info.maximumCapacity() - containerAmountStored;
        if (remainaingStorage <= 0) {
            return new ObjectIntImmutablePair<>(null, 0);
        }

        int amountToFill = (int) Math.min(stack.getStackSize(), Integer.MAX_VALUE);
        if (!info.canHoldPartialAmount()) {
            if (amountToFill < info.maximumCapacity()) {
                return new ObjectIntImmutablePair<>(null, 0);
            } else if (amountToFill > info.maximumCapacity()) {
                amountToFill = info.maximumCapacity();
            }
        } else {
            if (amountToFill > remainaingStorage) {
                amountToFill = remainaingStorage;
            }
        }

        ItemStack resultStack = null;

        // Is this a phial?
        if (containerItem instanceof ItemEssence) {
            // Create a new phial
            resultStack = EssentiaItemContainerHelper.INSTANCE.createFilledPhial(stack.getAspect());
        } else if (containerItem instanceof BlockJarItem) {
            // Create a fillable jar
            resultStack = EssentiaItemContainerHelper.INSTANCE
                    .createFilledJar(stack.getAspect(), amountToFill, container.getItemDamage(), false);
        }

        if (resultStack == null) {
            // Get the currently stored list.
            AspectList aspectList = ((IEssentiaContainerItem) containerItem).getAspects(container);

            // Is the containers list null?
            if (aspectList == null) {
                // Create a new aspect list
                aspectList = new AspectList();
            }
            // Does the list have anything in it?
            else if (aspectList.size() > 0) {
                // Does the stored aspect match the aspect to inject?
                if (stack.getAspect() != aspectList.getAspects()[0]) {
                    // Don't mix aspects
                    return new ObjectIntImmutablePair<>(null, 0);
                }
            }

            // Increase the list amount
            aspectList.add(stack.getAspect(), amountToFill);

            // Make a copy of the request
            resultStack = container.copy();

            // Set the stored amount
            ((IEssentiaContainerItem) Objects.requireNonNull(resultStack.getItem()))
                    .setAspects(resultStack, aspectList);
        }

        return new ObjectIntImmutablePair<>(resultStack, amountToFill);
    }
}
