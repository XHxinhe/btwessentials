package net.fabricmc.btwessentials;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.btwessentials.command.CommandBack;
import net.fabricmc.btwessentials.util.Pos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.CommandHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class BTWEssentials implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("BTWEssentials");
	private static BTWEssentials instance;
	private final Map<String, Pos> lastDeathPositions = new HashMap<>();
	private final Map<String, Pos> lastPositions = new HashMap<>();  // 添加这行

	public BTWEssentials() {
		instance = this;
	}

	public static BTWEssentials getInstance() {
		return instance;
	}

	@Override
	public void onInitialize() {
		LOGGER.info("BTW Essentials (Fabric) Initialized!");

		// 注册命令
		if (MinecraftServer.getServer() != null &&
				MinecraftServer.getServer().getCommandManager() instanceof CommandHandler) {
			CommandHandler commandHandler = (CommandHandler) MinecraftServer.getServer().getCommandManager();
			commandHandler.registerCommand(new CommandBack());
		}
	}
	public void setLastPosition(String playerName, double x, double y, double z, int dimension) {
		lastPositions.put(playerName, new Pos(x, y, z, dimension));
	}

	// 获取使用/home前的位置
	public Pos getLastPosition(String playerName) {
		return lastPositions.get(playerName);
	}
}