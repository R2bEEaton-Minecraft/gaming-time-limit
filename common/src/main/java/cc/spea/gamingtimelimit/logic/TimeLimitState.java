package cc.spea.gamingtimelimit.logic;

import java.time.LocalDate;

public final class TimeLimitState {
    private final LocalDate currentDate;
    private final int dailyLimitMinutes;
    private final long usedMillisToday;
    private final long remainingMillis;

    public TimeLimitState(LocalDate currentDate, int dailyLimitMinutes, long usedMillisToday) {
        this.currentDate = currentDate;
        this.dailyLimitMinutes = Math.max(0, dailyLimitMinutes);
        this.usedMillisToday = Math.max(0L, usedMillisToday);
        this.remainingMillis = Math.max(0L, getAllowedMillis() - this.usedMillisToday);
    }

    public LocalDate getCurrentDate() {
        return this.currentDate;
    }

    public int getDailyLimitMinutes() {
        return this.dailyLimitMinutes;
    }

    public long getUsedMillisToday() {
        return this.usedMillisToday;
    }

    public long getAllowedMillis() {
        return this.dailyLimitMinutes * 60_000L;
    }

    public long getRemainingMillis() {
        return this.remainingMillis;
    }

    public boolean isExhausted() {
        return this.getRemainingMillis() <= 0L;
    }
}
