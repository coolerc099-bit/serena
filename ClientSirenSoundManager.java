package ru.sirenblock.sound;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import ru.sirenblock.block.entity.SirenBlockEntity;

public final class ClientSirenSoundManager {
    private static final Map<BlockPos, ClientSirenSoundInstance> ACTIVE = new HashMap<>();
    private static int scanTicker = 0;

    private ClientSirenSoundManager() {}

    public static void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            stopAll(client);
            return;
        }
        if (++scanTicker < 10) return;
        scanTicker = 0;

        Level level = client.level;
        Map<BlockPos, SirenBlockEntity> nearby = new HashMap<>();
        int chunkRadius = Math.max(1, Math.min(34, (500 >> 4) + 2));
        int pcx = client.player.blockPosition().getX() >> 4;
        int pcz = client.player.blockPosition().getZ() >> 4;
        for (int cx = pcx - chunkRadius; cx <= pcx + chunkRadius; cx++) {
            for (int cz = pcz - chunkRadius; cz <= pcz + chunkRadius; cz++) {
                var chunk = level.getChunk(cx, cz);
                for (BlockEntity entity : chunk.getBlockEntities().values()) {
                    if (entity instanceof SirenBlockEntity be) nearby.put(be.getBlockPos(), be);
                }
            }
        }

        Set<BlockPos> keep = new HashSet<>();
        for (SirenBlockEntity be : nearby.values()) {
            double d = client.player.position().distanceTo(be.getBlockPos().getCenter());
            if (be.enabled() && d <= be.radius() + 4) {
                keep.add(be.getBlockPos());
                ClientSirenSoundInstance sound = ACTIVE.get(be.getBlockPos());
                if (sound == null || sound.isStopped()) {
                    sound = new ClientSirenSoundInstance(be.getBlockPos());
                    ACTIVE.put(be.getBlockPos(), sound);
                    client.getSoundManager().play(sound);
                }
                sound.update(be, client.player.position());
            }
        }

        ACTIVE.entrySet().removeIf(entry -> {
            if (!keep.contains(entry.getKey())) {
                client.getSoundManager().stop(entry.getValue());
                return true;
            }
            return false;
        });
    }

    private static void stopAll(Minecraft client) {
        ACTIVE.values().forEach(client.getSoundManager()::stop);
        ACTIVE.clear();
    }
}
