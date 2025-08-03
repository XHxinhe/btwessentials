package net.fabricmc.btwessentials.command;

import java.util.List;
import net.fabricmc.btwessentials.BTWEssentials;
import net.fabricmc.btwessentials.api.EntityPlayerMPAccessor;
import net.fabricmc.btwessentials.util.Pos;
import net.minecraft.src.ICommandSender;
import net.minecraft.src.WrongUsageException;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.CommandBase;
import net.minecraft.src.Entity;

public class CommandGoHome extends CommandBase {
    public CommandGoHome() {
    }

    public String getCommandName() {
        return "home";
    }

    public String getCommandUsage(ICommandSender iCommandSender) {
        return "commands.home.usage";
    }

    public int getRequiredPermissionLevel() {
        return 0;
    }

    public boolean canCommandSenderUseCommand(ICommandSender par1ICommandSender) {
        return true;
    }

    public void processCommand(ICommandSender sender, String[] arguments) {
        if (arguments.length > 1) {
            throw new WrongUsageException("Try /home [homename]", new Object[0]);
        }

        if (arguments.length < 1) {
            arguments = new String[]{"home"};
        }

        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        Pos pos = ((EntityPlayerMPAccessor)player).btwessentials$getHomePosition(arguments[0]);

        if (pos == null) {
            player.addChatMessage("§c你还没有设置该名称的家");
            return;
        }

        if (player.dimension == 1 && pos.dimension != 1) {
            player.addChatMessage("§c你不能从末地逃离这个世界");
            return;
        }

        // 保存当前位置用于/back命令
        BTWEssentials.getInstance().setLastPosition(
                player.getEntityName(),
                player.posX,
                player.posY,
                player.posZ,
                player.dimension
        );

        // 传送玩家
        player.mountEntity((Entity)null);
        if (player.dimension != pos.dimension) {
            player.travelToDimension(pos.dimension);
        }

        player.playerNetServerHandler.setPlayerLocation(pos.x, pos.y, pos.z, 0.0F, 0.0F);
        player.addChatMessage("§a已传送回home点，使用 /back 可以返回之前的位置");
    }

    public List<?> addTabCompletionOptions(ICommandSender par1ICommandSender, String[] par2ArrayOfStr) {
        EntityPlayerMP player = getCommandSenderAsPlayer(par1ICommandSender);
        if (par2ArrayOfStr.length >= 1) {
            List<String> homeNames = ((EntityPlayerMPAccessor)player).btwessentials$listHomeName();
            return getListOfStringsMatchingLastWord(par2ArrayOfStr, homeNames.toArray(new String[0]));
        }
        return null;
    }
}