package cc.spea.gamingtimelimit;

import cc.spea.gamingtimelimit.client.GamingTimeLimitClient;
import cc.spea.gamingtimelimit.client.GamingTimeLimitCommands;
import cc.spea.gamingtimelimit.client.TimeLimitConfigScreen;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(GamingTimeLimit.MOD_ID)
public final class GamingTimeLimitForge {
    public GamingTimeLimitForge() {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientOnly::init);
    }

    private static final class ClientOnly {
        private static void init() {
            IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
            modBus.addListener(ClientOnly::onClientSetup);

            net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(ClientOnly::onRegisterClientCommands);
            GamingTimeLimitClient.getInstance().init();
            ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory(parent -> new TimeLimitConfigScreen(parent)));
        }

        private static void onClientSetup(FMLClientSetupEvent event) {
        }

        private static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
            GamingTimeLimitCommands.register(event.getDispatcher());
        }
    }
}
