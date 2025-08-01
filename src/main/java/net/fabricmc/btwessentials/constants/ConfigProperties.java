package net.fabricmc.btwessentials.constants;

public enum ConfigProperties {
    /**
     * 控制整个备份功能的总开关。
     * 对应配置文件中的 'ENABLED' 键。
     */
    ENABLED,

    /**
     * 控制备份完成后是否在游戏内发送通知。
     * 对应配置文件中的 'NOTIFICATION' 键。
     */
    NOTIFICATION,

    /**
     * 游戏内一天的起始时刻（tick）。用于兼容其他可能修改此值的模组。
     * 对应配置文件中的 'DAY_START_OFFSET' 键。
     */
    DAY_START_OFFSET,

    /**
     * 存放备份文件的文件夹路径。
     * 对应配置文件中的 'BACKUP_FOLDER' 键。
     */
    BACKUP_FOLDER,

    /**
     * 自动备份频率的单位（'d' for day, 'h' for hour）。
     * 对应配置文件中的 'BACKUP_FREQUENCY_UNIT' 键。
     */
    BACKUP_FREQUENCY_UNIT,

    /**
     * 自动备份频率的数值，与单位配合使用。
     * 对应配置文件中的 'BACKUP_FREQUENCY' 键。
     */
    BACKUP_FREQUENCY,

    /**
     * 旧备份的清理策略（'queued' 或 'never_clean'）。
     * 对应配置文件中的 'CLEANUP_POLICY' 键。
     */
    CLEANUP_POLICY,

    /**
     * 当清理策略为 'queued' 时，保留的备份最大数量。
     * 对应配置文件中的 'QUEUE_SIZE' 键。
     */
    QUEUE_SIZE
}