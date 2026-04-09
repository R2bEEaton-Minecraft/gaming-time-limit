package cc.spea.gamingtimelimit.logic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;

public final class TimeLimitStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path path;
    private final Clock clock;

    public TimeLimitStore(Path path, Clock clock) {
        this.path = path;
        this.clock = clock;
    }

    public TimeLimitData load() {
        LocalDate today = LocalDate.now(this.clock);
        if (!Files.exists(this.path)) {
            return new TimeLimitData().sanitize(today);
        }

        try (Reader reader = Files.newBufferedReader(this.path)) {
            TimeLimitData data = GSON.fromJson(reader, TimeLimitData.class);
            return (data == null ? new TimeLimitData() : data).sanitize(today);
        } catch (IOException | JsonParseException ignored) {
            return new TimeLimitData().sanitize(today);
        }
    }

    public void save(TimeLimitData data) {
        try {
            Files.createDirectories(this.path.getParent());
            try (Writer writer = Files.newBufferedWriter(this.path)) {
                GSON.toJson(data.copy(LocalDate.now(this.clock)), writer);
            }
        } catch (IOException ignored) {
        }
    }
}
