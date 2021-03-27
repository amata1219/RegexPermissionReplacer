package amata1219.regex.permission.replacer.operation.target;

import com.google.common.collect.ImmutableSet;

import java.util.UUID;

public interface Target {

    Target ALL = new All();

    class All implements Target {

    }

    class Group implements Target {

        public final String groupName;

        public Group(String groupName) {
            this.groupName = groupName;
        }

    }

    class Players implements Target {

        public final ImmutableSet<UUID> playersUniqueIds;

        public Players(ImmutableSet<UUID> playersUniqueIds) {
            this.playersUniqueIds = playersUniqueIds;
        }

    }

}
