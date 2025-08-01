package net.fabricmc.btwessentials.command;

import btw.AddonHandler;
import net.fabricmc.btwessentials.controller.BackupController;
import net.fabricmc.btwessentials.controller.SingletonController;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static net.fabricmc.btwessentials.constants.Constants.SAVES;

public class BackMeUpCommand extends CommandBase {

    // --- 常量定义，用于用户反馈 (已更新) ---
    // --- 常量定义，用于用户反馈 (已中文化) ---
    private static final String USAGE_MESSAGE = "§c/backup <list | index>";
    private static final String INVALID_INDEX_MESSAGE = "§c'/backup list' 查看可用备份。";
    private static final String NO_BACKUPS_FOUND_MESSAGE = "§c当前世界没有任何备份。";

    // --- 命令基本定义 (已更新) ---

    @Override
    public String getCommandName() {
        return "backup";
    }

    @Override
    public String getCommandUsage(ICommandSender iCommandSender) {
        return USAGE_MESSAGE;
    }

    /**
     * 权限检查：确保只有玩家实体才能使用此命令。
     * 防止在服务器控制台或命令方块中执行，因为加载世界是客户端强相关的操作。
     */
    @Override
    public boolean canCommandSenderUseCommand(ICommandSender iCommandSender) {
        return iCommandSender instanceof EntityPlayer;
    }

    /**
     * 命令处理的主入口。根据参数的内容分发到不同的子命令处理方法 (已重构)。
     */
    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        // 新的命令结构下，有效的命令只有一个参数。
        if (args.length == 1) {
            String argument = args[0];

            // 情况一: 处理 /backup list
            if ("list".equalsIgnoreCase(argument)) {
                listCommand(sender);
                return; // 命令处理完毕，退出方法
            }

            // 情况二: 尝试处理 /backup <index>
            try {
                // 尝试将参数解析为整数，以判断它是否为索引。
                // 实际的解析和验证在 loadCommand 方法内部完成，这里只是用于判断类型。
                Integer.parseInt(argument);

                // 如果解析成功，说明是索引，调用加载命令。
                loadCommand(sender, argument);

            } catch (NumberFormatException e) {
                // 如果解析为整数失败，说明参数既不是 "list" 也不是数字，命令无效。
                sender.sendChatToPlayer(ChatMessageComponent.createFromText(getCommandUsage(sender)));
            }
        } else {
            // 如果参数数量不是1，则命令格式错误。
            sender.sendChatToPlayer(ChatMessageComponent.createFromText(getCommandUsage(sender)));
        }
    }

    /**
     * 处理 '/backup list' 命令。
     * 列出当前世界所有可用的备份。
     */
    private void listCommand(ICommandSender sender) {
        String worldDirName = Minecraft.getMinecraft().getIntegratedServer().getFolderName();
        List<String> backupNames = SingletonController.getInMemoryControllerInstance().getBackupNames(worldDirName);

        if (backupNames.isEmpty()) {
            sender.sendChatToPlayer(ChatMessageComponent.createFromText(NO_BACKUPS_FOUND_MESSAGE));
            return;
        }

        sender.sendChatToPlayer(ChatMessageComponent.createFromText("Available backups:"));
        for (int i = 0; i < backupNames.size(); i++) {
            // 发送带索引和名称的备份条目，并设置为斜体以示区分
            sender.sendChatToPlayer(ChatMessageComponent.createFromText(
                    " " + i + " : " + backupNames.get(i)
            ).setItalic(true));
        }
    }

    /**
     * 处理 '/backup <index>' 命令。
     * 验证索引并触发世界加载流程。
     * 设为 public static 是为了让快捷键等其他模块也能调用此逻辑。
     */
    public static void loadCommand(ICommandSender sender, String indexStr) {
        String worldDirName = Minecraft.getMinecraft().getIntegratedServer().getFolderName();
        List<String> backupNames = SingletonController.getInMemoryControllerInstance().getBackupNames(worldDirName);

        int index;
        try {
            index = Integer.parseInt(indexStr);
        } catch (NumberFormatException e) {
            // 增加 sender != null 检查，以兼容快捷键调用
            if (sender != null) {
                sender.sendChatToPlayer(ChatMessageComponent.createFromText(INVALID_INDEX_MESSAGE));
            }
            return;
        }

        // 检查索引是否在有效范围内 [0, backupNames.size() - 1]
        if (index < 0 || index >= backupNames.size()) {
            if (sender != null) {
                sender.sendChatToPlayer(ChatMessageComponent.createFromText(INVALID_INDEX_MESSAGE));
            }
            return;
        }

        // 索引有效，开始加载世界
        loadWorld(worldDirName, backupNames.get(index));
    }

    // --- 核心文件操作方法 (无需修改) ---

    /**
     * 执行加载世界的核心操作：断开连接、删除旧世界、复制备份、设置待加载世界。
     * 这是一个高风险操作，因为它直接修改了存档文件。
     */
    private static void loadWorld(String worldDirName, String backupDirName) {
        Path worldPath = Paths.get(MinecraftServer.getServer().getFile(SAVES).getPath(), worldDirName);

        try {
            // 1. 通知游戏客户端断开与当前集成服务器的连接，释放文件句柄。
            Minecraft.getMinecraft().theWorld.sendQuittingDisconnectingPacket();
            waitFor(1000); // 等待1秒，确保断开过程完成。

            // 2. 删除当前的世界存档文件夹。
            deleteOldWorld(worldPath);
            waitFor(1000); // 等待1秒，确保文件系统完成删除操作。

            // 3. 将备份文件夹复制到原世界存档的位置。
            copyBackupToSavesFolder(worldPath, backupDirName);

            // 4. 通知插件的加载控制器，在下一次进入世界时加载这个新恢复的存档。
            SingletonController.getWorldLoaderControllerInstance().setWorldToBeLoaded(worldDirName);

        } catch (Exception e) {
            // 如果过程中出现任何错误，打印日志并通知玩家，防止游戏卡在未知状态。
            AddonHandler.logMessage("FATAL: Failed to load backup '" + backupDirName + "'. Reason: " + e.getMessage());
            e.printStackTrace();
            // 尝试让玩家回到主菜单
            Minecraft.getMinecraft().displayGuiScreen(new GuiMainMenu());
        }
    }

    // --- 辅助方法 (无需修改) ---

    /**
     * 线程等待的辅助方法。
     * @param milliseconds 等待的毫秒数。
     */
    public static void waitFor(int milliseconds) {
        try {
            TimeUnit.MILLISECONDS.sleep(milliseconds);
        } catch (InterruptedException e) {
            // 在单人游戏中，线程中断不常见，但记录日志是好习惯。
            Thread.currentThread().interrupt(); // 重新设置中断状态
            AddonHandler.logMessage("Thread sleep interrupted: " + e.getMessage());
        }
    }

    /**
     * 删除旧的世界文件夹。
     * @param worldPath 要删除的世界文件夹的路径。
     * @throws IOException 如果删除失败。
     */
    private static void deleteOldWorld(Path worldPath) throws IOException {
        try {
            FileUtils.deleteDirectory(worldPath.toFile());
        } catch (IOException e) {
            // 向上抛出异常，由调用者（loadWorld）统一处理。
            throw new IOException("An error occurred while deleting the old world at: " + worldPath, e);
        }
    }

    /**
     * 将指定的备份文件夹内容复制到存档目录。
     * @param targetWorldPath 目标路径（即 saves/<world_name>）。
     * @param backupDirName   要复制的备份文件夹的名称。
     */
    private static void copyBackupToSavesFolder(Path targetWorldPath, String backupDirName) {
        Path backupBaseDir = SingletonController.getConfigurationController().getBackupFolder();
        Path sourceBackupPath = backupBaseDir.resolve(backupDirName);

        // 使用控制器中封装好的复制方法
        BackupController.copyFolder(sourceBackupPath, targetWorldPath);
    }
}