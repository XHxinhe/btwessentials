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

    @Inject(method = "writeEntityToNBT", at = @At("RETURN"))
    private void writeEntityToNBTMixin(NBTTagCompound nbt, CallbackInfo ci) {
        nbt.setTag("playerHomeList", this.playerHomeList);
    }

    @Inject(method = "readEntityFromNBT", at = @At("RETURN"))
    private void readEntityFromNBTMixin(NBTTagCompound nbt, CallbackInfo ci) {
        this.playerHomeList = nbt.getTagList("playerHomeList");
    }

    @Inject(method = "clonePlayer", at = @At("RETURN"))
    private void clonePlayer(EntityPlayer player, boolean par2, CallbackInfo ci) {
        NBTTagCompound homeList = ((EntityPlayerMPAccessor)player).btwessentials$getPlayerHomeList();
        this.playerHomeList = homeList.getTagList("homes");
    }

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

    @Override
    public void btwessentials$setHomePosition(String name, double posX, double posY, double posZ, int dimension) {
        for(int i = 0; i < this.playerHomeList.tagCount(); ++i) {
            if (((NBTTagCompound)this.playerHomeList.tagAt(i)).hasKey(name)) {
                ((NBTTagListAccesstor)this.playerHomeList).btwessentials$removeTag(i);
                break;
            }
        }
        NBTTagCompound home = this.newHomeNBTList(name, posX, posY, posZ, dimension);
        this.playerHomeList.appendTag(home);
    }

    @Override
    public void btwessentials$deleteHomePosition(String name) {
        for(int i = 0; i < this.playerHomeList.tagCount(); ++i) {
            if (((NBTTagCompound)this.playerHomeList.tagAt(i)).hasKey(name)) {
                ((NBTTagListAccesstor)this.playerHomeList).btwessentials$removeTag(i);
                break;
            }
        }
    }

    @Override
    public Pos btwessentials$getHomePosition(String name) {
        for(int i = 0; i < this.playerHomeList.tagCount(); ++i) {
            if (((NBTTagCompound)this.playerHomeList.tagAt(i)).hasKey(name)) {
                Map.Entry<String, NBTBase> firstEntry = this.getMapEntry(this.playerHomeList.tagAt(i));
                NBTTagList home = (NBTTagList)firstEntry.getValue();
                return new Pos(
                        ((NBTTagInt)home.tagAt(0)).data ,
                        ((NBTTagInt)home.tagAt(1)).data,
                        ((NBTTagInt)home.tagAt(2)).data,
                        ((NBTTagInt)home.tagAt(3)).data
                );
            }
        }
        return null;
    }

    @Override
    public List<String> btwessentials$listHomePosition() {
        List<String> homes = new ArrayList<>();
        for(int i = 0; i < this.playerHomeList.tagCount(); ++i) {
            Map.Entry<String, NBTBase> firstEntry = this.getMapEntry(this.playerHomeList.tagAt(i));
            NBTTagList home = (NBTTagList)firstEntry.getValue();
            String name = firstEntry.getKey();
            homes.add(name + " (" +
                    ((NBTTagInt)home.tagAt(0)).data + ", " +
                    ((NBTTagInt)home.tagAt(1)).data + ", " +
                    ((NBTTagInt)home.tagAt(2)).data + "), " +
                    ((NBTTagInt)home.tagAt(3)).data
            );
        }
        return homes;
    }

    @Override
    public List<String> btwessentials$listHomeName() {
        List<String> homes = new ArrayList<>();
        for(int i = 0; i < this.playerHomeList.tagCount(); ++i) {
            Map.Entry<String, NBTBase> firstEntry = this.getMapEntry(this.playerHomeList.tagAt(i));
            homes.add(firstEntry.getKey());
        }
        return homes;
    }

    @Unique
    protected NBTTagCompound newHomeNBTList(String name, double x, double y, double z, int dimension) {
        NBTTagList nbtTagList = new NBTTagList();
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        nbtTagList.appendTag(new NBTTagDouble(null, x));
        nbtTagList.appendTag(new NBTTagDouble(null, y));
        nbtTagList.appendTag(new NBTTagDouble(null, z));
        nbtTagList.appendTag(new NBTTagDouble(null, dimension));
        nbtTagCompound.setTag(name, nbtTagList);
        return nbtTagCompound;
    }

    @Unique
    protected Map.Entry<String, NBTBase> getMapEntry(NBTBase nbtTagCompound) {
        Map<String, NBTBase> tagMap = ((NBTTagCompoundAccessor)nbtTagCompound).btwessentials$getMap();
        return tagMap.entrySet().iterator().next();
    }
}