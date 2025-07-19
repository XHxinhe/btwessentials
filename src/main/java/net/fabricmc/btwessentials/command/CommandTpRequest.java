package net.fabricmc.btwessentials.command;

import java.util.List;
import java.util.Objects;
import net.fabricmc.btwessentials.api.EntityPlayerMPAccessor;
import net.minecraft.src.CommandBase;
import net.minecraft.src.ICommandSender;
import net.minecraft.src.PlayerNotFoundException;
import net.minecraft.src.WrongUsageException;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class CommandTpRequest extends CommandBase {
    public CommandTpRequest() {
    }

    @Override
    public String getCommandName() {
        return "tpa";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "commands.tpa.usage";
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
            throw new WrongUsageException("Try /tpa [playername]");
        } else {
            EntityPlayerMP teleportingPlayer = getCommandSenderAsPlayer(sender);
            EntityPlayerMP targetPlayer = getPlayer(sender, arguments[0]);

            if (targetPlayer == null) {
                throw new PlayerNotFoundException();
            } else if (Objects.equals(targetPlayer.getCommandSenderName(), teleportingPlayer.getCommandSenderName())) {
                teleportingPlayer.addChatMessage("你为什么传送你自己");
            } else {
                String targetPlayerName = targetPlayer.getCommandSenderName();
                ((EntityPlayerMPAccessor)targetPlayer).btwessentials$setTpaRequestName(teleportingPlayer.getCommandSenderName());
                teleportingPlayer.addChatMessage("你的传送请求已发送给" + targetPlayerName);
                targetPlayer.addChatMessage(teleportingPlayer.getCommandSenderName() + "向你发送了传送请求");
            }
        }
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