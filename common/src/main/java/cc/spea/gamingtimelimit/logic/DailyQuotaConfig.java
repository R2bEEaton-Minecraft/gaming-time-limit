package cc.spea.gamingtimelimit.logic;

public final class DailyQuotaConfig {
    public static final int DEFAULT_DAILY_LIMIT_MINUTES = 60;
    public static final int MAX_DAILY_LIMIT_MINUTES = 24 * 60;

    public int dailyLimitMinutes = DEFAULT_DAILY_LIMIT_MINUTES;
    public boolean autoKick = true;
    public boolean countWhilePausedSingleplayer = true;

    public DailyQuotaConfig copy() {
        DailyQuotaConfig copy = new DailyQuotaConfig();
        copy.dailyLimitMinutes = this.dailyLimitMinutes;
        copy.autoKick = this.autoKick;
        copy.countWhilePausedSingleplayer = this.countWhilePausedSingleplayer;
        return copy;
    }

    public DailyQuotaConfig sanitize() {
        this.dailyLimitMinutes = Math.max(0, Math.min(MAX_DAILY_LIMIT_MINUTES, this.dailyLimitMinutes));
        return this;
    }

    public static DailyQuotaConfig defaults() {
        return new DailyQuotaConfig().sanitize();
    }
}
