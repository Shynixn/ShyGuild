package com.github.shynixn.shyguild.impl.commandexecutor

import com.github.shynixn.mccoroutine.folia.globalRegionDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mcutils.common.ChatColor
import com.github.shynixn.mcutils.common.CoroutineHandler
import com.github.shynixn.mcutils.common.chat.ChatMessageService
import com.github.shynixn.mcutils.common.command.CommandBuilder
import com.github.shynixn.mcutils.common.command.CommandService
import com.github.shynixn.mcutils.common.command.Validator
import com.github.shynixn.mcutils.common.language.LanguageItem
import com.github.shynixn.mcutils.common.language.reloadTranslation
import com.github.shynixn.mcutils.common.placeholder.PlaceHolderService
import com.github.shynixn.mcutils.common.repository.CacheRepository
import com.github.shynixn.mcutils.common.translateChatColors
import com.github.shynixn.mcutils.database.api.CachePlayerRepository
import com.github.shynixn.shyguild.contract.GuildService
import com.github.shynixn.shyguild.contract.ShyGuildLanguage
import com.github.shynixn.shyguild.entity.GuildMember
import com.github.shynixn.shyguild.entity.Guild
import com.github.shynixn.shyguild.entity.GuildInvite
import com.github.shynixn.shyguild.entity.GuildRoleTemplate
import com.github.shynixn.shyguild.entity.ShyGuildSettings
import com.github.shynixn.shyguild.entity.GuildTemplate
import com.github.shynixn.shyguild.entity.PlayerInformation
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
    private val cachePlayerDataRepository: CachePlayerRepository<PlayerInformation>,
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
            guildService.getGuildCache().filter { e -> e.isMember(sender) }.map { e -> e.name }
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
            } catch (_: Exception) {
                return null
            }
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return placeHolderService.resolvePlaceHolder(
                language.shyGuildPlayerNotFoundMessage.text, null, mapOf("0" to openArgs[0])
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
            return placeHolderService.resolvePlaceHolder(
                language.shyGuildTemplateNotFoundMessage.text, null, mapOf("0" to openArgs[0])
            )
        }
    }

    private val guildMustExist = object : Validator<Guild> {
        override suspend fun transform(
            sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>
        ): Guild? {
            val guild = guildService.getGuildCache().firstOrNull { e -> e.name.equals(openArgs[0], true) }
            return guild
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return placeHolderService.resolvePlaceHolder(
                language.shyGuildGuildNotFoundMessage.text, null, mapOf("0" to openArgs[0])
            )
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

            // check if it follows the regex a-z, A-Z, 0-9 and - only
            if (!name.matches(Regex("^[a-zA-Z0-9-]+$"))) {
                return null
            }

            return name
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return placeHolderService.resolvePlaceHolder(
                language.shyGuildWordNotAllowedMessage.text, null, mapOf("0" to openArgs[0])
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

            if (name.length > 500) {
                return null
            }

            val strippedName = ChatColor.stripChatColors(name.translateChatColors())

            if (strippedName.length < settings.guildDisplayNameMinLength || strippedName.length > settings.guildDisplayNameMaxLength) {
                return null
            }

            return name
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return placeHolderService.resolvePlaceHolder(
                language.shyGuildWordNotAllowedMessage.text, null, mapOf("0" to openArgs[0])
            )
        }
    }

    private val onlinePlayerTabs: (CommandSender) -> List<String> = {
        Bukkit.getOnlinePlayers().map { e -> e.name }
    }

    init {
        commandService.registerCommand(
            CommandBuilder(
                coroutineHandler, plugin, settings.baseCommand, chatMessageService
            ) {
                usage(language.shyGuildCommandUsage.text)
                description(language.shyGuildCommandDescription.text)
                aliases(settings.commandAliases)
                permission(settings.commandPermission)
                permissionMessage(language.shyGuildNoPermissionCommand.text)
                subCommand("create") {
                    permission(settings.createCmdPermission)
                    toolTip {
                        language.shyGuildCreateCommandHint.text
                    }
                    builder().argument("template").validator(templateMustExists).tabs(templateTabs).argument("name")
                        .validator(guildNameValidator).tabs { listOf("<name>") }.argument("displayName")
                        .validator(guildDisplayNameValidator).tabs { listOf("<displayName>") }
                        .execute { sender, template, name, displayName ->
                            createGuild(sender, template, name, displayName.replace("_", " "))
                        }
                }
                subCommand("delete") {
                    permission(settings.deleteCmdPermission)
                    toolTip {
                        language.shyGuildDeleteCommandHint.text
                    }
                    builder().argument(settings.guildArgument).validator(guildMustExist).tabs(guildTabs)
                        .execute { sender, guild ->
                            deleteGuild(sender, guild)
                        }
                }
                subCommand("list") {
                    permission(settings.guildListPermission)
                    toolTip {
                        language.shyGuildListGuildsCommandHint.text
                    }
                    builder()
                        .execute { sender ->
                            listGuilds(sender)
                        }
                }
                subCommand("role") {
                    subCommand("add") {
                        permission(settings.addRoleCmdPermission)
                        toolTip {
                            language.shyGuildAddRoleCommandHint.text
                        }
                        builder().argument(settings.guildArgument).validator(guildMustExist).tabs(guildTabs)
                            .argument("name").validator(roleNameValidator).tabs { listOf("<role>") }
                            .executePlayer(senderHasToBePlayer) { sender, guild, role ->
                                addRoleToGuildMember(sender, guild, role, sender.name)
                            }.argument("player").tabs(onlinePlayerTabs).execute { sender, guild, role, playerNameOrId ->
                                addRoleToGuildMember(sender, guild, role, playerNameOrId)
                            }
                    }
                    subCommand("remove") {
                        permission(settings.removeRoleCmdPermission)
                        toolTip {
                            language.shyGuildRemoveRoleCommandHint.text
                        }
                        builder().argument(settings.guildArgument).validator(guildMustExist).tabs(guildTabs)
                            .argument("name").validator(roleNameValidator).tabs { listOf("<role>") }
                            .executePlayer(senderHasToBePlayer) { sender, guild, role ->
                                removeRoleFromGuildMember(sender, guild, role, sender.name)
                            }.argument("player").tabs(onlinePlayerTabs).execute { sender, guild, role, playerNameOrId ->
                                removeRoleFromGuildMember(sender, guild, role, playerNameOrId)
                            }
                    }
                    subCommand("list") {
                        permission(settings.listRoleCmdPermission)
                        toolTip {
                            language.shyGuildListRolesCommandHint.text
                        }
                        builder().argument(settings.guildArgument).validator(guildMustExist).tabs(guildTabs)
                            .execute { sender, guild ->
                                listRoles(sender, guild)
                            }.argument("player").tabs(onlinePlayerTabs).execute { sender, guild, playerNameOrId ->
                                listRoles(sender, guild, playerNameOrId)
                            }
                    }
                }
                subCommand("member") {
                    subCommand("add") {
                        permission(settings.addMemberPermission)
                        toolTip {
                            language.shyGuildMemberAddCommandHint.text
                        }
                        builder().argument(settings.guildArgument).validator(guildMustExist).tabs(guildTabs)
                            .argument("player").tabs(onlinePlayerTabs)
                            .execute { sender, guild, playerNameOrId ->
                                addMemberToGuild(sender, guild, playerNameOrId)
                            }
                    }
                    subCommand("remove") {
                        permission(settings.removeMemberPermission)
                        toolTip {
                            language.shyGuildMemberRemoveCommandHint.text
                        }
                        builder().argument(settings.guildArgument).validator(guildMustExist).tabs(guildTabs)
                            .argument("player").tabs(onlinePlayerTabs)
                            .execute { sender, guild, playerNameOrId ->
                                removeMemberFromGuild(sender, guild, playerNameOrId)
                            }
                    }
                    subCommand("list") {
                        permission(settings.listMembersPermission)
                        toolTip {
                            language.shyGuildMemberListCommandHint.text
                        }
                        builder().argument(settings.guildArgument).validator(guildMustExist).tabs(guildTabs)
                            .execute { sender, guild ->
                                listMembers(sender, guild)
                            }
                    }
                    subCommand("invite") {
                        permission(settings.inviteMemberPermission)
                        toolTip {
                            language.shyGuildMemberInviteCommandHint.text
                        }
                        builder().argument(settings.guildArgument).validator(guildMustExist).tabs(guildTabs)
                            .argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                            .executePlayer(senderHasToBePlayer) { sender, guild, player ->
                                inviteMemberToGuild(sender, guild, player)
                            }
                    }
                    subCommand("accept") {
                        permission(settings.acceptMemberPermission)
                        toolTip {
                            language.shyGuildMemberAcceptCommandHint.text
                        }
                        builder().argument(settings.guildArgument).tabs(guildTabs)
                            .executePlayer(senderHasToBePlayer) { sender, guildName ->
                                acceptMemberInvite(sender, guildName)
                            }
                    }
                    subCommand("leave") {
                        permission(settings.leaveMemberPermission)
                        toolTip {
                            language.shyGuildLeaveCommandHint.text
                        }
                        builder().argument(settings.guildArgument).validator(guildMustExist).tabs(guildTabs)
                            .executePlayer(senderHasToBePlayer) { sender, guild ->
                                leaveGuild(sender, guild)
                            }
                    }
                }
                subCommand("template") {
                    subCommand("list") {
                        permission(settings.templateListCmdPermission)
                        toolTip {
                            language.shyGuildTemplateListCommandHint.text
                        }
                        builder().execute { sender ->
                            listTemplates(sender)
                        }
                    }
                }
                subCommand("reload") {
                    permission(settings.reloadCmdPermission)
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

    private suspend fun listGuilds(sender: CommandSender) {
        val guilds = guildService.getGuildCache()
        sender.sendLanguageMessage(language.shyGuildListGuildsMessage)
        for (guild in guilds) {
            if (sender is Player && guild.isMember(sender)) {
                sender.sendMessage("- ${guild.displayNameColor}" + ChatColor.RESET + " [${guild.displayName}] (${guild.name})")
            }
        }
    }

    private suspend fun leaveGuild(sender: Player, guild: Guild) {
        val targetPlayerData = cachePlayerDataRepository.getByPlayer(sender)

        if (targetPlayerData == null) {
            sender.sendLanguageMessage(language.shyGuildPlayerNotFoundMessage, sender.name)
            return
        }

        val member = guild.getMember(sender)

        if (member == null) {
            sender.sendLanguageMessage(language.shyGuildPlayerNotAMemberMessage, sender.name)
            return
        }

        val permission = settings.guildMemberLeavePermission.replace("<guild>", guild.name)

        if (!member.roles.isEmpty() && !sender.hasPermission(permission)) {
            sender.sendLanguageMessage(language.shyGuildNoPermissionCommand)
            return
        }

        val owners =
            guild.members.filter { e -> e.roles.contains("owner") && e.playerUUID != sender.uniqueId.toString() }

        if (owners.isEmpty()) {
            sender.sendLanguageMessage(language.shyGuildCannotLeaveOwnerGuildMessage, guild.name)
            return
        }

        guild.members.remove(member)
        guildService.saveGuild(guild)
        targetPlayerData.guilds.remove(guild.name)
        cachePlayerDataRepository.save(targetPlayerData)
        guildService.applyGuildMemberPermissions(UUID.fromString(targetPlayerData.playerUUID), guild)
        sender.sendLanguageMessage(language.shyGuildLeaveSuccessMessage, sender.name, guild.name)
    }

    private suspend fun listRoles(sender: CommandSender, guild: Guild, playerNameOrId: String? = null) {
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

            sender.sendLanguageMessage(language.shyGuildRoleListPlayerMessage, playerNameOrId)
            for (role in member.roles) {
                sender.sendMessage("- $role")
            }
            return
        }

        sender.sendLanguageMessage(language.shyGuildRoleListAllMessage)
        for (role in guild.template!!.roles) {
            sender.sendMessage("- ${role.name}")
        }
    }

    private suspend fun addRoleToGuildMember(
        sender: CommandSender, guild: Guild, role: GuildRoleTemplate, playerNameOrId: String
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

        val targetPlayerData = cachePlayerDataRepository.getByPlayerUUID(UUID.fromString(member.playerUUID))

        if (targetPlayerData == null) {
            sender.sendLanguageMessage(language.shyGuildPlayerNotFoundMessage, playerNameOrId)
            return
        }

        member.roles.add(role.name)
        guildService.applyGuildMemberPermissions(UUID.fromString(member.playerUUID), guild)
        guildService.saveGuild(guild)

        if (role.name == "owner") {
            cachePlayerDataRepository.save(targetPlayerData)
        }

        sender.sendLanguageMessage(language.shyGuildAssignRoleSuccessMessage, role.name, playerNameOrId)
    }

    private suspend fun removeRoleFromGuildMember(
        sender: CommandSender, guild: Guild, role: GuildRoleTemplate, playerNameOrId: String
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

        val targetPlayerData = cachePlayerDataRepository.getByPlayerUUID(UUID.fromString(member.playerUUID))

        if (targetPlayerData == null) {
            sender.sendLanguageMessage(language.shyGuildPlayerNotFoundMessage, playerNameOrId)
            return
        }

        if (role.name == "owner") {
            val owners =
                guild.members.filter { e -> e.roles.contains("owner") && e.playerUUID != member.playerUUID }

            if (owners.isEmpty()) {
                sender.sendLanguageMessage(language.shyGuildThereCannotBeNoOwnerMessage, guild.name)
                return
            }
        }

        member.roles.remove(role.name)
        guildService.applyGuildMemberPermissions(UUID.fromString(member.playerUUID), guild)
        guildService.saveGuild(guild)

        if (role.name == "owner") {
            cachePlayerDataRepository.save(targetPlayerData)
        }

        sender.sendLanguageMessage(language.shyGuildRemoveRoleSuccessMessage, role.name, playerNameOrId)
    }

    private suspend fun addMemberToGuild(sender: CommandSender, guild: Guild, playerNameOrId: String) {
        val permission = settings.guildMemberAddPermission.replace("<guild>", guild.name)

        if (!sender.hasPermission(permission)) {
            sender.sendLanguageMessage(language.shyGuildNoPermissionCommand)
            return
        }

        if (guild.getMember(playerNameOrId) != null) {
            sender.sendLanguageMessage(language.shyGuildMemberAlreadyInGuildMessage, playerNameOrId, guild.name)
            return
        }

        val uuid = try {
            UUID.fromString(playerNameOrId)
        } catch (_: Exception) {
            null
        }

        var targetPlayerData: PlayerInformation? = null

        if (uuid != null) {
            targetPlayerData = cachePlayerDataRepository.getByPlayerUUID(uuid)
        }

        if (targetPlayerData == null) {
            val player = Bukkit.getPlayer(playerNameOrId)

            if (player != null) {
                targetPlayerData = cachePlayerDataRepository.getByPlayer(player)
            }
        }

        if (targetPlayerData == null) {
            sender.sendLanguageMessage(language.shyGuildPlayerNotFoundMessage, playerNameOrId)
            return
        }

        if (targetPlayerData.guilds.size >= settings.maxJoinGuildsPerPlayer) {
            sender.sendLanguageMessage(language.shyGuildMemberMaxGuildsReachedMessage, playerNameOrId)
            return
        }

        if (guild.members.size >= guild.template!!.maxPlayers) {
            sender.sendLanguageMessage(language.shyGuildMemberGuildFullMessage, guild.name)
            return
        }

        guild.members.add(GuildMember().also {
            it.playerName = targetPlayerData.playerName
            it.playerUUID = targetPlayerData.playerUUID
        })
        guildService.saveGuild(guild)
        targetPlayerData.guilds.add(guild.name)
        cachePlayerDataRepository.save(targetPlayerData)
        guildService.applyGuildMemberPermissions(UUID.fromString(targetPlayerData.playerUUID), guild)
        sender.sendLanguageMessage(language.shyGuildMemberAddSuccessMessage, targetPlayerData.playerName, guild.name)
    }

    private suspend fun inviteMemberToGuild(sender: Player, guild: Guild, targetPlayer: Player) {
        val permission = settings.guildMemberInvitePermission.replace("<guild>", guild.name)

        if (!sender.hasPermission(permission)) {
            sender.sendLanguageMessage(language.shyGuildNoPermissionCommand)
            return
        }

        if (guild.getMember(targetPlayer) != null) {
            sender.sendLanguageMessage(language.shyGuildMemberAlreadyInGuildMessage, targetPlayer.name, guild.name)
            return
        }

        val success = guildService.sendInvite(GuildInvite().also {
            it.guildName = guild.name
            it.receiverUUID = targetPlayer.uniqueId
            it.senderUUID = sender.uniqueId
        })

        if (success) {
            sender.sendLanguageMessage(language.shyGuildMemberInviteSuccessMessage, targetPlayer.name, guild.name)
            targetPlayer.sendLanguageMessage(language.shyGuildMemberInviteReceivedMessage, guild.name, sender.name)
        } else {
            sender.sendLanguageMessage(language.shyGuildMemberInviteFailedMessage, targetPlayer.name, guild.name)
        }
    }

    private suspend fun removeMemberFromGuild(sender: CommandSender, guild: Guild, playerNameOrId: String) {
        val permission = settings.guildMemberRemovePermission.replace("<guild>", guild.name)

        if (!sender.hasPermission(permission)) {
            sender.sendLanguageMessage(language.shyGuildNoPermissionCommand)
            return
        }

        val member = guild.getMember(playerNameOrId)

        if (member == null) {
            sender.sendLanguageMessage(language.shyGuildPlayerNotAMemberMessage, playerNameOrId)
            return
        }

        val targetPlayerData = cachePlayerDataRepository.getByPlayerUUID(UUID.fromString(member.playerUUID))

        if (targetPlayerData == null) {
            sender.sendLanguageMessage(language.shyGuildPlayerNotFoundMessage, playerNameOrId)
            return
        }

        val owners =
            guild.members.filter { e -> e.roles.contains("owner") && e.playerUUID != member.playerUUID }

        if (owners.isEmpty()) {
            sender.sendLanguageMessage(language.shyGuildThereCannotBeNoOwnerMessage, guild.name)
            return
        }

        guild.members.remove(member)
        guildService.saveGuild(guild)
        targetPlayerData.guilds.remove(guild.name)
        cachePlayerDataRepository.save(targetPlayerData)
        guildService.applyGuildMemberPermissions(UUID.fromString(targetPlayerData.playerUUID), guild)
        sender.sendLanguageMessage(language.shyGuildMemberRemoveSuccessMessage, playerNameOrId, guild.name)
    }

    private suspend fun listMembers(sender: CommandSender, guild: Guild) {
        val permission = settings.guildMemberListPermission.replace("<guild>", guild.name)

        if (!sender.hasPermission(permission)) {
            sender.sendLanguageMessage(language.shyGuildNoPermissionCommand)
            return
        }

        sender.sendLanguageMessage(language.shyGuildMemberListMessage, guild.name)
        for (member in guild.members) {
            sender.sendMessage("- ${member.playerName} (${member.roles.joinToString(", ")})")
        }
    }

    private suspend fun acceptMemberInvite(sender: Player, guildName: String) {
        val accepted = guildService.acceptInvite(sender, guildName)
        val guild = guildService.getGuildCache().firstOrNull { e -> e.name.equals(guildName, true) }
        val playerData = cachePlayerDataRepository.getByPlayer(sender) ?: return

        if (playerData.guilds.size >= settings.maxJoinGuildsPerPlayer) {
            sender.sendLanguageMessage(language.shyGuildMemberMaxGuildsReachedMessage, sender.name)
            return
        }

        if (guild != null && guild.members.size >= guild.template!!.maxPlayers) {
            sender.sendLanguageMessage(language.shyGuildMemberGuildFullMessage, guildName)
            return
        }

        if (accepted && guild != null && guild.getMember(sender) == null) {
            guild.members.add(GuildMember().also {
                it.playerName = sender.name
                it.playerUUID = sender.uniqueId.toString()
                it.roles = hashSetOf(guild.template!!.defaultRole)
            })
            guildService.saveGuild(guild)
            playerData.guilds.add(guild.name)
            cachePlayerDataRepository.save(playerData)
            guildService.applyGuildMemberPermissions(sender.uniqueId, guild)
            sender.sendLanguageMessage(language.shyGuildMemberAcceptSuccessMessage, guildName)
        } else {
            sender.sendLanguageMessage(language.shyGuildMemberAcceptNoInviteMessage, guildName)
        }
    }

    private suspend fun createGuild(sender: CommandSender, template: GuildTemplate, name: String, displayName: String) {
        if (!sender.hasPermission(settings.templateUsePermission.replace("<template>", template.name))) {
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
            it.displayName = ChatColor.stripChatColors(displayName.translateChatColors())
            it.displayNameColor = displayName.translateChatColors()
            it.templateName = template.name
        }

        if (sender is Player) {
            guild.members.add(GuildMember().also {
                it.playerName = sender.name
                it.playerUUID = sender.uniqueId.toString()
                it.roles.add("owner")
            })

            val playerData = cachePlayerDataRepository.getByPlayer(sender) ?: return
            if (playerData.guilds.size >= settings.maxJoinGuildsPerPlayer) {
                sender.sendLanguageMessage(language.shyGuildMemberMaxGuildsReachedMessage, sender.name)
                return
            }

            val guildsWherePlayerIsOwner = guildService.getGuildCache().filter { e -> e.getMember(sender)?.roles?.contains("owner") == true }

            if (guildsWherePlayerIsOwner.size >= settings.maxCreateGuildsPerPlayer) {
                sender.sendLanguageMessage(language.shyGuildCreateMaxGuildsReachedMessage, sender.name)
                return
            }

            playerData.guilds.add(guild.name)
            cachePlayerDataRepository.save(playerData)
        }

        guildService.saveGuild(guild)
        if (sender is Player) {
            guildService.applyGuildMemberPermissions(sender.uniqueId, guild)
        }
        sender.sendLanguageMessage(language.shyGuildCreateSuccessMessage, guildName)
    }

    private suspend fun deleteGuild(sender: CommandSender, guild: Guild) {
        val permission = settings.guildDeletePermission.replace("<guild>", guild.name)

        if (!sender.hasPermission(permission)) {
            sender.sendLanguageMessage(language.shyGuildNoPermissionCommand)
            return
        }

        if (sender is Player) {
            val playerData = cachePlayerDataRepository.getByPlayer(sender)
            if (playerData != null) {
                playerData.guilds.remove(guild.name)
                cachePlayerDataRepository.save(playerData)
            }
        }

        guildService.deleteGuild(guild)
        val memberCopy = ArrayList(guild.members)
        guild.members.clear()
        for (member in memberCopy) {
            guildService.applyGuildMemberPermissions(UUID.fromString(member.playerUUID), guild)
        }
        sender.sendLanguageMessage(language.shyGuildDeleteSuccessMessage, guild.name)
    }

    private suspend fun listTemplates(sender: CommandSender) {
        val templates = guildTemplateRepository.getAll()
        sender.sendLanguageMessage(language.shyGuildTemplateListMessage)
        for (template in templates) {
            sender.sendMessage("- ${template.name}")
        }
    }

    private suspend fun CommandSender.sendLanguageMessage(languageItem: LanguageItem, vararg args: String) {
        val sender = this
        plugin.launch(plugin.globalRegionDispatcher) {
            chatMessageService.sendLanguageMessage(sender, languageItem, *args)
        }.join()
    }
}
