package amata1219.regex.permission.replacer;

import amata1219.regex.permission.replacer.bridge.LuckPermsBridge;
import net.luckperms.api.LuckPerms;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class RegexPermissionReplacer extends JavaPlugin {

    private static RegexPermissionReplacer instance;
    private LuckPermsBridge luckPermsBridge;

    @Override
    public void onEnable() {
        instance = this;
        luckPermsBridge = new LuckPermsBridge((LuckPerms) getServer().getPluginManager().getPlugin("LuckPerms"));
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }

    public static RegexPermissionReplacer instance() {
        return instance;
    }

}
