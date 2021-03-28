package amata1219.regex.permission.replacer.command;

import amata1219.regex.permission.replacer.OperationId;
import amata1219.regex.permission.replacer.RegexPermissionReplacer;
import amata1219.regex.permission.replacer.bryionake.constant.Parsers;
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

    public static final FailableParser<OperationId> operationId = Parsers.i64.append(value -> OperationId.valueIsIssuedOperationId(value) ? success(new OperationId(value)) : failure(ChatColor.RED + "指定された操作IDは存在しません。"));

}
