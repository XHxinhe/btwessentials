package net.fabricmc.btwessentials.mixin;

import java.util.List;
import net.fabricmc.btwessentials.api.NBTTagListAccesstor;
import net.minecraft.src.NBTTagList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({NBTTagList.class})
public class NBTTagListMixin implements NBTTagListAccesstor {
    @Shadow
    private List<?> tagList;

    public NBTTagListMixin() {
    }

    /**
     * 从NBTTagList中移除指定索引的标签
     * @param i 要移除的标签索引
     */
    public void btwessentials$removeTag(int i) {
        this.tagList.remove(i);
    }
}