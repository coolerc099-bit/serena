package siren.controller;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import siren.controller.network.OpenSirenScreenPayload;
import siren.controller.network.SirenActionPayload;
import siren.controller.network.SirenStatePayload;

public final class SirenNetworking {
    private SirenNetworking() {
    }

    public static void registerCommon() {
        PayloadTypeRegistry.serverboundPlay().register(SirenActionPayload.TYPE, SirenActionPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(OpenSirenScreenPayload.TYPE, OpenSirenScreenPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SirenStatePayload.TYPE, SirenStatePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SirenActionPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            if (player.level().getBlockEntity(payload.pos()) instanceof SirenBlockEntity siren) {
                siren.applyAction(player, payload.action());
            }
        });
    }

    /** Opens the GUI on the player's client with the current server-side state. */
    public static void sendOpenScreen(ServerPlayer player, BlockPos pos, int type, int radius, boolean active) {
        ServerPlayNetworking.send(player, new OpenSirenScreenPayload(pos, type, radius, active, true));
    }

    /** Refreshes an already open GUI after a button press (never opens a new screen). */
    public static void sendScreenRefresh(ServerPlayer player, BlockPos pos, int type, int radius, boolean active) {
        ServerPlayNetworking.send(player, new OpenSirenScreenPayload(pos, type, radius, active, false));
    }

    public static void broadcastState(ServerLevel level, BlockPos pos, int type, int radius, boolean active) {
        double cx = pos.getX() + 0.5D;
        double cy = pos.getY() + 0.5D;
        double cz = pos.getZ() + 0.5D;
        double maxDistanceSq = (double) radius * radius;
        SirenStatePayload payload = new SirenStatePayload(pos, type, radius, active);

        for (ServerPlayer player : level.players()) {
            double dx = player.getX() - cx;
            double dy = player.getY() - cy;
            double dz = player.getZ() - cz;
            if (dx * dx + dy * dy + dz * dz <= maxDistanceSq) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }
}
