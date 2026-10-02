package ru.sirenblock.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import ru.sirenblock.SirenBlockMod;

public final class SirenBlockEntity extends BlockEntity {
    public static final int MIN_RADIUS = 20;
    public static final int MAX_RADIUS = 500;

    private boolean enabled = false;
    private int radius = 200;
    private int innerRadius = 20;
    private int volume = 100;
    private boolean loop = true;
    private boolean redstoneControl = false;
    private boolean remoteSync = false;
    private int curve = 1; // 0 linear, 1 smooth, 2 exponential
    private String soundId = "air_raid_siren";
    private String url = "";

    public SirenBlockEntity(BlockPos pos, BlockState state) {
        super(SirenBlockMod.SIREN_BE, pos, state);
    }

    public boolean enabled() { return enabled; }
    public int radius() { return radius; }
    public int innerRadius() { return innerRadius; }
    public int volume() { return volume; }
    public boolean loop() { return loop; }
    public boolean redstoneControl() { return redstoneControl; }
    public boolean remoteSync() { return remoteSync; }
    public int curve() { return curve; }
    public String soundId() { return soundId; }
    public String url() { return url; }

    public void apply(boolean enabled, int radius, int innerRadius, int volume, boolean loop,
                      boolean redstoneControl, boolean remoteSync, int curve, String soundId, String url) {
        this.enabled = enabled;
        this.radius = clamp(radius, MIN_RADIUS, MAX_RADIUS);
        this.innerRadius = clamp(innerRadius, 0, this.radius);
        this.volume = clamp(volume, 0, 100);
        this.loop = loop;
        this.redstoneControl = redstoneControl;
        this.remoteSync = remoteSync;
        this.curve = clamp(curve, 0, 2);
        this.soundId = sanitizeSoundId(soundId);
        this.url = url == null ? "" : url.substring(0, Math.min(url.length(), 2048));
        setChanged();
        broadcast();
    }

    public void setEnabled(boolean value) {
        if (this.enabled == value) return;
        this.enabled = value;
        setChanged();
        broadcast();
    }

    public void serverTick() {
        if (level == null || level.isClientSide()) return;
        if (redstoneControl) {
            boolean signal = level.hasNeighborSignal(worldPosition);
            if (signal != enabled) {
                enabled = signal;
                setChanged();
                broadcast();
            }
        }
    }

    private void broadcast() {
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("Enabled", enabled);
        output.putInt("Radius", radius);
        output.putInt("InnerRadius", innerRadius);
        output.putInt("Volume", volume);
        output.putBoolean("Loop", loop);
        output.putBoolean("RedstoneControl", redstoneControl);
        output.putBoolean("RemoteSync", remoteSync);
        output.putInt("Curve", curve);
        output.putString("SoundId", soundId);
        output.putString("Url", url);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        enabled = input.getBooleanOr("Enabled", false);
        radius = clamp(input.getIntOr("Radius", 200), MIN_RADIUS, MAX_RADIUS);
        innerRadius = clamp(input.getIntOr("InnerRadius", 20), 0, radius);
        volume = clamp(input.getIntOr("Volume", 100), 0, 100);
        loop = input.getBooleanOr("Loop", true);
        redstoneControl = input.getBooleanOr("RedstoneControl", false);
        remoteSync = input.getBooleanOr("RemoteSync", false);
        curve = clamp(input.getIntOr("Curve", 1), 0, 2);
        soundId = sanitizeSoundId(input.getStringOr("SoundId", "air_raid_siren"));
        url = input.getStringOr("Url", "");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static String sanitizeSoundId(String value) {
        if (value == null || value.isBlank()) return "air_raid_siren";
        return value.length() > 128 ? value.substring(0, 128) : value;
    }
}
