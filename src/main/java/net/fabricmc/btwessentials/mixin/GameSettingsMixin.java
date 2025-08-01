package net.fabricmc.btwessentials.mixin;

import net.fabricmc.btwessentials.addon.BackMeUpKeybind;
import net.minecraft.src.GameSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameSettings.class)
public class GameSettingsMixin {
    @Inject(method = "loadOptions", at = @At("HEAD"))
    private void loadOptions(CallbackInfo cbi) {
        GameSettings gameSettings = (GameSettings)(Object)this;
        BackMeUpKeybind.initKeybind(gameSettings);
    }
}
