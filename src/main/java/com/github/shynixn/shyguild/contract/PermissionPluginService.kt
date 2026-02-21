package com.github.shynixn.shyguild.contract

import com.github.shynixn.shyguild.entity.Guild
import java.util.UUID

interface PermissionPluginService {
    /**
     * Creates or updates the permissions for the guild.
     */
    suspend fun createOrUpdatePermissions(guild: Guild)

    /**
     * Creates or updates the permissions for the guild.
     */
    suspend fun deletePermissions(guild: Guild)

    /**
     * Applies the guild member roles.
     */
    suspend fun applyRoles(playerUUID: UUID, guild: Guild)
}