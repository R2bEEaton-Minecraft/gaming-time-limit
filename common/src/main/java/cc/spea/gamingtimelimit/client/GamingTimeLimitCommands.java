package cc.spea.gamingtimelimit.client;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class GamingTimeLimitCommands {
    private GamingTimeLimitCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("gamingtimelimit").executes(context -> openScreen()));
        dispatcher.register(Commands.literal("gtl").executes(context -> openScreen()));
    }

    private static int openScreen() {
        Minecraft.getInstance().execute(() -> GamingTimeLimitClient.getInstance().openConfigScreen(null));
        return 1;
    }
}
