package com.github.shynixn.shyguild.entity

import com.github.shynixn.fasterxml.jackson.annotation.JsonIgnore
import com.github.shynixn.mcutils.common.repository.Element
import org.bukkit.entity.Player

class Guild : Element {
    /**
     *  Marker if this player data has been stored before.
     */
    var isPersisted: Boolean = false

    /**
     * Unique lowercase Identifier of the guild.
     */
    override var name: String = ""

    /**
     * How the name was created.
     */
    var displayName: String = ""

    /**
     * How the name was created with color codes.
     */
    var displayNameColor: String = ""

    /**
     * Name of the template.
     */
    var templateName: String = ""

    /**
     * All guids members of the guild.
     */
    var members: MutableList<GuildMember> = ArrayList()

    /**
     * Template
     */
    @JsonIgnore
    var template: GuildTemplate? = null

    fun isMember(player: Player): Boolean {
        return getMember(player) != null
    }

    fun getMember(playerNameOrId: String): GuildMember? {
        return members.firstOrNull { e -> e.playerUUID == playerNameOrId || e.playerName == playerNameOrId }
    }

    fun getMember(player: Player): GuildMember? {
        return members.firstOrNull { e -> e.playerUUID == player.uniqueId.toString() }
    }
}
