package net.fabricmc.btwessentials.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

public class CleanupController {

    // 定义清理策略的常量字符串，避免在代码中使用"魔法字符串"
    private static final String NEVER_CLEAN = "never_clean"; // 策略：从不清理
    private static final String QUEUED = "queued";           // 策略：保留一个固定数量的队列

    /**
     * 清理操作的主入口方法。
     * @param backupDirectory 备份文件所在的根目录。
     * @param worldDirectoryName 需要清理备份的特定世界的名称。
     */
    public void cleanup(Path backupDirectory, String worldDirectoryName) {
        // 从配置中获取当前的清理策略
        String cleanupPolicy = SingletonController.getConfigurationController().getCleanupPolicy();

        // 如果策略是"从不清理"，则直接返回，不执行任何操作
        if (cleanupPolicy.equals(NEVER_CLEAN)) {
            return;
        }

        // 获取当前该世界已有的备份数量
        long numberOfBackups = getNumberOfBackups(backupDirectory, worldDirectoryName);
        // 从配置中获取需要保留的备份数量（队列大小）
        int queueSize = SingletonController.getConfigurationController().getQueueSize();

        // 如果当前备份数量未超过队列大小，则无需清理，直接返回
        if (numberOfBackups <= queueSize) {
            return;
        }

        // 如果备份数量超出限制，则循环删除最旧的备份，直到数量达标
        do {
            // 查找并返回该世界最旧的一个备份目录
            Optional<Path> oldestDirectoryOpt = findOldestDirectory(backupDirectory, worldDirectoryName);

            // 健壮性检查：如果未能找到最旧的目录（理论上不应发生），则抛出异常
            if (oldestDirectoryOpt.isEmpty()) {
                throw new RuntimeException("Error occurred while cleanup process");
            }

            // 获取最旧目录的路径
            Path oldestDirectory = oldestDirectoryOpt.get();
            // 删除该目录及其所有内容
            deleteDirectory(oldestDirectory);
            // 将备份数量计数器减一
            numberOfBackups--;

            // 从内存状态中移除已被删除的备份记录，保持状态同步
            SingletonController.getInMemoryControllerInstance().removeBackup(worldDirectoryName, oldestDirectory.getFileName().toString());
        } while (numberOfBackups > queueSize);
    }

    /**
     * 计算指定世界当前有多少个备份。
     */
    private static long getNumberOfBackups(Path backupDirectory, String worldDirectoryName) {
        // 调用 BackupController 的辅助方法来获取列表，然后返回其大小
        return BackupController.getBackupsDateStripped(backupDirectory, worldDirectoryName).size();
    }

    /**
     * 在指定目录下，根据文件的创建时间找到属于特定世界的最旧的子目录。
     * @return 一个包含最旧目录路径的 Optional，如果找不到则为空。
     */
    public static Optional<Path> findOldestDirectory(Path parentDirectory, String worldDirectoryName) {
        try (Stream<Path> paths = Files.list(parentDirectory)) {
            return paths
                    .filter(Files::isDirectory) // 筛选出所有子目录
                    // 筛选出属于目标世界的备份（通过剥离文件名中的日期时间部分来判断）
                    .filter(folder -> BackupController.stripDateTime(folder.getFileName().toString()).equals(worldDirectoryName))
                    // 使用比较器找到创建时间最早的那个目录
                    .min(Comparator.comparing(CleanupController::getCreationTime));
        } catch (IOException e) {
            // 如果在遍历目录时发生IO错误，则抛出运行时异常
            throw new RuntimeException("Failed to find the oldest directory", e);
        }
    }

    /**
     * 获取一个文件或目录的创建时间。
     */
    private static FileTime getCreationTime(Path path) {
        try {
            // 读取文件的基本属性，并返回其中的创建时间
            return Files.readAttributes(path, BasicFileAttributes.class).creationTime();
        } catch (IOException e) {
            // 如果读取属性失败，则抛出运行时异常
            throw new RuntimeException("Unable to read creation time", e);
        }
    }

    /**
     * 递归删除一个目录及其所有内容。
     */
    public static void deleteDirectory(Path directory) {
        try (Stream<Path> walk = Files.walk(directory)) {
            // 必须先对路径进行反向排序，这样才能确保先删除文件，再删除空的父目录
            walk.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            // 如果删除失败，抛出包含具体路径的异常信息
                            throw new RuntimeException("Failed to delete: " + path, e);
                        }
                    });
        } catch (Exception exception) {
            // 如果在遍历或删除过程中发生任何错误，则抛出异常
            throw new RuntimeException("Couldn't delete oldest backup folder", exception);
        }
    }
}