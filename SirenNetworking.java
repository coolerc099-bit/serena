package ru.sirenblock.network;

import net.fabricmc.fabric.api.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import ru.sirenblock.SirenBlockMod;
import ru.sirenblock.block.entity.SirenBlockEntity;

public final class SirenNetworking {
    private SirenNetworking() {}

    public static void registerCommon() {
        PayloadTypeRegistry.clientboundPlay().register(OpenSirenScreenPayload.TYPE, OpenSirenScreenPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SirenUpdatePayload.TYPE, SirenUpdatePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SirenUpdatePayload.TYPE, (payload, context) -> {
            var player = context.player();
            if (player.level().dimension() != player.level().dimension()) return;
            if (!player.blockPosition().closerThan(payload.pos(), 8.0)) return;
            if (player.level().getBlockEntity(payload.pos()) instanceof SirenBlockEntity be) {
                be.apply(payload.enabled(), payload.radius(), payload.innerRadius(), payload.volume(), payload.loop(),
                        payload.redstoneControl(), payload.remoteSync(), payload.curve(), payload.soundId(), payload.url());
            }
        });
    }

    public static void registerClientReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(OpenSirenScreenPayload.TYPE, (payload, context) ->
                context.client().execute(() -> context.client().setScreen(new ru.sirenblock.client.SirenScreen(payload)))
        );
    }
}
