package com.github.shynixn.shyguild

import com.github.shynixn.fasterxml.jackson.core.type.TypeReference
import com.github.shynixn.mcutils.common.ConfigurationService
import com.github.shynixn.mcutils.common.ConfigurationServiceImpl
import com.github.shynixn.mcutils.common.CoroutineHandler
import com.github.shynixn.mcutils.common.chat.ChatMessageService
import com.github.shynixn.mcutils.common.command.CommandService
import com.github.shynixn.mcutils.common.command.CommandServiceImpl
import com.github.shynixn.mcutils.common.di.DependencyInjectionModule
import com.github.shynixn.mcutils.common.placeholder.PlaceHolderService
import com.github.shynixn.mcutils.common.repository.CacheRepository
import com.github.shynixn.mcutils.common.repository.CachedRepositoryImpl
import com.github.shynixn.mcutils.common.repository.Repository
import com.github.shynixn.mcutils.common.repository.YamlFileRepositoryImpl
import com.github.shynixn.mcutils.database.api.CachePlayerRepository
import com.github.shynixn.mcutils.database.api.PlayerDataRepository
import com.github.shynixn.mcutils.database.api.SqlConnectionService
import com.github.shynixn.mcutils.database.impl.AutoSavePlayerDataRepositoryImpl
import com.github.shynixn.mcutils.database.impl.CachedPlayerDataRepositoryImpl
import com.github.shynixn.mcutils.database.impl.PlayerDataSqlRepositoryImpl
import com.github.shynixn.mcutils.packet.api.PacketService
import com.github.shynixn.mcutils.packet.impl.service.ChatMessageServiceImpl
import com.github.shynixn.mcutils.packet.impl.service.PacketServiceImpl
import com.github.shynixn.shyguild.contract.GuildMetaSqlRepository
import com.github.shynixn.shyguild.contract.GuildService
import com.github.shynixn.shyguild.contract.PermissionPluginService
import com.github.shynixn.shyguild.contract.ShyGuildLanguage
import com.github.shynixn.shyguild.entity.Guild
import com.github.shynixn.shyguild.entity.PlayerInformation
import com.github.shynixn.shyguild.entity.GuildTemplate
import com.github.shynixn.shyguild.entity.ShyGuildSettings
import com.github.shynixn.shyguild.impl.commandexecutor.ShyGuildCommandExecutor
import com.github.shynixn.shyguild.impl.listener.ShyGuildListener
import com.github.shynixn.shyguild.impl.service.EmptyPermissionPluginServiceImpl
import com.github.shynixn.shyguild.impl.service.GuildMetaSqlRepositoryImpl
import com.github.shynixn.shyguild.impl.service.GuildServiceImpl
import com.github.shynixn.shyguild.impl.service.LuckPermsPermissionServiceImpl
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.ServicePriority

class ShyGuildDependencyInjectionModule(
    private val plugin: Plugin,
    private val coroutineHandler: CoroutineHandler,
    private val settings: ShyGuildSettings,
    private val language: ShyGuildLanguage,
    private val placeHolderService: PlaceHolderService,
    private val sqlConnectionService: SqlConnectionService
) {
    fun build(): DependencyInjectionModule {
        val module = DependencyInjectionModule()

        // Params
        module.addService<Plugin>(plugin)
        module.addService<CoroutineHandler>(coroutineHandler)
        module.addService<ShyGuildLanguage>(language)
        module.addService<ShyGuildSettings>(settings)
        module.addService<PlaceHolderService>(placeHolderService)

        // Repositories
        module.addService<Repository<GuildTemplate>> {
            module.getService<CacheRepository<GuildTemplate>>()
        }
        module.addService<CacheRepository<GuildTemplate>> {
            CachedRepositoryImpl(
                YamlFileRepositoryImpl<GuildTemplate>(
                    plugin,
                    "guild",
                    plugin.dataFolder.toPath().resolve("guild"),
                    settings.defaultTemplates,
                    emptyList(),
                    object : TypeReference<GuildTemplate>() {})
            )
        }
        module.addService<PlayerDataRepository<PlayerInformation>> {
            module.getService<CachePlayerRepository<PlayerInformation>>()
        }
        module.addService<CachePlayerRepository<PlayerInformation>> {
            AutoSavePlayerDataRepositoryImpl(
                1000 * 60L * plugin.config.getInt("database.autoSaveIntervalMinutes"), CachedPlayerDataRepositoryImpl(
                    PlayerDataSqlRepositoryImpl(
                        "${plugin.name}GuildPlayer",
                        plugin.config.getLong("database.readDelayMs"),
                        object : TypeReference<PlayerInformation>() {},
                        sqlConnectionService
                    )
                ), coroutineHandler
            )
        }
        module.addService<GuildMetaSqlRepository> {
            GuildMetaSqlRepositoryImpl(
                "${plugin.name}Guild", sqlConnectionService, object : TypeReference<Guild>() {})
        }

        // Services
        module.addService<ShyGuildCommandExecutor> {
            ShyGuildCommandExecutor(
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService()
            )
        }
        module.addService<ShyGuildListener> {
            ShyGuildListener(module.getService(), module.getService(), module.getService())
        }
        module.addService<GuildService> {
            GuildServiceImpl(
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService()
            )
        }
        module.addService<PermissionPluginService> {
            if (plugin.server.pluginManager.getPlugin("LuckPerms") != null) {
                LuckPermsPermissionServiceImpl(module.getService())
            } else {
                EmptyPermissionPluginServiceImpl()
            }
        }
        module.addService<ConfigurationService> {
            ConfigurationServiceImpl(module.getService())
        }
        module.addService<PacketService> {
            PacketServiceImpl(module.getService())
        }
        module.addService<CommandService> {
            CommandServiceImpl(module.getService())
        }
        module.addService<ChatMessageService> {
            ChatMessageServiceImpl(module.getService(), module.getService(), module.getService())
        }

        // Developer Api
        Bukkit.getServicesManager().register(
            GuildService::class.java, module.getService<GuildService>(), plugin, ServicePriority.Normal
        )

        return module
    }
}
