package ru.sirenblock.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import ru.sirenblock.block.entity.SirenBlockEntity;

public final class ClientSirenSoundInstance extends AbstractTickableSoundInstance {
    private final BlockPos pos;
    private boolean active = true;
    private int radius = 200;
    private int inner = 20;
    private int curve = 1;
    private float baseVolume = 1.0f;

    public ClientSirenSoundInstance(BlockPos pos) {
        super(SirenSounds.AIR_RAID, SoundSource.MASTER, RandomSource.create());
        this.pos = pos.immutable();
        this.looping = true;
        this.relative = false;
        this.attenuation = SoundInstance.Attenuation.NONE;
        this.pitch = 1.0f;
        this.volume = 0.0f;
        this.x = pos.getX() + 0.5;
        this.y = pos.getY() + 0.5;
        this.z = pos.getZ() + 0.5;
    }

    public void update(SirenBlockEntity be, Vec3 listener) {
        this.radius = be.radius();
        this.inner = Math.min(be.innerRadius(), be.radius());
        this.curve = be.curve();
        this.baseVolume = be.volume() / 100.0f;
        this.looping = be.loop();
        this.x = pos.getX() + 0.5;
        this.y = pos.getY() + 0.5;
        this.z = pos.getZ() + 0.5;
        double distance = listener.distanceTo(pos.getCenter());
        this.volume = this.baseVolume * attenuation(distance);
        if (!be.enabled() || distance > be.radius() + 2) this.active = false;
    }

    private float attenuation(double distance) {
        if (distance <= inner) return 1.0f;
        if (distance >= radius) return 0.0f;
        double t = (distance - inner) / Math.max(1.0, radius - inner);
        double v;
        switch (curve) {
            case 0 -> v = 1.0 - t;
            case 2 -> v = Math.pow(1.0 - t, 2.8);
            default -> {
                double smooth = t * t * (3.0 - 2.0 * t);
                v = 1.0 - smooth;
            }
        }
        return (float)Math.max(0.0, Math.min(1.0, v));
    }

    @Override public boolean canPlaySound() { return active; }
    @Override public boolean canStartSilent() { return true; }
    @Override public void tick() { if (!active) stop(); }
}
