package siren.controller.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import siren.controller.SirenBlocks;

public final class SirenSoundInstance extends AbstractTickableSoundInstance {
    private final ClientLevel level;
    private final BlockPos pos;
    private final int type;
    private final int radius;
    private final SirenSoundManager.SirenKey key;

    public SirenSoundInstance(
            SoundEvent event,
            ClientLevel level,
            BlockPos pos,
            int type,
            int radius,
            SirenSoundManager.SirenKey key
    ) {
        super(event, SoundSource.BLOCKS, RandomSource.create());
        this.level = level;
        this.pos = pos.immutable();
        this.type = type;
        this.radius = radius;
        this.key = key;
        this.looping = true;
        this.relative = false;
        this.attenuation = SoundInstance.Attenuation.NONE;
        this.x = pos.getX() + 0.5D;
        this.y = pos.getY() + 0.5D;
        this.z = pos.getZ() + 0.5D;
        this.pitch = 1.0F;

        // IMPORTANT: Minecraft skips a sound that starts with volume 0 (canStartSilent() is false),
        // so the siren would never play. Start from a small, non-zero volume instead.
        float initial = 0.05F;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            double dx = player.getX() - this.x;
            double dy = player.getY() - this.y;
            double dz = player.getZ() - this.z;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            initial = Math.max(0.02F, Math.min(0.35F, volumeFor(distance, radius)));
        }
        this.volume = initial;
    }

    static float volumeFor(double distance, int radius) {
        double normalized = Math.max(0.0D, Math.min(1.0D, distance / radius));
        double distanceFactor = 1.0D - Math.pow(normalized, 2.15D);
        double roomTail = 0.012D * (1.0D - normalized);
        return (float) Math.max(0.001D, Math.min(1.0D, 0.98D * distanceFactor + roomTail));
    }

    public boolean matches(int otherType, int otherRadius) {
        return type == otherType && radius == otherRadius && !isStopped();
    }

    public void forceStop() {
        stop();
        SirenSoundManager.remove(key, this);
    }

    void forceStopWithoutRegistryRemoval() {
        stop();
    }

    @Override
    public void tick() {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || client.level != level || !level.getBlockState(pos).is(SirenBlocks.SIREN)) {
            forceStop();
            return;
        }

        double dx = player.getX() - x;
        double dy = player.getY() - y;
        double dz = player.getZ() - z;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance > radius) {
            forceStop();
            return;
        }

        double targetVolume = volumeFor(distance, radius);

        // A tiny attack avoids a digital click when a fresh looping instance starts.
        volume += (float) ((targetVolume - volume) * 0.22D);
        if (Math.abs(targetVolume - volume) < 0.002F) {
            volume = (float) targetVolume;
        }
    }
}
