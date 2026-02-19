package com.github.shynixn.shyguild.entity

class GuildMemberMeta {
    /**
     * UUID of the player.
     */
    var playerUUID: String = ""

    /**
     * Name of a player.
     */
    var playerName: String = ""

    /**
     * A list of roles.
     */
    var roles = HashSet<String>()
}