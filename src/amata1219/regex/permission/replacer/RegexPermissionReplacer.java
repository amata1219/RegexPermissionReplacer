package amata1219.regex.permission.replacer;

import amata1219.regex.permission.replacer.bridge.LuckPermsBridge;
import amata1219.regex.permission.replacer.command.MainCommand;
import amata1219.regex.permission.replacer.operation.record.OperationRecord;
import net.luckperms.api.LuckPerms;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class RegexPermissionReplacer extends JavaPlugin {

    private static RegexPermissionReplacer instance;
    private LuckPermsBridge luckPermsBridge;

    private final Map<Long, OperationRecord> operationRecords = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        luckPermsBridge = new LuckPermsBridge((LuckPerms) getServer().getPluginManager().getPlugin("LuckPerms"));
        getCommand("rpr").setExecutor(new MainCommand(operationRecords));
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }

    public static RegexPermissionReplacer instance() {
        return instance;
    }

    public LuckPermsBridge luckPermsBridge() {
        return luckPermsBridge;
    }

}
