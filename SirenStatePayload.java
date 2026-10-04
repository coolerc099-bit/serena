package siren.controller.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SirenStatePayload(BlockPos pos, int type, int radius, boolean enabled) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath("siren_controller", "siren_state");
    public static final Type<SirenStatePayload> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SirenStatePayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            SirenStatePayload::pos,
            ByteBufCodecs.VAR_INT,
            SirenStatePayload::type,
            ByteBufCodecs.VAR_INT,
            SirenStatePayload::radius,
            ByteBufCodecs.BOOL,
            SirenStatePayload::enabled,
            SirenStatePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
