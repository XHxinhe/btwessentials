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
        // 修改了用法说明，使其更通用
        return "/back - 返回到上一个传送前的位置 (tp, home等)";
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
        // 这行代码会获取由 /tp 或 /home 命令保存的最后位置
        Pos lastPos = BTWEssentials.getInstance().getLastPosition(player.getEntityName());

        if (lastPos == null) {
            player.addChatMessage("§c没有可以返回的位置");
            return;
        }

        // 安全检查：不能从末地传送出去
        if (player.dimension == 1 && lastPos.dimension != 1) {
            player.addChatMessage("§c你不能从末地逃离这个世界");
            return;
        }

        // 在传送前，先保存当前位置，这样就可以在 /back 之后再次 /back 回来
        Pos currentPosForFutureBack = new Pos(player.posX, player.posY, player.posZ, player.dimension);
        BTWEssentials.getInstance().setLastPosition(player.getEntityName(), currentPosForFutureBack);
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