package net.fabricmc.btwessentials.mixin;

import java.util.Map;
import net.fabricmc.btwessentials.api.NBTTagCompoundAccessor;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({NBTTagCompound.class})
public class NBTTagCompoundMixin implements NBTTagCompoundAccessor {
    @Shadow
    private Map<String, NBTBase> tagMap;

    public NBTTagCompoundMixin() {
    }

    public Map<String, NBTBase> btwessentials$getMap() {
        return this.tagMap;
    }
}