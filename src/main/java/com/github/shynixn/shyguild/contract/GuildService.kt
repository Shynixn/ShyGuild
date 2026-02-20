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

    /**
     * Save the given guild.
     */
    suspend fun saveGuild(guild: GuildMeta)

    /**
     * Deletes an entire guild.
     */
    suspend fun deleteGuild(owner : Player, guild: GuildMeta)

    /**
     * Refreshes permissions and template cache for the given guild.
     */
    suspend fun refreshGuild(guild: GuildMeta)
}