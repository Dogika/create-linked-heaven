package com.dogika.lh.client;

import com.dogika.lh.network.GroupEntry;

import java.util.List;

public final class GroupListCache {

    private static volatile List<GroupEntry> current = List.of();

    private GroupListCache() {
    }

    static void update(List<GroupEntry> groups) {
        current = groups;
    }

    public static List<GroupEntry> current() {
        return current;
    }
}
