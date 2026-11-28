package thaumicenergistics.common.network.packet.client;

import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.beams.FXBeam;
import thaumcraft.common.Thaumcraft;
import thaumicenergistics.common.network.NetworkHandler;

/**
 * Particle effect packet, clients-in-area-bound
 *
 * @author Nividica
 *
 */
public class Packet_R_ParticleFX extends ThEAreaPacket {

    private static final byte MODE_WRENCH_FX = 0;
    private static final byte MODE_INFUSION_RUNES = 1;

    /**
     * Destination coords.
     */
    private float dX, dY, dZ;

    /**
     * Color of the FX.
     */
    private int aspectColor;

    public Packet_R_ParticleFX() {}

    private Packet_R_ParticleFX(final byte mode, final World world, final double x, final double y, final double z,
            final int aspectColor) {
        this.mode = mode;
        this.targetWorld = world;
        this.targetX = (float) x;
        this.targetY = (float) y;
        this.targetZ = (float) z;
        this.aspectColor = aspectColor;
    }

    public static void createWrenchFX(final World world, final double sourceX, final double sourceY,
            final double sourceZ, final double destinationX, final double destinationY, final double destinationZ,
            final Aspect aspectColor, final float range) {
        Packet_R_ParticleFX packet = new Packet_R_ParticleFX(
                MODE_WRENCH_FX,
                world,
                sourceX,
                sourceY,
                sourceZ,
                aspectColor.getColor());
        packet.dX = (float) destinationX;
        packet.dY = (float) destinationY;
        packet.dZ = (float) destinationZ;
        NetworkHandler.sendAreaPacketToClients(packet, range);
    }

    public static void sendInfusionRunes(final TileEntity provider, final int aspectColor) {
        Packet_R_ParticleFX packet = new Packet_R_ParticleFX(
                MODE_INFUSION_RUNES,
                provider.getWorldObj(),
                provider.xCoord,
                provider.yCoord,
                provider.zCoord,
                aspectColor);
        NetworkHandler.sendAreaPacketToClients(packet, 32);
    }

    @SideOnly(Side.CLIENT)
    private void addInfusionRunes() {
        Color color = new Color(this.aspectColor);
        float red = color.getRed() / 255.0F;
        float green = color.getGreen() / 255.0F;
        float blue = color.getBlue() / 255.0F;
        for (int i = 0; i < 10; i++) {
            float gravity = i < 5 ? -0.1F : 0.1F;
            Thaumcraft.proxy.blockRunes(targetWorld, targetX, targetY, targetZ, red, green, blue, 15, gravity);
        }
    }

    @SideOnly(Side.CLIENT)
    private void addWrenchFX() {
        // Get the color
        Color color = new Color(this.aspectColor);

        // Create the particle
        FXBeam beam = new FXBeam(
                this.targetWorld,
                this.targetX,
                this.targetY + 1.5D,
                this.targetZ,
                this.dX + 0.5D,
                this.dY + 0.5D,
                this.dZ + 0.5D,
                color.getRed() / 255.0F,
                color.getGreen() / 255.0F,
                color.getBlue() / 255.0F,
                6);

        // Set the width
        beam.width = 0.2F;

        // Add the particle to thaumcraft engine
        ParticleEngine.instance.addEffect(this.targetWorld, beam);
    }

    @Override
    protected void readData(final ByteBuf stream) {
        super.readData(stream);
        if (this.mode == MODE_WRENCH_FX) {
            this.dX = stream.readFloat();
            this.dY = stream.readFloat();
            this.dZ = stream.readFloat();
        }
        this.aspectColor = stream.readInt();
    }

    @Override
    protected void writeData(final ByteBuf stream) {
        super.writeData(stream);
        if (this.mode == MODE_WRENCH_FX) {
            stream.writeFloat(this.dX);
            stream.writeFloat(this.dY);
            stream.writeFloat(this.dZ);
        }
        stream.writeInt(this.aspectColor);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void areaExecute() {
        // Ensure the world is valid
        if (this.targetWorld == null) {
            return;
        }

        // Has the player set particles to minimum?
        if (Minecraft.getMinecraft().gameSettings.particleSetting == 2) {
            return;
        }

        switch (this.mode) {
            case MODE_WRENCH_FX:
                this.addWrenchFX();
                break;
            case MODE_INFUSION_RUNES:
                this.addInfusionRunes();
                break;
        }
    }
}
