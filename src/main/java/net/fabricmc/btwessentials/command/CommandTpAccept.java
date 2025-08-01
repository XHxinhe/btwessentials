//package net.fabricmc.btwessentials.command;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//import net.fabricmc.btwessentials.api.EntityPlayerMPAccessor;
//import net.minecraft.src.CommandBase;      // class_955
//import net.minecraft.src.ICommandSender;   // class_1061
//import net.minecraft.src.EntityPlayerMP; // class_798
//import net.minecraft.src.World;             // class_864
//
//public class CommandTpAccept extends CommandBase {
//    public CommandTpAccept() {
//    }
//
//    @Override
//    public String getCommandName() {          // method_3277
//        return "tpaccept";
//    }
//
//    @Override
//    public String getCommandUsage(ICommandSender sender) {  // method_3275
//        return "commands.tpaccept.usage";
//    }
//
//    @Override
//    public List<?> getCommandAliases() {      // method_3274
//        return Collections.singletonList("tpyes");
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
//        EntityPlayerMP acceptingPlayer = getCommandSenderAsPlayer(sender);
//        Optional<String> optionalName = ((EntityPlayerMPAccessor) acceptingPlayer).serverManager$getTpaRequestName();
//        String tpaRequestName = optionalName.orElse("");
//
//        if (tpaRequestName.isEmpty()) {
//            acceptingPlayer.addChatMessage("No active teleport requests found from anyone.");
//        } else {
//            // 使用正确的方法获取玩家名称和目标玩家
//            EntityPlayerMP teleportingPlayer = getPlayer(sender, tpaRequestName);
//            String acceptingPlayerName = acceptingPlayer.getCommandSenderName();
//
//            acceptingPlayer.addChatMessage("传送" + tpaRequestName + "到你的坐标");
//            teleportingPlayer.addChatMessage("传送到" + acceptingPlayerName + "的坐标");
//
//            // 末地维度检查
//            if (teleportingPlayer.dimension == 1 && acceptingPlayer.dimension != 1) {
//                teleportingPlayer.addChatMessage("你不能从末地逃离这个世界");
//            } else {
//                teleportingPlayer.mountEntity(null);
//
//                // 如果两个玩家在不同维度，先切换维度
//                if (teleportingPlayer.dimension != acceptingPlayer.dimension) {
//                    teleportingPlayer.travelToDimension(acceptingPlayer.dimension);
//                }
//
//                // 执行传送
//                teleportingPlayer.playerNetServerHandler.setPlayerLocation(
//                        acceptingPlayer.posX,
//                        acceptingPlayer.posY,
//                        acceptingPlayer.posZ,
//                        acceptingPlayer.rotationYaw,
//                        acceptingPlayer.rotationPitch
//                );
//
//                // 清除传送请求
//                ((EntityPlayerMPAccessor) acceptingPlayer).btwessentials$setTpaRequestName("");
//            }
//        }
//    }
//}