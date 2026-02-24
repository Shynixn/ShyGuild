package com.github.shynixn.shyguild.impl.service

import com.github.shynixn.mcutils.common.placeholder.PlaceHolderService
import com.github.shynixn.shyguild.ShyGuildPlugin
import com.github.shynixn.shyguild.contract.PermissionPluginService
import com.github.shynixn.shyguild.entity.Guild
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.node.Node
import net.luckperms.api.node.types.InheritanceNode
import org.bukkit.plugin.Plugin
import java.util.*

class LuckPermsPermissionServiceImpl(private val plugin: Plugin, private val placeHolderService: PlaceHolderService) :
    PermissionPluginService {
    private val luckPermsApiGroupManager = LuckPermsProvider.get().groupManager
    private val luckPermsUserManager = LuckPermsProvider.get().userManager

    override suspend fun createOrUpdatePermissions(guild: Guild) {
        val template = guild.template ?: return

        withContext(Dispatchers.IO) {
            val context = mapOf(ShyGuildPlugin.guildNameKey to guild.name)

            for (role in template.roles) {
                val groupName = "${plugin.name.lowercase(Locale.ENGLISH)}-${guild.name}-${role.name}"
                var group = luckPermsApiGroupManager.getGroup(groupName)

                if (group == null) {
                    group = luckPermsApiGroupManager.createAndLoadGroup(groupName).get()
                }

                group!!.data().clear()

                for (permission in role.allowPermissions) {
                    group.data().add(
                        Node.builder(placeHolderService.resolvePlaceHolder(permission, null, context)).value(true)
                            .build()
                    )
                }
                for (permission in role.denyPermissions) {
                    group.data().add(
                        Node.builder(placeHolderService.resolvePlaceHolder(permission, null, context)).value(false)
                            .build()
                    )
                }

                luckPermsApiGroupManager.saveGroup(group).get()
            }
        }
    }

    override suspend fun deletePermissions(guild: Guild) {
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

    override suspend fun applyRoles(playerUUID: UUID, guild: Guild) {
        val member = guild.getMember(playerUUID.toString()) ?: return
        val template = guild.template ?: return

        withContext(Dispatchers.IO) {
            val user = luckPermsUserManager.loadUser(playerUUID).get()

            for (role in template.roles) {
                val groupName = "${plugin.name.lowercase(Locale.ENGLISH)}-${guild.name}-${role.name}"
                val roleNode = InheritanceNode.builder(groupName).build()
                user.data().remove(roleNode)

                if (member.roles.contains(role.name)) {
                    user.data().add(roleNode)
                }
            }

            luckPermsUserManager.saveUser(user).get()
        }
    }
}