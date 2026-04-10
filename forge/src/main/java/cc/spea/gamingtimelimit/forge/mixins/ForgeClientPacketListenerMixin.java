package cc.spea.gamingtimelimit.forge.mixins;

import cc.spea.gamingtimelimit.client.GamingTimeLimitClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPacketListener.class)
public abstract class ForgeClientPacketListenerMixin {
    @Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true, require = 0)
    private void gamingtimelimit$handleClientCommand(String command, CallbackInfo ci) {
        if (!this.isTimeLimitCommand(command)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> GamingTimeLimitClient.getInstance().openConfigScreenNow(null));
        ci.cancel();
    }

    @Inject(method = "sendUnsignedCommand", at = @At("HEAD"), cancellable = true, require = 0)
    private void gamingtimelimit$handleUnsignedClientCommand(String command, CallbackInfoReturnable<Boolean> cir) {
        if (!this.isTimeLimitCommand(command)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> GamingTimeLimitClient.getInstance().openConfigScreenNow(null));
        cir.setReturnValue(true);
    }

    private boolean isTimeLimitCommand(String command) {
        if (command == null) {
            return false;
        }

        String trimmed = command.trim();
        return trimmed.equals("gamingtimelimit") || trimmed.equals("gtl");
    }
}
