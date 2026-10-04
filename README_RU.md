# Siren Controller для Minecraft 26.3 + Fabric

Полный исходный проект мода без самодельных Minecraft/Fabric stub-классов.
Сборка выполняется через Fabric Loom против настоящего `com.mojang:minecraft:26.3`.

## Что делает мод

- Добавляет отдельный блок `Сирена`.
- ПКМ по блоку открывает GUI.
- Команды не нужны.
- Сервер хранит настройки в `SirenBlockEntity`.
- Настройки: включение, тип и дальность.
- Дальность: 20–500 блоков, шаг 20.
- Типы: воздушная тревога, полиция, пожарная, ядерная тревога, промышленная.
- Клиент получает состояние через custom payload и проигрывает настоящий looping `SoundInstance`.
- Звук автоматически останавливается при выключении, удалении блока, уходе за радиус или смене измерения.
- Блок есть в творческой вкладке Building Blocks.
- Для блока есть loot table, рецепт и теги `minecraft:mineable/pickaxe` + `minecraft:needs_stone_tool`.

## Важная поправка по `codec()`

В Minecraft 26.3 `Block#codec()` и реестр block codecs были удалены в рамках технических изменений 26.x. Поэтому `SirenBlock` **намеренно не содержит** `codec()` и `MapCodec`. Добавлять их в 26.3 было бы ошибкой.

## Почему нет статической Map с конфигом

Состояние сирены принадлежит конкретному `SirenBlockEntity`, а значит автоматически принадлежит конкретному `ServerLevel`. Никакого глобального ключа `x,y,z` для настроек нет.

На клиенте карта запущенных звуков использует пару `(dimension, BlockPos.asLong())`, поэтому одинаковые координаты в разных измерениях не конфликтуют.

## Звук

Файлы генерируются скриптом `tools/generate_audio.py` методом детерминированного additive synthesis:

- 48 kHz;
- stereo;
- 8 секунд на цикл;
- плавные sinusoidal frequency sweeps;
- гармоники;
- мягкая нелинейная сатурация без hard clipping;
- 18 ms seam crossfade для близкого совпадения начала/конца файла;
- кодирование в OGG/Vorbis quality 6 через FFmpeg.

Это синтетические сирены, а не сэмплы чужих записей. Каждый тип имеет собственную частотную структуру и характер модуляции.

Сами OGG уже лежат в репозитории, поэтому GitHub Actions не должен запускать генератор звука для обычной сборки.

## Структура проекта

```text
build.gradle
gradle.properties
settings.gradle
fabric.mod.json
.github/workflows/build.yml
src/main/java/...
src/client/java/...
src/main/resources/...
tools/generate_audio.py
```

Используется split environment source sets из Fabric Loom, поэтому client-only классы GUI и sound instance не загружаются как common-код сервера.

## Сборка в GitHub Actions

1. Создай пустой GitHub repository.
2. Загрузи **все файлы проекта** в repository.
3. Сделай push в `main`.
4. Открой GitHub → **Actions** → **Build Siren Controller**.
5. После завершения открой run → **Artifacts**.
6. Скачай `siren-controller-mc26.3`.
7. Внутри будет обычный mod JAR из `build/libs`.

Workflow автоматически ставит JDK 25 и Gradle 9.6.0.

## Локальная сборка

Локально нужен JDK 25. Пользовательский ПК не обязан иметь его, если сборка идёт через GitHub Actions.

```bash
gradle build --no-daemon --stacktrace
```

## Проверяемые 26.3 API

Критические места проекта намеренно сделаны по API ветки Fabric 26.3:

- `ResourceKey.create(Registries.BLOCK/ITEM, Identifier)` + `Registry.register(BuiltInRegistries..., key, value)`;
- `BlockBehaviour.Properties.of().setId(...).strength(...).requiresCorrectToolForDrops().sound(...).noOcclusion()`;
- `BlockEntityType.Builder.of(...).build(null)`;
- `ValueInput` / `ValueOutput` для BlockEntity storage;
- `PayloadTypeRegistry.serverboundPlay()` / `clientboundPlay()`;
- `BlockPos.STREAM_CODEC` + `ByteBufCodecs` + `StreamCodec.composite(...)`;
- `Minecraft.level` как `ClientLevel`;
- `Minecraft.setScreenAndShow(Screen)` для открытия GUI;
- `AbstractTickableSoundInstance` + `looping = true` для бесшовного циклического SoundInstance.

## Ограничение проверки

В этой среде нет установленного JDK 25, Gradle и локальной копии Minecraft 26.3, а исходящие сетевые запросы контейнера недоступны. Поэтому здесь нельзя честно заявить, что `gradle build` был реально выполнен на полном Minecraft 26.3 runtime.

Файл проекта, однако, специально настроен так, чтобы GitHub Actions скачал реальные зависимости через Fabric Loom и **не использовал самодельные stub-классы**.
