package amata1219.regex.permission.replacer;

import net.luckperms.api.LuckPerms;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class RegexPermissionReplacer extends JavaPlugin {

    private static RegexPermissionReplacer instance;
    private final LuckPerms luckPerms = (LuckPerms) getServer().getPluginManager().getPlugin("LuckPerms");

    @Override
    public void onEnable() {
        instance = this;
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }

    public static RegexPermissionReplacer instance() {
        return instance;
    }

    public LuckPerms luckPerms() {
        return luckPerms;
    }

}
