package thaumicenergistics.common.storage;

import static thaumicenergistics.common.storage.AEEssentiaStackType.ESSENTIA_STACK_TYPE;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import com.djgiannuzz.thaumcraftneiplugin.ModItems;
import com.djgiannuzz.thaumcraftneiplugin.items.ItemAspect;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEStackType;
import appeng.api.storage.data.IAETagCompound;
import appeng.util.item.AEStack;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.Thaumcraft;

public class AEEssentiaStack extends AEStack<AEEssentiaStack> {

    private static final String NBTKEY_ASPECT_TAG = "AspectTag";
    private static final String NBTKEY_ASPECT_AMOUNT = "Amount";
    private static final String NBTKEY_CRAFTABLE = "Craftable";

    @Nonnull
    private final Aspect aspect;

    public AEEssentiaStack(@Nonnull Aspect aspect) {
        this.aspect = aspect;
    }

    private AEEssentiaStack(@Nonnull AEEssentiaStack stack) {
        this.aspect = stack.aspect;
        this.setStackSize(stack.getStackSize());
        this.setCraftable(stack.isCraftable());
        this.setCountRequestable(stack.getCountRequestable());
        this.setCountRequestableCrafts(stack.getCountRequestableCrafts());
        this.setUsedPercent(stack.getUsedPercent());
    }

    @NotNull
    public Aspect getAspect() {
        return this.aspect;
    }

    @Override
    public IAEStackType<AEEssentiaStack> getStackType() {
        return ESSENTIA_STACK_TYPE;
    }

    @Override
    public void add(AEEssentiaStack is) {
        if (is == null) {
            return;
        }

        this.incStackSize(is.getStackSize());
        this.setCountRequestable(this.getCountRequestable() + is.getCountRequestable());
        this.setCraftable(this.isCraftable() || is.isCraftable());
        this.setCountRequestableCrafts(this.getCountRequestableCrafts() + is.getCountRequestableCrafts());
    }

    @Override
    public AEEssentiaStack empty() {
        final AEEssentiaStack dup = this.copy();
        dup.reset();
        return dup;
    }

    @Override
    public AEEssentiaStack copy() {
        return new AEEssentiaStack(this);
    }

    @Override
    public int hashCode() {
        return this.aspect.getTag().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AEEssentiaStack aes && this.isSameType(aes);
    }

    @Override
    public boolean isSameType(AEEssentiaStack stack) {
        return stack != null && stack.aspect.getTag().equals(this.aspect.getTag());
    }

    @Override
    public boolean isSameType(Object stack) {
        return stack instanceof AEEssentiaStack aes && this.isSameType(aes);
    }

    @Override
    public String getUnlocalizedName() {
        // TODO
        return this.aspect.getTag();
    }

    @Override
    public String getLocalizedName() {
        return this.aspect.getName();
    }

    @Override
    public String getDisplayName() {
        return this.aspect.getName();
    }

    public String getDisplayName(final EntityPlayer player) {
        if (!this.hasPlayerDiscovered(player)) {
            return StatCollector.translateToLocal("tc.aspect.unknown");
        }

        return this.getDisplayName();
    }

    @Override
    public boolean hasTagCompound() {
        return false;
    }

    @Override
    public void setTagCompound(NBTTagCompound tag) {

    }

    @Override
    public IAETagCompound getTagCompound() {
        return null;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setString(NBTKEY_ASPECT_TAG, this.aspect.getTag());

        tag.setByte("Count", (byte) 0);
        tag.setLong("Cnt", this.getStackSize());
        tag.setLong("Req", this.getCountRequestable());
        tag.setBoolean("Craft", this.isCraftable());
        if (this.getCountRequestableCrafts() != 0L) tag.setLong("ReqMade", this.getCountRequestableCrafts());
        if (this.getUsedPercent() != 0) tag.setFloat("UsedPercent", this.getUsedPercent());
    }

    public static AEEssentiaStack loadStackFromNBT(NBTTagCompound tag) {
        if (!tag.hasKey(NBTKEY_ASPECT_TAG)) return null;

        Aspect aspect = Aspect.aspects.get(tag.getString(NBTKEY_ASPECT_TAG));
        if (aspect == null) return null;

        final AEEssentiaStack stack = new AEEssentiaStack(aspect);
        stack.setStackSize(tag.getLong("Cnt"));
        stack.setCountRequestable(tag.getLong("Req"));
        stack.setCraftable(tag.getBoolean("Craft"));
        stack.setCountRequestableCrafts(tag.getLong("ReqMade"));
        stack.setUsedPercent(tag.getFloat("UsedPercent"));

        if (tag.hasKey(NBTKEY_ASPECT_AMOUNT)) {
            stack.setStackSize(tag.getLong(NBTKEY_ASPECT_AMOUNT));
        }
        if (tag.hasKey(NBTKEY_CRAFTABLE)) {
            stack.setCraftable(tag.getBoolean(NBTKEY_CRAFTABLE));
        }

        return stack;
    }

    public static AEEssentiaStack loadEssentiaStackFromPacket(final ByteBuf data) throws IOException {
        final byte mask = data.readByte();
        final byte stackType = (byte) ((mask & 0x0C) >> 2);
        final byte countReqType = (byte) ((mask & 0x30) >> 4);
        final boolean isCraftable = (mask & 0x40) > 0;

        final byte len2 = data.readByte();
        final byte[] name = new byte[len2];
        data.readBytes(name, 0, len2);

        final long stackSize = getPacketValue(stackType, data);
        final long countRequestable = getPacketValue(countReqType, data);

        final byte mask2 = data.readByte();
        final byte countReqMadeType = (byte) ((mask2 & 0x3));
        final byte usedPercentType = (byte) ((mask2 & 0xC) >> 2);
        final long countRequestableCrafts = getPacketValue(countReqMadeType, data);
        final long longUsedPercent = getPacketValue(usedPercentType, data);

        Aspect aspect = Aspect.getAspect(new String(name, StandardCharsets.UTF_8));
        if (aspect == null) return null;

        final AEEssentiaStack aes = new AEEssentiaStack(aspect);
        aes.setStackSize(stackSize);
        aes.setCountRequestable(countRequestable);
        aes.setCraftable(isCraftable);
        aes.setCountRequestableCrafts(countRequestableCrafts);
        aes.setUsedPercent(longUsedPercent / 10000f);
        return aes;
    }

    protected void writeIdentity(final ByteBuf i) throws IOException {
        final byte[] name = this.aspect.getTag().getBytes(StandardCharsets.UTF_8);
        i.writeByte((byte) name.length);
        i.writeBytes(name);
    }

    @Override
    protected void readNBT(ByteBuf i) throws IOException {}

    @Override
    public String getModId() {
        return "thaumcraft";
    }

    @Override
    public boolean fuzzyComparison(Object st, FuzzyMode mode) {
        return this.isSameType(st);
    }

    @Override
    public boolean isItem() {
        return false;
    }

    @Override
    public boolean isFluid() {
        return false;
    }

    @Override
    public StorageChannel getChannel() {
        return null;
    }

    @Override
    public int getAmountPerUnit() {
        return 1;
    }

    public boolean hasPlayerDiscovered(final EntityPlayer player) {
        if (player != null) {
            // Ask Thaumcraft if the player has discovered the aspect
            return Thaumcraft.proxy.getPlayerKnowledge()
                    .hasDiscoveredAspect(player.getCommandSenderName(), this.aspect);
        }

        return false;
    }

    @Nullable
    @Override
    public ItemStack getItemStackForNEI() {
        if (Loader.isModLoaded("thaumcraftneiplugin")) {
            ItemStack stack = new ItemStack(ModItems.itemAspect);
            ItemAspect.setAspects(
                    stack,
                    (new AspectList()).add(this.aspect, (int) Math.min(this.getStackSize(), Integer.MAX_VALUE)));
            return stack;
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    private static final ResourceLocation UNKNOWN_TEXTURE = new ResourceLocation(
            "thaumcraft",
            "textures/aspects/_unknown.png");

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInGui(Minecraft mc, int x, int y) {
        if (this.hasPlayerDiscovered(mc.thePlayer)) {
            // Draw the aspect
            UtilsFX.drawTag(x, y, this.aspect, 0, 0, 0);
        }
        // Draw the question mark
        else {
            // Bind the Thaumcraft question mark texture
            mc.renderEngine.bindTexture(UNKNOWN_TEXTURE);

            int color = this.aspect.getColor();
            GL11.glColor4ub(
                    (byte) ((color >> 8) & 0xff),
                    (byte) ((color >> 16) & 0xff),
                    (byte) ((color >> 24) & 0xff),
                    (byte) 255);

            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            // Ask Thaumcraft to draw the question texture
            UtilsFX.drawTexturedQuadFull(x, y, 0);

            // Disable blending
            GL11.glDisable(GL11.GL_BLEND);
        }
    }
}
