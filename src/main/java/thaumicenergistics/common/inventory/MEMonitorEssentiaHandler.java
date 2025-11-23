package thaumicenergistics.common.inventory;

import static thaumicenergistics.common.storage.AEEssentiaStackType.ESSENTIA_STACK_TYPE;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.StorageFilter;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageBusMonitor;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEStackType;
import appeng.api.storage.data.IItemList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.common.tiles.TileEssentiaReservoir;
import thaumicenergistics.api.storage.IAspectStack;
import thaumicenergistics.common.integration.tc.EssentiaTileContainerHelper;
import thaumicenergistics.common.storage.AEEssentiaStack;
import thaumicenergistics.common.storage.EssentiaList;

public class MEMonitorEssentiaHandler implements IStorageBusMonitor<AEEssentiaStack> {

    @NotNull
    private final IAspectContainer aspectContainer;

    private final HashMap<IMEMonitorHandlerReceiver<AEEssentiaStack>, Object> listeners = new HashMap<>();
    private EssentiaList cache = new EssentiaList();
    private BaseActionSource mySource;
    private StorageFilter mode;
    private boolean init = false;

    public MEMonitorEssentiaHandler(@NotNull IAspectContainer aspectContainer) {
        this.aspectContainer = aspectContainer;
    }

    @Override
    public TickRateModulation onTick() {
        this.init = true;

        EssentiaList currentList = new EssentiaList();
        for (AEEssentiaStack stack : EssentiaTileContainerHelper.INSTANCE
                .getEssentiaStacksFromContainer(this.aspectContainer)) {
            if (this.mode != StorageFilter.EXTRACTABLE_ONLY || EssentiaTileContainerHelper.INSTANCE
                    .extractFromContainer(this.aspectContainer, 1, stack.getAspect(), Actionable.SIMULATE) > 0) {
                currentList.add(stack);
            }
        }

        final EssentiaList changes = new EssentiaList();

        for (AEEssentiaStack stack : this.cache) {
            AEEssentiaStack copy = stack.copy();
            copy.setStackSize(-copy.getStackSize());
            changes.add(copy);
        }

        for (AEEssentiaStack stack : currentList) {
            changes.add(stack);
        }

        this.cache = currentList;

        Iterator<AEEssentiaStack> iter = changes.iterator();
        while (iter.hasNext()) {
            AEEssentiaStack stack = iter.next();
            if (stack.getStackSize() == 0) {
                iter.remove();
            }
        }

        if (!changes.isEmpty()) {
            this.postDifference(changes);
            return TickRateModulation.URGENT;
        }

        return TickRateModulation.SLOWER;
    }

    private void postDifference(Iterable<AEEssentiaStack> changes) {
        Iterator<Map.Entry<IMEMonitorHandlerReceiver<AEEssentiaStack>, Object>> iter = this.listeners.entrySet()
                .iterator();

        while (iter.hasNext()) {
            Map.Entry<IMEMonitorHandlerReceiver<AEEssentiaStack>, Object> entry = iter.next();
            IMEMonitorHandlerReceiver<AEEssentiaStack> listener = entry.getKey();
            if (listener.isValid(entry.getValue())) {
                listener.postChange(this, changes, this.mySource);
            } else {
                iter.remove();
            }
        }
    }

    @Override
    public void setMode(StorageFilter mode) {
        this.mode = mode;
    }

    @Override
    public void setActionSource(BaseActionSource mySource) {
        this.mySource = mySource;
    }

    @Override
    public IItemList<AEEssentiaStack> getStorageList() {
        return this.cache;
    }

    @Override
    public IItemList<AEEssentiaStack> getAvailableItems(IItemList<AEEssentiaStack> out, int iteration) {
        if (!this.init) {
            this.onTick();
        }
        for (AEEssentiaStack fs : this.cache) {
            out.addStorage(fs);
        }

        return out;
    }

    @Override
    public AEEssentiaStack getAvailableItem(@NotNull AEEssentiaStack request, int iteration) {
        if (!this.init) {
            this.onTick();
        }
        return this.cache.findPrecise(request);
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public void addListener(IMEMonitorHandlerReceiver l, Object verificationToken) {
        this.listeners.put(l, verificationToken);
    }

    @Override
    public void removeListener(IMEMonitorHandlerReceiver l) {
        this.listeners.remove(l);
    }

    @Override
    public boolean isPrioritized(AEEssentiaStack input) {
        return false;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public int getSlot() {
        return 0;
    }

    @Override
    public boolean validForPass(int i) {
        return true;
    }

    @Override
    public AccessRestriction getAccess() {
        return AccessRestriction.READ_WRITE;
    }

    @Override
    public boolean canAccept(AEEssentiaStack input) {
        if (this.aspectContainer instanceof TileEssentiaReservoir) {
            return true;
        }

        IAspectStack containerStack = EssentiaTileContainerHelper.INSTANCE
                .getAspectStackFromContainer(this.aspectContainer);

        if (containerStack == null) {
            return true;
        }

        return input.getAspect() == containerStack.getAspect();
    }

    @Override
    public AEEssentiaStack injectItems(AEEssentiaStack input, Actionable action, BaseActionSource src) {
        if (input == null || !this.canAccept(input)) {
            return input;
        }

        long injected = EssentiaTileContainerHelper.INSTANCE.injectEssentiaIntoContainer(
                this.aspectContainer,
                (int) Math.min(Integer.MAX_VALUE, input.getStackSize()),
                input.getAspect(),
                action);

        if (action == Actionable.MODULATE) {
            this.onTick();
        }

        if (input.getStackSize() == injected) {
            return null;
        }

        AEEssentiaStack leftover = input.copy();
        leftover.decStackSize(injected);
        return leftover;
    }

    @Override
    public AEEssentiaStack extractItems(AEEssentiaStack request, Actionable action, BaseActionSource src) {
        if (request == null) {
            return null;
        }

        long extracted = EssentiaTileContainerHelper.INSTANCE.extractFromContainer(
                this.aspectContainer,
                (int) Math.min(Integer.MAX_VALUE, request.getStackSize()),
                request.getAspect(),
                action);

        if (action == Actionable.MODULATE) {
            this.onTick();
        }

        if (extracted == 0) return null;

        return request.copy().setStackSize(extracted);
    }

    @Override
    public StorageChannel getChannel() {
        return null;
    }

    @Override
    public @NotNull IAEStackType<?> getStackType() {
        return ESSENTIA_STACK_TYPE;
    }
}
