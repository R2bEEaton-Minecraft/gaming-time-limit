package cc.spea.gamingtimelimit.client;

import net.minecraft.client.gui.components.Button;
#if MC_VER == MC_26_1 || MC_VER == MC_26_1_1 || MC_VER == MC_26_1_2
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.TextAlignment;
#endif
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

#if MC_VER == MC_26_1 || MC_VER == MC_26_1_1 || MC_VER == MC_26_1_2
import net.minecraft.client.gui.GuiGraphicsExtractor;
#else
import net.minecraft.client.gui.GuiGraphics;
#endif

import java.util.List;

public final class TimeLimitExhaustedScreen extends Screen {
    private static final int TEXT_WIDTH = 240;

    private final Screen parent;
#if MC_VER == MC_26_1 || MC_VER == MC_26_1_1 || MC_VER == MC_26_1_2
    private MultiLineLabel message = MultiLineLabel.EMPTY;
#endif

    public TimeLimitExhaustedScreen(Screen parent) {
        super(Component.translatable("gamingtimelimit.exhausted.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int buttonWidth = 150;
        int left = centerX - buttonWidth / 2;
        int rowY = this.height / 2 + 24;

        this.addRenderableWidget(
            Button.builder(Component.translatable("gamingtimelimit.exhausted.button"), button -> GamingTimeLimitClient.getInstance().openConfigScreen(this))
                .bounds(left, rowY, buttonWidth, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(Component.translatable("gamingtimelimit.exhausted.back"), button -> this.goBack())
                .bounds(left, rowY + 24, buttonWidth, 20)
                .build()
        );

#if MC_VER == MC_26_1 || MC_VER == MC_26_1_1 || MC_VER == MC_26_1_2
        this.message = MultiLineLabel.create(this.font, Component.translatable("gamingtimelimit.exhausted.message"), this.width - 50);
#endif
    }

    @Override
    public void onClose() {
        this.goBack();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

#if MC_VER == MC_26_1 || MC_VER == MC_26_1_1 || MC_VER == MC_26_1_2
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(this.font, this.title, this.width / 2, this.height / 2 - 54, 0xFFFFFF);
        this.message.visitLines(TextAlignment.CENTER, this.width / 2, this.height / 2 - 32, 9, graphics.textRenderer());
    }
#else
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
#if MC_VER >= MC_1_21
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
#else
#if MC_VER == MC_1_20_1
        this.renderBackground(graphics);
#else
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
#endif
#endif
        this.drawStaticText(
            (text, x, y, color) -> graphics.drawString(this.font, text, x, y, color, false),
            (text, x, y, color) -> graphics.drawCenteredString(this.font, text, x, y, color)
        );
        super.render(graphics, mouseX, mouseY, partialTick);
    }
#endif

    private void drawStaticText(LineDrawer lineDrawer, TitleDrawer titleDrawer) {
        int centerX = this.width / 2;
        int textLeft = centerX - TEXT_WIDTH / 2;
        int titleY = this.height / 2 - 54;
        int messageY = titleY + 22;

        titleDrawer.draw(this.title, centerX, titleY, 0xFFFFFF);
        List<FormattedCharSequence> lines = this.font.split(Component.translatable("gamingtimelimit.exhausted.message"), TEXT_WIDTH);
        for (int i = 0; i < lines.size(); i++) {
            lineDrawer.draw(lines.get(i), textLeft, messageY + i * 10, 0xC0C0C0);
        }
    }

    private void goBack() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @FunctionalInterface
    private interface LineDrawer {
        void draw(FormattedCharSequence text, int x, int y, int color);
    }

    @FunctionalInterface
    private interface TitleDrawer {
        void draw(Component text, int x, int y, int color);
    }
}
