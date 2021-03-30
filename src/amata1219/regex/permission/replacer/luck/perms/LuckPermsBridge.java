package amata1219.regex.permission.replacer.luck.perms;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import org.bukkit.Bukkit;

import java.util.UUID;

public class LuckPermsBridge {

    private static final LuckPerms LUCK_PERMS = LuckPermsProvider.get();

    public static User toUser(UUID playerUniqueId) {
        UserManager manager = LUCK_PERMS.getUserManager();
        if (Bukkit.getPlayer(playerUniqueId).isOnline()) return manager.getUser(playerUniqueId);
        else return manager.loadUser(playerUniqueId).join();
    }

}
