package net.fabricmc.btwessentials.command;

import java.util.List;
import java.util.Objects;

import net.fabricmc.btwessentials.BTWEssentials;
import net.fabricmc.btwessentials.util.Pos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.CommandBase;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.ICommandSender;
import net.minecraft.src.PlayerNotFoundException;
import net.minecraft.src.WrongUsageException;

// 注意：这个类的文件名是 CommandTpRequest.java，但它实现的命令现在是 /tp
public class CommandTpRequest extends CommandBase {
    public CommandTpRequest() {
    }

    @Override
    public String getCommandName() {
        // 将命令从 "tpa" 修改为 "tp"
        return "tp";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        // 用法提示也相应更新
        return "用法: /tp <玩家名>";
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
    public void processCommand(ICommandSender sender, String[] arguments) {
        if (arguments.length != 1) {
            throw new WrongUsageException("用法: /tp <玩家名>");
        }

        EntityPlayerMP teleportingPlayer = getCommandSenderAsPlayer(sender);
        EntityPlayerMP targetPlayer = getPlayer(sender, arguments[0]);

        if (targetPlayer == null) {
            throw new PlayerNotFoundException();
        }

        if (Objects.equals(targetPlayer.getCommandSenderName(), teleportingPlayer.getCommandSenderName())) {
            teleportingPlayer.addChatMessage("你不能将自己传送到自己的位置。");
            return;
        }
        Pos currentPos = new Pos(teleportingPlayer.posX, teleportingPlayer.posY, teleportingPlayer.posZ, teleportingPlayer.dimension);
        BTWEssentials.getInstance().setLastPosition(teleportingPlayer.getEntityName(), currentPos);
        if (teleportingPlayer.dimension == 1 && targetPlayer.dimension != 1) {
            teleportingPlayer.addChatMessage("你不能从末地逃离这个世界");
            return;
        }

        teleportingPlayer.mountEntity(null);

        if (teleportingPlayer.dimension != targetPlayer.dimension) {
            teleportingPlayer.travelToDimension(targetPlayer.dimension);
        }

        teleportingPlayer.playerNetServerHandler.setPlayerLocation(
                targetPlayer.posX,
                targetPlayer.posY,
                targetPlayer.posZ,
                targetPlayer.rotationYaw,
                targetPlayer.rotationPitch
        );

        teleportingPlayer.addChatMessage("已将你传送到 " + targetPlayer.getCommandSenderName() + " 的位置。");
        targetPlayer.addChatMessage(teleportingPlayer.getCommandSenderName() + " 已传送到你的位置。");
    }

    @Override
    public List<?> addTabCompletionOptions(ICommandSender sender, String[] args) {
        return args.length >= 1 ? getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getConfigurationManager().getAllUsernames()) : null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return index == 0;
    }
}