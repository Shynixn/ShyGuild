package com.github.shynixn.shyguild.impl.commandexecutor

import com.github.shynixn.mccoroutine.folia.globalRegionDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mcutils.common.CoroutineHandler
import com.github.shynixn.mcutils.common.chat.ChatMessageService
import com.github.shynixn.mcutils.common.command.CommandBuilder
import com.github.shynixn.mcutils.common.command.CommandService
import com.github.shynixn.mcutils.common.command.Validator
import com.github.shynixn.mcutils.common.language.LanguageItem
import com.github.shynixn.mcutils.common.placeholder.PlaceHolderService
import com.github.shynixn.mcutils.common.repository.CacheRepository
import com.github.shynixn.shyguild.contract.GuildService
import com.github.shynixn.shyguild.contract.ShyGuildLanguage
import com.github.shynixn.shyguild.entity.ShyGuildSettings
import com.github.shynixn.shyguild.entity.ShyGuildTemplate
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
    private val guildTemplateRepository: CacheRepository<ShyGuildTemplate>,
    private val guildMetaRepository: CacheRepository<ShyGuildTemplate>,
    private val placeHolderService: PlaceHolderService,
    coroutineHandler: CoroutineHandler,
    commandService: CommandService
) {
    private val senderHasToBePlayer: () -> String = {
        language.shyGuildCommandSenderHasToBePlayer.text
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

    private val booleanTabs: (CommandSender) -> List<String> = {
        listOf("true", "false")
    }


    private val onlinePlayerTabs: (CommandSender) -> List<String> = {
        Bukkit.getOnlinePlayers().map { e -> e.name }
    }

    init {
        commandService.registerCommand(CommandBuilder(coroutineHandler, plugin, settings.baseCommand, chatMessageService) {
            usage(language.shyGuildCommandUsage.text)
            description(language.shyGuildCommandDescription.text)
            aliases(settings.commandAliases)
            permission(settings.commandPermission)
            permissionMessage(language.shyGuildNoPermissionCommand.text)
         /*   subCommand("reload") {
                permission(settings)
                toolTip {
                    language.shyGuild.text
                }
                builder().execute { sender ->
                    plugin.saveDefaultConfig()
                    plugin.reloadConfig()
                    plugin.reloadTranslation(language)
                    guildService.reload()
                    sender.sendLanguageMessage(language.shyGuildReloadMessage)
                }
            }*/.helpCommand()
        })
    }

    private fun CommandSender.sendLanguageMessage(languageItem: LanguageItem, vararg args: String) {
        val sender = this
        plugin.launch(plugin.globalRegionDispatcher) {
            chatMessageService.sendLanguageMessage(sender, languageItem, *args)
        }
    }
}
