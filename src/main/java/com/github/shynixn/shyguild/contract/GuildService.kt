package com.github.shynixn.shyguild.contract

import com.github.shynixn.shyguild.entity.Guild
import com.github.shynixn.shyguild.entity.GuildInvite
import org.bukkit.entity.Player

interface GuildService : AutoCloseable {
    /**
     * Gets all guilds of a player.
     */
    suspend fun getGuilds(player: Player): List<Guild>

    /**
     * Gets all cached guilds
     */
    fun getGuildCache(): List<Guild>

    /**
     * Checks the database if the guild already exists.
     */
    suspend fun existsGuild(guildName: String): Boolean

    /**
     * Unloads all guilds of a player if no other player online has those guilds loaded. This should be called when a player leaves the server.
     */
    suspend fun cleanCache(player: Player)

    /**
     * Save the given guild.
     */
    suspend fun saveGuild(guild: Guild)

    /**
     * Deletes an entire guild.
     */
    suspend fun deleteGuild(guild: Guild)

    /**
     * Sends a guild invite.
     */
    suspend fun sendInvite(invite: GuildInvite) : Boolean

    /**
     * Accepts a guild invite if it exists.
     * Returns true if an invite was accepted, false if no invite was found.
     */
    suspend fun acceptInvite(player: Player, name : String) : Boolean
}