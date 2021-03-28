package amata1219.regex.permission.replacer.config;

import amata1219.regex.permission.replacer.config.section.UndoSection;

public class MainConfig extends Config {

    public final UndoSection undoSection;

    public MainConfig() {
        super("config.yml");
        undoSection = new UndoSection(this);
    }

    @Override
    public void loadSections() {
        undoSection.loadValues();
    }

}
