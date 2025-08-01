//package net.fabricmc.btwessentials.command;
//
//import java.util.Collections;
//import java.util.List;
//import net.fabricmc.btwessentials.api.EntityPlayerMPAccessor;
//import net.minecraft.src.CommandBase;      // class_955
//import net.minecraft.src.ICommandSender;   // class_1061
//import net.minecraft.src.EntityPlayerMP; // class_798
//
//public class CommandTpDeny extends CommandBase {
//    public CommandTpDeny() {
//    }
//
//    @Override
//    public String getCommandName() {          // method_3277
//        return "tpdeny";
//    }
//
//    @Override
//    public String getCommandUsage(ICommandSender sender) {  // method_3275
//        return "commands.tpdeny.usage";
//    }
//
//    @Override
//    public List<?> getCommandAliases() {      // method_3274
//        return Collections.singletonList("tpno");
//    }
//
//    @Override
//    public int getRequiredPermissionLevel() {  // method_4634
//        return 0;
//    }
//
//    @Override
//    public boolean canCommandSenderUseCommand(ICommandSender sender) {  // method_3278
//        return true;
//    }
//
//    @Override
//    public void processCommand(ICommandSender sender, String[] arguments) {
//        EntityPlayerMP cancelingPlayer = getCommandSenderAsPlayer(sender);
//        cancelingPlayer.addChatMessage("已拒绝和删除传送请求");
//        ((EntityPlayerMPAccessor)cancelingPlayer).btwessentials$setTpaRequestName("");
//    }
//}