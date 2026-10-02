package ru.sirenblock.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import ru.sirenblock.network.SirenNetworking;
import ru.sirenblock.sound.ClientSirenSoundManager;

public final class SirenBlockClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SirenNetworking.registerClientReceiver();
        ClientTickEvents.END_CLIENT_TICK.register(ClientSirenSoundManager::tick);
    }
}
