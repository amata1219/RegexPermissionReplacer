package amata1219.regex.permission.replacer.bridge;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.PermissionHolder;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LuckPermsBridge {

    public final LuckPerms luckPerms;

    public LuckPermsBridge(LuckPerms luckPerm) {
        this.luckPerms = luckPerm;
    }

    public User toUser(UUID playerUniqueId) {
        return luckPerms.getUserManager().getUser(playerUniqueId);
    }

    public void replaceUsersPermissions(List<User> users, String regex, String replacement) {
        users.forEach(user -> replacePermissions(user, luckPerms.getUserManager()::saveUser, regex, replacement));
    }

    public void replaceGroupPermissions(Group group, String regex, String replacement) {
        replacePermissions(group, luckPerms.getGroupManager()::saveGroup, regex, replacement);
    }

    private static <T extends PermissionHolder> void replacePermissions(T holder, Consumer<T> save, String regex, String replacement) {
        List<Node> matched = new ArrayList<>();
        List<String> replaced = new ArrayList<>();

        Pattern pattern = Pattern.compile(regex);
        for (Node node : holder.data().toCollection()) {
            Matcher matcher = pattern.matcher(node.getKey());
            if (!matcher.matches()) continue;

            matched.add(node);
            replaced.add(matcher.replaceFirst(replacement));
        }

        matched.forEach(holder.data()::remove);
        replaced.forEach(permission -> holder.data().add(Node.builder(permission).build()));

        save.accept(holder);
    }

}
