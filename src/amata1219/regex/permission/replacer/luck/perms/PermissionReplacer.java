package amata1219.regex.permission.replacer.luck.perms;

import com.google.common.collect.ImmutableList;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.PermissionHolder;
import net.luckperms.api.model.data.DataType;
import net.luckperms.api.model.data.NodeMap;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PermissionReplacer {

    private static final LuckPerms LUCK_PERMS = LuckPermsProvider.get();

    public static void replaceUsersPermissions(List<User> users, Pattern regex, String replacement) {
        users.forEach(user -> replacePermissions(user, LUCK_PERMS.getUserManager()::saveUser, regex, replacement));
    }

    public static void replaceGroupPermissions(Group group, Pattern regex, String replacement) {
        replacePermissions(group, LUCK_PERMS.getGroupManager()::saveGroup, regex, replacement);
    }

    /*
        AbstractNode.key, .resolvedShorthand, calculateHashCode()
     */

    public static final Field key, resolvedShorthand, hashCode, delegate;
    public static final Method calculateHashCode, expandShorthand, normalData, discardChanges;

    static {
        Field field1 = null, field2 = null, field3 = null, field4 = null;
        Method method1 = null, method2 = null, method3 = null, method4 = null;

        try {
            Class<?> AbstractNode = Node.builder("").build().getClass().getSuperclass();
                    //Class.forName("me.lucko.luckperms.common.node.AbstractNode", false, BukkitLoaderPlugin.class.getClassLoader());
                    //Class.forName("me.lucko.luckperms.common.node.AbstractNode");
            field1 = AbstractNode.getDeclaredField("key");
            field1.setAccessible(true);

            field2 = AbstractNode.getDeclaredField("resolvedShorthand");
            field2.setAccessible(true);

            field3 = AbstractNode.getDeclaredField("hashCode");
            field3.setAccessible(true);

            method1 = AbstractNode.getDeclaredMethod("calculateHashCode");
            method1.setAccessible(true);

            Class<?> ShorthandParser = Class.forName("me.lucko.luckperms.common.node.utils.ShorthandParser", false, AbstractNode.getClassLoader());

            method2 = ShorthandParser.getDeclaredMethod("expandShorthand", String.class);
            method2.setAccessible(true);

            Class<?> PermissionHolder = Class.forName("me.lucko.luckperms.common.model.PermissionHolder", false, AbstractNode.getClassLoader());

            method3 = PermissionHolder.getDeclaredMethod("normalData");
            method3.setAccessible(true);

            Class<?> RecordedNodeMap = Class.forName("me.lucko.luckperms.common.model.nodemap.RecordedNodeMap", false, AbstractNode.getClassLoader());

            field4 = RecordedNodeMap.getDeclaredField("delegate");
            field4.setAccessible(true);

            method4 = RecordedNodeMap.getDeclaredMethod("discardChanges");
            method4.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        key = field1;
        resolvedShorthand = field2;
        hashCode = field3;
        calculateHashCode = method1;
        expandShorthand = method2;
        normalData = method3;
        delegate = field4;
        discardChanges = method4;
    }

    public static final Pattern PATTERN = Pattern.compile("exp\\.([0-9]+)");
    public static final List<Node> NODES = IntStream.range(0, 100)
            .mapToObj(i -> "exp." + i)
            .map(Node::builder)
            .map(NodeBuilder::build)
            .collect(Collectors.toList());

    public static CompletableFuture<Void> experiment(User user) {
        NodeMap nodeMap = user.data();
        for (Node node : nodeMap.toCollection()) {
            Matcher matcher = PATTERN.matcher(node.getKey());
            if (!matcher.matches()) continue;

            try {
                String perm = matcher.replaceFirst("rpr.$1");
                key.set(node, perm);
                resolvedShorthand.set(node, ImmutableList.copyOf((Set<String>) expandShorthand.invoke(null, perm)));
                hashCode.set(node, calculateHashCode.invoke(node));
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        user.getCachedData().invalidate();
        user.getCachedData().invalidatePermissionCalculators();
        user.getCachedData().getPermissionData().invalidateCache();
        user.getCachedData().permissionData().recalculate();



        /*CompletableFuture<Void> save = LUCK_PERMS.getUserManager().saveUser(user).whenComplete(($, ex) -> {
            System.out.println("rpr saved");

            System.out.println("DOUBLE");
            System.out.println(user.data().toCollection());
            Object nData;
            try {
                nData = normalData.invoke(user);
                Object del = delegate.get(nData);
                System.out.println(del);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });*/

        /*Object nData;
        try {
            nData = normalData.invoke(user);
            Object del = delegate.get(nData);
            System.out.println(del);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }*/

        return null;
    }

    private static <T extends PermissionHolder> void replacePermissions(T holder, Consumer<T> save, Pattern regex, String replacement) {
        List<Node> matched = new ArrayList<>();
        List<String> replaced = new ArrayList<>();

        for (Node node : holder.data().toCollection()) {
            Matcher matcher = regex.matcher(node.getKey());
            if (!matcher.matches()) continue;

            matched.add(node);
            replaced.add(matcher.replaceFirst(replacement));

            //System.out.println("matched: " + node + " replaced: " + matcher.replaceFirst(replacement));
        }

        matched.forEach(holder.data()::remove);
        replaced.forEach(permission -> holder.data().add(Node.builder(permission).build()));

        if (matched.isEmpty()) return;

        save.accept(holder);
    }

}
