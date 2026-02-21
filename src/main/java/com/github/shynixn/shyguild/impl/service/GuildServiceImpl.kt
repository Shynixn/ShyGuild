package com.github.shynixn.shyguild.impl.service

import com.github.shynixn.mcutils.common.CoroutineHandler
import com.github.shynixn.mcutils.common.repository.CacheRepository
import com.github.shynixn.mcutils.database.api.CachePlayerRepository
import com.github.shynixn.shyguild.contract.GuildMetaSqlRepository
import com.github.shynixn.shyguild.contract.GuildService
import com.github.shynixn.shyguild.contract.PermissionPluginService
import com.github.shynixn.shyguild.entity.Guild
import com.github.shynixn.shyguild.entity.GuildTemplate
import com.github.shynixn.shyguild.entity.PlayerInformation
import com.github.shynixn.shyguild.entity.ShyGuildSettings
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class GuildServiceImpl(
    private val settings: ShyGuildSettings,
    coroutineHandler: CoroutineHandler,
    private val guildMetaSqlRepository: GuildMetaSqlRepository,
    private val cachePlayerDataRepository: CachePlayerRepository<PlayerInformation>,
    private val permissionPluginService: PermissionPluginService,
    private val templateService: CacheRepository<GuildTemplate>
) : GuildService {
    private var isDisposed = false
    private var guilds = HashMap<String, Guild>()

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
        val guildNames = ArrayList(playerInfo.guilds)

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

        cachePlayerDataRepository.clearByPlayer(player)
    }

    override suspend fun saveGuild(guild: Guild) {
        if (!guilds.containsKey(guild.name)) {
            refreshGuild(guild)
        }

        guildMetaSqlRepository.save(guild)
    }

    override suspend fun deleteGuild(
        owner: Player,
        guild: Guild
    ) {
        val playerInformation = cachePlayerDataRepository.getByPlayer(owner) ?: return
        playerInformation.guilds.remove(guild.name)
        permissionPluginService.deletePermissions(guild)
        guildMetaSqlRepository.delete(guild)
    }

    private suspend fun refreshGuild(guild: Guild) {
        guild.template = templateService.getAll().firstOrNull { e -> e.name == guild.templateName }
        guilds[guild.name] = guild
        permissionPluginService.createOrUpdatePermissions(guild)
    }

    override fun close() {
        guilds.clear()
    }
}