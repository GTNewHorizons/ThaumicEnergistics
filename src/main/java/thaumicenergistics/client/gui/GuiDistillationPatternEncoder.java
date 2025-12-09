package thaumicenergistics.client.gui;

import java.util.ArrayList;
import java.util.Collections;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.slots.VirtualMEPhantomSlot;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.config.ConfigItems;
import thaumicenergistics.client.gui.buttons.GuiButtonEncodePattern;
import thaumicenergistics.client.gui.buttons.GuiButtonResetAspectSlot;
import thaumicenergistics.client.textures.AEStateIconsEnum;
import thaumicenergistics.client.textures.GuiTextureManager;
import thaumicenergistics.common.container.ContainerDistillationPatternEncoder;
import thaumicenergistics.common.network.packet.server.Packet_S_DistillationEncoder;
import thaumicenergistics.common.registries.ThEStrings;
import thaumicenergistics.common.storage.AEEssentiaStack;
import thaumicenergistics.common.tiles.TileDistillationPatternEncoder;
import thaumicenergistics.common.utils.ThEUtils;

/**
 * {@link TileDistillationPatternEncoder} GUI
 *
 * @author Nividica
 *
 */
@SideOnly(Side.CLIENT)
public class GuiDistillationPatternEncoder extends AEBaseGui {

    private static final ResourceLocation UNKNOWN_TEXTURE = new ResourceLocation(
            "thaumcraft",
            "textures/aspects/_unknown.png");

    /**
     * Gui size.
     */
    private static final int GUI_WIDTH = 176, GUI_HEIGHT = 234;

    /**
     * Position of the title string.
     */
    private static final int TITLE_POS_X = 6, TITLE_POS_Y = 6;

    /**
     * Position of the encode button
     */
    private static final int BUTTON_ENCODE_POS_X = 146, BUTTON_ENCODE_POS_Y = 78;

    /**
     * Position of the reset button
     */
    private static final int BUTTON_RESET_POS_X = 146, BUTTON_RESET_POS_Y = 42;

    /**
     * Half of the size of a standard item
     */
    private static final int ITEM_HALF_SIZE = 8;

    /**
     * Scale to draw the Thaumometer at.
     */
    private static final float THAUMOMETER_SCALE = 2.8f;

    /**
     * Title of the gui.
     */
    private final String title;

    /**
     * The GUI's container.
     */
    private final ContainerDistillationPatternEncoder deContainer;

    /**
     * Thaumcraft's thaumometer
     */
    private final ItemStack thaumometer = new ItemStack(ConfigItems.itemThaumometer);

    /**
     * Particles
     */
    private final ArrayList<GuiParticleAnimator> particles = new ArrayList<GuiParticleAnimator>();

    /**
     * The encode button.
     */
    private GuiButtonEncodePattern buttonEncode;

    /**
     * The encode button.
     */
    private GuiButtonResetAspectSlot buttonReset;

    /**
     * Set true when the source item may have been changed.
     */
    private boolean sourceItemDirty = false;
    private boolean unknownAspect = false;

    public VirtualMEPhantomSlot[] aspectSlots = new VirtualMEPhantomSlot[16];

    public GuiDistillationPatternEncoder(final EntityPlayer player, final World world, final int x, final int y,
            final int z) {
        // Call super
        super(new ContainerDistillationPatternEncoder(player, world, x, y, z));

        // Set the title
        this.title = ThEStrings.Block_DistillationEncoder.getLocalized();

        // Set the GUI size
        this.xSize = GuiDistillationPatternEncoder.GUI_WIDTH;
        this.ySize = GuiDistillationPatternEncoder.GUI_HEIGHT;

        // Set the container
        this.deContainer = (ContainerDistillationPatternEncoder) this.inventorySlots;
    }

    @Override
    public void drawFG(int i, int i1, int i2, int i3) {
        // Draw the title
        this.fontRendererObj.drawString(
                this.title,
                GuiDistillationPatternEncoder.TITLE_POS_X,
                GuiDistillationPatternEncoder.TITLE_POS_Y,
                0);

        // Check the source item
        if (this.sourceItemDirty) {
            this.checkSourceItem();
        }

        // Any particles?
        if (!this.particles.isEmpty()) {
            // Prep
            EnumGuiParticles.Orb.prepareDraw();

            // Draw each
            // Remove if done.
            this.particles.removeIf(guiParticleAnimator -> !guiParticleAnimator.draw(this, false));

            // Finish
            EnumGuiParticles.finishDraw();
        }
    }

    @Override
    public void drawBG(int i, int i1, int i2, int i3) {
        // Full white
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        // Bind the encoder gui texture
        Minecraft.getMinecraft().renderEngine.bindTexture(GuiTextureManager.DISTILLATION_ENCODER.getTexture());

        // Draw the gui texture
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);

        // Calculate the position and rotation of the thaumometer
        float th_PosX = (float) this.guiLeft + ContainerDistillationPatternEncoder.SLOT_SOURCE_ITEM_POS_X
                + GuiDistillationPatternEncoder.ITEM_HALF_SIZE;
        float th_PosY = this.guiTop + (ContainerDistillationPatternEncoder.SLOT_SOURCE_ITEM_POS_Y - 0.25f)
                + GuiDistillationPatternEncoder.ITEM_HALF_SIZE;
        float th_Rotation = (System.currentTimeMillis() % 36000) * 0.02f;
        float th_ScaleOffset = (float) Math.sin(th_Rotation * 0.15f) * 0.1f;

        if (unknownAspect) {
            mc.renderEngine.bindTexture(UNKNOWN_TEXTURE);

            GL11.glColor4f(0.5f, 0.5f, 0.5f, 0.5f);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

            for (int x = 0; x < 4; x++) {
                for (int y = 0; y < 4; y++) {
                    UtilsFX.drawTexturedQuadFull(this.guiLeft + 62 + 18 * x, this.guiTop + 42 + 18 * y, 0);
                }
            }

            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glDisable(GL11.GL_BLEND);
        }

        // Disable depth testing and push the matrix
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glPushMatrix();

        // Translate
        GL11.glTranslatef(th_PosX, th_PosY, 0.0F);

        // Scale
        GL11.glScalef(
                GuiDistillationPatternEncoder.THAUMOMETER_SCALE + th_ScaleOffset,
                GuiDistillationPatternEncoder.THAUMOMETER_SCALE + th_ScaleOffset,
                1.0f);

        // Rotate
        GL11.glRotatef(th_Rotation, 0.0f, 0.0f, 1.0f);

        // Draw thaumometer
        GuiScreen.itemRender.renderItemAndEffectIntoGUI(
                this.fontRendererObj,
                this.mc.getTextureManager(),
                this.thaumometer,
                -GuiDistillationPatternEncoder.ITEM_HALF_SIZE,
                -GuiDistillationPatternEncoder.ITEM_HALF_SIZE);

        // Restore
        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    /**
     * Checks for item changes to the source item.
     */
    private void checkSourceItem() {
        // Clear the dirty bit
        this.sourceItemDirty = false;

        // Clear any existing particles
        this.particles.clear();

        // Is there a source item to check?
        if (!this.deContainer.slotSourceItem.getHasStack()) {
            unknownAspect = false;
            return;
        }

        // Check each slot
        boolean isItemScanned = false;
        for (VirtualMEPhantomSlot slot : this.aspectSlots) {
            // Does the slot have a stack?
            if (slot.getAEStack() != null) {
                // Get the aspect for that stack
                Aspect aspect = ((AEEssentiaStack) slot.getAEStack()).getAspect();

                // Found something
                isItemScanned = true;

                // Create the animator
                GuiParticleAnimator gpa = new GuiParticleAnimator(
                        this.deContainer.slotSourceItem.xDisplayPosition,
                        this.deContainer.slotSourceItem.yDisplayPosition,
                        slot.getX(),
                        slot.getY(),
                        0.3f,
                        EnumGuiParticles.Orb);

                // Set FPS
                gpa.setFPS(30);

                // Set the color
                float[] argb = ThEGuiHelper.INSTANCE.convertPackedColorToARGBf(aspect.getColor());
                gpa.setColor(argb[1], argb[2], argb[3]);

                // Add to the list
                this.particles.add(gpa);
            } else {
                break;
            }
        }

        if (isItemScanned) {
            unknownAspect = false;
            // Play the on sound
            ThEUtils.playClientSound(null, "thaumcraft:hhon");
        } else {
            unknownAspect = true;
            // Play the off sound
            ThEUtils.playClientSound(null, "thaumcraft:hhoff");
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == this.buttonEncode) {
            // Ask server to encode

            Packet_S_DistillationEncoder.sendEncodePattern();
        } else if (button == this.buttonReset) {
            // Ask server to reset aspects
            Packet_S_DistillationEncoder.sendResetAspect();
        } else {
            super.actionPerformed(button);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initGui() {
        // Call super
        super.initGui();

        // Create the encode button
        this.buttonEncode = new GuiButtonEncodePattern(
                0,
                GuiDistillationPatternEncoder.BUTTON_ENCODE_POS_X + this.guiLeft,
                GuiDistillationPatternEncoder.BUTTON_ENCODE_POS_Y + this.guiTop,
                AEStateIconsEnum.STANDARD_ICON_SIZE,
                AEStateIconsEnum.STANDARD_ICON_SIZE);
        this.buttonList.add(this.buttonEncode);

        // Create the reset aspect button
        this.buttonReset = new GuiButtonResetAspectSlot(
                1,
                GuiDistillationPatternEncoder.BUTTON_RESET_POS_X + this.guiLeft,
                GuiDistillationPatternEncoder.BUTTON_RESET_POS_Y + this.guiTop,
                AEStateIconsEnum.STANDARD_ICON_SIZE,
                AEStateIconsEnum.STANDARD_ICON_SIZE);
        this.buttonList.add(this.buttonReset);

        // Reset flags
        this.sourceItemDirty = false;

        for (int i = 0; i < this.aspectSlots.length; i++) {
            VirtualMEPhantomSlot slot = new VirtualMEPhantomSlot(
                    62 + 18 * (i % 4),
                    42 + 18 * (i / 4),
                    this.deContainer.getAspectsInventory(),
                    i);
            this.aspectSlots[i] = slot;
            this.registerVirtualSlots(slot);
        }
    }

    @Override
    protected void handlePhantomSlotInteraction(VirtualMEPhantomSlot slot, int mouseButton) {
        slot.handleMouseClicked(Collections.EMPTY_LIST, isCtrlKeyDown(), mouseButton);
    }

    public void setChangedSrcItem() {
        this.sourceItemDirty = true;
        this.unknownAspect = false;
    }

    public void setUnknownAspect() {
        this.unknownAspect = true;
    }
}
