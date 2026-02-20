package com.github.shynixn.shyguild.contract

import com.github.shynixn.shyguild.entity.GuildMeta

interface PermissionPluginService {
    /**
     * Creates or updates the permissions for the guild.
     */
    suspend fun createOrUpdatePermissions(guild: GuildMeta)

    /**
     * Creates or updates the permissions for the guild.
     */
    suspend fun deletePermissions(guild: GuildMeta)
}