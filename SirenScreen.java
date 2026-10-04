package siren.controller.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import siren.controller.SirenBlockEntity;
import siren.controller.SirenBlocks;
import siren.controller.network.SirenActionPayload;

/**
 * Siren control screen.
 *
 * Layout (all rows are 20 px high, 4 px gap, one 260 px wide column, everything centred):
 *   title
 *   [            Enable / Disable            ]
 *   [            Type: ...                   ]
 *   [-100] [-20] [   Range: N m   ] [+20] [+100]
 *   [               Close                    ]
 *   position
 *
 * The state is sent by the server in the payload; the GUI does not read the client block entity.
 */
public final class SirenScreen extends Screen {
    private static final int PANEL_WIDTH = 260;
    private static final int BUTTON_HEIGHT = 20;
    private static final int GAP = 4;
    private static final int ROW = BUTTON_HEIGHT + GAP;
    private static final int SMALL_WIDTH = 40;
    private static final int TITLE_SPACE = 20;
    private static final int PANEL_HEIGHT = TITLE_SPACE + ROW * 4 + 14;

    private final BlockPos pos;
    private int type;
    private int radius;
    private boolean active;

    private Button toggleButton;
    private Button typeButton;
    private Button radiusLabel;
    private Button radiusDownBig;
    private Button radiusDown;
    private Button radiusUp;
    private Button radiusUpBig;

    public SirenScreen(BlockPos pos, int type, int radius, boolean active) {
        super(Component.translatable("screen.siren_controller.title"));
        this.pos = pos.immutable();
        this.type = type;
        this.radius = radius;
        this.active = active;
    }

    public boolean isFor(BlockPos other) {
        return pos.equals(other);
    }

    public void applyState(int type, int radius, boolean active) {
        this.type = type;
        this.radius = radius;
        this.active = active;
        refreshWidgets();
    }

    @Override
    protected void init() {
        int left = (width - PANEL_WIDTH) / 2;
        int top = Math.max(6, (height - PANEL_HEIGHT) / 2);

        int y1 = top + TITLE_SPACE;
        int y2 = y1 + ROW;
        int y3 = y2 + ROW;
        int y4 = y3 + ROW;

        toggleButton = addRenderableWidget(Button.builder(toggleText(), button -> send(SirenBlockEntity.ACTION_TOGGLE))
                .bounds(left, y1, PANEL_WIDTH, BUTTON_HEIGHT)
                .build());

        typeButton = addRenderableWidget(Button.builder(typeText(), button -> send(SirenBlockEntity.ACTION_TYPE))
                .bounds(left, y2, PANEL_WIDTH, BUTTON_HEIGHT)
                .build());

        int labelWidth = PANEL_WIDTH - 4 * SMALL_WIDTH - 4 * GAP;
        int x = left;

        radiusDownBig = addRenderableWidget(Button.builder(
                        Component.translatable("screen.siren_controller.radius_down_big"),
                        button -> send(SirenBlockEntity.ACTION_RADIUS_DOWN_BIG))
                .bounds(x, y3, SMALL_WIDTH, BUTTON_HEIGHT)
                .build());
        x += SMALL_WIDTH + GAP;

        radiusDown = addRenderableWidget(Button.builder(
                        Component.translatable("screen.siren_controller.radius_down"),
                        button -> send(SirenBlockEntity.ACTION_RADIUS_DOWN))
                .bounds(x, y3, SMALL_WIDTH, BUTTON_HEIGHT)
                .build());
        x += SMALL_WIDTH + GAP;

        radiusLabel = addRenderableWidget(Button.builder(radiusText(), button -> { })
                .bounds(x, y3, labelWidth, BUTTON_HEIGHT)
                .build());
        radiusLabel.active = false;
        x += labelWidth + GAP;

        radiusUp = addRenderableWidget(Button.builder(
                        Component.translatable("screen.siren_controller.radius_up"),
                        button -> send(SirenBlockEntity.ACTION_RADIUS_UP))
                .bounds(x, y3, SMALL_WIDTH, BUTTON_HEIGHT)
                .build());
        x += SMALL_WIDTH + GAP;

        radiusUpBig = addRenderableWidget(Button.builder(
                        Component.translatable("screen.siren_controller.radius_up_big"),
                        button -> send(SirenBlockEntity.ACTION_RADIUS_UP_BIG))
                .bounds(x, y3, SMALL_WIDTH, BUTTON_HEIGHT)
                .build());

        addRenderableWidget(Button.builder(
                        Component.translatable("screen.siren_controller.close"),
                        button -> onClose())
                .bounds(left, y4, PANEL_WIDTH, BUTTON_HEIGHT)
                .build());

        refreshWidgets();
    }

    private void refreshWidgets() {
        if (toggleButton == null) {
            return;
        }
        toggleButton.setMessage(toggleText());
        typeButton.setMessage(typeText());
        radiusLabel.setMessage(radiusText());

        boolean canDown = radius > SirenBlockEntity.MIN_RADIUS;
        boolean canUp = radius < SirenBlockEntity.MAX_RADIUS;
        radiusDownBig.active = canDown;
        radiusDown.active = canDown;
        radiusUp.active = canUp;
        radiusUpBig.active = canUp;
    }

    private Component toggleText() {
        return Component.translatable(active
                ? "screen.siren_controller.disable"
                : "screen.siren_controller.enable");
    }

    private Component typeText() {
        return Component.translatable(
                "screen.siren_controller.type",
                Component.translatable("screen.siren_controller.type." + typeName(type))
        );
    }

    private Component radiusText() {
        return Component.translatable("screen.siren_controller.radius", radius);
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
        if (minecraft != null && minecraft.player != null) {
            ClientPlayNetworking.send(new SirenActionPayload(pos, action));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (minecraft == null
                || minecraft.level == null
                || !minecraft.level.getBlockState(pos).is(SirenBlocks.SIREN)) {
            onClose();
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        int top = Math.max(6, (height - PANEL_HEIGHT) / 2);

        Component titleText = getTitle();
        graphics.text(font, titleText, (width - font.width(titleText)) / 2, top + 4, 0xFFFFFFFF);

        Component posText = Component.translatable(
                "screen.siren_controller.position", pos.getX(), pos.getY(), pos.getZ());
        graphics.text(font, posText, (width - font.width(posText)) / 2,
                top + TITLE_SPACE + ROW * 4 + 2, 0xFF999999);
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(null);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
