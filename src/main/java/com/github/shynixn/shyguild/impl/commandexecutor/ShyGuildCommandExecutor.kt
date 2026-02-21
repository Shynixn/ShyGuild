package com.github.shynixn.shyguild.impl.commandexecutor

import com.github.shynixn.mccoroutine.folia.globalRegionDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mcutils.common.CoroutineHandler
import com.github.shynixn.mcutils.common.chat.ChatMessageService
import com.github.shynixn.mcutils.common.command.CommandBuilder
import com.github.shynixn.mcutils.common.command.CommandService
import com.github.shynixn.mcutils.common.command.Validator
import com.github.shynixn.mcutils.common.language.LanguageItem
import com.github.shynixn.mcutils.common.language.reloadTranslation
import com.github.shynixn.mcutils.common.placeholder.PlaceHolderService
import com.github.shynixn.mcutils.common.repository.CacheRepository
import com.github.shynixn.shyguild.contract.GuildService
import com.github.shynixn.shyguild.contract.ShyGuildLanguage
import com.github.shynixn.shyguild.entity.GuildMember
import com.github.shynixn.shyguild.entity.Guild
import com.github.shynixn.shyguild.entity.GuildRoleTemplate
import com.github.shynixn.shyguild.entity.ShyGuildSettings
import com.github.shynixn.shyguild.entity.GuildTemplate
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*

class ShyGuildCommandExecutor(
    private val settings: ShyGuildSettings,
    private val plugin: Plugin,
    private val guildService: GuildService,
    private val language: ShyGuildLanguage,
    private val chatMessageService: ChatMessageService,
    private val guildTemplateRepository: CacheRepository<GuildTemplate>,
    private val placeHolderService: PlaceHolderService,
    coroutineHandler: CoroutineHandler,
    commandService: CommandService
) {
    private val senderHasToBePlayer: () -> String = {
        language.shyGuildCommandSenderHasToBePlayer.text
    }

    private val templateTabs: (CommandSender) -> List<String> = {
        guildTemplateRepository.getCache()?.map { e -> e.name } ?: emptyList()
    }

    private val guildTabs: (CommandSender) -> List<String> = { sender ->
        if (sender is Player) {
            guildService.getGuildCache()
                .filter { e -> e.isMember(sender) }
                .map { e -> e.name }
        } else {
            guildService.getGuildCache().map { e -> e.name }
        }
    }

    private val playerMustExist = object : Validator<Player> {
        override suspend fun transform(
            sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>
        ): Player? {
            try {
                val playerId = openArgs[0]
                val player = Bukkit.getPlayer(playerId)

                if (player != null) {
                    return player
                }
                return Bukkit.getPlayer(UUID.fromString(playerId))
            } catch (e: Exception) {
                return null
            }
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return placeHolderService.resolvePlaceHolder(
                language.shyGuildPlayerNotFoundMessage.text,
                null,
                mapOf("0" to openArgs[0])
            )
        }
    }

    private val templateMustExists = object : Validator<GuildTemplate> {
        override suspend fun transform(
            sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>
        ): GuildTemplate? {
            return guildTemplateRepository.getAll().firstOrNull { e -> e.name.equals(openArgs[0], true) }
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return language.shyGuildTemplateNotFoundMessage.text.format(openArgs[0])
        }
    }

    private val guildMustExist = object : Validator<Guild> {
        override suspend fun transform(
            sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>
        ): Guild? {
            return guildService.getGuildCache().firstOrNull { e -> e.name.equals(openArgs[0], true) }
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return language.shyGuildGuildNotFoundMessage.text.format(openArgs[0])
        }
    }

    private val guildNameValidator = object : Validator<String> {
        override suspend fun transform(
            sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>
        ): String? {
            val name = openArgs[0]

            for (badWord in settings.blackList) {
                if (name.lowercase(Locale.ENGLISH).contains(badWord.lowercase(Locale.ENGLISH))) {
                    return null
                }
            }

            if (name.length < settings.guildNameMinLength || name.length > settings.guildNameMaxLength) {
                return null
            }

            return name
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return placeHolderService.resolvePlaceHolder(
                language.shyGuildWordNotAllowedMessage.text,
                null,
                mapOf("0" to openArgs[0])
            )
        }
    }

    private val roleNameValidator = object : Validator<GuildRoleTemplate> {
        override suspend fun transform(
            sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>
        ): GuildRoleTemplate? {
            val guild = prevArgs[0] as Guild
            val name = openArgs[0]
            return guild.template?.roles?.firstOrNull { e -> e.name.equals(name, true) }
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return placeHolderService.resolvePlaceHolder(
                language.shyGuildRoleNotFoundMessage.text,
                null,
                mapOf("0" to openArgs[0], "1" to (prevArgs[0] as Guild).name)
            )
        }
    }

    private val guildDisplayNameValidator = object : Validator<String> {
        override suspend fun transform(
            sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>
        ): String? {
            val name = openArgs[0]

            for (badWord in settings.blackList) {
                if (name.lowercase(Locale.ENGLISH).contains(badWord.lowercase(Locale.ENGLISH))) {
                    return null
                }
            }

            if (name.length < settings.guildDisplayNameMinLength || name.length > settings.guildDisplayNameMaxLength) {
                return null
            }

            return name
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return placeHolderService.resolvePlaceHolder(
                language.shyGuildWordNotAllowedMessage.text,
                null,
                mapOf("0" to openArgs[0])
            )
        }
    }

    private val booleanTabs: (CommandSender) -> List<String> = {
        listOf("true", "false")
    }


    private val onlinePlayerTabs: (CommandSender) -> List<String> = {
        Bukkit.getOnlinePlayers().map { e -> e.name }
    }

    init {
        commandService.registerCommand(
            CommandBuilder(
                coroutineHandler,
                plugin,
                settings.baseCommand,
                chatMessageService
            ) {
                usage(language.shyGuildCommandUsage.text)
                description(language.shyGuildCommandDescription.text)
                aliases(settings.commandAliases)
                permission(settings.commandPermission)
                permissionMessage(language.shyGuildNoPermissionCommand.text)
                subCommand("create") {
                    toolTip {
                        language.shyGuildTemplateListCommandHint.text
                    }
                    builder().argument("template").validator(templateMustExists).tabs(templateTabs)
                        .argument("name").validator(guildNameValidator).tabs { listOf("<name>") }
                        .argument("displayName").validator(guildDisplayNameValidator).tabs { listOf("<displayName>") }
                        .execute { sender, template, name, displayName ->
                            createGuild(sender, template, name, displayName)
                        }
                }
                subCommand("role") {
                    subCommand("add") {
                        toolTip {
                            language.shyGuildAddRoleCommandHint.text
                        }
                        builder().argument(settings.guildArgument).validator(guildMustExist).tabs(guildTabs)
                            .argument("name").validator(roleNameValidator).tabs { listOf("<role>") }
                            .executePlayer(senderHasToBePlayer) { sender, guild, role ->
                                addRoleToGuildMember(sender, guild, role, sender.name)
                            }.argument("player").tabs(onlinePlayerTabs)
                            .execute { sender, guild, role, playerNameOrId ->
                                addRoleToGuildMember(sender, guild, role, playerNameOrId)
                            }
                    }
                    subCommand("remove") {
                        toolTip {
                            language.shyGuildRemoveRoleCommandHint.text
                        }
                        builder().argument(settings.guildArgument).validator(guildMustExist).tabs(guildTabs)
                            .argument("name").validator(roleNameValidator).tabs { listOf("<role>") }
                            .executePlayer(senderHasToBePlayer) { sender, guild, role ->
                                removeRoleFromGuildMember(sender, guild, role, sender.name)
                            }.argument("player").tabs(onlinePlayerTabs)
                            .execute { sender, guild, role, playerNameOrId ->
                                removeRoleFromGuildMember(sender, guild, role, playerNameOrId)
                            }
                    }
                    subCommand("list") {
                        toolTip {
                            language.shyGuildListRolesCommandHint.text
                        }
                        builder().argument(settings.guildArgument).validator(guildMustExist).tabs(guildTabs)
                            .execute { sender, guild ->
                                listRoles(sender, guild)
                            }.argument("player").tabs(onlinePlayerTabs)
                            .execute { sender, guild, playerNameOrId ->
                                listRoles(sender, guild, playerNameOrId)
                            }
                    }
                }
                subCommand("template") {
                    subCommand("list") {
                        permission(settings.templateListPermission)
                        toolTip {
                            language.shyGuildTemplateListCommandHint.text
                        }
                        builder().execute { sender ->
                            listTemplates(sender)
                        }
                    }
                }
                subCommand("reload") {
                    permission(settings.reloadPermission)
                    toolTip {
                        language.shyGuildReloadCommandHint.text
                    }
                    builder().execute { sender ->
                        plugin.saveDefaultConfig()
                        plugin.reloadConfig()
                        plugin.reloadTranslation(language)
                        guildService.close()
                        sender.sendLanguageMessage(language.shyGuildReloadMessage)
                    }
                }.helpCommand()
            })
    }

    private fun listRoles(sender: CommandSender, guild: Guild, playerNameOrId: String? = null) {
        val permission = settings.guildListRolePermission.replace("<guild>", guild.name)

        if (!sender.hasPermission(permission)) {
            sender.sendLanguageMessage(language.shyGuildNoPermissionCommand)
            return
        }

        if (playerNameOrId != null) {
            val member = guild.getMember(playerNameOrId)

            if (member == null) {
                sender.sendLanguageMessage(language.shyGuildPlayerNotAMemberMessage, playerNameOrId)
                return
            }

            sender.sendLanguageMessage(language.shyGuildRoleListPlayerMessage)
            for (role in member.roles) {
                sender.sendMessage("- $role")
            }
            return
        }

        sender.sendLanguageMessage(language.shyGuildRoleListAllMessage)
        for (role in guild.template!!.roles) {
            sender.sendMessage("- $role")
        }
    }

    private suspend fun addRoleToGuildMember(
        sender: CommandSender,
        guild: Guild,
        role: GuildRoleTemplate,
        playerNameOrId: String
    ) {
        val permission = settings.guildAddRolePermission.replace("<guild>", guild.name).replace("<role>", role.name)

        if (!sender.hasPermission(permission)) {
            sender.sendLanguageMessage(language.shyGuildNoPermissionRoleMessage, role.name)
            return
        }

        val member = guild.getMember(playerNameOrId)

        if (member == null) {
            sender.sendLanguageMessage(language.shyGuildPlayerNotAMemberMessage, playerNameOrId)
            return
        }

        member.roles.add(role.name)
        guildService.saveGuild(guild)
        sender.sendLanguageMessage(language.shyGuildAssignRoleSuccessMessage, role.name, playerNameOrId)
    }

    private suspend fun removeRoleFromGuildMember(
        sender: CommandSender,
        guild: Guild,
        role: GuildRoleTemplate,
        playerNameOrId: String
    ) {
        val permission = settings.guildRemoveRolePermission.replace("<guild>", guild.name).replace("<role>", role.name)

        if (!sender.hasPermission(permission)) {
            sender.sendLanguageMessage(language.shyGuildNoPermissionRoleMessage, role.name)
            return
        }

        val member = guild.getMember(playerNameOrId)

        if (member == null) {
            sender.sendLanguageMessage(language.shyGuildPlayerNotAMemberMessage, playerNameOrId)
            return
        }

        member.roles.remove(role.name)
        guildService.saveGuild(guild)
        sender.sendLanguageMessage(language.shyGuildRemoveRoleSuccessMessage, role.name, playerNameOrId)
    }

    private suspend fun createGuild(sender: CommandSender, template: GuildTemplate, name: String, displayName: String) {
        if (!sender.hasPermission(settings.templateUsePermission + template.name)) {
            sender.sendLanguageMessage(language.shyGuildNoPermissionTemplateMessage, template.name)
            return
        }

        val guildName = name.lowercase(Locale.ENGLISH)

        if (guildService.existsGuild(guildName)) {
            sender.sendLanguageMessage(language.shyGuildAlreadyExistsMessage, guildName)
            return
        }

        val guild = Guild().also {
            it.name = guildName
            it.displayName = name
            it.displayNameColor = displayName
            it.templateName = template.name
        }

        if (sender is Player) {
            guild.members.add(GuildMember().also {
                it.playerName = sender.name
                it.playerUUID = sender.uniqueId.toString()
                it.roles.add("owner")
            })
        }

        guildService.saveGuild(guild)
        sender.sendLanguageMessage(language.shyGuildCreateSuccessMessage, guildName)
    }

    private suspend fun listTemplates(sender: CommandSender) {
        val templates = guildTemplateRepository.getAll()
        sender.sendLanguageMessage(language.shyGuildTemplateListMessage)
        for (template in templates) {
            sender.sendMessage("- ${template.name}")
        }
    }

    private fun CommandSender.sendLanguageMessage(languageItem: LanguageItem, vararg args: String) {
        val sender = this
        plugin.launch(plugin.globalRegionDispatcher) {
            chatMessageService.sendLanguageMessage(sender, languageItem, *args)
        }
    }
}
