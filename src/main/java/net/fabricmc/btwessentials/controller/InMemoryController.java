package net.fabricmc.btwessentials.controller;

import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryController {

    // 核心数据结构：一个Map，用于存储每个世界（以其目录名作为key）对应的备份名称列表（value）。
    // 使用 final 确保该 Map 实例本身不会被替换。
    private final Map<String, List<String>> backups;

    /**
     * 构造函数，初始化内存控制器。
     */
    public InMemoryController() {
        // 创建一个空的 HashMap 来存储备份信息。
        backups = new HashMap<>();
    }

    /**
     * 初始化方法，在插件启动时调用。
     * 它的作用是扫描磁盘上的备份文件，并将它们加载到内存缓存中。
     */
    public void initialize() {
        // 1. 获取所有不重复的世界存档目录名。
        List<String> distinctWorldNames = getDistinctWorldNames();

        // 2. 为每个世界，加载其最近的备份列表。
        distinctWorldNames.forEach(worldName -> {
            // 获取备份文件所在的根目录。
            Path backupDirectory = getBackupDirectory();

            // 从磁盘读取该世界最近的10个备份。
            // 注意：这里硬编码了只加载10个，这可能是一个为了启动性能的设计决策。
            List<String> backupDirectoryNames = BackupController.getBackups(backupDirectory, worldName, 10);

            // 将读取到的备份列表存入内存的Map中。
            // 使用 new ArrayList<>() 是一个好习惯，可以确保我们得到一个可变的列表。
            backups.put(worldName, new ArrayList<>(backupDirectoryNames));
        });
    }

    /**
     * 获取备份根目录的路径。
     * 如果该目录不存在，此方法会尝试创建它。
     * @return 备份根目录的Path对象。
     */
    private static Path getBackupDirectory() {
        // 从配置控制器获取备份文件夹的路径。
        File backupDirectory = SingletonController.getConfigurationController().getBackupFolder().toFile();
        // 检查目录是否存在。
        if (!backupDirectory.exists()) {
            // 如果不存在，则尝试创建该目录（以及所有必需的父目录）。
            boolean created = backupDirectory.mkdirs();

            // 如果创建失败，则抛出运行时异常，因为后续操作将无法进行。
            if (!created) {
                throw new RuntimeException("backups folder cannot be created");
            }
        }

        // 返回目录的Path对象。
        return backupDirectory.toPath();
    }

    /**
     * 获取当前服务器上所有不重复的世界存档目录名称列表。
     * 例如，即使有主世界、地狱、末地，如果它们都在同一个 "world" 文件夹下，此方法也只会返回 "world" 一次。
     */
    private static List<String> getDistinctWorldNames() {
        // 使用 Stream 和 Collectors.toMap 来高效地去重。
        return Arrays.stream(MinecraftServer.getServer().worldServers)
                .collect(Collectors.toMap(
                        worldServer -> worldServer.getSaveHandler().getWorldDirectoryName(), // Key: 存档目录名
                        worldServer -> worldServer,                                         // Value: WorldServer 实例
                        (existing, replacement) -> existing))                              // Merge function: 如果key重复，保留已存在的
                .values()
                .stream()
                // 从去重后的 WorldServer 对象中提取出目录名。
                .map(worldServer -> worldServer.getSaveHandler().getWorldDirectoryName())
                .toList();
    }

    /**
     * 根据世界目录名，从内存缓存中获取其备份列表。
     * @param worldDirectoryName 世界的存档目录名。
     * @return 一个包含备份名称的列表，如果该世界没有备份记录，则返回一个空列表。
     */
    public List<String> getBackupNames(String worldDirectoryName) {
        // 检查缓存中是否存在该世界的条目，如果不存在，返回一个不可变的空列表，避免NullPointerException。
        if (!backups.containsKey(worldDirectoryName)) return Collections.emptyList();

        // 返回缓存中的列表。
        return backups.get(worldDirectoryName);
    }

    /**
     * 向内存缓存中添加一个新的备份记录。
     * @param worldDirectoryName 备份所属的世界目录名。
     * @param worldBackupName 新备份的文件夹名称。
     */
    public void addBackup(String worldDirectoryName, String worldBackupName) {
        // 如果是这个世界的第一个备份，需要先在Map中为它创建一个新的空列表。
        if (!backups.containsKey(worldDirectoryName)) {
            backups.put(worldDirectoryName, new ArrayList<>());
        }

        // 将新的备份名添加到列表的开头 (索引0)。
        // 这使得列表自然地保持了从新到旧的顺序。
        backups.get(worldDirectoryName).add(0, worldBackupName);
    }

    /**
     * 从内存缓存中移除一个备份记录。
     * 这通常在旧备份被删除后调用，以保持内存与磁盘同步。
     * @param worldDirectoryName 备份所属的世界目录名。
     * @param worldBackupName 要移除的备份文件夹名称。
     */
    public void removeBackup(String worldDirectoryName, String worldBackupName) {
        // 如果缓存中没有该世界的记录，则直接返回。
        if (!backups.containsKey(worldDirectoryName)) return;

        // 从列表中移除指定的备份名称。
        backups.get(worldDirectoryName).remove(worldBackupName);
    }
}