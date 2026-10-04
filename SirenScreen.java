package siren.controller.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import siren.controller.SirenBlockEntity;
import siren.controller.network.SirenActionPayload;

public final class SirenScreen extends Screen {
    private final BlockPos pos;
    private Button toggleButton;
    private Button typeButton;
    private Button radiusDownButton;
    private Button radiusUpButton;

    public SirenScreen(BlockPos pos) {
        super(Component.translatable("screen.siren_controller.title"));
        this.pos = pos.immutable();
    }

    private SirenBlockEntity entity() {
        if (minecraft.level == null) {
            return null;
        }
        BlockEntity entity = minecraft.level.getBlockEntity(pos);
        return entity instanceof SirenBlockEntity siren ? siren : null;
    }

    @Override
    protected void init() {
        int panelW = 320;
        int left = (width - panelW) / 2;
        int top = (height - 210) / 2;

        toggleButton = addRenderableWidget(Button.builder(toggleText(), button -> send(0))
                .bounds(left, top + 42, panelW, 20)
                .build());

        typeButton = addRenderableWidget(Button.builder(typeText(), button -> send(1))
                .bounds(left, top + 76, panelW, 20)
                .build());

        radiusDownButton = addRenderableWidget(Button.builder(
                        Component.translatable("screen.siren_controller.radius_down"),
                        button -> send(2))
                .bounds(left, top + 110, 42, 20)
                .build());

        radiusUpButton = addRenderableWidget(Button.builder(
                        Component.translatable("screen.siren_controller.radius_up"),
                        button -> send(3))
                .bounds(left + panelW - 42, top + 110, 42, 20)
                .build());

        addRenderableWidget(Button.builder(
                        Component.translatable("screen.siren_controller.close"),
                        button -> onClose())
                .bounds(left, top + 158, panelW, 20)
                .build());
    }

    private Component toggleText() {
        SirenBlockEntity e = entity();
        return Component.translatable(e != null && e.isActive()
                ? "screen.siren_controller.disable"
                : "screen.siren_controller.enable");
    }

    private Component typeText() {
        SirenBlockEntity e = entity();
        int type = e == null ? 0 : e.getType();
        return Component.translatable(
                "screen.siren_controller.type",
                Component.translatable("screen.siren_controller.type." + typeName(type))
        );
    }

    private static String typeName(int type) {
        return switch (Math.floorMod(type, 5)) {
            case 0 -> "air_raid";
            case 1 -> "police";
            case 2 -> "fire";
            case 3 -> "nuclear";
            default -> "industrial";
        };
    }

    private void send(int action) {
        if (minecraft.player != null) {
            ClientPlayNetworking.send(new SirenActionPayload(pos, action));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (entity() == null) {
            onClose();
            return;
        }

        toggleButton.setMessage(toggleText());
        typeButton.setMessage(typeText());
        SirenBlockEntity e = entity();
        int radius = e.getRadius();
        radiusDownButton.active = radius > SirenBlockEntity.MIN_RADIUS;
        radiusUpButton.active = radius < SirenBlockEntity.MAX_RADIUS;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        SirenBlockEntity e = entity();
        int panelW = 320;
        int left = (width - panelW) / 2;
        int top = (height - 210) / 2;
        Font font = this.font;

        graphics.text(font, Component.translatable("screen.siren_controller.title"), left, top, 0xFFFFFFFF);
        int radius = e == null ? SirenBlockEntity.MIN_RADIUS : e.getRadius();
        graphics.text(font,
                Component.translatable("screen.siren_controller.radius", radius),
                left + 42,
                top + 116,
                0xFFE0E0E0);
        graphics.text(font,
                Component.translatable("screen.siren_controller.position", pos.getX(), pos.getY(), pos.getZ()),
                left,
                top + 195,
                0xFF888888);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
