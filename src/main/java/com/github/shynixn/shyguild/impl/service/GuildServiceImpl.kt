package com.github.shynixn.shyguild.impl.service

import com.github.shynixn.mcutils.common.CoroutineHandler
import com.github.shynixn.mcutils.database.api.CachePlayerRepository
import com.github.shynixn.shyguild.contract.GuildMetaSqlRepository
import com.github.shynixn.shyguild.contract.GuildService
import com.github.shynixn.shyguild.entity.GuildMeta
import com.github.shynixn.shyguild.entity.PlayerInformation
import com.github.shynixn.shyguild.entity.ShyGuildSettings
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class GuildServiceImpl(
    private val settings: ShyGuildSettings,
    coroutineHandler: CoroutineHandler,
    private val guildMetaSqlRepository: GuildMetaSqlRepository,
    private val cachePlayerDataRepository: CachePlayerRepository<PlayerInformation>
) : GuildService {
    private var isDisposed = false
    private var guilds = HashMap<String, GuildMeta>()

    init {
        coroutineHandler.execute {
            while (!isDisposed) {
                val guildNames = guilds.keys.toList()
                val newGuilds = HashMap<String, GuildMeta>()
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

    override suspend fun getGuilds(player: Player): List<GuildMeta> {
        var playerInfo = cachePlayerDataRepository.getByPlayer(player)

        if (playerInfo == null) {
            playerInfo = PlayerInformation().also {
                it.playerName = player.name
                it.playerUUID = player.uniqueId.toString()
            }
            cachePlayerDataRepository.save(playerInfo)
        }

        val result = ArrayList<GuildMeta>()
        val guildNames = ArrayList(playerInfo.guilds)

        for (guildName in guildNames) {
            if (guilds.containsKey(guildName)) {
                result.add(guilds[guildName]!!)
                continue
            }

            val loadedGuild = guildMetaSqlRepository.getByName(guildName)
            if (loadedGuild != null) {
                guilds[guildName] = loadedGuild
                result.add(loadedGuild)
            } else {
                // Guild was deleted.
                playerInfo.guilds.remove(guildName)
            }
        }

        return result
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

    override fun close() {
        guilds.clear()
        guildMetaSqlRepository.close()
    }
}