package siren.controller.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server -> client. Carries the full siren state for the GUI.
 * open == true  : open the screen;
 * open == false : only refresh an already open screen for the same block.
 */
public record OpenSirenScreenPayload(BlockPos pos, int type, int radius, boolean active, boolean open)
        implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath("siren_controller", "open_siren_screen");
    public static final Type<OpenSirenScreenPayload> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenSirenScreenPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            OpenSirenScreenPayload::pos,
            ByteBufCodecs.VAR_INT,
            OpenSirenScreenPayload::type,
            ByteBufCodecs.VAR_INT,
            OpenSirenScreenPayload::radius,
            ByteBufCodecs.BOOL,
            OpenSirenScreenPayload::active,
            ByteBufCodecs.BOOL,
            OpenSirenScreenPayload::open,
            OpenSirenScreenPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
