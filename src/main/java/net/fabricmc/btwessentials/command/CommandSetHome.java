package net.fabricmc.btwessentials.command;

import net.fabricmc.btwessentials.api.EntityPlayerMPAccessor;
import net.minecraft.src.CommandBase;      // class_955
import net.minecraft.src.ICommandSender;   // class_1061
import net.minecraft.src.WrongUsageException; // class_165
import net.minecraft.src.EntityPlayerMP; // class_798

public class CommandSetHome extends CommandBase {
    public CommandSetHome() {
    }

    @Override
    public String getCommandName() {          // method_3277
        return "sethome";
    }

    @Override
    public String getCommandUsage(ICommandSender iCommandSender) {  // method_3275
        return "commands.sethome.usage";
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
        if (arguments.length > 1) {
            throw new WrongUsageException("Try /sethome [homename]");
        } else {
            if (arguments.length < 1) {
                arguments = new String[]{"home"};
            }

            EntityPlayerMP player = getCommandSenderAsPlayer(sender);

            if (((EntityPlayerMPAccessor)player).btwessentials$listHomeName().size() > 20) {
                player.addChatMessage("最多设置20个家");
            } else {
                ((EntityPlayerMPAccessor)player).btwessentials$setHomePosition(
                        arguments[0],
                        player.posX,
                        player.posY,
                        player.posZ,
                        player.dimension
                );
                player.addChatMessage("设置家成功");
            }
        }
    }
}