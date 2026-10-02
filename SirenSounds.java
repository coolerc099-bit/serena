package ru.sirenblock.sound;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.Identifier;
import ru.sirenblock.SirenBlockMod;

public final class SirenSounds {
    public static final Identifier AIR_RAID_ID = SirenBlockMod.id("air_raid_siren");
    public static final SoundEvent AIR_RAID = Registry.register(
            BuiltInRegistries.SOUND_EVENT,
            AIR_RAID_ID,
            SoundEvent.createVariableRangeEvent(AIR_RAID_ID)
    );

    private SirenSounds() {}
    public static void register() {}
}
