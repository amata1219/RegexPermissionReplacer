package amata1219.regex.permission.replacer.command;

import amata1219.regex.permission.replacer.RegexPermissionReplacer;
import amata1219.regex.permission.replacer.bryionake.dsl.parser.FailableParser;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.group.GroupManager;
import org.bukkit.ChatColor;

import static amata1219.regex.permission.replacer.bryionake.adt.Either.*;

public class ParserTemplates {

    public static final FailableParser<Group> group = arg -> {
        GroupManager manager = RegexPermissionReplacer.instance().luckPermsBridge().luckPerms.getGroupManager();
        return manager.isLoaded(arg) ? success(manager.getGroup(arg)) : failure(ChatColor.RED + "指定されたグループは存在しません。");
    };

}
