package net.fabricmc.btwessentials.controller;

import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;
import java.util.Map;

import static net.fabricmc.btwessentials.constants.ConfigProperties.*;

public class ConfigurationController {
    private static final String BACKUPS = "backups";

    // --- 私有字段，用于存储加载后的配置值 ---
    private boolean enabled;                // 备份功能总开关
    private boolean notificationEnabled;    // 是否在备份完成后发送聊天通知
    private long dayStartOffset;            // 游戏内“天”开始时间的偏移量（以ticks为单位）
    private Path backupFolder;              // 备份文件夹的Path对象（延迟初始化）
    private String backupFolderString;      // 从配置文件读取的原始备份文件夹路径字符串
    private String backupFrequencyUnit;     // 备份频率的单位 ("d" for day, "h" for hour)
    private int backupFrequency;            // 备份频率的数值
    private String cleanupPolicy;           // 旧备份的清理策略 ("never_clean", "queued")
    private int queueSize;                  // 当清理策略为 "queued" 时，保留的备份数量

    /**
     * 从一个Map中加载所有配置项。
     * 这个Map通常是由读取 .properties 文件后生成的。
     * @param propertyValues 包含配置键和值的Map，键和值都为字符串。
     */
    public void loadConfigurations(Map<String, String> propertyValues) {
        // 将字符串 "true" 或 "false" 转换为布尔值
        enabled = Boolean.parseBoolean(propertyValues.get(ENABLED.name()));
        notificationEnabled = Boolean.parseBoolean(propertyValues.get(NOTIFICATION.name()));
        // 将数字字符串转换为 long 类型
        dayStartOffset = Long.parseLong(propertyValues.get(DAY_START_OFFSET.name()));
        // 直接存储原始字符串，Path对象的转换将延迟到第一次使用时
        backupFolderString = propertyValues.get(BACKUP_FOLDER.name());
        // 存储频率单位字符串
        backupFrequencyUnit = propertyValues.get(BACKUP_FREQUENCY_UNIT.name());
        // 将数字字符串转换为 int 类型
        backupFrequency = Integer.parseInt(propertyValues.get(BACKUP_FREQUENCY.name()));
        // 存储清理策略字符串
        cleanupPolicy = propertyValues.get(CLEANUP_POLICY.name());
        // 将数字字符串转换为 int 类型
        queueSize = Integer.parseInt(propertyValues.get(QUEUE_SIZE.name()));
    }

    /**
     * @return 备份功能是否启用。
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @return 备份完成通知是否启用。
     */
    public boolean isNotificationEnabled() {
        return notificationEnabled;
    }

    /**
     * @return 游戏天数计算的起始偏移量（ticks）。
     */
    public long getDayStartOffset() {
        return dayStartOffset;
    }

    /**
     * 获取备份文件夹的Path对象。
     * 这个方法使用了延迟初始化（Lazy Initialization）和缓存策略。
     * @return 备份文件夹的Path对象。
     */
    public Path getBackupFolder() {
        // 缓存检查：如果Path对象已经被创建，则直接返回，避免重复计算。
        if (backupFolder != null) {
            return backupFolder;
        }

        // 如果是第一次调用，则根据字符串配置创建Path对象。
        // 检查用户是否在配置文件中指定了路径。如果未指定或值为"null"，则使用默认路径。
        if (backupFolderString == null || backupFolderString.equals("null")) {
            // 默认路径：Minecraft服务器根目录下的 "backups" 文件夹。
            backupFolder = MinecraftServer.getServer().getFile(BACKUPS).toPath();
        } else {
            // 用户自定义路径：根据用户提供的字符串创建Path对象。
            backupFolder = Path.of(backupFolderString);
        }

        // 返回新创建的Path对象（它也同时被缓存在了 backupFolder 字段中）。
        return backupFolder;
    }

    /**
     * @return 备份频率的单位 ("d" 或 "h")。
     */
    public String getBackupFrequencyUnit() {
        return backupFrequencyUnit;
    }

    /**
     * @return 备份频率的数值。
     */
    public int getBackupFrequency() {
        return backupFrequency;
    }

    /**
     * @return 旧备份的清理策略。
     */
    public String getCleanupPolicy() {
        return cleanupPolicy;
    }

    /**
     * @return 在 "queued" 清理策略下，要保留的备份数量。
     */
    public int getQueueSize() {
        return queueSize;
    }
}