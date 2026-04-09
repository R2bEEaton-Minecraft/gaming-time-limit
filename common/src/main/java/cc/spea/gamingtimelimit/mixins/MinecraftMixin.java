package cc.spea.gamingtimelimit.mixins;

import cc.spea.gamingtimelimit.client.GamingTimeLimitClient;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void gamingtimelimit$tick(CallbackInfo ci) {
        GamingTimeLimitClient.getInstance().tick((Minecraft) (Object) this);
    }
}
