package amata1219.regex.permission.replacer.command;

import amata1219.regex.permission.replacer.Pair;
import amata1219.regex.permission.replacer.luck.perms.LuckPermsBridge;
import amata1219.regex.permission.replacer.bryionake.adt.Either;
import amata1219.regex.permission.replacer.bryionake.constant.Parsers;
import amata1219.regex.permission.replacer.bryionake.dsl.BukkitCommandExecutor;
import amata1219.regex.permission.replacer.bryionake.dsl.context.BranchContext;
import amata1219.regex.permission.replacer.bryionake.dsl.context.CommandContext;
import amata1219.regex.permission.replacer.bryionake.dsl.context.ExecutionContext;
import amata1219.regex.permission.replacer.OperationId;
import amata1219.regex.permission.replacer.config.MainConfig;
import amata1219.regex.permission.replacer.luck.perms.PermissionReplacer;
import amata1219.regex.permission.replacer.operation.record.*;
import amata1219.regex.permission.replacer.operation.target.Target;
import amata1219.regex.permission.replacer.regex.RegexOperations;
import at.pcgamingfreaks.UUIDConverter;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainCommand implements BukkitCommandExecutor {

    private static final LuckPerms LUCK_PERMS = LuckPermsProvider.get();

    private final CommandContext<CommandSender> executor;

    private final OperationRecords operationRecords;

    /*
        非同期化
     */

    public MainCommand(MainConfig config, OperationRecords operationRecords) {
        this.operationRecords = operationRecords;

        CommandContext<CommandSender> all = (sender, unparsedArguments, parsedArguments) -> {
            String regex = parsedArguments.poll();
            String replacement = parsedArguments.poll();
            if (!RegexOperations.checkSyntax(regex, replacement)) {
                sender.sendMessage(ChatColor.RED + "正規表現と置き換える権限の文法に誤りがあるため実行できませんでした。");
                return;
            }

            OperationRecord record = new ReplaceOperationRecord(OperationId.issueNewOperationId(), regex, replacement, Target.ALL);
            operationRecords.put(record);

            PermissionReplacer.replaceUsersPermissions(allUsers(), regex, replacement);
        };

        ExecutionContext<CommandSender> group = define(
                () -> join(
                        ChatColor.RED + "コマンドが正しくありませんでした。",
                        ChatColor.GRAY + "/rpr replace [正規表現] [置き換える権限] -group [グループ名]"
                ),
                (sender, unparsedArguments, parsedArguments) -> {
                    String regex = parsedArguments.poll();
                    String replacement = parsedArguments.poll();
                    if (!RegexOperations.checkSyntax(regex, replacement)) {
                        sender.sendMessage(ChatColor.RED + "正規表現と置き換える権限の文法に誤りがあるため実行できませんでした。");
                        return;
                    }

                    Group target = parsedArguments.poll();

                    OperationRecord record = new ReplaceOperationRecord(OperationId.issueNewOperationId(), regex, replacement, new Target.Group(target.getName()));
                    operationRecords.put(record);

                    PermissionReplacer.replaceGroupPermissions(target, regex, replacement);
                },
                ParserTemplates.group
        );

        CommandContext<CommandSender> players = (sender, unparsedArguments, parsedArguments) -> {
            String regex = parsedArguments.poll();
            String replacement = parsedArguments.poll();
            if (!RegexOperations.checkSyntax(regex, replacement)) {
                sender.sendMessage(ChatColor.RED + "正規表現と置き換える権限の文法に誤りがあるため実行できませんでした。");
                return;
            }

            List<User> users = new ArrayList<>();
            ImmutableSet.Builder<UUID> builder = ImmutableSet.builder();
            while (!unparsedArguments.isEmpty()) {
                UUID playerUniqueId = UUIDConverter.getUUIDFromNameAsUUID(parsedArguments.poll(), Bukkit.getOnlineMode());
                users.add(toUser(playerUniqueId));
                builder.add(playerUniqueId);
            }

            OperationRecord record = new ReplaceOperationRecord(OperationId.issueNewOperationId(), regex, replacement, new Target.Players(builder.build()));
            operationRecords.put(record);

            PermissionReplacer.replaceUsersPermissions(users, regex, replacement);
        };

        BranchContext<CommandSender> targetsBranches = define(
                () -> join(
                        ChatColor.RED + "正しいコマンドが入力されなかったため実行できませんでした。",
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
            List<ReplaceOperationRecord> targetOperationRecords = new ArrayList<>();
            if (unparsedArguments.isEmpty()) {
                if (operationRecords.operationRecords.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + "まだ一度も置換操作が行われていないためundo処理を実行できませんでした。");
                    return;
                }

                OperationRecord targetOperationRecord = operationRecords.operationRecords.firstEntry().getValue();
                if (targetOperationRecord instanceof UndoOperationRecord) {
                    sender.sendMessage(ChatColor.RED + "undo操作をundoすることはできません。");
                    return;
                }

                targetOperationRecords.add((ReplaceOperationRecord) targetOperationRecord);
            } else {
                Either<String, OperationId> result = ParserTemplates.operationId.tryParse(unparsedArguments.poll());
                if (result instanceof Either.Failure) {
                    String error = ((Either.Failure<String, OperationId>) result).error;
                    sender.sendMessage(error);
                    return;
                }

                OperationId targetOperationId = ((Either.Success<String, OperationId>) result).value;
                int count = 0;
                int undoLimitAtOneTime = config.undoSection.undoLimitAtOneTime();
                for (OperationRecord record : operationRecords.operationRecords.values()) {
                    if (record instanceof UndoOperationRecord) continue;

                    targetOperationRecords.add(extractRootRecord(record));

                    if (record.id == targetOperationId) break;

                    if (count++ >= undoLimitAtOneTime) {
                        message(
                                sender,
                                ChatColor.RED + "undo処理の対象となる置換操作の数(" + targetOperationRecords.size() + ")が多すぎて実行できませんでした。",
                                ChatColor.RED + "一度にundoする置換操作の数Nは、0 < N ≦ " + undoLimitAtOneTime + " の間になるよう指定して下さい。"
                        );
                        return;
                    }
                }

                if (targetOperationRecords.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + "まだ一度も置換操作が行われていないためundo処理を実行することができませんでした。");
                    return;
                }
            }

            targetOperationRecords.forEach(this::undo);
        };

        CommandContext<CommandSender> redo = (sender, unparsedArguments, parsedArguments) -> {
            List<ReplaceOperationRecord> targetOperationRecords = new ArrayList<>();
            if (unparsedArguments.isEmpty()) {
                if (operationRecords.operationRecords.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + "まだ一度もundo操作が行われていないためredo処理を実行できませんでした。");
                    return;
                }

                OperationRecord targetOperationRecord = operationRecords.operationRecords.firstEntry().getValue();
                if (!(targetOperationRecord instanceof UndoOperationRecord)) {
                    sender.sendMessage(ChatColor.RED + "undo以外の操作をredoすることはできません。");
                    return;
                }

                targetOperationRecords.add(extractRootRecord(targetOperationRecord));
            } else {
                Either<String, OperationId> result = ParserTemplates.operationId.tryParse(unparsedArguments.poll());
                if (result instanceof Either.Failure) {
                    String error = ((Either.Failure<String, OperationId>) result).error;
                    sender.sendMessage(error);
                    return;
                }

                OperationId targetOperationId = ((Either.Success<String, OperationId>) result).value;
                int count = 0;
                int undoLimitAtOneTime = config.undoSection.undoLimitAtOneTime();
                for (OperationRecord record : operationRecords.operationRecords.values()) {
                    if (record instanceof ReplaceOperationRecord) break;
                    else if (record instanceof RedoOperationRecord) continue;

                    targetOperationRecords.add(extractRootRecord(record));

                    if (record.id == targetOperationId) break;

                    if (count++ >= undoLimitAtOneTime) {
                        message(
                                sender,
                                ChatColor.RED + "undo処理の対象となる置換操作の数(" + targetOperationRecords.size() + ")が多すぎて実行できませんでした。",
                                ChatColor.RED + "一度にundoする置換操作の数Nは、0 < N ≦ " + undoLimitAtOneTime + " の間になるよう指定して下さい。"
                        );
                        return;
                    }
                }

                if (targetOperationRecords.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + "まだ一度も置換操作が行われていないためundo処理を実行することができませんでした。");
                    return;
                }
            }

            targetOperationRecords.forEach(this::redo);
        };

        this.executor = define(
                () -> join(
                        ChatColor.RED + "正しいコマンドが入力されなかったため実行できませんでした。",
                        ChatColor.GRAY + "/rpr replace [正規表現] [置き換える権限] -all",
                        ChatColor.GRAY + "/rpr replace [正規表現] [置き換える権限] -group [グループ名]",
                        ChatColor.GRAY + "/rpr replace [正規表現] [置き換える権限] -players [プレイヤー名1] [プレイヤー名2] …… [プレイヤー名N]",
                        ChatColor.GRAY + "/rpr undo ([操作ID])",
                        ChatColor.GRAY + "/rpr redo ([操作ID])"
                ),
                bind("replace", replace),
                bind("undo", undo),
                bind("redo", redo)
        );
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

    private static List<User> allUsers() {
        return Arrays.stream(Bukkit.getOfflinePlayers())
                .map(OfflinePlayer::getUniqueId)
                .map(MainCommand::toUser)
                .collect(Collectors.toList());
    }

    private static User toUser(UUID playerUniqueId) {
        UserManager manager = LUCK_PERMS.getUserManager();
        return manager.isLoaded(playerUniqueId) ? manager.getUser(playerUniqueId) : manager.loadUser(playerUniqueId).join();
    }

    private static ReplaceOperationRecord extractRootRecord(OperationRecord record) {
        if (record instanceof UndoOperationRecord) return extractRootRecord(((UndoOperationRecord) record).operationUndone);
        else if (record instanceof RedoOperationRecord) return extractRootRecord(((RedoOperationRecord) record).operationRedone);
        else return (ReplaceOperationRecord) record;
    }

    private BiConsumer<String, String> correspondingReplaceMethod(ReplaceOperationRecord targetOperationRecord) {
        Target target = targetOperationRecord.target;
        if (target instanceof Target.All) {
            return (regex, replacement) -> PermissionReplacer.replaceUsersPermissions(allUsers(), regex, replacement);
        } else if (target instanceof Target.Group) {
            Group targetGroup = LUCK_PERMS.getGroupManager().getGroup(((Target.Group) target).groupName);
            return (regex, replacement) -> PermissionReplacer.replaceGroupPermissions(targetGroup, regex, replacement);
        } else if (target instanceof Target.Players) {
            List<User> users = ((Target.Players) target).playersUniqueIds.stream()
                    .map(MainCommand::toUser)
                    .collect(Collectors.toList());
            return (regex, replacement) -> PermissionReplacer.replaceUsersPermissions(users, regex, replacement);
        } else {
            throw new IllegalStateException("Target class is not shielded.");
        }
    }

    private void undo(ReplaceOperationRecord targetOperationRecord) {
        UndoOperationRecord record = new UndoOperationRecord(OperationId.issueNewOperationId(), targetOperationRecord);
        operationRecords.put(record);

        Pair<String, String> swapped = RegexOperations.swap(targetOperationRecord.regex, targetOperationRecord.replacement);
        correspondingReplaceMethod(targetOperationRecord).accept(swapped.right, swapped.left);
    }

    private void redo(ReplaceOperationRecord targetOperationRecord) {
        UndoOperationRecord record = new UndoOperationRecord(OperationId.issueNewOperationId(), targetOperationRecord);
        operationRecords.put(record);

        correspondingReplaceMethod(targetOperationRecord).accept(targetOperationRecord.regex, targetOperationRecord.replacement);
    }

}
