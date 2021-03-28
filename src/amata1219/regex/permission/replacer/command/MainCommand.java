package amata1219.regex.permission.replacer.command;

import amata1219.regex.permission.replacer.RegexPermissionReplacer;
import amata1219.regex.permission.replacer.bridge.LuckPermsBridge;
import amata1219.regex.permission.replacer.bryionake.adt.Either;
import amata1219.regex.permission.replacer.bryionake.constant.Parsers;
import amata1219.regex.permission.replacer.bryionake.dsl.BukkitCommandExecutor;
import amata1219.regex.permission.replacer.bryionake.dsl.context.BranchContext;
import amata1219.regex.permission.replacer.bryionake.dsl.context.CommandContext;
import amata1219.regex.permission.replacer.bryionake.dsl.context.ExecutionContext;
import amata1219.regex.permission.replacer.OperationId;
import amata1219.regex.permission.replacer.config.MainConfig;
import amata1219.regex.permission.replacer.operation.record.OperationRecord;
import amata1219.regex.permission.replacer.operation.record.ReplaceOperationRecord;
import amata1219.regex.permission.replacer.operation.target.Target;
import amata1219.regex.permission.replacer.regex.RegexOperations;
import at.pcgamingfreaks.UUIDConverter;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.stream.Collectors;

public class MainCommand implements BukkitCommandExecutor {

    private final CommandContext<CommandSender> executor = null;

    private final MainConfig config;
    private final TreeMap<OperationId, OperationRecord> operationRecords;

    public MainCommand(MainConfig config, TreeMap<OperationId, OperationRecord> records) {
        this.config = config;
        operationRecords = records;

        LuckPermsBridge luckPermsBridge = RegexPermissionReplacer.instance().luckPermsBridge();

        CommandContext<CommandSender> all = (sender, unparsedArguments, parsedArguments) -> {
            String regex = parsedArguments.poll();
            String replacement = parsedArguments.poll();
            if (RegexOperations.checkSyntax(regex, replacement)) {
                sender.sendMessage(ChatColor.RED + "正規表現と置き換える権限の文法に誤りが含まれていたため実行できませんでした。");
                return;
            }

            List<User> users = Arrays.stream(Bukkit.getOfflinePlayers())
                    .map(OfflinePlayer::getUniqueId)
                    .map(luckPermsBridge::toUser)
                    .collect(Collectors.toList());

            OperationRecord record = new ReplaceOperationRecord(OperationId.issueNewOperationId(), regex, replacement, Target.ALL);
            operationRecords.put(record.id, record);

            luckPermsBridge.replaceUsersPermissions(users, regex, replacement);
        };

        ExecutionContext<CommandSender> group = define(
                () -> join(
                        ChatColor.RED + "コマンドが正しくありませんでした。",
                        ChatColor.GRAY + "/rpr replace [正規表現] [置き換える権限] -group [グループ名]"
                ),
                (sender, unparsedArguments, parsedArguments) -> {
                    String regex = parsedArguments.poll();
                    String replacement = parsedArguments.poll();
                    if (RegexOperations.checkSyntax(regex, replacement)) {
                        sender.sendMessage(ChatColor.RED + "正規表現と置き換える権限の文法に誤りが含まれていたため実行できませんでした。");
                        return;
                    }

                    Group target = parsedArguments.poll();

                    OperationRecord record = new ReplaceOperationRecord(OperationId.issueNewOperationId(), regex, replacement, new Target.Group(target.getName()));
                    operationRecords.put(record.id, record);

                    luckPermsBridge.replaceGroupPermissions(target, regex, replacement);
                },
                ParserTemplates.group
        );

        CommandContext<CommandSender> players = (sender, unparsedArguments, parsedArguments) -> {
            String regex = parsedArguments.poll();
            String replacement = parsedArguments.poll();
            if (RegexOperations.checkSyntax(regex, replacement)) {
                sender.sendMessage(ChatColor.RED + "正規表現と置き換える権限の文法に誤りが含まれていたため実行できませんでした。");
                return;
            }

            ImmutableSet.Builder<UUID> builder = ImmutableSet.builder();
            List<User> users = new ArrayList<>();
            while (!unparsedArguments.isEmpty()) {
                UUID playerUniqueId = UUIDConverter.getUUIDFromNameAsUUID(unparsedArguments.poll(), Bukkit.getOnlineMode());
                builder.add(playerUniqueId);
                users.add(luckPermsBridge.toUser(playerUniqueId));
            }

            OperationRecord record = new ReplaceOperationRecord(OperationId.issueNewOperationId(), regex, replacement, new Target.Players(builder.build()));
            operationRecords.put(record.id, record);

            luckPermsBridge.replaceUsersPermissions(users, regex, replacement);
        };

        BranchContext<CommandSender> targetsBranches = define(
                () -> join(
                        ChatColor.RED + "コマンドが正しくありませんでした。",
                        ChatColor.GRAY + "/rpr replace [正規表現] [置き換える権限] -all",
                        ChatColor.GRAY + "/rpr replace [正規表現] [置き換える権限] -group [グループ名]",
                        ChatColor.GRAY + "/rpr replace [正規表現] [置き換える権限] -players [プレイヤー名1] [プレイヤー名2] …… [プレイヤー名N]"
                ),
                bind("-all", all),
                bind("-group", group),
                bind("-players", players)
        );

        ExecutionContext<CommandSender> replace = define(
                () -> join(
                        ChatColor.GRAY + "/kari replace [正規表現] [置き換える権限] [ターゲット指定子] [引数…]"
                ),
                targetsBranches,
                Parsers.str,
                Parsers.str
        );

        CommandContext<CommandSender> undo = (sender, unparsedArguments, parsedArguments) -> {
            OperationId targetOperationId;
            if (unparsedArguments.isEmpty()) {
                if (operationRecords.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + "また一度も置換操作が行われていないため、undo処理を実行できませんでした。");
                    return;
                }
                targetOperationId = operationRecords.lastKey();
            } else {
                Either<String, OperationId> result = ParserTemplates.operationId.tryParse(unparsedArguments.poll());
                if (result instanceof Either.Failure) {
                    String error = ((Either.Failure<String, OperationId>) result).error;
                    sender.sendMessage(error);
                    return;
                }
                targetOperationId = ((Either.Success<String, OperationId>) result).value;
            }

            SortedMap<OperationId, OperationRecord> targetOperationRecords = operationRecords.tailMap(targetOperationId);
            int undoLimitAtOneTime = config.undoSection.undoLimitAtOneTime();
            if (targetOperationRecords.size() > undoLimitAtOneTime) {
                message(
                        sender,
                        ChatColor.RED + "undo処理の対象となる操作の数(" + targetOperationRecords.size() + ")が多すぎて実行できませんでした。",
                        ChatColor.RED + "一度にundoする操作の数Nは、0 < N ≦ " + undoLimitAtOneTime + " の間になるよう指定して下さい。"
                );
                return;
            }

        };
    }

    @Override
    public CommandContext<CommandSender> executor() {
        return executor;
    }

    private static String join(String... parts) {
        return Joiner.on("\n").join(parts);
    }

    private static void message(CommandSender sender, String... lines) {
        sender.sendMessage(lines);
    }

}
