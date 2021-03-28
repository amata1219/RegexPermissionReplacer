package amata1219.regex.permission.replacer.config.section;

import amata1219.regex.permission.replacer.config.MainConfig;
import org.bukkit.configuration.ConfigurationSection;

public class UndoSection extends ConfigSection {

    private int undoLimitAtOneTime;

    public UndoSection(MainConfig config) {
        super(config, "Undo");
    }

    @Override
    public void loadValues() {
        ConfigurationSection section = section();
        undoLimitAtOneTime = section.getInt("Number of operations that can be undone at one time");
    }

}
