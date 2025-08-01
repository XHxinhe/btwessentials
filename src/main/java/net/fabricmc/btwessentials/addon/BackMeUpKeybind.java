/**
 * @author AI聊天机器人
 * @date 2025-08-01
 * @description 负责管理 BackMeUp 插件的自定义快捷键。
 *              此类将“手动保存”和“手动加载”的快捷键注册到 Minecraft 的游戏设置中，
 *              允许玩家在控制菜单中查看和修改它们。
 */
package net.fabricmc.btwessentials.addon;

import net.minecraft.src.GameSettings;
import net.minecraft.src.KeyBinding;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BackMeUpKeybind {

    // --- 常量定义 ---
    // 用于在游戏设置和 options.txt 文件中唯一标识“保存”快捷键的字符串。
    private static final String SAVE_KEY_DESCRIPTION = "backup.save";
    // 用于唯一标识“加载”快捷键的字符串。
    private static final String LOAD_KEY_DESCRIPTION = "backup.load";
    // “保存”功能的默认按键：F6。
    private static final int DEFAULT_SAVE_KEY = Keyboard.KEY_B;
    // “加载”功能的默认按键：F7。
    private static final int DEFAULT_LOAD_KEY = Keyboard.KEY_END;

    // --- 静态快捷键实例 ---
    // 公共静态变量，方便插件的其他部分直接访问和检查按键状态 (e.g., BackMeUpKeybind.saveKey.isPressed())。
    public static KeyBinding saveKey;
    public static KeyBinding loadKey;

    /**
     * 初始化并注册快捷键。
     * 此方法会检查游戏设置中是否已存在我们的自定义快捷键。
     * 如果不存在，则创建它们并添加到游戏设置中。
     *
     * @param gameSettings Minecraft 的游戏设置实例，其中包含了所有的快捷键绑定。
     */
    public static void initKeybind(GameSettings gameSettings) {
        // 将游戏设置中的快捷键数组转换为一个可修改的列表，方便添加新元素。
        List<KeyBinding> keyBindings = new ArrayList<>(Arrays.asList(gameSettings.keyBindings));

        // 遍历现有的所有快捷键，查找是否已经注册过我们的快捷键。
        // 这样做是为了持久化玩家的自定义设置。如果玩家修改了按键，下次启动游戏时会加载修改后的设置。
        for (KeyBinding keyBinding : keyBindings) {
            if (keyBinding.keyDescription.equals(SAVE_KEY_DESCRIPTION)) {
                saveKey = keyBinding; // 如果找到了，就直接使用已存在的实例。
            }
            else if (keyBinding.keyDescription.equals(LOAD_KEY_DESCRIPTION)) {
                loadKey = keyBinding; // 同上。
            }
        }

        // 用于标记是否需要更新游戏设置中的快捷键数组。
        boolean needsUpdate = false;

        // 如果遍历后发现“保存”快捷键还未注册...
        if (saveKey == null) {
            // ...则创建一个新的 KeyBinding 实例，并添加到列表中。
            saveKey = new KeyBinding(SAVE_KEY_DESCRIPTION, DEFAULT_SAVE_KEY);
            keyBindings.add(saveKey);
            needsUpdate = true; // 标记需要更新。
        }

        // 如果遍历后发现“加载”快捷键还未注册...
        if (loadKey == null) {
            // ...也创建一个新的实例并添加。
            loadKey = new KeyBinding(LOAD_KEY_DESCRIPTION, DEFAULT_LOAD_KEY);
            keyBindings.add(loadKey);
//            needsUpdate = true; // 标记需要更新。
        }

        // 如果我们向列表中添加了任何新的快捷键...
        if (needsUpdate) {
            // ...就将更新后的列表转换回数组，并替换掉游戏设置中旧的数组。
            // 这是将新快捷键正式注册到游戏中的关键步骤。
            gameSettings.keyBindings = keyBindings.toArray(new KeyBinding[0]);
        }
    }
}