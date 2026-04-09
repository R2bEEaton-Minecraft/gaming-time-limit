package cc.spea.gamingtimelimit.logic;

import java.time.LocalDate;

public final class TimeLimitData {
    public DailyQuotaConfig config = DailyQuotaConfig.defaults();
    public DailyUsageState usage = new DailyUsageState();

    public TimeLimitData sanitize(LocalDate today) {
        if (this.config == null) {
            this.config = DailyQuotaConfig.defaults();
        } else {
            this.config = this.config.copy().sanitize();
        }

        if (this.usage == null) {
            this.usage = new DailyUsageState();
        } else {
            this.usage = this.usage.copy();
        }

        this.usage.sanitize(today);
        return this;
    }

    public TimeLimitData copy(LocalDate today) {
        TimeLimitData copy = new TimeLimitData();
        copy.config = this.config == null ? DailyQuotaConfig.defaults() : this.config.copy();
        copy.usage = this.usage == null ? new DailyUsageState() : this.usage.copy();
        return copy.sanitize(today);
    }
}
