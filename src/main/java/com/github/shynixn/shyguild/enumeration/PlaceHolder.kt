package com.github.shynixn.shyguild.enumeration

import com.github.shynixn.mcutils.common.ChatColor
import com.github.shynixn.mcutils.common.placeholder.PlaceHolderService
import com.github.shynixn.mcutils.common.translateChatColors
import com.github.shynixn.shyguild.ShyGuildPlugin
import com.github.shynixn.shyguild.contract.GuildService
import com.github.shynixn.shyguild.entity.Guild
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*

enum class PlaceHolder(
    val text: String,
    val f: ((Player?, Guild?, Map<String, Any>) -> String?),
) {
    PLAYER_NAME("player_name", { p, guild, _ -> p?.name }),
    GUILD_NAME("guild_name", { _, guild, context ->
        guild?.name
    }),
    GUILD_DISPLAY_NAME("guild_displayName", { _, guild, context ->
        guild?.displayName
    }),
    GUILD_DISPLAY_COLOR("guild_displayNameColor", { _, guild, context ->
        guild?.displayNameColor
    }),
    GUILD_MEMBER("guild_member_[role]", { _, guild, context ->
        if (guild != null) {
            val role = context["role"] as String?
            if (role != null) {
                val members = guild.members.filter { e -> e.roles.firstOrNull { r -> r == role } != null }
                members.joinToString(", ") { e -> e.playerName }
            } else {
                null
            }
        } else {
            null
        }
    }),

    PARAM_1("param_1", { _, guild, context ->
        val item = context["0"] as String?
        item
    }),
    PARAM_2("param_2", { _, guild, context ->
        val item = context["1"] as String?
        item
    });

    fun getFullPlaceHolder(plugin: Plugin): String {
        return "%${plugin.name.lowercase(Locale.ENGLISH)}_${text}%"
    }

    companion object {
        /**
         * Registers all placeHolder. Overrides previously registered placeholders.
         */
        fun registerAll(
            plugin: Plugin,
            placeHolderService: PlaceHolderService,
            guildService: GuildService
        ) {
            for (placeHolder in PlaceHolder.values()) {
                placeHolderService.register(placeHolder.getFullPlaceHolder(plugin)) { player, context ->
                    val newContext = context.toMutableMap()
                    val guildNameReference = newContext[ShyGuildPlugin.guildNameKey] as String?
                    val guild = if (guildNameReference != null) {
                        guildService.getGuildCache().firstOrNull { e -> e.name.equals(guildNameReference, true) }
                    } else {
                        null
                    }

                    placeHolder.f.invoke(player, guild, context)
                }
            }
        }
    }
}
