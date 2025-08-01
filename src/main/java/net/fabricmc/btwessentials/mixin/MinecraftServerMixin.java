package net.fabricmc.btwessentials.mixin;

import net.fabricmc.btwessentials.controller.SingletonController;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
	@Unique
	private boolean initialized = false;

	/**
	 * 将我们的初始化逻辑注入到服务器的主循环 tick() 方法中。
	 * @param ci 回调信息对象，由 Mixin 框架提供。
	 */
	// @Inject 注解定义了注入细节：
	//   - method = "tick()V": 目标是服务器的主循环方法。
	//   - at = @At("TAIL"): 注入点是 "TAIL"，表示我们的代码在每个tick的原版逻辑执行完毕后运行。
	@Inject(at = @At("TAIL"), method = "tick()V")
	private void init(CallbackInfo ci) {
		// 卫语句1：如果已经初始化过了，则直接返回。
		// 这使得这个注入方法在第一次成功执行后，在后续的所有 tick 中都变成一个空操作，开销极小。
		if (initialized) return;

		// 卫语句2：安全检查。在服务器启动的极早期阶段，静态的 getServer() 方法可能返回 null。
		// 这个检查确保我们只在服务器实例完全可用后才继续执行。
		if (MinecraftServer.getServer() == null) return;

		// --- 核心逻辑 ---
		// 获取 InMemoryController 的单例，并调用其 initialize() 方法。
		// 这个方法会扫描磁盘上的备份文件，并将它们加载到内存缓存中。
		// 这是插件启动流程的关键一步。
		SingletonController.getInMemoryControllerInstance().initialize();

		// 将标志位设置为 true，以防止在未来的 tick 中重复执行初始化。
		initialized = true;
	}
}