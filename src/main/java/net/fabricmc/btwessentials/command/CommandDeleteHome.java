package net.fabricmc.btwessentials.command;

import java.util.Collections;
import java.util.List;
import net.fabricmc.btwessentials.api.EntityPlayerMPAccessor;
import net.minecraft.src.CommandBase;
import net.minecraft.src.ICommandSender;
import net.minecraft.src.WrongUsageException;
import net.minecraft.src.EntityPlayerMP;

public class CommandDeleteHome extends CommandBase {
    public CommandDeleteHome() {
    }

    @Override
    public String getCommandName() {          // method_3277
        return "delhome";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {  // method_3275
        return "commands.delhome.usage";
    }

    @Override
    public List<?> getCommandAliases() {      // method_3274
        return Collections.singletonList("deletehome");
    }

    @Override
    public int getRequiredPermissionLevel() {  // method_4634
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {  // method_3278
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] arguments) {  // method_3279
        if (arguments.length != 1) {
            throw new WrongUsageException("Try /delhome [homename]");
        } else {
            EntityPlayerMP player = getCommandSenderAsPlayer(sender);  // method_4638
            ((EntityPlayerMPAccessor)player).btwessentials$deleteHomePosition(arguments[0]);
            player.addChatMessage("你成功删除了家: " + arguments[0]);  // method_3199
        }
    }

    @Override
    public List<?> addTabCompletionOptions(ICommandSender sender, String[] args) {  // method_3276
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        return args.length >= 1 ? getListOfStringsMatchingLastWord(  // method_2894
                args,
                new String[]{String.join(",", ((EntityPlayerMPAccessor)player).btwessentials$listHomeName())}
        ) : null;
    }
}