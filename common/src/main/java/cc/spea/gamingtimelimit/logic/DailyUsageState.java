package cc.spea.gamingtimelimit.logic;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public final class DailyUsageState {
    public String lastResetDate = "";
    public long usedMillisToday;

    public DailyUsageState copy() {
        DailyUsageState copy = new DailyUsageState();
        copy.lastResetDate = this.lastResetDate;
        copy.usedMillisToday = this.usedMillisToday;
        return copy;
    }

    public DailyUsageState sanitize(LocalDate fallbackDate) {
        if (this.lastResetDate == null || this.lastResetDate.isBlank()) {
            this.lastResetDate = fallbackDate.toString();
        } else {
            try {
                LocalDate.parse(this.lastResetDate);
            } catch (DateTimeParseException ignored) {
                this.lastResetDate = fallbackDate.toString();
            }
        }

        this.usedMillisToday = Math.max(0L, this.usedMillisToday);
        return this;
    }
}
