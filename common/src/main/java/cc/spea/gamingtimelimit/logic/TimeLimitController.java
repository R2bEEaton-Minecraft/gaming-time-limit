package cc.spea.gamingtimelimit.logic;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

public final class TimeLimitController {
    private final Clock clock;
    private DailyQuotaConfig config = DailyQuotaConfig.defaults();
    private DailyUsageState usage = new DailyUsageState();

    public TimeLimitController(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.usage.lastResetDate = this.today().toString();
    }

    public synchronized void load(TimeLimitData data) {
        TimeLimitData sanitized = data == null ? new TimeLimitData() : data.copy(this.today());
        this.config = sanitized.config;
        this.usage = sanitized.usage;
        this.ensureCurrentDay();
    }

    public synchronized TimeLimitData snapshot() {
        this.ensureCurrentDay();
        TimeLimitData snapshot = new TimeLimitData();
        snapshot.config = this.config.copy();
        snapshot.usage = this.usage.copy();
        return snapshot.sanitize(this.today());
    }

    public synchronized DailyQuotaConfig getConfig() {
        return this.config.copy();
    }

    public synchronized void updateConfig(DailyQuotaConfig updated) {
        this.config = (updated == null ? DailyQuotaConfig.defaults() : updated.copy()).sanitize();
        this.ensureCurrentDay();
    }

    public synchronized TimeLimitState getState() {
        this.ensureCurrentDay();
        return new TimeLimitState(this.today(), this.config.dailyLimitMinutes, this.usage.usedMillisToday);
    }

    public synchronized boolean applyElapsedMillis(long elapsedMillis, boolean shouldCount) {
        boolean changed = this.ensureCurrentDay();
        if (!shouldCount || elapsedMillis <= 0L) {
            return changed;
        }

        this.usage.usedMillisToday += elapsedMillis;
        return true;
    }

    public synchronized boolean ensureCurrentDay() {
        LocalDate today = this.today();
        LocalDate storedDate = this.parseUsageDate(today);
        if (!storedDate.equals(today)) {
            this.usage.lastResetDate = today.toString();
            this.usage.usedMillisToday = 0L;
            return true;
        }

        return false;
    }

    private LocalDate today() {
        return LocalDate.now(this.clock);
    }

    private LocalDate parseUsageDate(LocalDate fallback) {
        this.usage.sanitize(fallback);
        return LocalDate.parse(this.usage.lastResetDate);
    }
}
