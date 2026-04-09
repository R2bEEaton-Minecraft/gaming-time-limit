package cc.spea.gamingtimelimit.client;

import cc.spea.gamingtimelimit.GamingTimeLimit;
import cc.spea.gamingtimelimit.logic.DailyQuotaConfig;
import cc.spea.gamingtimelimit.logic.TimeFormatter;
import cc.spea.gamingtimelimit.logic.TimeLimitController;
import cc.spea.gamingtimelimit.logic.TimeLimitState;
import cc.spea.gamingtimelimit.logic.TimeLimitStore;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.nio.file.Path;
import java.time.Clock;
import java.util.Objects;

public final class GamingTimeLimitClient {
    private static final long SAVE_INTERVAL_MILLIS = 1_000L;

    private static final GamingTimeLimitClient INSTANCE = new GamingTimeLimitClient();

    private final Clock clock = Clock.systemDefaultZone();
    private final TimeLimitController controller = new TimeLimitController(this.clock);
    private final TimeLimitStore store = new TimeLimitStore(Path.of("config", GamingTimeLimit.MOD_ID + "-client.json"), this.clock);

    private long lastTickMillis = System.currentTimeMillis();
    private Component rawFooter;
    private Component lastAppliedFooter;
    private boolean applyingFooter;
    private boolean exhaustedHandledThisSession;
    private boolean wasInSession;
    private boolean dirty;
    private long lastSaveMillis = System.currentTimeMillis();
    private Screen lastReturnScreen;

    private GamingTimeLimitClient() {
    }

    public static GamingTimeLimitClient getInstance() {
        return INSTANCE;
    }

    public void init() {
        this.controller.load(this.store.load());
    }

    public void tick(Minecraft minecraft) {
        long now = System.currentTimeMillis();
        long elapsedMillis = Math.max(0L, now - this.lastTickMillis);
        this.lastTickMillis = now;
        this.captureReturnScreen(minecraft.screen);

        boolean inSession = this.isInTrackedSession(minecraft);
        boolean shouldCount = inSession && this.shouldCountCurrentTick(minecraft);
        if (this.controller.applyElapsedMillis(elapsedMillis, shouldCount)) {
            this.dirty = true;
        }

        if (inSession && this.shouldAutoKick() && this.controller.getState().isExhausted()) {
            this.enforceExhaustion(minecraft);
        } else if (!inSession) {
            this.exhaustedHandledThisSession = false;
        }

        if (this.wasInSession && !inSession) {
            this.resetTabFooter(minecraft);
            this.rawFooter = null;
            this.lastAppliedFooter = null;
        }

        this.wasInSession = inSession;
        this.refreshTabFooter(minecraft);
        this.flushIfNeeded(now);
    }

    public boolean beforeConnecting(Minecraft minecraft, Screen parent) {
        if (!this.shouldBlockEntry()) {
            return true;
        }

        minecraft.execute(() -> minecraft.setScreen(this.createExhaustedScreen(parent)));
        return false;
    }

    public boolean beforeOpeningSingleplayer(Minecraft minecraft, Screen parent) {
        if (!this.shouldBlockEntry()) {
            return true;
        }

        Screen returnScreen = parent != null ? parent : this.getReturnScreen(minecraft);
        minecraft.execute(() -> minecraft.setScreen(this.createExhaustedScreen(returnScreen)));
        return false;
    }

    public void onExternalFooterSet(Component footer) {
        if (this.applyingFooter) {
            return;
        }

        this.rawFooter = footer;
        this.lastAppliedFooter = null;
    }

    public void onTabListReset() {
        this.rawFooter = null;
        this.lastAppliedFooter = null;
    }

    public boolean isApplyingFooter() {
        return this.applyingFooter;
    }

    public boolean shouldShowTabFooter(Minecraft minecraft) {
        return this.isInTrackedSession(minecraft);
    }

    public void openConfigScreen(Screen parent) {
        Minecraft minecraft = Minecraft.getInstance();
        Screen previous = parent != null ? parent : minecraft.screen;
        minecraft.setScreen(new TimeLimitConfigScreen(previous));
    }

    public DailyQuotaConfig getConfig() {
        return this.controller.getConfig();
    }

    public TimeLimitState getState() {
        return this.controller.getState();
    }

    public void saveConfig(DailyQuotaConfig config) {
        this.controller.updateConfig(config);
        this.dirty = true;
        this.save();
        this.refreshTabFooter(Minecraft.getInstance());
    }

    private void save() {
        this.store.save(this.controller.snapshot());
        this.dirty = false;
        this.lastSaveMillis = System.currentTimeMillis();
    }

    private void flushIfNeeded(long now) {
        if (this.dirty && now - this.lastSaveMillis >= SAVE_INTERVAL_MILLIS) {
            this.save();
        }
    }

    private boolean shouldAutoKick() {
        return this.controller.getConfig().autoKick;
    }

    private boolean shouldBlockEntry() {
        return this.shouldAutoKick() && this.controller.getState().isExhausted();
    }

    private boolean isInTrackedSession(Minecraft minecraft) {
        return minecraft.level != null && minecraft.player != null;
    }

    private boolean shouldCountCurrentTick(Minecraft minecraft) {
        DailyQuotaConfig config = this.controller.getConfig();
        if (minecraft.hasSingleplayerServer() && minecraft.isPaused()) {
            return config.countWhilePausedSingleplayer;
        }

        return true;
    }

    private void enforceExhaustion(Minecraft minecraft) {
        if (this.exhaustedHandledThisSession) {
            return;
        }

        this.exhaustedHandledThisSession = true;
        if (minecraft.getConnection() != null) {
            minecraft.getConnection().getConnection().disconnect(Component.translatable("gamingtimelimit.kick.message"));
        }
    }

    private void refreshTabFooter(Minecraft minecraft) {
        PlayerTabOverlay tabOverlay = minecraft.gui == null ? null : minecraft.gui.getTabList();
        if (tabOverlay == null) {
            return;
        }

        Component merged = null;
        if (this.isInTrackedSession(minecraft)) {
            merged = this.mergeFooter(this.rawFooter, this.createTabFooter());
        }

        if (Objects.equals(merged, this.lastAppliedFooter)) {
            return;
        }

        this.applyingFooter = true;
        try {
            tabOverlay.setFooter(merged);
            this.lastAppliedFooter = merged;
        } finally {
            this.applyingFooter = false;
        }
    }

    private void resetTabFooter(Minecraft minecraft) {
        PlayerTabOverlay tabOverlay = minecraft.gui == null ? null : minecraft.gui.getTabList();
        if (tabOverlay == null) {
            return;
        }

        this.applyingFooter = true;
        try {
            tabOverlay.setFooter(this.rawFooter);
        } finally {
            this.applyingFooter = false;
        }
    }

    private Component mergeFooter(Component footer, Component timerFooter) {
        if (footer == null) {
            return timerFooter;
        }

        MutableComponent merged = footer.copy();
        merged.append(Component.literal("\n"));
        merged.append(timerFooter);
        return merged;
    }

    private Component createTabFooter() {
        TimeLimitState state = this.controller.getState();
        MutableComponent footer = Component.translatable(
            "gamingtimelimit.tab.remaining",
            TimeFormatter.formatDuration(state.getRemainingMillis())
        );

        return state.isExhausted() ? footer.withStyle(ChatFormatting.RED) : footer.withStyle(ChatFormatting.YELLOW);
    }

    private Screen createExhaustedScreen(Screen parent) {
        return new TimeLimitExhaustedScreen(parent);
    }

    private Screen getReturnScreen(Minecraft minecraft) {
        if (this.lastReturnScreen != null) {
            return this.lastReturnScreen;
        }

        if (this.isReturnScreen(minecraft.screen)) {
            return minecraft.screen;
        }

        return new TitleScreen();
    }

    private void captureReturnScreen(Screen screen) {
        if (this.isReturnScreen(screen)) {
            this.lastReturnScreen = screen;
        }
    }

    private boolean isReturnScreen(Screen screen) {
        if (screen == null || screen instanceof TimeLimitConfigScreen || screen instanceof TimeLimitExhaustedScreen) {
            return false;
        }

        String simpleName = screen.getClass().getSimpleName();
        return !simpleName.equals("ConnectScreen")
            && !simpleName.equals("LevelLoadingScreen")
            && !simpleName.equals("ProgressScreen")
            && !simpleName.equals("ReceivingLevelScreen")
            && !simpleName.equals("GenericMessageScreen")
            && !simpleName.equals("GenericDirtMessageScreen");
    }
}
