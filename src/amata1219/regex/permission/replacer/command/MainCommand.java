package amata1219.regex.permission.replacer.command;

import amata1219.regex.permission.replacer.OperationId;
import amata1219.regex.permission.replacer.Pair;
import amata1219.regex.permission.replacer.bryionake.adt.Either;
import amata1219.regex.permission.replacer.bryionake.constant.Parsers;
import amata1219.regex.permission.replacer.bryionake.dsl.BukkitCommandExecutor;
import amata1219.regex.permission.replacer.bryionake.dsl.context.BranchContext;
import amata1219.regex.permission.replacer.bryionake.dsl.context.CommandContext;
import amata1219.regex.permission.replacer.bryionake.dsl.context.ExecutionContext;
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
import net.luckperms.api.model.data.NodeMap;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MainCommand implements BukkitCommandExecutor {

    private static final LuckPerms LUCK_PERMS = LuckPermsProvider.get();

    private final CommandContext<CommandSender> executor;

    private final OperationRecords operationRecords;

    /*
        非同期化
        .* $0 の無効化
        records.yml
        config.yml redo-section limitation
     */

    private User temp = null;
    private NodeMap nodemap = null;

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

            PermissionReplacer.replaceUsersPermissions(allUsers(), Pattern.compile(regex), replacement);
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

                    PermissionReplacer.replaceGroupPermissions(target, Pattern.compile(regex), replacement);
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

            if (unparsedArguments.isEmpty()) {
                sender.sendMessage(ChatColor.RED + "権限の置き換えの対象とするプレイヤーを指定して下さい。");
                return;
            }

            UUID debug = null;

            List<User> users = new ArrayList<>();
            List<UUID> uuids = new ArrayList<>();
            ImmutableSet.Builder<UUID> builder = ImmutableSet.builder();
            while (!unparsedArguments.isEmpty()) {
                UUID playerUniqueId = debug = UUIDConverter.getUUIDFromNameAsUUID(unparsedArguments.poll(), Bukkit.getOnlineMode());
                users.add(toUser(playerUniqueId));
                builder.add(playerUniqueId);

                uuids.add(playerUniqueId);
            }

            OperationRecord record = new ReplaceOperationRecord(OperationId.issueNewOperationId(), regex, replacement, new Target.Players(builder.build()));
            operationRecords.put(record);

            UUID finalDebug = debug;
            for (UUID uuid : uuids) {
                AtomicReference<User> u1 = new AtomicReference<>();
                AtomicReference<User> u2 = new AtomicReference<>();
                LUCK_PERMS.getUserManager().modifyUser(uuid, user -> {
                    u1.set(user);
                    PermissionReplacer.NODES.forEach(user.data()::add);
                }).join();
                System.out.println("exp saved");
                System.out.println();

                /*
                    RecordedNodeMapのchangesが空の場合saveUserが走らない可能性
                 */

                LUCK_PERMS.getUserManager().modifyUser(uuid, user -> {
                    u2.set(user);
                    PermissionReplacer.experiment(user);
                }).join();
                System.out.println("rpr saved");
                System.out.println();

                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "rpr out amata1219");
                System.out.println("outed");
                System.out.println();

                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "rpr debug amata1219 rpr.24");
                System.out.println("checked");
                System.out.println();

                for (int k = 0; k < 100; k++) {
                    boolean hasExp = Bukkit.getPlayer(finalDebug).hasPermission("exp." + k);
                    boolean hasRpr = Bukkit.getPlayer(finalDebug).hasPermission("rpr." + k);
                    System.out.print("(" + k + ": " + hasExp + ", " + hasRpr + "), ");
                }
                System.out.println();
                System.out.println("looped");
                System.out.println();

                User loaded = LUCK_PERMS.getUserManager().getUser(uuid);
                System.out.println(u1.get() == u2.get());
                System.out.println(u1.get() == loaded);
                System.out.println(u2.get() == loaded);
                System.out.println("compared");

                new ArrayList<>(loaded.data().toCollection()).stream().forEach(node -> {
                    System.out.print(node.getKey() + ": " + node.getValue() + " ");
                });
                System.out.println();
                System.out.println("loaded: outed");

                temp = loaded;
                nodemap = loaded.data();
            }
            /*Bukkit.getScheduler().runTaskAsynchronously(RegexPermissionReplacer.instance(), task -> {
                User user = users.get(0);
                PermissionReplacer.NODES.forEach(user.data()::add);
                LUCK_PERMS.getUserManager().saveUser(user).whenComplete(($, ex) -> {
                    System.out.println("exp saved");
                    //for (int i = 0; i < 1; i++)
                    PermissionReplacer.experiment(user).whenComplete(($$, exx) -> {
                        for (int k = 0; k < 100; k++) {
                            System.out.println(k + ": " + Bukkit.getPlayer(finalDebug).hasPermission("exp." + k) + ", " + Bukkit.getPlayer(finalDebug).hasPermission("rpr." + k));
                        }
                    });
                });
            });*/
            //PermissionReplacer.replaceUsersPermissions(users, Pattern.compile(regex), replacement);
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

        CommandContext<CommandSender> debug = define(
                () -> "/rpr debug <player-name> <permission>",
                (sender, unparsedArguments, parsedArguments) -> {
                    Player player = parsedArguments.poll();
                    String permission = parsedArguments.poll();
                    player.sendMessage(String.valueOf(player.hasPermission(permission)));
                },
                Parsers.player,
                Parsers.str
        );

        CommandContext<CommandSender> out = define(
                () -> "/rpr debug <player-name> <permission>",
                (sender, unparsedArguments, parsedArguments) -> {
                    Player player = parsedArguments.poll();
                    User user = LUCK_PERMS.getUserManager().getUser(player.getUniqueId());
                    NodeMap data = user.data();
                    new ArrayList<>(data.toCollection()).stream().forEach(node -> {
                        System.out.print(node.getKey() + ": " + node.getValue() + " ");
                    });
                    System.out.println("cmd-outed");
                    System.out.println(temp == user);
                    System.out.println(nodemap == data);
                    System.out.println("cmd-compared");
                },
                Parsers.player
        );

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
                bind("redo", redo),
                bind("debug", debug),
                bind("out", out)
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

    private BiConsumer<Pattern, String> correspondingReplaceMethod(ReplaceOperationRecord targetOperationRecord) {
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
        correspondingReplaceMethod(targetOperationRecord).accept(Pattern.compile(swapped.right), swapped.left);
    }

    private void redo(ReplaceOperationRecord targetOperationRecord) {
        RedoOperationRecord record = new RedoOperationRecord(OperationId.issueNewOperationId(), targetOperationRecord);
        operationRecords.put(record);

        correspondingReplaceMethod(targetOperationRecord).accept(Pattern.compile(targetOperationRecord.regex), targetOperationRecord.replacement);
    }

}
