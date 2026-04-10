package cc.spea.gamingtimelimit.mixins;

import cc.spea.gamingtimelimit.client.GamingTimeLimitClient;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "runTick", at = @At("TAIL"), require = 0)
    private void gamingtimelimit$runTick(boolean shouldTick, CallbackInfo ci) {
        GamingTimeLimitClient.getInstance().tick((Minecraft) (Object) this);
    }

    @Inject(method = "tick", at = @At("TAIL"), require = 0)
    private void gamingtimelimit$tick(CallbackInfo ci) {
        GamingTimeLimitClient.getInstance().tick((Minecraft) (Object) this);
    }
}
