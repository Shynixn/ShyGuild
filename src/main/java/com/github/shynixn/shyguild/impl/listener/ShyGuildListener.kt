package com.github.shynixn.shyguild.impl.listener

import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.shyguild.contract.GuildService
import com.github.shynixn.shyguild.entity.ShyGuildSettings
import kotlinx.coroutines.delay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin

class ShyGuildListener(
    private val settings: ShyGuildSettings,
    private val plugin: Plugin,
    private val guildService: GuildService
) : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        plugin.launch {
            delay(settings.joinDelaySeconds * 1000L)
            if (player.isOnline) {
                guildService.getGuilds(player)
            }
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        plugin.launch {
            guildService.cleanCache(event.player)
        }
    }
}
