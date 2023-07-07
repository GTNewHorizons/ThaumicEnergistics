package thaumicenergistics.client.gui.mui2.sync;

import java.io.IOException;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.api.sync.SyncHandler;

public class AspectCostSH extends SyncHandler {

    private Supplier<Float> visCostSupplier;
    private Supplier<Float> visDiscountSupplier;
    private Supplier<Float> visAmountSupplier;
    private BooleanSupplier enoughSupplier;
    private float cachedVisCost;
    private float cachedVisDiscount;
    private float cachedVisStored;
    private boolean cachedEnough;

    private AspectCostSH() {}

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        read(id, buf);
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        read(id, buf);
    }

    private void read(int id, PacketBuffer buf) {
        this.cachedVisCost = buf.readFloat();
        this.cachedVisDiscount = buf.readFloat();
        this.cachedVisStored = buf.readFloat();
        this.cachedEnough = buf.readBoolean();
    }

    public boolean hasEnoughVis() {
        return cachedEnough;
    }

    public float getVisStored() {
        return cachedVisStored;
    }

    public float getVisDiscount() {
        return cachedVisDiscount;
    }

    public float getVisCost() {
        return cachedVisCost;
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        float newVisCost = visCostSupplier.get();
        float newVisDiscount = visDiscountSupplier.get();
        float newVisStored = visAmountSupplier.get();
        boolean newEnough = enoughSupplier.getAsBoolean();
        if (init || newVisCost != cachedVisCost || newEnough != cachedEnough || newVisStored != cachedVisStored || newVisDiscount != cachedVisDiscount) {
            syncToClient(0, buffer -> {
                buffer.writeFloat(newVisCost);
                buffer.writeFloat(newVisDiscount);
                buffer.writeFloat(newVisStored);
                buffer.writeBoolean(newEnough);
            });
        }
        cachedVisCost = newVisCost;
        cachedVisStored = newVisStored;
        cachedEnough = newEnough;
    }

    public static AspectCostSHBuilder builder() {
        return new AspectCostSHBuilder();
    }

    public static class AspectCostSHBuilder {

        private final AspectCostSH result;

        private AspectCostSHBuilder() {
            result = new AspectCostSH();
        }

        public AspectCostSHBuilder visCost(Supplier<Float> visCostSupplier) {
            if (visCostSupplier != null) {
                result.visCostSupplier = visCostSupplier;
            }
            return this;
        }

        public AspectCostSHBuilder visDiscount(Supplier<Float> visDiscountSupplier) {
            if (visDiscountSupplier != null) {
                result.visDiscountSupplier = visDiscountSupplier;
            }
            return this;
        }

        public AspectCostSHBuilder visStored(Supplier<Float> visStoredSupplier) {
            if (visStoredSupplier != null) {
                result.visAmountSupplier = visStoredSupplier;
            }
            return this;
        }

        public AspectCostSHBuilder enoughVis(BooleanSupplier enoughSupplier) {
            if (enoughSupplier != null) {
                result.enoughSupplier = enoughSupplier;
            }
            return this;
        }

        public AspectCostSH build() {
            if (result.enoughSupplier == null) {
                throw new IllegalStateException("No enough supplier set!");
            }
            if (result.visDiscountSupplier == null) {
                throw new IllegalStateException("No discount supplier set!");
            }
            if (result.visAmountSupplier == null) {
                throw new IllegalStateException("No vis amount supplier set!");
            }
            if (result.visCostSupplier == null) {
                throw new IllegalStateException("No vis cost supplier set!");
            }
            return result;
        }
    }
}
