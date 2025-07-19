package net.fabricmc.btwessentials.api;

import java.util.List;
import java.util.Optional;
import net.fabricmc.btwessentials.util.Pos;
import net.minecraft.src.NBTTagCompound;

public interface EntityPlayerMPAccessor {
    // TPA相关
    void btwessentials$setTpaRequestName(String playerName);

    default Optional<String> serverManager$getTpaRequestName() {
        return Optional.empty();
    }

    // Home相关
    void btwessentials$setHomePosition(String homeName, double x, double y, double z, int dimension);

    Pos btwessentials$getHomePosition(String homeName);

    void btwessentials$deleteHomePosition(String homeName);

    List<String>btwessentials$listHomePosition();

    List<String> btwessentials$listHomeName();

    NBTTagCompound btwessentials$getPlayerHomeList();
}