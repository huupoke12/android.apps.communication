package io.github.huupoke12.android.apps.communication.util

import org.matrix.android.sdk.api.provider.RoomDisplayNameFallbackProvider

class DefaultRoomDisplayNameFallbackProvider: RoomDisplayNameFallbackProvider {
    override fun getNameFor1member(name: String): String {
        return name
    }

    override fun getNameFor2members(name1: String, name2: String): String {
        return "$name1 and $name2"
    }

    override fun getNameFor3members(name1: String, name2: String, name3: String): String {
        return "$name1, $name2 and $name3"
    }

    override fun getNameFor4members(
        name1: String,
        name2: String,
        name3: String,
        name4: String
    ): String {
        return "$name1, $name2, $name3 and $name4"
    }

    override fun getNameFor4membersAndMore(
        name1: String,
        name2: String,
        name3: String,
        remainingCount: Int
    ): String {
        return "$name1, $name2, $name3 and $remainingCount other people"
    }

    override fun getNameForEmptyRoom(isDirect: Boolean, leftMemberNames: List<String>): String {
        return "Empty ${if (isDirect) "direct" else "group"} room"
    }

    override fun getNameForRoomInvite(): String {
        return "Room invitation"
    }

}