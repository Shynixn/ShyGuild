package com.github.shynixn.shyguild.entity

import com.github.shynixn.mcutils.common.repository.Element

class GuildTemplate : Element {
    /**
     * Unique Identifier of the element.
     */
    override var name: String = ""

    /**
     * List of roles.
     */
    var roles: List<GuildRoleTemplate> = ArrayList()

    var defaultRole : String = ""

    /**
     * Maximum amount of players that can be in the guild.
     */
    var maxPlayers : Int = 10
}
