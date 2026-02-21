package com.github.shynixn.shyguild.contract

import com.github.shynixn.shyguild.entity.Guild

interface GuildMetaSqlRepository : AutoCloseable {
    /**
     * Creates the database if it does not exist yet.
     */
    fun createIfNotExist()

    /**
     * Saves the given data.
     */
    suspend fun save(data: Guild)

    /**
     * Deletes the given data from the storage.
     */
    suspend fun delete(data: Guild)

    /**
     * Gets the data from the storage by name.
     * Returns null if it does not exist.
     */
    suspend fun getByName(name: String): Guild?

    /**
     * Gets all guilds from the storage.
     */
    suspend fun getAll(): Sequence<Guild>
}