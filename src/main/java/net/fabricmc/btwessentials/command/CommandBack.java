package net.fabricmc.btwessentials.command;

import net.fabricmc.btwessentials.BTWEssentials;
import net.fabricmc.btwessentials.util.Pos;
import net.minecraft.src.CommandBase;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.ICommandSender;
import net.minecraft.src.WrongUsageException;

public class CommandBack extends CommandBase {
    @Override
    public String getCommandName() {
        return "back";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/back - 返回使用/home前的位置";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (!(sender instanceof EntityPlayerMP)) {
            throw new WrongUsageException("命令只能由玩家执行");
        }

        EntityPlayerMP player = (EntityPlayerMP)sender;
        Pos lastPos = BTWEssentials.getInstance().getLastPosition(player.getEntityName());

        if (lastPos == null) {
            player.addChatMessage("§c没有可以返回的位置");
            return;
        }

        if (player.dimension == 1 && lastPos.dimension != 1) {
            player.addChatMessage("§c你不能从末地传送");
            return;
        }

        player.mountEntity((Entity)null);
        if (player.dimension != lastPos.dimension) {
            player.travelToDimension(lastPos.dimension);
        }

        player.playerNetServerHandler.setPlayerLocation(
                lastPos.x,
                lastPos.y,
                lastPos.z,
                player.rotationYaw,
                player.rotationPitch
        );

        player.addChatMessage("§a已返回到之前的位置");
    }
}