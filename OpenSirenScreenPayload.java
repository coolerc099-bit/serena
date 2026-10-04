package siren.controller.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record OpenSirenScreenPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath("siren_controller", "open_siren_screen");
    public static final Type<OpenSirenScreenPayload> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenSirenScreenPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            OpenSirenScreenPayload::pos,
            OpenSirenScreenPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
