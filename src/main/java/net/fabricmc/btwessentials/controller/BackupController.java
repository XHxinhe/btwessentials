package net.fabricmc.btwessentials.controller;

import btw.AddonHandler;
import net.fabricmc.btwessentials.records.WorldInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.Minecraft;
import net.minecraft.src.MinecraftException;
import net.minecraft.src.WorldServer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.fabricmc.btwessentials.constants.Constants.*;

public class BackupController {
    /**
     * 主更新方法，由外部定时器或事件（如tick事件）调用。
     * @param forceBackup 如果为 true，则无视时间条件，强制执行备份。
     */
    public void update(boolean forceBackup) {
        // 检查功能总开关是否开启
        if (!SingletonController.getConfigurationController().isEnabled()) {
            AddonHandler.logMessage("Backup enabled is false, skipping...");
            return;
        }

        // 获取所有符合备份条件的世界列表
        List<WorldInfo> eligibleWorlds = getEligibleWorlds(forceBackup);

        // 如果没有世界需要备份，则直接返回
        if (eligibleWorlds.isEmpty()) {
            return;
        }

        // 获取配置中指定的备份文件夹路径
        Path backupDirectory = SingletonController.getConfigurationController().getBackupFolder();

        // 遍历每一个需要备份的世界
        eligibleWorlds.forEach(worldInfo -> {
            // 源路径：当前世界的存档文件夹
            Path source = Paths.get(MinecraftServer.getServer().getFile(SAVES).getPath(), worldInfo.worldDirectoryName());
            // 根据世界信息生成本次备份的文件夹名称
            String worldBackupName = createWorldBackupName(worldInfo);
            // 目标路径：备份文件夹下的新备份文件夹
            Path destination = backupDirectory.resolve(worldBackupName);

            // 检查：如果同名备份已存在，或在同一游戏分钟内已有备份，则跳过
            if (Files.exists(destination) || isSameMinute(worldBackupName, forceBackup)) {
                return;
            }
            // 执行文件夹复制操作，完成备份
            copyFolder(source, destination);
            // 备份完成后，将新备份的名称记录到内存中
            SingletonController.getInMemoryControllerInstance().addBackup(worldInfo.worldDirectoryName(), worldBackupName);
            // 触发清理逻辑，根据配置删除旧的备份
            SingletonController.getCleanupController().cleanup(backupDirectory, worldInfo.worldDirectoryName());
        });

        // 如果配置了通知，并且确实执行了备份，则在聊天框发送消息
        if (SingletonController.getConfigurationController().isNotificationEnabled()) {
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage("§e成功备份");
        }
    }

    /**
     * 检查在同一游戏分钟内是否已存在备份。
     * 这是为了防止因游戏暂停或tick波动导致在短时间内创建多个几乎相同的备份。
     */
    private boolean isSameMinute(String worldBackupName, boolean forceBackup) {
        // 如果是强制备份，则不进行此项检查
        if (forceBackup) return false;

        // --- 从新的备份名称中解析出日期和时间 ---
        String dayStr = (worldBackupName.split("_Day")[1]).split("_")[0];
        String timeStr = worldBackupName.split("_Day(.*)_")[1];
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH.mm.ss");

        Integer day = Integer.parseInt(dayStr);
        LocalTime time = LocalTime.parse(timeStr, dtf);

        // --- 遍历内存中已有的备份记录进行比较 ---
        String worldName = Minecraft.getMinecraft().getIntegratedServer().getFolderName();
        List<String> backups = SingletonController.getInMemoryControllerInstance().getBackupNames(worldName);

        for (String backup : backups) {
            // 从已有的备份名称中解析出日期和时间
            String backupDayStr = (backup.split("_Day")[1]).split("_")[0];
            String backupTimeStr = backup.split("_Day(.*)_")[1];

            Integer backupDay = Integer.parseInt(backupDayStr);
            LocalTime backupTime = LocalTime.parse(backupTimeStr, dtf);

            // 如果天、小时、分钟都相同，则认为是在同一分钟内
            if (day.equals(backupDay) && time.getHour() == backupTime.getHour() && time.getMinute() == backupTime.getMinute()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 获取所有符合备份条件的世界列表。
     */
    private static List<WorldInfo> getEligibleWorlds(boolean forceBackup) {
        // 获取服务器上所有的世界实例（主世界、地狱、末地等）
        // 使用 Collectors.toMap 进行去重，因为不同维度的世界可能共享同一个存档目录
        List<WorldServer> distinctWorlds = Arrays.stream(MinecraftServer.getServer().worldServers)
                .collect(Collectors.toMap(
                        worldServer -> worldServer.getSaveHandler().getWorldDirectoryName(), // Key: 存档目录名
                        worldServer -> worldServer,                                         // Value: WorldServer 实例
                        (existing, replacement) -> existing))                              // Merge function: 如果key重复，保留已存在的
                .values()
                .stream()
                .toList();

        // 从去重后的世界列表中进行过滤和处理
        return distinctWorlds.stream()
                // 过滤条件：要么是强制备份，要么是到达了自动备份的时间点
                .filter(worldServer -> forceBackup || isBackupTime(worldServer.getTotalWorldTime()))
                // 对符合条件的世界进行处理
                .map(worldServer -> {
                    // 在备份前，关键一步：将世界所有数据和玩家数据强制保存到磁盘
                    saveWorldAndPlayerData(worldServer);

                    // 创建一个包含世界信息（目录名和当前时间）的记录
                    return new WorldInfo(worldServer.getSaveHandler().getWorldDirectoryName(), worldServer.getTotalWorldTime());
                })
                .toList();
    }

    /**
     * 强制将世界数据和玩家数据保存到磁盘，确保备份的是最新状态。
     */
    private static void saveWorldAndPlayerData(WorldServer worldServer) {
        // 保存所有在线玩家的数据
        MinecraftServer.getServer().getConfigurationManager().saveAllPlayerData();

        try {
            // 保存所有已加载的区块
            worldServer.saveAllChunks(true, null);
        } catch (MinecraftException e) {
            // 如果保存失败，则抛出运行时异常
            throw new RuntimeException("An exception occurred while saving the chunks", e);
        }

        // 刷新世界数据到磁盘
        worldServer.flush();
    }

    /**
     * 根据当前游戏时间判断是否到达了配置的自动备份时间点。
     */
    private static boolean isBackupTime(long totalWorldTime) {
        // 获取配置的日始偏移量和时间间隔常量
        long dayStartOffset = SingletonController.getConfigurationController().getDayStartOffset();
        // 计算当前是第几天和当天过去了多少tick
        long day = (totalWorldTime + dayStartOffset + GAP_TICKS) / ONE_DAY_TICKS + 1;
        long remainingTicks = (totalWorldTime + dayStartOffset + GAP_TICKS) % ONE_DAY_TICKS;
        // 将tick转换为24小时制时间
        LocalTime time = convertTicksToTime(remainingTicks);

        // 获取配置的备份频率单位和数值
        String backupFrequencyUnit = SingletonController.getConfigurationController().getBackupFrequencyUnit();
        int backupFrequency = SingletonController.getConfigurationController().getBackupFrequency();

        // 根据单位判断是否到达备份时间
        return switch (backupFrequencyUnit) {
            // 按天备份：检查是否是每天的开始（0点），并且天数是频率的倍数
            case "d" -> time.getHour() == 0 && day % backupFrequency == 0;
            // 按小时备份：检查小时数是否是频率的倍数
            case "h" -> time.getHour() % backupFrequency == 0;
            // 无效单位
            default -> throw new RuntimeException("Backup frequency unit has invalid value");
        };
    }

    /**
     * 根据世界信息生成标准格式的备份文件夹名称。
     * 格式: WorldName_DayX_HH.mm.ss
     */
    private String createWorldBackupName(WorldInfo worldInfo) {
        long dayStartOffset = SingletonController.getConfigurationController().getDayStartOffset();
        long day = (worldInfo.totalWorldTime() + dayStartOffset + GAP_TICKS) / ONE_DAY_TICKS + 1;
        long remainingTicks = (worldInfo.totalWorldTime() + dayStartOffset + GAP_TICKS) % ONE_DAY_TICKS;
        LocalTime time = convertTicksToTime(remainingTicks);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH.mm.ss");

        return worldInfo.worldDirectoryName() + "_Day" + day + "_" + dtf.format(time);
    }

    /**
     * 将游戏刻 (ticks) 转换为24小时制的时间 (LocalTime)。
     * 这个转换算法基于 1 tick = 3.6 现实秒的假设。
     */
    public static LocalTime convertTicksToTime(long ticks) {
        int totalSeconds = (int) (ticks * 3.6);

        int hours = totalSeconds / 3600;
        int minutes = (int) (((totalSeconds / 3600.0) % 1) * 60);
        int seconds = (int) (((((totalSeconds / 3600.0) % 1) * 60) % 1) * 60);

        // Format into 24-hour time
        return LocalTime.of(hours, minutes, seconds);
    }

    /**
     * 递归地将一个文件夹（源）复制到另一个位置（目标）。
     */
    public static void copyFolder(Path source, Path destination) {
        try (Stream<Path> sourcePathStream = Files.walk(source)) {
            sourcePathStream.forEach(sourcePath -> {
                try {
                    // 计算出在目标文件夹中对应的路径
                    Path targetPath = destination.resolve(source.relativize(sourcePath));
                    // 复制文件，如果目标文件已存在则替换
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception exception) {
                    throw new RuntimeException("An error occurred while copying backup folder", exception);
                }
            });
        } catch (Exception exception) {
            throw new RuntimeException("An error occurred while copying backup folder", exception);
        }
    }

    /**
     * 获取指定世界的前 N 个备份。
     * (注意：此方法未在当前类中使用，可能是为其他模块提供的公共API)
     */
    public static List<String> getBackups(Path backupDirectory, String worldDirectoryName, int numberOfBackups) {
        try (Stream<Path> files = Files.list(backupDirectory)) {
            return files
                    .filter(Files::isDirectory) // 只看文件夹
                    .map(Path::getFileName)
                    .map(Path::toString)
                    // 过滤出属于当前世界的备份
                    .filter(folder -> stripDateTime(folder).equals(worldDirectoryName))
                    .sorted(sortNames()) // 按时间顺序排序
                    .collect(Collectors.collectingAndThen(
                            Collectors.toList(),
                            list -> {
                                Collections.reverse(list); // 反转列表，得到从新到旧的顺序
                                return list;
                            }
                    ))
                    .stream()
                    .limit(numberOfBackups) // 取前N个
                    .toList();
        } catch (Exception exception) {
            throw new RuntimeException("An error occurred while traversing backup directory", exception);
        }
    }

    /**
     * 创建一个用于比较备份名称时间顺序的比较器。
     */
    private static Comparator<String> sortNames() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH.mm.ss");

        return (o1, o2) -> {
            // 从两个文件名中分别解析出天数和时间
            String day1Str = (o1.split("_Day")[1]).split("_")[0];
            String day2Str = (o2.split("_Day")[1]).split("_")[0];
            String time1Str = o1.split("_Day(.*)_")[1];
            String time2Str = o2.split("_Day(.*)_")[1];

            LocalTime time1 = LocalTime.parse(time1Str, dtf);
            LocalTime time2 = LocalTime.parse(time2Str, dtf);

            Integer day1 = Integer.parseInt(day1Str);
            Integer day2 = Integer.parseInt(day2Str);

            // 优先比较天数
            if (day1.equals(day2)) {
                // 如果天数相同，则比较时间
                return time1.compareTo(time2);
            }

            return day1.compareTo(day2);
        };
    }

    /**
     * 获取指定世界的所有备份，但只返回剥离了日期时间的世界名。
     * (注意：此方法未在当前类中使用，可能是为其他模块提供的公共API)
     */
    public static List<String> getBackupsDateStripped(Path backupDirectory, String worldDirectoryName) {
        try (Stream<Path> files = Files.list(backupDirectory)) {
            return files
                    .filter(Files::isDirectory)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .map(BackupController::stripDateTime) // 剥离日期时间
                    .filter(folder -> folder.equals(worldDirectoryName)) // 过滤
                    .toList();
        } catch (Exception exception) {
            throw new RuntimeException("An error occurred while traversing backup directory", exception);
        }
    }

    /**
     * 从备份文件夹名称中剥离日期和时间，只返回原始世界名。
     * 例如: "MyWorld_Day123_14.30.05" -> "MyWorld"
     */
    public static String stripDateTime(String wDateTime) {
        return wDateTime.split("_Day")[0];
    }
}