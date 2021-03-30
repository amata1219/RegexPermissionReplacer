package amata1219.regex.permission.replacer;

import amata1219.regex.permission.replacer.command.MainCommand;
import amata1219.regex.permission.replacer.config.MainConfig;
import amata1219.regex.permission.replacer.operation.record.OperationRecord;
import amata1219.regex.permission.replacer.operation.record.OperationRecords;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Comparator;
import java.util.TreeMap;

public class RegexPermissionReplacer extends JavaPlugin {

    private static RegexPermissionReplacer instance;

    private MainConfig config;
    private final TreeMap<OperationId, OperationRecord> operationRecords = new TreeMap<>(Comparator.reverseOrder());

    @Override
    public void onEnable() {
        instance = this;
        config = new MainConfig();
        getCommand("rpr").setExecutor(new MainCommand(config, new OperationRecords(operationRecords)));
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }

    public static RegexPermissionReplacer instance() {
        return instance;
    }

}
