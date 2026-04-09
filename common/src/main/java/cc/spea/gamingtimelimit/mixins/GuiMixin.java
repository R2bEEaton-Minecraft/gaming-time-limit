package cc.spea.gamingtimelimit.mixins;

import cc.spea.gamingtimelimit.client.GamingTimeLimitClient;

#if MC_VER == MC_26_1 || MC_VER == MC_26_1_1 || MC_VER == MC_26_1_2
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private PlayerTabOverlay tabList;

    @Inject(method = "extractTabList", at = @At("TAIL"))
    private void gamingtimelimit$renderSingleplayerFooter(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!this.shouldForceSingleplayerTabOverlay()) {
            return;
        }

        Scoreboard scoreboard = this.minecraft.level.getScoreboard();
        Objective objective = scoreboard.getDisplayObjective(DisplaySlot.LIST);
        graphics.nextStratum();
        this.tabList.setVisible(true);
        this.tabList.extractRenderState(graphics, graphics.guiWidth(), scoreboard, objective);
    }

    private boolean shouldForceSingleplayerTabOverlay() {
        if (!this.minecraft.options.keyPlayerList.isDown() || !this.minecraft.isLocalServer()) {
            return false;
        }

        if (this.minecraft.level == null || this.minecraft.player == null || this.minecraft.player.connection == null) {
            return false;
        }

        if (!GamingTimeLimitClient.getInstance().shouldShowTabFooter(this.minecraft)) {
            return false;
        }

        Scoreboard scoreboard = this.minecraft.level.getScoreboard();
        if (scoreboard.getDisplayObjective(DisplaySlot.LIST) != null) {
            return false;
        }

        return this.minecraft.player.connection.getListedOnlinePlayers().size() <= 1;
    }
}
#elif MC_VER == MC_1_20_1
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    private static final int GAMINGTIMELIMIT$listDisplaySlot = 0;

    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private PlayerTabOverlay tabList;

    @Inject(method = "render", at = @At("TAIL"), require = 0)
    private void gamingtimelimit$renderSingleplayerFooter(GuiGraphics graphics, float partialTick, CallbackInfo ci) {
        if (!this.shouldForceSingleplayerTabOverlay()) {
            return;
        }

        Scoreboard scoreboard = this.minecraft.level.getScoreboard();
        Objective objective = scoreboard.getDisplayObjective(GAMINGTIMELIMIT$listDisplaySlot);
        this.tabList.setVisible(true);
        this.tabList.render(graphics, graphics.guiWidth(), scoreboard, objective);
    }

    private boolean shouldForceSingleplayerTabOverlay() {
        if (!this.minecraft.options.keyPlayerList.isDown() || !this.minecraft.isLocalServer()) {
            return false;
        }

        if (this.minecraft.level == null || this.minecraft.player == null || this.minecraft.player.connection == null) {
            return false;
        }

        if (!GamingTimeLimitClient.getInstance().shouldShowTabFooter(this.minecraft)) {
            return false;
        }

        Scoreboard scoreboard = this.minecraft.level.getScoreboard();
        if (scoreboard.getDisplayObjective(GAMINGTIMELIMIT$listDisplaySlot) != null) {
            return false;
        }

        return this.minecraft.player.connection.getListedOnlinePlayers().size() <= 1;
    }
}
#else
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private PlayerTabOverlay tabList;

    @Inject(method = "renderTabList", at = @At("TAIL"), require = 0)
    private void gamingtimelimit$renderSingleplayerFooter(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!this.shouldForceSingleplayerTabOverlay()) {
            return;
        }

        Scoreboard scoreboard = this.minecraft.level.getScoreboard();
        Objective objective = scoreboard.getDisplayObjective(DisplaySlot.LIST);
        this.tabList.setVisible(true);
        this.tabList.render(graphics, graphics.guiWidth(), scoreboard, objective);
    }

    private boolean shouldForceSingleplayerTabOverlay() {
        if (!this.minecraft.options.keyPlayerList.isDown() || !this.minecraft.isLocalServer()) {
            return false;
        }

        if (this.minecraft.level == null || this.minecraft.player == null || this.minecraft.player.connection == null) {
            return false;
        }

        if (!GamingTimeLimitClient.getInstance().shouldShowTabFooter(this.minecraft)) {
            return false;
        }

        Scoreboard scoreboard = this.minecraft.level.getScoreboard();
        if (scoreboard.getDisplayObjective(DisplaySlot.LIST) != null) {
            return false;
        }

        return this.minecraft.player.connection.getListedOnlinePlayers().size() <= 1;
    }
}
#endif
