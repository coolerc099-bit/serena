package siren.controller;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class SirenBlocks {
    public static final ResourceKey<Block> SIREN_KEY = ResourceKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(SirenMod.MOD_ID, "siren")
    );

    public static final Block SIREN = Registry.register(
            BuiltInRegistries.BLOCK,
            SIREN_KEY,
            new SirenBlock(
                    BlockBehaviour.Properties.of()
                            .setId(SIREN_KEY)
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)
                            .noOcclusion()
            )
    );

    public static final ResourceKey<Item> SIREN_ITEM_KEY = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(SirenMod.MOD_ID, "siren")
    );

    public static final BlockItem SIREN_ITEM = Registry.register(
            BuiltInRegistries.ITEM,
            SIREN_ITEM_KEY,
            new BlockItem(SIREN, new Item.Properties().setId(SIREN_ITEM_KEY).useBlockDescriptionPrefix())
    );

    private SirenBlocks() {
    }

    public static void register() {
        // Put the siren into several vanilla tabs so it is easy to find.
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS)
                .register(entries -> entries.accept(SIREN_ITEM));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register(entries -> entries.accept(SIREN_ITEM));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.REDSTONE_BLOCKS)
                .register(entries -> entries.accept(SIREN_ITEM));
    }
}
