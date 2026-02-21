package com.github.shynixn.shyguild.contract

import com.github.shynixn.shyguild.entity.Guild

interface PermissionPluginService {
    /**
     * Creates or updates the permissions for the guild.
     */
    suspend fun createOrUpdatePermissions(guild: Guild)

    /**
     * Creates or updates the permissions for the guild.
     */
    suspend fun deletePermissions(guild: Guild)
}