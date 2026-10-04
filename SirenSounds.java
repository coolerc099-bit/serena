package siren.controller;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;

public final class SirenSounds {
    public static final int TYPE_COUNT = 5;

    public static final SoundEvent AIR_RAID = register("air_raid");
    public static final SoundEvent POLICE = register("police");
    public static final SoundEvent FIRE = register("fire");
    public static final SoundEvent NUCLEAR = register("nuclear");
    public static final SoundEvent INDUSTRIAL = register("industrial");

    private SirenSounds() {
    }

    private static SoundEvent register(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(SirenMod.MOD_ID, name);
        ResourceKey<SoundEvent> key = ResourceKey.create(Registries.SOUND_EVENT, id);
        SoundEvent event = SoundEvent.createVariableRangeEvent(id);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, key, event);
    }

    public static void register() {
        // Loads the class and therefore all five registrations.
    }

    public static SoundEvent byType(int type) {
        return switch (Math.floorMod(type, TYPE_COUNT)) {
            case 0 -> AIR_RAID;
            case 1 -> POLICE;
            case 2 -> FIRE;
            case 3 -> NUCLEAR;
            default -> INDUSTRIAL;
        };
    }
}
