package cc.spea.gamingtimelimit.mixins;

import cc.spea.gamingtimelimit.client.GamingTimeLimitClient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ConnectScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConnectScreen.class)
public abstract class ConnectScreenMixin {
    @Inject(method = "startConnecting", at = @At("HEAD"), cancellable = true, require = 0)
    private static void gamingtimelimit$blockConnection(Screen parent, Minecraft minecraft, Object address, Object serverData, boolean transferred, Object transferState, CallbackInfo ci) {
        if (!GamingTimeLimitClient.getInstance().beforeConnecting(minecraft, parent)) {
            ci.cancel();
        }
    }
}
