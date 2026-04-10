package cc.spea.gamingtimelimit.client;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Locale;

final class ClientText {
    private ClientText() {
    }

    static MutableComponent tr(String key, String fallback, Object... args) {
        Language language = Language.getInstance();
        if (language != null && language.has(key)) {
            return Component.translatable(key, args);
        }

        return Component.literal(String.format(Locale.ROOT, fallback, args));
    }
}
