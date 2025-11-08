package thaumicenergistics.common.storage;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import javax.annotation.Nonnull;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IItemList;
import appeng.util.item.MeaningfulAEStackIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class EssentiaList implements IItemList<AEEssentiaStack> {

    private final ObjectOpenHashSet<AEEssentiaStack> records = new ObjectOpenHashSet<>();

    @Override
    public void add(AEEssentiaStack stack) {
        if (stack == null) {
            return;
        }

        final AEEssentiaStack existing = this.records.get(stack);

        if (existing != null) {
            existing.add(stack);
            return;
        }

        this.records.add(stack.copy());
    }

    @Override
    public AEEssentiaStack findPrecise(AEEssentiaStack stack) {
        if (stack == null) return null;

        return this.records.get(stack);
    }

    @Override
    public Collection<AEEssentiaStack> findFuzzy(AEEssentiaStack filter, FuzzyMode fuzzy) {
        if (filter == null) {
            return Collections.emptyList();
        }

        AEEssentiaStack found = this.findPrecise(filter);
        if (found == null) {
            return Collections.emptyList();
        }

        return Collections.singletonList(found);
    }

    @Override
    public boolean isEmpty() {
        return !this.iterator().hasNext();
    }

    @Override
    public void addStorage(AEEssentiaStack stack) {
        if (stack == null) {
            return;
        }

        final AEEssentiaStack st = this.records.get(stack);

        if (st != null) {
            st.incStackSize(stack.getStackSize());
            return;
        }

        this.records.add(stack.copy());
    }

    @Override
    public void addCrafting(AEEssentiaStack stack) {
        if (stack == null) {
            return;
        }

        final AEEssentiaStack st = this.records.get(stack);

        if (st != null) {
            st.setCraftable(true);
            return;
        }

        final AEEssentiaStack opt = stack.copy();
        opt.setStackSize(0);
        opt.setCraftable(true);

        this.records.add(opt);
    }

    @Override
    public void addRequestable(AEEssentiaStack stack) {
        if (stack == null) {
            return;
        }

        final AEEssentiaStack st = this.records.get(stack);

        if (st != null) {
            st.setCountRequestable(st.getCountRequestable() + stack.getCountRequestable());
            return;
        }

        final AEEssentiaStack opt = stack.copy();
        opt.setStackSize(0);
        opt.setCraftable(false);
        opt.setCountRequestable(stack.getCountRequestable());

        this.records.add(opt);
    }

    @Override
    public AEEssentiaStack getFirstItem() {
        for (final AEEssentiaStack stackType : this) {
            return stackType;
        }

        return null;
    }

    @Override
    public int size() {
        return this.records.size();
    }

    @Override
    @Nonnull
    public Iterator<AEEssentiaStack> iterator() {
        return new MeaningfulAEStackIterator<>(this.records.iterator());
    }

    @Override
    public void resetStatus() {
        for (final AEEssentiaStack i : this) {
            i.reset();
        }
    }

    @Override
    public byte getStackType() {
        // TODO
        return 10;
    }
}
