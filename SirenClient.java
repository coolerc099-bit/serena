package siren.controller.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import siren.controller.SirenBlocks;
import siren.controller.network.OpenSirenScreenPayload;
import siren.controller.network.SirenStatePayload;

public final class SirenClient implements ClientModInitializer {
    private static ClientLevel lastLevel;

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(OpenSirenScreenPayload.TYPE, (payload, context) ->
                context.client().execute(() -> {
                    Minecraft client = context.client();
                    if (client.level == null) {
                        return;
                    }
                    if (client.level.getBlockState(payload.pos()).is(SirenBlocks.SIREN)) {
                        client.setScreenAndShow(new SirenScreen(payload.pos()));
                    }
                })
        );

        ClientPlayNetworking.registerGlobalReceiver(SirenStatePayload.TYPE, (payload, context) ->
                context.client().execute(() -> SirenSoundManager.applyState(context.client(), payload))
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level != lastLevel) {
                SirenSoundManager.clear();
                lastLevel = client.level;
            }
        });
    }
}
