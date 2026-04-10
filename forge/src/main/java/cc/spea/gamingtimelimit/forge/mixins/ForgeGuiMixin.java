package cc.spea.gamingtimelimit.forge.mixins;

import cc.spea.gamingtimelimit.client.GamingTimeLimitClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ForgeGui.class)
public abstract class ForgeGuiMixin {
    private static final int GAMINGTIMELIMIT$listDisplaySlot = 0;

    @Shadow public abstract Minecraft getMinecraft();

    @Inject(method = "renderPlayerList", at = @At("HEAD"), cancellable = true, require = 0)
    private void gamingtimelimit$renderSingleplayerFooter(int width, int height, GuiGraphics graphics, CallbackInfo ci) {
        if (!this.shouldForceSingleplayerTabOverlay()) {
            return;
        }

        Minecraft minecraft = this.getMinecraft();
        GamingTimeLimitClient.getInstance().refreshTabFooterNow(minecraft);
        PlayerTabOverlay tabList = minecraft.gui == null ? null : minecraft.gui.getTabList();
        if (tabList == null) {
            return;
        }

        Scoreboard scoreboard = minecraft.level.getScoreboard();
        Objective objective = scoreboard.getDisplayObjective(GAMINGTIMELIMIT$listDisplaySlot);
        tabList.setVisible(true);
        tabList.render(graphics, width, scoreboard, objective);
        ci.cancel();
    }

    private boolean shouldForceSingleplayerTabOverlay() {
        Minecraft minecraft = this.getMinecraft();
        if (!minecraft.options.keyPlayerList.isDown() || !minecraft.hasSingleplayerServer()) {
            return false;
        }

        if (minecraft.level == null || minecraft.player == null || minecraft.player.connection == null) {
            return false;
        }

        if (!GamingTimeLimitClient.getInstance().shouldShowTabFooter(minecraft)) {
            return false;
        }

        Scoreboard scoreboard = minecraft.level.getScoreboard();
        if (scoreboard.getDisplayObjective(GAMINGTIMELIMIT$listDisplaySlot) != null) {
            return false;
        }

        return minecraft.player.connection.getOnlinePlayers().size() <= 1;
    }
}
