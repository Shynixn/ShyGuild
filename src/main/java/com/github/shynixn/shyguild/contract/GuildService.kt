package com.github.shynixn.shyguild.contract

import com.github.shynixn.shyguild.entity.GuildMeta
import org.bukkit.entity.Player

interface GuildService : AutoCloseable {
    /**
     * Gets all guilds of a player.
     */
    suspend fun getGuilds(player: Player): List<GuildMeta>

    /**
     * Unloads all guilds of a player if no other player online has those guilds loaded. This should be called when a player leaves the server.
     */
    suspend fun cleanCache(player: Player)
}