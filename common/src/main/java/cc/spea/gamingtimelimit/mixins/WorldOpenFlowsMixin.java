package cc.spea.gamingtimelimit.mixins;

import cc.spea.gamingtimelimit.client.GamingTimeLimitClient;

import net.minecraft.client.Minecraft;
#if MC_VER != MC_1_20_1
import net.minecraft.client.gui.screens.Screen;
#endif
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(WorldOpenFlows.class)
public abstract class WorldOpenFlowsMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "openWorld", at = @At("HEAD"), cancellable = true)
    private void gamingtimelimit$blockOpenWorld(String levelId, Runnable onCancel, CallbackInfo ci) {
        if (!GamingTimeLimitClient.getInstance().beforeOpeningSingleplayer(this.minecraft, null)) {
            ci.cancel();
        }
    }

#if MC_VER == MC_1_20_1
    @Inject(method = "createFreshLevel", at = @At("HEAD"), cancellable = true)
    private void gamingtimelimit$blockCreateFreshLevel(
        String levelId,
        LevelSettings levelSettings,
        WorldOptions worldOptions,
        Function<HolderLookup.Provider, WorldDimensions> dimensionsFactory,
        CallbackInfo ci
    ) {
        if (!GamingTimeLimitClient.getInstance().beforeOpeningSingleplayer(this.minecraft, null)) {
            ci.cancel();
        }
    }
#else
    @Inject(method = "createFreshLevel", at = @At("HEAD"), cancellable = true)
    private void gamingtimelimit$blockCreateFreshLevel(
        String levelId,
        LevelSettings levelSettings,
        WorldOptions worldOptions,
        Function<HolderLookup.Provider, WorldDimensions> dimensionsFactory,
        Screen parent,
        CallbackInfo ci
    ) {
        if (!GamingTimeLimitClient.getInstance().beforeOpeningSingleplayer(this.minecraft, parent)) {
            ci.cancel();
        }
    }
#endif
}
