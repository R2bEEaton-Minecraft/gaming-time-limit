package cc.spea.gamingtimelimit.mixins;

import cc.spea.gamingtimelimit.client.GamingTimeLimitClient;

import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerTabOverlayMixin {
    @Inject(method = "setFooter", at = @At("HEAD"))
    private void gamingtimelimit$captureFooter(Component footer, CallbackInfo ci) {
        GamingTimeLimitClient.getInstance().onExternalFooterSet(footer);
    }

    @Inject(method = "reset", at = @At("HEAD"))
    private void gamingtimelimit$resetFooter(CallbackInfo ci) {
        GamingTimeLimitClient.getInstance().onTabListReset();
    }
}
