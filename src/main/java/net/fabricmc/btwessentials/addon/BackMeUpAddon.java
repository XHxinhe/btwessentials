package net.fabricmc.btwessentials.addon;

import btw.BTWAddon;
import net.fabricmc.btwessentials.command.BackMeUpCommand;
import net.fabricmc.btwessentials.controller.SingletonController;

import java.util.Map;

// 静态导入配置项枚举，可以直接使用 ENABLED, NOTIFICATION 等，无需写 ConfigProperties.ENABLED
import static net.fabricmc.btwessentials.constants.ConfigProperties.*;

public class BackMeUpAddon extends BTWAddon {

    /**
     * 预初始化方法。在插件生命周期的早期被调用。
     * 主要用于注册本插件所有可用的配置项及其默认值和描述。
     * 这些配置项会出现在 .minecraft/btwmodtex/BTWAddon_Back-me-Up.properties 文件中。
     */
    @Override
    public void preInitialize() {
        // 注册配置项的方法: this.registerProperty(配置名, "默认值", "描述文字");

        // --- 基本开关和通知 ---
        // 注册“启用备份”配置项：控制整个备份功能的开关。
        this.registerProperty(ENABLED.name(), "true", "Enables or disables the backup functionality.");
        // 注册“通知”配置项：控制备份完成后是否在游戏内聊天框发送提示。
        this.registerProperty(NOTIFICATION.name(), "true", "你已经成功备份存档.");

        // --- 备份频率与时机 ---
        // 注册“游戏日开始偏移”配置项：用于兼容其他可能修改一天开始时间的模组。原版默认是6000 ticks。
        this.registerProperty(DAY_START_OFFSET.name(), "6000", "If you're playing with mods that change the day start time, you can enter the offset ticks here. Leave 6000 if using none.");
        // 注册“备份频率单位”配置项：'d' 代表游戏天, 'h' 代表游戏小时。
        this.registerProperty(BACKUP_FREQUENCY_UNIT.name(), "h", "Backup frequency unit: 'd' for in-game days, 'h' for in-game hours.");
        // 注册“备份频率数值”配置项：与上面的单位配合使用，决定备份间隔。
        this.registerProperty(BACKUP_FREQUENCY.name(), "12", "How frequent backups should be made, based on the unit above.");

        // --- 存储与清理策略 ---
        // 注册“备份文件夹路径”配置项：默认值为 null，程序会自动设为游戏根目录下的 'backups' 文件夹。
        this.registerProperty(BACKUP_FOLDER.name(), null, "The folder to store backups. Defaults to 'game_folder/backups'.");
        // 注册“旧备份清理策略”配置项：'queued' (队列) 或 'never_clean' (从不清理)。
        this.registerProperty(CLEANUP_POLICY.name(), "never_clean", "Cleanup policy for old backups: 'queued' or 'never_clean'.");
        // 注册“队列大小”配置项：仅在清理策略为 'queued' 时生效，当备份数量超过此值时，会删除最旧的一个。
        this.registerProperty(QUEUE_SIZE.name(), "30", "Max number of backups to keep when using the 'queued' cleanup policy.");
    }

    /**
     * 初始化方法。在 preInitialize 之后调用。
     * 主要用于注册插件的命令、事件监听器等。
     */
    @Override
    public void initialize() {
        // 注册 /backmeup 命令，使其在游戏中可用。
        this.registerAddonCommand(new BackMeUpCommand());
    }

    /**
     * 处理配置属性的方法。
     * 当游戏从 .properties 文件中读取到配置后，会调用此方法。
     * @param propertyValues 一个包含所有已加载配置项键值对的 Map。
     */
    @Override
    public void handleConfigProperties(Map<String, String> propertyValues) {
        // 将从配置文件加载的键值对传递给配置控制器，让其更新插件内部的配置状态。
        SingletonController.getConfigurationController().loadConfigurations(propertyValues);
    }
}