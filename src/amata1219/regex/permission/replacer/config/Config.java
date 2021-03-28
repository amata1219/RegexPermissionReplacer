package amata1219.regex.permission.replacer.config;

import amata1219.regex.permission.replacer.RegexPermissionReplacer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public abstract class Config {

    protected final RegexPermissionReplacer plugin = RegexPermissionReplacer.instance();
    private FileConfiguration config;
    private final File file;
    private final String fileName;

    public Config(String configFileName) {
        if (!configFileName.endsWith(".yml")) throw new IllegalArgumentException("extension of configFileName must be .yml");

        this.fileName = configFileName;
        this.file = new File(plugin.getDataFolder(), fileName);
        saveDefault();
    }

    public void saveDefault() {
        if (!file.exists()) plugin.saveResource(fileName, false);
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(file);
        InputStream input = plugin.getResource(fileName);
        if (input != null) config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(input, StandardCharsets.UTF_8)));
    }

    public void save() {
        if (config == null) {
            try {
                config().save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public FileConfiguration config() {
        if (config == null) reload();
        return config;
    }

    public abstract void loadValues();

}
