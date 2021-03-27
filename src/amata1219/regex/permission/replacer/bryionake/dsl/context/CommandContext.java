package amata1219.regex.permission.replacer.bryionake.dsl.context;

import amata1219.regex.permission.replacer.bryionake.dsl.argument.ParsedArgumentQueue;
import org.bukkit.command.CommandSender;

import java.util.Queue;

public interface CommandContext<S extends CommandSender> {

    void execute(S sender, Queue<String> unparsedArguments, ParsedArgumentQueue parsedArguments);

}
