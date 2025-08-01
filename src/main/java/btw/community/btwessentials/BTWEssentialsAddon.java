package btw.community.btwessentials;

import btw.AddonHandler;
import btw.BTWAddon;
import net.fabricmc.btwessentials.command.*;

public class BTWEssentialsAddon extends BTWAddon {
    private static BTWEssentialsAddon instance;

    public BTWEssentialsAddon() {
    }

    @Override
    public void initialize() {
        AddonHandler.logMessage(this.getName() + " Version " + this.getVersionString() + " Initializing...");

        this.registerAddonCommand(new CommandBack());       // 返回死亡点
        this.registerAddonCommand(new CommandTpRequest());  // 请求传送
//        this.registerAddonCommand(new CommandTpAccept());   // 接受传送
//        this.registerAddonCommand(new CommandTpDeny());     // 拒绝传送
        this.registerAddonCommand(new CommandSetHome());    // 设置家
        this.registerAddonCommand(new CommandGoHome());     // 回家
        this.registerAddonCommand(new CommandListHome());   // 列出家园点
        this.registerAddonCommand(new CommandDeleteHome()); // 删除家园点

        // 如果未来有任何只应在服务器上运行的命令，使用这个方法：
        // this.registerAddonCommandServerOnly(new YourServerOnlyCommand());
    }
}
