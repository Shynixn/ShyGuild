package com.github.shynixn.shyguild.entity

import com.github.shynixn.shyguild.enumeration.Permission

class ShyGuildSettings(private val reloadFun: (ShyGuildSettings) -> Unit) {
    /**
     * Delay when joining the server.
     */
    var joinDelaySeconds = 3

    /**
     * Base Command.
     */
    var baseCommand: String = "shyguild"

    /**
     * Command aliases.
     */
    var commandAliases: List<String> = ArrayList()


    var commandPermission: String = Permission.COMMAND.text


    var defaultScoreboards: List<Pair<String, String>> = listOf(
        "guild/sample_guild.yml" to "sample_guild.yml"
    )

    /**
     * Reloads the config.
     */
    fun reload() {
        reloadFun.invoke(this)
    }
}
