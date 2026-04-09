package cc.spea.gamingtimelimit.logic;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TimeLimitControllerTest {
    @Test
    void resetsUsageWhenDayChanges() {
        MutableClock clock = new MutableClock(Instant.parse("2026-04-09T10:00:00Z"), ZoneOffset.UTC);
        TimeLimitController controller = new TimeLimitController(clock);
        TimeLimitData data = new TimeLimitData();
        data.usage.lastResetDate = "2026-04-08";
        data.usage.usedMillisToday = 5_000L;
        controller.load(data);

        assertEquals(0L, controller.getState().getUsedMillisToday());
    }

    @Test
    void calculatesRemainingTimeFromUsedTime() {
        MutableClock clock = new MutableClock(Instant.parse("2026-04-09T10:00:00Z"), ZoneOffset.UTC);
        TimeLimitController controller = new TimeLimitController(clock);
        controller.applyElapsedMillis(30L * 60_000L, true);

        assertEquals(30L * 60_000L, controller.getState().getRemainingMillis());
    }

    @Test
    void changingLimitKeepsUsedTime() {
        MutableClock clock = new MutableClock(Instant.parse("2026-04-09T10:00:00Z"), ZoneOffset.UTC);
        TimeLimitController controller = new TimeLimitController(clock);
        controller.applyElapsedMillis(20L * 60_000L, true);

        DailyQuotaConfig config = controller.getConfig();
        config.dailyLimitMinutes = 45;
        controller.updateConfig(config);

        assertEquals(25L * 60_000L, controller.getState().getRemainingMillis());
    }

    @Test
    void pausedSingleplayerSettingCanSkipElapsedTime() {
        MutableClock clock = new MutableClock(Instant.parse("2026-04-09T10:00:00Z"), ZoneOffset.UTC);
        TimeLimitController controller = new TimeLimitController(clock);
        controller.applyElapsedMillis(5_000L, false);

        assertEquals(0L, controller.getState().getUsedMillisToday());
    }

    @Test
    void remainingTimeClampsAtZeroWhileUsedTimeKeepsGrowing() {
        MutableClock clock = new MutableClock(Instant.parse("2026-04-09T10:00:00Z"), ZoneOffset.UTC);
        TimeLimitController controller = new TimeLimitController(clock);
        controller.applyElapsedMillis(90L * 60_000L, true);

        assertEquals(90L * 60_000L, controller.getState().getUsedMillisToday());
        assertEquals(0L, controller.getState().getRemainingMillis());
        assertTrue(controller.getState().isExhausted());
    }

    private static final class MutableClock extends Clock {
        private Instant instant;
        private final ZoneId zoneId;

        private MutableClock(Instant instant, ZoneId zoneId) {
            this.instant = instant;
            this.zoneId = zoneId;
        }

        @Override
        public ZoneId getZone() {
            return this.zoneId;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(this.instant, zone);
        }

        @Override
        public Instant instant() {
            return this.instant;
        }
    }
}
