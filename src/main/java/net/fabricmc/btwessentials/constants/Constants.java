package net.fabricmc.btwessentials.constants;

public final class Constants {

    /**
     * 私有构造函数，防止外部代码实例化这个常量类。
     * 工具类和常量类通常只包含静态成员，不应该被创建对象。
     */
    private Constants() {
        // This class is not meant to be instantiated.
    }

    /**
     * Minecraft 存档文件夹的名称。
     * 在构建世界路径时会用到，例如：.minecraft/saves/<world_name>
     */
    public static final String SAVES = "saves";

    /**
     * Minecraft 中一个完整游戏日所包含的总刻数 (ticks)。
     * 1秒 = 20 ticks, 1天 = 20分钟 = 1200秒 = 24000 ticks.
     * 这个值用于基于“天”的定时任务计算。
     */
    public static final long ONE_DAY_TICKS = 24000L; // 在 long 类型数值后加上 'L' 是一个好习惯。

    /**
     * 一个时间间隔，代表 6000 ticks。
     * 这通常是 Minecraft 中从日出（tick 0）到正午（tick 6000）的时间。
     * 在这个插件中，它可能被用作配置项 `DAY_START_OFFSET` 的默认值或用于某些时间计算。
     */
    public static final long GAP_TICKS = 6000L; // 同上，使用 'L' 明确表示为 long 类型。
}