package siren.controller;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public final class SirenBlockEntity extends BlockEntity {
    public static final int MIN_RADIUS = 20;
    public static final int MAX_RADIUS = 500;
    public static final int RADIUS_STEP = 20;
    public static final int RADIUS_BIG_STEP = 100;

    public static final int ACTION_TOGGLE = 0;
    public static final int ACTION_TYPE = 1;
    public static final int ACTION_RADIUS_DOWN = 2;
    public static final int ACTION_RADIUS_UP = 3;
    public static final int ACTION_RADIUS_DOWN_BIG = 4;
    public static final int ACTION_RADIUS_UP_BIG = 5;

    private int type;
    private int radius = 200;
    private boolean active;
    private int syncTimer;

    public SirenBlockEntity(BlockPos pos, BlockState state) {
        super(SirenBlockEntities.SIREN, pos, state);
    }

    public int getType() {
        return type;
    }

    public int getRadius() {
        return radius;
    }

    public boolean isActive() {
        return active;
    }

    public void applyAction(ServerPlayer player, int action) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (player.level() != serverLevel) {
            return;
        }

        double dx = player.getX() - (worldPosition.getX() + 0.5D);
        double dy = player.getY() - (worldPosition.getY() + 0.5D);
        double dz = player.getZ() - (worldPosition.getZ() + 0.5D);
        if (dx * dx + dy * dy + dz * dz > 64.0D) {
            return;
        }
        if (!serverLevel.getBlockState(worldPosition).is(SirenBlocks.SIREN)) {
            return;
        }

        switch (action) {
            case ACTION_TOGGLE -> setActive(serverLevel, !active);
            case ACTION_TYPE -> setType(serverLevel, (type + 1) % SirenSounds.TYPE_COUNT);
            case ACTION_RADIUS_DOWN -> setRadius(serverLevel, radius - RADIUS_STEP);
            case ACTION_RADIUS_UP -> setRadius(serverLevel, radius + RADIUS_STEP);
            case ACTION_RADIUS_DOWN_BIG -> setRadius(serverLevel, radius - RADIUS_BIG_STEP);
            case ACTION_RADIUS_UP_BIG -> setRadius(serverLevel, radius + RADIUS_BIG_STEP);
            default -> {
            }
        }

        // Always answer, so the open GUI shows the real server state.
        SirenNetworking.sendScreenRefresh(player, worldPosition, type, radius, active);
    }

    private void setActive(ServerLevel serverLevel, boolean value) {
        if (active == value) {
            return;
        }

        if (!value) {
            SirenNetworking.broadcastState(serverLevel, worldPosition, type, radius, false);
        }
        active = value;
        markAndSync(serverLevel);
        if (value) {
            SirenNetworking.broadcastState(serverLevel, worldPosition, type, radius, true);
        }
    }

    private void setType(ServerLevel serverLevel, int value) {
        value = Math.floorMod(value, SirenSounds.TYPE_COUNT);
        if (type == value) {
            return;
        }

        if (active) {
            SirenNetworking.broadcastState(serverLevel, worldPosition, type, radius, false);
        }
        type = value;
        markAndSync(serverLevel);
        if (active) {
            SirenNetworking.broadcastState(serverLevel, worldPosition, type, radius, true);
        }
    }

    private void setRadius(ServerLevel serverLevel, int value) {
        value = Math.max(MIN_RADIUS, Math.min(MAX_RADIUS, value));
        if (radius == value) {
            return;
        }

        if (active) {
            SirenNetworking.broadcastState(serverLevel, worldPosition, type, radius, false);
        }
        radius = value;
        markAndSync(serverLevel);
        if (active) {
            SirenNetworking.broadcastState(serverLevel, worldPosition, type, radius, true);
        }
    }

    private void markAndSync(ServerLevel serverLevel) {
        setChanged();
        serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SirenBlockEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!state.is(SirenBlocks.SIREN)) {
            if (entity.active) {
                entity.active = false;
            }
            return;
        }
        if (!entity.active) {
            return;
        }

        if (++entity.syncTimer < 10) {
            return;
        }
        entity.syncTimer = 0;
        SirenNetworking.broadcastState(serverLevel, pos, entity.type, entity.radius, true);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Type", type);
        output.putInt("Radius", radius);
        output.putBoolean("Active", active);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        type = Math.floorMod(input.getIntOr("Type", 0), SirenSounds.TYPE_COUNT);
        radius = Math.max(MIN_RADIUS, Math.min(MAX_RADIUS, input.getIntOr("Radius", 200)));
        active = input.getBooleanOr("Active", false);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
