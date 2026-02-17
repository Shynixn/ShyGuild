package com.github.shynixn.shyguild.contract

import org.bukkit.entity.Player

interface GuildService : AutoCloseable {
    /**
     * Gets all guilds of a player.
     */
    suspend fun getGuilds(player: Player): List<Guild>

    /**
     * Unloads all guilds of a player if no other player online has those guilds loaded. This should be called when a player leaves the server.
     */
    suspend fun unloadGuild(player: Player)
}