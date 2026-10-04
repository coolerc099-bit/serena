package siren.controller.client;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import siren.controller.SirenBlocks;
import siren.controller.SirenSounds;
import siren.controller.network.SirenStatePayload;

public final class SirenSoundManager {
    private static final Map<SirenKey, SirenSoundInstance> PLAYING = new HashMap<>();

    private SirenSoundManager() {
    }

    public static void applyState(Minecraft client, SirenStatePayload payload) {
        if (!(client.level instanceof ClientLevel level)) {
            return;
        }

        SirenKey key = new SirenKey(level.dimension(), payload.pos().asLong());
        SirenSoundInstance previous = PLAYING.get(key);

        if (!payload.enabled()) {
            if (previous != null) {
                previous.forceStop();
            }
            return;
        }

        if (!level.getBlockState(payload.pos()).is(SirenBlocks.SIREN)) {
            if (previous != null) {
                previous.forceStop();
            }
            return;
        }

        if (previous != null && previous.matches(payload.type(), payload.radius())) {
            return;
        }

        if (previous != null) {
            previous.forceStop();
        }

        SoundEvent event = SirenSounds.byType(payload.type());
        SirenSoundInstance instance = new SirenSoundInstance(
                event,
                level,
                payload.pos(),
                payload.type(),
                payload.radius(),
                key
        );
        client.getSoundManager().play(instance);
        PLAYING.put(key, instance);
    }

    static void remove(SirenKey key, SirenSoundInstance instance) {
        if (PLAYING.get(key) == instance) {
            PLAYING.remove(key);
        }
    }

    public static void clear() {
        for (SirenSoundInstance instance : PLAYING.values()) {
            instance.forceStopWithoutRegistryRemoval();
        }
        PLAYING.clear();
    }

    public record SirenKey(ResourceKey<Level> dimension, long pos) {
    }
}
