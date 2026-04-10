package cc.spea.gamingtimelimit;

import cc.spea.gamingtimelimit.client.GamingTimeLimitClient;
import cc.spea.gamingtimelimit.client.GamingTimeLimitCommands;
import cc.spea.gamingtimelimit.client.TimeLimitConfigScreen;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
#if MC_VER >= MC_1_21_11
import net.minecraftforge.common.MinecraftForge;
#else
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.ModLoadingContext;
#endif
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(GamingTimeLimit.MOD_ID)
public final class GamingTimeLimitForge {
    public GamingTimeLimitForge() {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return;
        }

        GamingTimeLimitClient.getInstance().init();
#if MC_VER >= MC_1_21_11
        RegisterClientCommandsEvent.BUS.addListener(GamingTimeLimitForge::onRegisterClientCommands);
        MinecraftForge.registerConfigScreen(parent -> new TimeLimitConfigScreen(parent));
#else
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(GamingTimeLimitForge::onRegisterClientCommands);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(GamingTimeLimitForge::onClientTick);
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory(parent -> new TimeLimitConfigScreen(parent)));
#endif
    }

    private static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        GamingTimeLimitCommands.register(event.getDispatcher());
    }

#if MC_VER == MC_1_20_1
    private static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        GamingTimeLimitClient.getInstance().tick(Minecraft.getInstance());
    }
#endif
}
