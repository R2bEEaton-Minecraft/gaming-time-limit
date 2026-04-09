package cc.spea.gamingtimelimit.client;

import cc.spea.gamingtimelimit.logic.DailyQuotaConfig;
import cc.spea.gamingtimelimit.logic.TimeFormatter;
import cc.spea.gamingtimelimit.logic.TimeLimitState;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

#if MC_VER == MC_26_1 || MC_VER == MC_26_1_1 || MC_VER == MC_26_1_2
import net.minecraft.client.gui.GuiGraphicsExtractor;
#else
import net.minecraft.client.gui.GuiGraphics;
#endif

public final class TimeLimitConfigScreen extends Screen {
    private final Screen parent;

    private EditBox minutesBox;
    private CycleButton<Boolean> autoKickButton;
    private CycleButton<Boolean> pausedSingleplayerButton;
    private Button saveButton;
    private String minutesValue = Integer.toString(DailyQuotaConfig.DEFAULT_DAILY_LIMIT_MINUTES);
    private boolean autoKick = true;
    private boolean countWhilePausedSingleplayer = true;
    private boolean minutesValid = true;

    public TimeLimitConfigScreen(Screen parent) {
        super(Component.translatable("gamingtimelimit.screen.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        DailyQuotaConfig config = GamingTimeLimitClient.getInstance().getConfig();
        this.minutesValue = Integer.toString(config.dailyLimitMinutes);
        this.autoKick = config.autoKick;
        this.countWhilePausedSingleplayer = config.countWhilePausedSingleplayer;

        int centerX = this.width / 2;
        int left = centerX - 100;
        int rowY = this.height / 4 + 20;

        this.minutesBox = this.addRenderableWidget(new EditBox(this.font, left, rowY, 200, 20, Component.translatable("gamingtimelimit.option.daily_minutes")));
        this.minutesBox.setValue(this.minutesValue);
        this.minutesBox.setResponder(this::onMinutesChanged);
        rowY += 28;

        this.autoKickButton = this.addRenderableWidget(
            CycleButton.onOffBuilder(this.autoKick)
                .create(left, rowY, 200, 20, Component.translatable("gamingtimelimit.option.auto_kick"), (button, value) -> this.autoKick = value)
        );
        rowY += 28;

        this.pausedSingleplayerButton = this.addRenderableWidget(
            CycleButton.onOffBuilder(this.countWhilePausedSingleplayer)
                .create(left, rowY, 200, 20, Component.translatable("gamingtimelimit.option.paused_singleplayer"), (button, value) -> this.countWhilePausedSingleplayer = value)
        );
        rowY += 36;

        this.saveButton = this.addRenderableWidget(Button.builder(Component.translatable("gamingtimelimit.screen.save"), button -> this.saveAndClose()).bounds(left, rowY, 64, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gamingtimelimit.screen.cancel"), button -> this.onClose()).bounds(left + 68, rowY, 64, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gamingtimelimit.screen.defaults"), button -> this.restoreDefaults()).bounds(left + 136, rowY, 64, 20).build());

        this.setInitialFocus(this.minutesBox);
        this.updateSaveState();
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

#if MC_VER == MC_26_1 || MC_VER == MC_26_1_1 || MC_VER == MC_26_1_2
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.drawStaticText(graphics);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    private void drawStaticText(GuiGraphicsExtractor graphics) {
        TimeLimitState state = GamingTimeLimitClient.getInstance().getState();
        int centerX = this.width / 2;
        int left = centerX - 100;
        int textColor = 0xFFFFFF;
        int subtextColor = 0xA0A0A0;
        int alertColor = 0xFF5555;

        graphics.centeredText(this.font, this.title, centerX, this.height / 4 - 28, textColor);
        graphics.text(this.font, Component.translatable("gamingtimelimit.screen.remaining", TimeFormatter.formatDuration(state.getRemainingMillis())), left, this.height / 4 - 6, state.isExhausted() ? alertColor : textColor);
        graphics.text(this.font, Component.translatable("gamingtimelimit.option.daily_minutes"), left, this.minutesBox.getY() - 10, subtextColor);

        if (!this.minutesValid) {
            graphics.text(this.font, Component.translatable("gamingtimelimit.error.minutes").withStyle(ChatFormatting.RED), left, this.minutesBox.getY() + 24, alertColor);
        }
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
        this.drawStaticText(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawStaticText(GuiGraphics graphics) {
        TimeLimitState state = GamingTimeLimitClient.getInstance().getState();
        int centerX = this.width / 2;
        int left = centerX - 100;
        int textColor = 0xFFFFFF;
        int subtextColor = 0xA0A0A0;
        int alertColor = 0xFF5555;

        graphics.drawCenteredString(this.font, this.title, centerX, this.height / 4 - 28, textColor);
        graphics.drawString(this.font, Component.translatable("gamingtimelimit.screen.remaining", TimeFormatter.formatDuration(state.getRemainingMillis())), left, this.height / 4 - 6, state.isExhausted() ? alertColor : textColor, false);
        graphics.drawString(this.font, Component.translatable("gamingtimelimit.option.daily_minutes"), left, this.minutesBox.getY() - 10, subtextColor, false);

        if (!this.minutesValid) {
            graphics.drawString(this.font, Component.translatable("gamingtimelimit.error.minutes").withStyle(ChatFormatting.RED), left, this.minutesBox.getY() + 24, alertColor, false);
        }
    }
#endif

    private void onMinutesChanged(String value) {
        this.minutesValue = value == null ? "" : value.trim();
        this.updateSaveState();
    }

    private void restoreDefaults() {
        DailyQuotaConfig defaults = DailyQuotaConfig.defaults();
        this.minutesBox.setValue(Integer.toString(defaults.dailyLimitMinutes));
        this.autoKickButton.setValue(defaults.autoKick);
        this.pausedSingleplayerButton.setValue(defaults.countWhilePausedSingleplayer);
        this.autoKick = defaults.autoKick;
        this.countWhilePausedSingleplayer = defaults.countWhilePausedSingleplayer;
        this.updateSaveState();
    }

    private void saveAndClose() {
        Integer parsedMinutes = this.parseMinutes();
        if (parsedMinutes == null) {
            return;
        }

        DailyQuotaConfig config = new DailyQuotaConfig();
        config.dailyLimitMinutes = parsedMinutes;
        config.autoKick = this.autoKick;
        config.countWhilePausedSingleplayer = this.countWhilePausedSingleplayer;
        GamingTimeLimitClient.getInstance().saveConfig(config);
        this.onClose();
    }

    private Integer parseMinutes() {
        if (this.minutesValue.isEmpty()) {
            return null;
        }

        try {
            int parsed = Integer.parseInt(this.minutesValue);
            return parsed >= 0 && parsed <= DailyQuotaConfig.MAX_DAILY_LIMIT_MINUTES ? parsed : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private void updateSaveState() {
        this.minutesValid = this.parseMinutes() != null;
        if (this.saveButton != null) {
            this.saveButton.active = this.minutesValid;
        }
    }
}
