package net.fabricmc.btwessentials.command;

import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.btwessentials.api.EntityPlayerMPAccessor;
import net.minecraft.src.CommandBase;
import net.minecraft.src.ICommandSender;
import net.minecraft.src.WrongUsageException;
import net.minecraft.src.EntityPlayerMP;

public class CommandListHome extends CommandBase {
    public CommandListHome() {
    }

    @Override
    public String getCommandName() {          // method_3277
        return "listhome";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {  // method_3275
        return "commands.listhome.usage";
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
    public void processCommand(ICommandSender sender, String[] arguments) {
        if (arguments.length > 0) {
            throw new WrongUsageException("Try /listhome");
        } else {
            EntityPlayerMP player = getCommandSenderAsPlayer(sender);
            List<String> homes = ((EntityPlayerMPAccessor)player).btwessentials$listHomePosition();
            player.addChatMessage(homes.stream()  // method_3199
                    .map(String::valueOf)
                    .collect(Collectors.joining("\n"))
            );
        }
    }
}