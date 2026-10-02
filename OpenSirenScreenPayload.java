package ru.sirenblock.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import ru.sirenblock.SirenBlockMod;
import ru.sirenblock.block.entity.SirenBlockEntity;

public record OpenSirenScreenPayload(BlockPos pos, boolean enabled, int radius, int innerRadius, int volume,
                                     boolean loop, boolean redstoneControl, boolean remoteSync, int curve,
                                     String soundId, String url) implements CustomPacketPayload {
    public static final Identifier ID = SirenBlockMod.id("open_siren_screen");
    public static final Type<OpenSirenScreenPayload> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenSirenScreenPayload> CODEC = new StreamCodec<>() {
        @Override public void encode(RegistryFriendlyByteBuf b, OpenSirenScreenPayload p) {
            b.writeBlockPos(p.pos()); b.writeBoolean(p.enabled()); b.writeVarInt(p.radius()); b.writeVarInt(p.innerRadius());
            b.writeVarInt(p.volume()); b.writeBoolean(p.loop()); b.writeBoolean(p.redstoneControl()); b.writeBoolean(p.remoteSync());
            b.writeVarInt(p.curve()); b.writeUtf(p.soundId(), 128); b.writeUtf(p.url(), 2048);
        }
        @Override public OpenSirenScreenPayload decode(RegistryFriendlyByteBuf b) {
            return new OpenSirenScreenPayload(b.readBlockPos(), b.readBoolean(), b.readVarInt(), b.readVarInt(), b.readVarInt(),
                    b.readBoolean(), b.readBoolean(), b.readBoolean(), b.readVarInt(), b.readUtf(128), b.readUtf(2048));
        }
    };
    public OpenSirenScreenPayload { }
    public static OpenSirenScreenPayload from(SirenBlockEntity be) {
        return new OpenSirenScreenPayload(be.getBlockPos(), be.enabled(), be.radius(), be.innerRadius(), be.volume(), be.loop(),
                be.redstoneControl(), be.remoteSync(), be.curve(), be.soundId(), be.url());
    }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
