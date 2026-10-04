package siren.controller.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SirenActionPayload(BlockPos pos, int action) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath("siren_controller", "siren_action");
    public static final Type<SirenActionPayload> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SirenActionPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            SirenActionPayload::pos,
            ByteBufCodecs.VAR_INT,
            SirenActionPayload::action,
            SirenActionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
