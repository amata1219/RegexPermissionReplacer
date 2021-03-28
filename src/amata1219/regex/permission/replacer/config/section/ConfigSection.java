package amata1219.regex.permission.replacer.config.section;

import amata1219.regex.permission.replacer.config.Config;
import org.bukkit.configuration.ConfigurationSection;

public abstract class ConfigSection {

    private final Config config;
    private final String sectionName;

    public ConfigSection(Config config, String sectionName) {
        this.config = config;
        this.sectionName = sectionName;
    }

    protected ConfigurationSection section() {
        return config.config().getConfigurationSection(sectionName);
    }

    public abstract void loadValues();

}
