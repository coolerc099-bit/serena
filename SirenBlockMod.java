package ru.sirenblock;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sirenblock.block.SirenBlock;
import ru.sirenblock.block.entity.SirenBlockEntity;
import ru.sirenblock.network.SirenNetworking;
import ru.sirenblock.sound.SirenSounds;

public final class SirenBlockMod implements ModInitializer {
    public static final String MOD_ID = "sirenblock";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier SIREN_ID = Identifier.fromNamespaceAndPath(MOD_ID, "siren");
    public static final Block SIREN = Registry.register(
            BuiltInRegistries.BLOCK,
            SIREN_ID,
            new SirenBlock(BlockBehaviour.Properties.of().strength(1.5f).sound(SoundType.METAL).setId(ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, SIREN_ID)))
    );
    public static final Item SIREN_ITEM = Registry.register(
            BuiltInRegistries.ITEM,
            SIREN_ID,
            new BlockItem(SIREN, new Item.Properties().useBlockDescriptionPrefix().setId(ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, SIREN_ID)))
    );
    public static final BlockEntityType<SirenBlockEntity> SIREN_BE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(MOD_ID, "siren"),
            FabricBlockEntityTypeBuilder.create(SirenBlockEntity::new, SIREN).build()
    );

    @Override
    public void onInitialize() {
        SirenNetworking.registerCommon();
        SirenSounds.register();
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.REDSTONE_BLOCKS).register(tab -> tab.accept(new ItemStack(SIREN_ITEM)));
        LOGGER.info("Siren Block initialized for Minecraft 26.3");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
