package com.github.shynixn.shyguild.impl.service

import com.github.shynixn.shyguild.contract.PermissionPluginService
import com.github.shynixn.shyguild.entity.GuildMeta

class EmptyPermissionPluginServiceImpl : PermissionPluginService {
    override suspend fun createOrUpdatePermissions(guild: GuildMeta) {
    }

    override suspend fun deletePermissions(guild: GuildMeta) {
    }
}