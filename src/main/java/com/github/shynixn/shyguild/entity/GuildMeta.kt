package com.github.shynixn.shyguild.entity

import com.github.shynixn.mcutils.common.repository.Element

class GuildMeta : Element {
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
    var displayNameColor : String = ""

    /**
     * Name of the template.
     */
    var template: String = ""

    /**
     * All guids members of the guild.
     */
    var members: List<GuildMemberMeta> = ArrayList()
}
