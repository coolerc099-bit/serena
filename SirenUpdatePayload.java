package ru.sirenblock.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import ru.sirenblock.SirenBlockMod;

public record SirenUpdatePayload(BlockPos pos, boolean enabled, int radius, int innerRadius, int volume,
                                 boolean loop, boolean redstoneControl, boolean remoteSync, int curve,
                                 String soundId, String url) implements CustomPacketPayload {
    public static final Identifier ID = SirenBlockMod.id("siren_update");
    public static final Type<SirenUpdatePayload> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SirenUpdatePayload> CODEC = new StreamCodec<>() {
        @Override public void encode(RegistryFriendlyByteBuf b, SirenUpdatePayload p) {
            b.writeBlockPos(p.pos()); b.writeBoolean(p.enabled()); b.writeVarInt(p.radius()); b.writeVarInt(p.innerRadius());
            b.writeVarInt(p.volume()); b.writeBoolean(p.loop()); b.writeBoolean(p.redstoneControl()); b.writeBoolean(p.remoteSync());
            b.writeVarInt(p.curve()); b.writeUtf(p.soundId(), 128); b.writeUtf(p.url(), 2048);
        }
        @Override public SirenUpdatePayload decode(RegistryFriendlyByteBuf b) {
            return new SirenUpdatePayload(b.readBlockPos(), b.readBoolean(), b.readVarInt(), b.readVarInt(), b.readVarInt(),
                    b.readBoolean(), b.readBoolean(), b.readBoolean(), b.readVarInt(), b.readUtf(128), b.readUtf(2048));
        }
    };
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
