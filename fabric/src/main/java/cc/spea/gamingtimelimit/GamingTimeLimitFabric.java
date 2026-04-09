package cc.spea.gamingtimelimit;

import cc.spea.gamingtimelimit.client.GamingTimeLimitClient;

import net.fabricmc.api.ClientModInitializer;
#if MC_VER == MC_26_1 || MC_VER == MC_26_1_1 || MC_VER == MC_26_1_2
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
#else
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
#endif
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public final class GamingTimeLimitFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        GamingTimeLimitClient.getInstance().init();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
#if MC_VER == MC_26_1 || MC_VER == MC_26_1_1 || MC_VER == MC_26_1_2
            dispatcher.register(ClientCommands.literal("gamingtimelimit").executes(context -> openConfigScreen()));
            dispatcher.register(ClientCommands.literal("gtl").executes(context -> openConfigScreen()));
#else
            dispatcher.register(ClientCommandManager.literal("gamingtimelimit").executes(context -> openConfigScreen()));
            dispatcher.register(ClientCommandManager.literal("gtl").executes(context -> openConfigScreen()));
#endif
        });
    }

    private static int openConfigScreen() {
        net.minecraft.client.Minecraft.getInstance().execute(() -> GamingTimeLimitClient.getInstance().openConfigScreen(null));
        return 1;
    }
}
