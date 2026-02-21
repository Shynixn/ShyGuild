package com.github.shynixn.shyguild.entity

import com.github.shynixn.shyguild.enumeration.Permission

class ShyGuildSettings(private val reloadFun: (ShyGuildSettings) -> Unit) {
    var maxCreateGuildsPerPlayer: Int = 1
    var maxJoinGuildsPerPlayer: Int = 3

    /**
     * Delay when joining the server.
     */
    var joinDelaySeconds = 3

    /**
     * Delay when synchronizing guilds.
     */
    var synchronizeGuildsSeconds = 300

    /**
     * Base Command.
     */
    var baseCommand: String = "shyguild"

    /**
     * Command aliases.
     */
    var commandAliases: List<String> = ArrayList()


    var guildArgument = "guild"

    var commandPermission: String = Permission.COMMAND.text

    var reloadPermission: String = Permission.RELOAD.text

    var templateListPermission: String = Permission.TEMPLATE_LIST.text

    var templateUsePermission : String = Permission.DYN_TEMPLATE.text

    var guildAddRolePermission : String = Permission.GUILD_ADD_ROLE.text

    var guildRemoveRolePermission : String = Permission.GUILD_REMOVE_ROLE.text

    var guildListRolePermission : String = Permission.GUILD_LIST_ROLE.text

    var blackList: List<String> = ArrayList()

    var guildNameMinLength: Int = 3
    var guildNameMaxLength: Int = 16
    var guildDisplayNameMinLength: Int = 3
    var guildDisplayNameMaxLength: Int = 32

    var defaultTemplates: List<Pair<String, String>> = listOf(
        "guild/sample_guild.yml" to "sample_guild.yml"
    )

    /**
     * Reloads the config.
     */
    fun reload() {
        reloadFun.invoke(this)
    }
}
