package cc.spea.gamingtimelimit;

import cc.spea.gamingtimelimit.client.GamingTimeLimitClient;
import cc.spea.gamingtimelimit.client.GamingTimeLimitCommands;
import cc.spea.gamingtimelimit.client.TimeLimitConfigScreen;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;

#if MC_VER == MC_1_21_11
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
#else
import net.neoforged.neoforge.client.ConfigScreenHandler;
#endif

@Mod(GamingTimeLimit.MOD_ID)
public final class GamingTimeLimitNeoForge {
    public GamingTimeLimitNeoForge(IEventBus modBus, ModContainer modContainer) {
        if (
#if MC_VER >= MC_1_21_11
            net.neoforged.fml.loading.FMLEnvironment.getDist() == Dist.CLIENT
#else
            net.neoforged.fml.loading.FMLEnvironment.dist == Dist.CLIENT
#endif
        ) {
            GamingTimeLimitClient.getInstance().init();
            NeoForge.EVENT_BUS.addListener(this::onRegisterClientCommands);
#if MC_VER == MC_1_21_11
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, parent) -> new TimeLimitConfigScreen(parent));
#else
            modContainer.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> new TimeLimitConfigScreen(parent)));
#endif
        }
    }

    private void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        GamingTimeLimitCommands.register(event.getDispatcher());
    }
}
