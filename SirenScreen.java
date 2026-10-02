package ru.sirenblock.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.core.BlockPos;
import ru.sirenblock.network.OpenSirenScreenPayload;
import ru.sirenblock.network.SirenUpdatePayload;
import net.fabricmc.fabric.api.networking.v1.ClientPlayNetworking;

public final class SirenScreen extends Screen {
    private final BlockPos pos;
    private boolean enabled, loop, redstone, remote;
    private int radius, inner, volume, curve;
    private String soundId, url;
    private EditBox radiusBox, innerBox, volumeBox, urlBox;
    private Button powerButton;

    public SirenScreen(OpenSirenScreenPayload data) {
        super(Component.translatable("screen.sirenblock.siren"));
        this.pos = data.pos();
        this.enabled = data.enabled();
        this.radius = data.radius();
        this.inner = data.innerRadius();
        this.volume = data.volume();
        this.loop = data.loop();
        this.redstone = data.redstoneControl();
        this.remote = data.remoteSync();
        this.curve = data.curve();
        this.soundId = data.soundId();
        this.url = data.url();
    }

    @Override
    protected void init() {
        super.init();
        int left = this.width / 2 - 160;
        int top = this.height / 2 - 110;

        radiusBox = addRenderableWidget(new EditBox(this.font, left + 82, top + 28, 80, 20, Component.translatable("sirenblock.radius")));
        radiusBox.setValue(Integer.toString(radius));
        innerBox = addRenderableWidget(new EditBox(this.font, left + 82, top + 55, 80, 20, Component.translatable("sirenblock.inner")));
        innerBox.setValue(Integer.toString(inner));
        volumeBox = addRenderableWidget(new EditBox(this.font, left + 82, top + 82, 80, 20, Component.translatable("sirenblock.volume")));
        volumeBox.setValue(Integer.toString(volume));
        urlBox = addRenderableWidget(new EditBox(this.font, left + 82, top + 109, 215, 20, Component.translatable("sirenblock.url")));
        urlBox.setValue(url);

        powerButton = addRenderableWidget(Button.builder(Component.empty(), b -> { enabled = !enabled; updatePowerText(); }).bounds(left, top, 150, 20).build());
        updatePowerText();
        addRenderableWidget(Button.builder(Component.translatable("sirenblock.curve", curveName()), b -> { curve = (curve + 1) % 3; b.setMessage(Component.translatable("sirenblock.curve", curveName())); }).bounds(left + 165, top + 28, 135, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("sirenblock.loop", loop ? "ON" : "OFF"), b -> { loop = !loop; b.setMessage(Component.translatable("sirenblock.loop", loop ? "ON" : "OFF")); }).bounds(left + 165, top + 55, 135, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("sirenblock.redstone", redstone ? "ON" : "OFF"), b -> { redstone = !redstone; b.setMessage(Component.translatable("sirenblock.redstone", redstone ? "ON" : "OFF")); }).bounds(left + 165, top + 82, 135, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("sirenblock.apply"), b -> saveAndClose()).bounds(left, top + 145, 145, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("sirenblock.cancel"), b -> onClose()).bounds(left + 155, top + 145, 145, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("sirenblock.import_local"), b -> importLocalOgg()).bounds(left, top + 173, 145, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("sirenblock.save_url"), b -> { saveUrlHint(); }).bounds(left + 155, top + 173, 145, 20).build());
    }

    private void updatePowerText() {
        powerButton.setMessage(Component.translatable(enabled ? "sirenblock.disable" : "sirenblock.enable"));
    }

    private String curveName() {
        return switch (curve) { case 0 -> "Линейное"; case 2 -> "Экспоненциальное"; default -> "Плавное"; };
    }

    private void parseFields() {
        radius = Mth.clamp(parseInt(radiusBox.getValue(), 200), 20, 500);
        inner = Mth.clamp(parseInt(innerBox.getValue(), 20), 0, radius);
        volume = Mth.clamp(parseInt(volumeBox.getValue(), 100), 0, 100);
        url = urlBox.getValue().trim();
    }

    private int parseInt(String v, int def) {
        try { return Integer.parseInt(v.trim()); } catch (NumberFormatException e) { return def; }
    }

    private void saveAndClose() {
        parseFields();
        ClientPlayNetworking.send(new SirenUpdatePayload(pos, enabled, radius, inner, volume, loop, redstone, remote, curve, soundId, url));
        onClose();
    }

    private void saveUrlHint() {
        parseFields();
        this.minecraft.gui.setScreen(new MessageScreen(this, "Ссылка сохранена. Для прямого аудиопотока используй URL на .ogg. Обычная страница SoundCloud не является прямым аудиофайлом."));
    }

    private void importLocalOgg() {
        try {
            Path dir = Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("sirenblock").resolve("sounds");
            Files.createDirectories(dir);
            this.minecraft.gui.setScreen(new MessageScreen(this, "Папка для пользовательских OGG создана:\n" + dir.toAbsolutePath() + "\n\nПоложи туда .ogg-файл. Он будет подключаться как пользовательский звук после перезагрузки ресурсов."));
        } catch (IOException ignored) {}
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        int left = this.width / 2 - 160;
        int top = this.height / 2 - 110;
        g.fill(left - 8, top - 18, left + 308, top + 205, 0xE0101010);
        g.centeredText(this.font, this.title, this.width / 2, top - 8, 0xFFFFFFFF);
        g.text(this.font, Component.translatable("sirenblock.radius_label"), left, top + 34, 0xFFB0B0B0);
        g.text(this.font, Component.translatable("sirenblock.inner_label"), left, top + 61, 0xFFB0B0B0);
        g.text(this.font, Component.translatable("sirenblock.volume_label"), left, top + 88, 0xFFB0B0B0);
        g.text(this.font, Component.translatable("sirenblock.url_label"), left, top + 115, 0xFFB0B0B0);
        g.text(this.font, Component.translatable("sirenblock.sound_label", "Воздушная тревога"), left, top + 132, 0xFFB0B0B0);
    }

    @Override public void onClose() { this.minecraft.gui.setScreen(null); }

    private static final class MessageScreen extends Screen {
        private final Screen parent;
        private final String message;
        MessageScreen(Screen parent, String message) { super(Component.translatable("sirenblock.info")); this.parent = parent; this.message = message; }
        @Override protected void init() { addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> minecraft.gui.setScreen(parent)).bounds(width / 2 - 60, height - 45, 120, 20).build()); }
        @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float a) { super.extractRenderState(g, mx, my, a); g.centeredText(font, title, width / 2, 40, -1); int y = 70; for (String line : message.split("\\n")) { g.centeredText(font, line, width / 2, y, 0xFFBBBBBB); y += 12; } }
    }
}
