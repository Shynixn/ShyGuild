package com.github.shynixn.shyguild.entity

import com.github.shynixn.mcutils.database.api.PlayerData

class PlayerInformation : PlayerData {
    /**
     *  Marker if this player data has been stored before.
     */
    override var isPersisted: Boolean = false

    /**
     * Name of the player.
     */
    override var playerName: String = ""

    /**
     * UUID of the player.
     */
    override var playerUUID: String = ""

    /**
     * All guilds the player is member of.
     */
    var guilds: MutableSet<String> = HashSet()

    /**
     * Created guilds by the player.
     */
    var createdGuilds: MutableSet<String> = HashSet()
}
