package net.fabricmc.btwessentials.mixin;

import java.util.*;
import net.fabricmc.btwessentials.api.EntityPlayerMPAccessor;
import net.fabricmc.btwessentials.api.NBTTagCompoundAccessor;
import net.fabricmc.btwessentials.api.NBTTagListAccesstor;
import net.fabricmc.btwessentials.util.Pos;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({EntityPlayerMP.class})
public class EntityPlayerMPHomeMixin implements EntityPlayerMPAccessor {
    @Unique
    private String tpaRequestPlayerName;
    @Unique
    private NBTTagList playerHomeList = new NBTTagList();

    // --- NBT 数据持久化 ---
    // 将家的列表写入玩家NBT数据
    @Inject(method = "writeEntityToNBT", at = @At("RETURN"))
    private void writeEntityToNBTMixin(NBTTagCompound nbt, CallbackInfo ci) {
        nbt.setTag("playerHomeList", this.playerHomeList);
    }

    // 从玩家NBT数据中读取家的列表
    @Inject(method = "readEntityFromNBT", at = @At("RETURN"))
    private void readEntityFromNBTMixin(NBTTagCompound nbt, CallbackInfo ci) {
        if (nbt.hasKey("playerHomeList")) {
            this.playerHomeList = nbt.getTagList("playerHomeList");
        }
    }

    // 玩家重生时复制家的列表
    @Inject(method = "clonePlayer", at = @At("RETURN"))
    private void clonePlayer(EntityPlayer player, boolean par2, CallbackInfo ci) {
        NBTTagCompound homeList = ((EntityPlayerMPAccessor)player).btwessentials$getPlayerHomeList();
        this.playerHomeList = homeList.getTagList("homes");
    }

    // --- 接口实现 ---
    @Override
    public NBTTagCompound btwessentials$getPlayerHomeList() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag("homes", this.playerHomeList);
        return compound;
    }

    @Override
    public void btwessentials$setTpaRequestName(String name) {
        this.tpaRequestPlayerName = name;
    }

    @Override
    public Optional<String> serverManager$getTpaRequestName() {
        return Optional.ofNullable(this.tpaRequestPlayerName);
    }

    /**
     * 设置家的方法。
     * 总是使用新的、精确的 NBTTagDouble 格式来保存坐标。
     */
    @Override
    public void btwessentials$setHomePosition(String name, double posX, double posY, double posZ, int dimension) {
        // 先删除同名的旧家，防止重复
        for(int i = 0; i < this.playerHomeList.tagCount(); ++i) {
            if (((NBTTagCompound)this.playerHomeList.tagAt(i)).hasKey(name)) {
                ((NBTTagListAccesstor)this.playerHomeList).btwessentials$removeTag(i);
                break;
            }
        }
        // 创建并添加新家
        NBTTagCompound home = this.newHomeNBTList(name, posX, posY, posZ, dimension);
        this.playerHomeList.appendTag(home);
    }

    /**
     * 删除家的方法。
     */
    @Override
    public void btwessentials$deleteHomePosition(String name) {
        for(int i = 0; i < this.playerHomeList.tagCount(); ++i) {
            if (((NBTTagCompound)this.playerHomeList.tagAt(i)).hasKey(name)) {
                ((NBTTagListAccesstor)this.playerHomeList).btwessentials$removeTag(i);
                break;
            }
        }
    }

    /**
     * 【已打补丁】获取家位置的方法。
     * 这个方法现在可以兼容新旧两种数据格式。
     */
    @Override
    public Pos btwessentials$getHomePosition(String name) {
        for(int i = 0; i < this.playerHomeList.tagCount(); ++i) {
            NBTTagCompound homeEntry = (NBTTagCompound)this.playerHomeList.tagAt(i);
            if (homeEntry.hasKey(name)) {
                NBTTagList homeCoordsList = (NBTTagList)homeEntry.getTag(name);

                if (homeCoordsList.tagCount() < 4) continue; // 数据不完整，跳过

                // 【补丁核心】检查第一个元素的类型来判断格式
                NBTBase firstTag = homeCoordsList.tagAt(0);

                // --- 新格式处理 (NBTTagDouble) ---
                if (firstTag instanceof NBTTagDouble) {
                    return new Pos(
                            ((NBTTagDouble)homeCoordsList.tagAt(0)).data,
                            ((NBTTagDouble)homeCoordsList.tagAt(1)).data,
                            ((NBTTagDouble)homeCoordsList.tagAt(2)).data,
                            (int)((NBTTagDouble)homeCoordsList.tagAt(3)).data
                    );
                }
                // --- 旧格式处理 (NBTTagInt) ---
                else if (firstTag instanceof NBTTagInt) {
                    return new Pos(
                            ((NBTTagInt)homeCoordsList.tagAt(0)).data + 0.5, // 旧格式加0.5，传送到方块中心
                            ((NBTTagInt)homeCoordsList.tagAt(1)).data,
                            ((NBTTagInt)homeCoordsList.tagAt(2)).data + 0.5, // 旧格式加0.5，传送到方块中心
                            ((NBTTagInt)homeCoordsList.tagAt(3)).data
                    );
                }
            }
        }
        return null; // 没找到家
    }

    /**
     * 【已打补丁】列出所有家的方法。
     * 这个方法现在可以正确显示新旧两种格式的坐标。
     */
    @Override
    public List<String> btwessentials$listHomePosition() {
        List<String> homes = new ArrayList<>();
        for(int i = 0; i < this.playerHomeList.tagCount(); ++i) {
            NBTTagCompound homeEntry = (NBTTagCompound)this.playerHomeList.tagAt(i);
            Map.Entry<String, NBTBase> firstEntry = this.getMapEntry(homeEntry);
            String name = firstEntry.getKey();
            NBTTagList homeCoordsList = (NBTTagList)firstEntry.getValue();

            if (homeCoordsList.tagCount() < 4) continue;

            NBTBase firstTag = homeCoordsList.tagAt(0);
            String posString;

            // --- 新格式显示 ---
            if (firstTag instanceof NBTTagDouble) {
                posString = String.format("%.1f, %.1f, %.1f",
                        ((NBTTagDouble)homeCoordsList.tagAt(0)).data,
                        ((NBTTagDouble)homeCoordsList.tagAt(1)).data,
                        ((NBTTagDouble)homeCoordsList.tagAt(2)).data
                );
                homes.add(name + " (" + posString + "), dim: " + (int)((NBTTagDouble)homeCoordsList.tagAt(3)).data);
            }
            // --- 旧格式显示 ---
            else if (firstTag instanceof NBTTagInt) {
                posString = String.format("%d, %d, %d",
                        ((NBTTagInt)homeCoordsList.tagAt(0)).data,
                        ((NBTTagInt)homeCoordsList.tagAt(1)).data,
                        ((NBTTagInt)homeCoordsList.tagAt(2)).data
                );
                homes.add(name + " (" + posString + "), dim: " + ((NBTTagInt)homeCoordsList.tagAt(3)).data + " [旧格式]");
            }
        }
        return homes;
    }

    /**
     * 列出所有家的名称，用于Tab补全。
     */
    @Override
    public List<String> btwessentials$listHomeName() {
        List<String> homes = new ArrayList<>();
        for(int i = 0; i < this.playerHomeList.tagCount(); ++i) {
            Map.Entry<String, NBTBase> firstEntry = this.getMapEntry(this.playerHomeList.tagAt(i));
            homes.add(firstEntry.getKey());
        }
        return homes;
    }

    // --- 内部辅助方法 ---
    /**
     * 创建一个新的家的NBT数据。
     * 总是使用 NBTTagDouble 来保证精度。
     */
    @Unique
    protected NBTTagCompound newHomeNBTList(String name, double x, double y, double z, int dimension) {
        NBTTagList nbtTagList = new NBTTagList();
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        nbtTagList.appendTag(new NBTTagDouble(null, x));
        nbtTagList.appendTag(new NBTTagDouble(null, y));
        nbtTagList.appendTag(new NBTTagDouble(null, z));
        nbtTagList.appendTag(new NBTTagDouble(null, (double)dimension));
        nbtTagCompound.setTag(name, nbtTagList);
        return nbtTagCompound;
    }

    /**
     * 获取NBTTagCompound中的第一个条目，因为你的结构是每个Compound只有一个家。
     */
    @Unique
    protected Map.Entry<String, NBTBase> getMapEntry(NBTBase nbtTagCompound) {
        Map<String, NBTBase> tagMap = ((NBTTagCompoundAccessor)nbtTagCompound).btwessentials$getMap();
        return tagMap.entrySet().iterator().next();
    }
}