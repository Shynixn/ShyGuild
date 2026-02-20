package com.github.shynixn.shyguild.impl.service

import com.github.shynixn.shyguild.contract.PermissionPluginService
import com.github.shynixn.shyguild.entity.GuildMeta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.node.Node
import org.bukkit.plugin.Plugin
import java.util.Locale

class LuckPermsPermissionServiceImpl(private val plugin: Plugin) : PermissionPluginService {
    private val luckPermsApiGroupManager = LuckPermsProvider.get().groupManager

    override suspend fun createOrUpdatePermissions(guild: GuildMeta) {
        val template = guild.template ?: return

        withContext(Dispatchers.IO) {
            for (role in template.roles) {
                val groupName = "${plugin.name.lowercase(Locale.ENGLISH)}-${guild.name}-${role.name}"
                var group = luckPermsApiGroupManager.getGroup(groupName)

                if (group != null) {
                    group = luckPermsApiGroupManager.createAndLoadGroup(groupName).get()
                }

                group!!.data().clear()

                for (permission in role.allowPermission) {
                    group.data().add(Node.builder(permission).value(true).build())
                }
                for (permission in role.denyPermission) {
                    group.data().add(Node.builder(permission).value(false).build())
                }

                val roleModification = guild.roleModifications.firstOrNull { e -> e.name == role.name }
                if (roleModification != null) {
                    for (permission in roleModification.allowPermission) {
                        group.data().add(Node.builder(permission).value(true).build())
                    }
                    for (permission in roleModification.denyPermission) {
                        group.data().add(Node.builder(permission).value(false).build())
                    }
                }

                luckPermsApiGroupManager.saveGroup(group).get()
            }
        }
    }

    override suspend fun deletePermissions(guild: GuildMeta) {
        val template = guild.template ?: return

        withContext(Dispatchers.IO) {
            for (role in template.roles) {
                val groupName = "${plugin.name.lowercase(Locale.ENGLISH)}-${guild.name}-${role.name}"
                val group = luckPermsApiGroupManager.getGroup(groupName)

                if (group != null) {
                    luckPermsApiGroupManager.deleteGroup(group).get()
                }
            }
        }
    }
}