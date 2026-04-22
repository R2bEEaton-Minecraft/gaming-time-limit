package cc.spea.gamingtimelimit;

#if MC_VER != MC_1_21_11 && MC_VER != MC_26_1 && MC_VER != MC_26_1_1
import cc.spea.gamingtimelimit.client.TimeLimitConfigScreen;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public final class GamingTimeLimitModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return TimeLimitConfigScreen::new;
    }
}
#else
public final class GamingTimeLimitModMenu {
}
#endif
