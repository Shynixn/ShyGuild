package com.github.shynixn.shyguild.impl.service

import com.github.shynixn.mcutils.common.CoroutineHandler
import com.github.shynixn.mcutils.common.repository.CacheRepository
import com.github.shynixn.mcutils.database.api.CachePlayerRepository
import com.github.shynixn.shyguild.contract.GuildMetaSqlRepository
import com.github.shynixn.shyguild.contract.GuildService
import com.github.shynixn.shyguild.contract.PermissionPluginService
import com.github.shynixn.shyguild.entity.Guild
import com.github.shynixn.shyguild.entity.GuildInvite
import com.github.shynixn.shyguild.entity.GuildTemplate
import com.github.shynixn.shyguild.entity.PlayerInformation
import com.github.shynixn.shyguild.entity.ShyGuildSettings
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.Date
import java.util.UUID
import java.util.logging.Level

class GuildServiceImpl(
    private val plugin: Plugin,
    private val settings: ShyGuildSettings,
    coroutineHandler: CoroutineHandler,
    private val guildMetaSqlRepository: GuildMetaSqlRepository,
    private val cachePlayerDataRepository: CachePlayerRepository<PlayerInformation>,
    private val permissionPluginService: PermissionPluginService,
    private val templateService: CacheRepository<GuildTemplate>
) : GuildService {
    private var isDisposed = false
    private var guilds = HashMap<String, Guild>()
    private var sentInvites = HashMap<UUID, MutableList<GuildInvite>>()
    private var receivedInvites = HashMap<UUID, MutableList<GuildInvite>>()

    init {
        coroutineHandler.execute {
            while (!isDisposed) {
                val guildNames = guilds.keys.toList()
                val newGuilds = HashMap<String, Guild>()
                for (guildName in guildNames) {
                    val newGuildData = guildMetaSqlRepository.getByName(guildName)

                    if (newGuildData == null) {
                        continue
                    }

                    newGuilds[guildName] = newGuildData
                }

                // Check if the guild is still in the cache and only then update it.
                for (newGuild in newGuilds) {
                    if (guilds.containsKey(newGuild.key)) {
                        newGuild.value.template = templateService.getAll().firstOrNull { e -> e.name == newGuild.value.templateName }
                        guilds[newGuild.key] = newGuild.value
                    }
                }
                delay(1000L * settings.synchronizeGuildsSeconds)
            }
        }
    }

    override suspend fun getGuilds(player: Player): List<Guild> {
        var playerInfo = cachePlayerDataRepository.getByPlayer(player)

        if (playerInfo == null) {
            playerInfo = PlayerInformation().also {
                it.playerName = player.name
                it.playerUUID = player.uniqueId.toString()
            }
            cachePlayerDataRepository.save(playerInfo)
        }

        val result = ArrayList<Guild>()
        val guildNames = HashSet<String>()
        guildNames.addAll(playerInfo.guilds)

        for (guildName in guildNames) {
            if (guilds.containsKey(guildName)) {
                result.add(guilds[guildName]!!)
                continue
            }

            val loadedGuild = guildMetaSqlRepository.getByName(guildName)
            if (loadedGuild != null) {
                refreshGuild(loadedGuild)
                result.add(loadedGuild)
            } else {
                // Guild was deleted.
                playerInfo.guilds.remove(guildName)
                cachePlayerDataRepository.save(playerInfo)
            }
        }

        return result
    }

    override fun getGuildCache(): List<Guild> {
        return guilds.values.toList()
    }

    override suspend fun existsGuild(guildName: String): Boolean {
        if (guilds.containsKey(guildName)) {
            return true
        }

        return guildMetaSqlRepository.getByName(guildName) != null
    }

    override suspend fun cleanCache(player: Player) {
        val playerInfo = cachePlayerDataRepository.getByPlayer(player) ?: return
        val onlinePlayers = Bukkit.getOnlinePlayers().toHashSet()
        for (guildName in playerInfo.guilds) {
            val loadedGuild = guilds[guildName] ?: continue

            var areOtherGuildMembersOnline = false

            for (onlinePlayer in onlinePlayers) {
                val playerUUID = onlinePlayer.uniqueId.toString()

                if (playerUUID == playerInfo.playerUUID) {
                    continue
                }

                val guildMember = loadedGuild.members.firstOrNull { e -> e.playerUUID == playerUUID }

                if (guildMember != null) {
                    areOtherGuildMembersOnline = true
                }
            }

            if (!areOtherGuildMembersOnline) {
                guilds.remove(guildName)
            }
        }

        sentInvites.remove(player.uniqueId)
        receivedInvites.remove(player.uniqueId)
        cachePlayerDataRepository.save(playerInfo)
        cachePlayerDataRepository.clearByPlayer(player)
    }

    override suspend fun saveGuild(guild: Guild) {
        if (!guilds.containsKey(guild.name)) {
            refreshGuild(guild)
        }

        guildMetaSqlRepository.save(guild)
    }

    override suspend fun deleteGuild(
        guild: Guild
    ) {
        guilds.remove(guild.name)
        try {
            permissionPluginService.deletePermissions(guild)
        } catch (e: Exception) {
            plugin.logger.log(Level.SEVERE, "Failed to communicate with permission plugin!", e)
        }
        guildMetaSqlRepository.delete(guild)
    }

    override suspend fun applyGuildMemberPermissions(
        playerUUID: UUID,
        guild: Guild
    ) {
        try {
            permissionPluginService.applyRoles(playerUUID, guild)
        } catch (e: Exception) {
            plugin.logger.log(Level.SEVERE, "Failed to communicate with permission plugin!", e)
        }
    }

    private suspend fun refreshGuild(guild: Guild) {
        guild.template = templateService.getAll().firstOrNull { e -> e.name == guild.templateName }
        guilds[guild.name] = guild
        try {
            permissionPluginService.createOrUpdatePermissions(guild)
        } catch (e: Exception) {
            plugin.logger.log(Level.SEVERE, "Failed to communicate with permission plugin!", e)
        }
    }

    override suspend fun sendInvite(invite: GuildInvite): Boolean {
        if (sentInvites.containsKey(invite.senderUUID)) {
            val openInvites = sentInvites[invite.senderUUID]!!

            val currentDate = Date()
            for (invite in ArrayList(openInvites)) {
                if (currentDate.time - invite.creationDate.time > 1000L * 300) { // Invite is older than 5 minutes, remove it.
                    openInvites.remove(invite)
                }
            }

            if (openInvites.size >= settings.guildMaxInvites) {
                return false
            }

            if (openInvites.firstOrNull { e -> e.receiverUUID == invite.receiverUUID } != null) {
                return false
            }

            openInvites.add(invite)
            sentInvites[invite.senderUUID] = openInvites
        } else {
            sentInvites[invite.senderUUID] = mutableListOf(invite)
        }

        if (receivedInvites.containsKey(invite.receiverUUID)) {
            receivedInvites[invite.receiverUUID] = (receivedInvites[invite.receiverUUID]!! + invite).toMutableList()
        } else {
            receivedInvites[invite.receiverUUID] = mutableListOf(invite)
        }

        return true
    }

    override suspend fun acceptInvite(player: Player, name: String): Boolean {
        val invitedUUID = player.uniqueId
        val invites = receivedInvites[invitedUUID] ?: return false
        val guildInvite = invites.firstOrNull { e -> e.guildName == name }

        if (guildInvite == null) {
            return false
        }

        receivedInvites[invitedUUID] = invites.filter { e -> e.guildName != name }.toMutableList()
        sentInvites[guildInvite.senderUUID] =
            sentInvites[guildInvite.senderUUID]!!.filter { e -> e.receiverUUID != invitedUUID }.toMutableList()

        if (receivedInvites[invitedUUID]!!.isEmpty()) {
            receivedInvites.remove(invitedUUID)
        }
        if (sentInvites[guildInvite.senderUUID]!!.isEmpty()) {
            sentInvites.remove(guildInvite.senderUUID)
        }

        return true
    }

    override fun close() {
        guilds.clear()
        isDisposed = true
    }
}