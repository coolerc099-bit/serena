package siren.controller;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class SirenBlockEntities {
    public static final ResourceKey<BlockEntityType<SirenBlockEntity>> SIREN_KEY = ResourceKey.create(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(SirenMod.MOD_ID, "siren")
    );

    public static final BlockEntityType<SirenBlockEntity> SIREN = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            SIREN_KEY,
            BlockEntityType.Builder.of(SirenBlockEntity::new, SirenBlocks.SIREN).build(null)
    );

    private SirenBlockEntities() {}

    public static void register() {
        // Static initialization performs the registration.
    }
}
