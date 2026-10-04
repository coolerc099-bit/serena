# API verification for Minecraft 26.3 + Fabric

This file records the high-risk API decisions used by the project. The build itself resolves the real Minecraft 26.3 artifact through Fabric Loom.

## Minecraft 26.3 / Fabric build

The official Fabric example for branch `26.3` uses:

- Minecraft `26.3`
- Fabric Loader `0.19.5`
- Fabric API `0.161.0+26.3`
- Loom `1.18-SNAPSHOT`
- Java release `25`
- split environment source sets

Reference: https://github.com/FabricMC/fabric-example-mod/tree/26.3

## Registration

`SirenBlocks` uses the 26.3-style pattern with `ResourceKey`, `BuiltInRegistries` and `BlockBehaviour.Properties.setId(...)`.

`SirenBlockEntities` uses a real `BlockEntityType.Builder` and registers in `BuiltInRegistries.BLOCK_ENTITY_TYPE`.

Reference: https://github.com/FabricMC/fabric-docs/blob/main/reference/latest/src/main/java/com/example/docs/block/ModBlocks.java

## Block codec correction

Do not add `MapCodec`/`Block#codec()` to `SirenBlock` for 26.3. The block codec registry and `Block#codec` were removed during the 26.x technical changes.

## Networking

The project uses the 26.2+ naming used by modern Fabric API:

- `PayloadTypeRegistry.serverboundPlay()`
- `PayloadTypeRegistry.clientboundPlay()`
- `StreamCodec`
- `BlockPos.STREAM_CODEC`
- `ByteBufCodecs`

Reference: https://docs.fabricmc.net/develop/networking

## Client screen

The client opens the GUI with:

`Minecraft#setScreenAndShow(Screen)`

The 26.3 client exposes `Minecraft.level` as `ClientLevel`.

The screen uses `GuiGraphicsExtractor` and `Button.builder(...)`, matching the 26.3 GUI API.

## Block entity storage

The 26.3 BlockEntity storage API uses `ValueInput` / `ValueOutput` for `loadAdditional` / `saveAdditional`. The siren stores type, radius and active state there.

## Sound

The project registers five variable-range `SoundEvent`s and uses a custom `AbstractTickableSoundInstance` with `looping = true`.

Distance attenuation is calculated by the sound instance so the configured radius is exactly 20–500 blocks without registering 25/26 separate sound events.

## Explicitly removed uncertain/obsolete code

There is no reflection, no `Component.literal` GUI text, no static configuration map, no fake `net.minecraft` classes, no `createVariableRangeEvent(id, radius)` call, and no block `codec()` override.

The project intentionally does not rely on a block-removal callback just to delete configuration: the configuration is owned by the BlockEntity and is removed with the BlockEntity. The client sound also verifies every tick that the block is still present and immediately stops otherwise.
