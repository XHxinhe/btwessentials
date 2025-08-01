package net.fabricmc.btwessentials.mixin;

import net.fabricmc.btwessentials.controller.SingletonController;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.Minecraft;
import net.minecraft.src.WorldClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

	/**
	 * 将我们的逻辑注入到 Minecraft 的主游戏循环 runTick() 方法中。
	 * 这个方法每秒会被调用20次。
	 * @param ci 回调信息对象，由 Mixin 框架提供。
	 */
	// @Inject 注解定义了注入细节：
	//   - method = "runTick()V": 目标是主循环方法。
	//   - at = @At("TAIL"): 注入点是 "TAIL"，表示我们的代码在每一帧的原版逻辑都执行完毕后运行。
	//     这是一个安全的选择，可以确保我们基于一个完全更新的游戏状态来做判断。
	@Inject(at = @At("TAIL"), method = "runTick()V")
	private void init(CallbackInfo ci) {
		// --- 任务1: 驱动世界加载器 ---
		// 每一帧都调用 WorldLoaderController 的 load() 方法。
		// 这个调用本身开销极小，因为 load() 方法内部有检查，只有在真正有世界需要加载时才会执行逻辑。
		// 这是一种“轮询”机制，用于执行由其他地方（如命令或快捷键）安排的加载任务。
		SingletonController.getWorldLoaderControllerInstance().load();

		// --- 任务2: 驱动自动备份调度器 ---
		// 安全检查：确保我们当前在一个已加载的世界中，并且有服务器在运行。
		// 这可以防止在主菜单等界面下执行备份逻辑，避免空指针异常。
		WorldClient world = Minecraft.getMinecraft().theWorld;
		MinecraftServer server = MinecraftServer.getServer();
		if (world == null || server == null) {
			return;
		}

		// 性能优化和调度：调用 isExactHour() 检查现在是否是触发备份检查的正确时机。
		// 如果不是，则直接返回，避免了不必要的计算。
		if (!isExactHour()) {
			return;
		}

		// 如果时间条件满足，则调用 BackupController 的 update 方法来处理备份逻辑。
		// 参数 false 表示这是一个自动触发的备份，而不是由玩家手动发起的。
		SingletonController.getBackupController().update(false);
	}

	/**
	 * 一个被 @Unique 注解标记的私有辅助方法，用于判断当前是否是游戏内的“整点”时刻。
	 * @return 如果当前游戏时间是1000 ticks的整数倍，则返回 true。
	 */
	@Unique
	private boolean isExactHour() {
		// 获取世界自创建以来经过的总 tick 数。
		long totalWorldTime = Minecraft.getMinecraft().theWorld.getTotalWorldTime();

		// 在 Minecraft 中，1 个游戏内小时 = 1000 ticks。
		// (24000 ticks/天, 24小时/天 -> 1000 ticks/小时)
		// 这个取模运算 `totalWorldTime % 1000` 的结果为 0，当且仅当总时间是1000的整数倍。
		// 因此，这个方法会在每个游戏内小时的开始时刻（xx:00）返回 true。
		return totalWorldTime % 1000 == 0;
	}
}