package com.github.shynixn.shyguild.impl.service

import com.github.shynixn.shyguild.contract.PermissionPluginService
import com.github.shynixn.shyguild.entity.Guild

class EmptyPermissionPluginServiceImpl : PermissionPluginService {
    override suspend fun createOrUpdatePermissions(guild: Guild) {
    }

    override suspend fun deletePermissions(guild: Guild) {
    }
}