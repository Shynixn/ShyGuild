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
import com.github.shynixn.mcutils.packet.api.PacketService
import com.github.shynixn.mcutils.packet.impl.service.ChatMessageServiceImpl
import com.github.shynixn.mcutils.packet.impl.service.PacketServiceImpl
import com.github.shynixn.mcutils.worldguard.WorldGuardService
import com.github.shynixn.shyguild.contract.ShyGuildLanguage
import com.github.shynixn.shyguild.entity.ShyGuildTemplate
import com.github.shynixn.shyguild.entity.ShyGuildSettings
import com.github.shynixn.shyguild.impl.commandexecutor.ShyGuildCommandExecutor
import com.github.shynixn.shyguild.impl.listener.ShyGuildListener
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.ServicePriority

class ShyGuildDependencyInjectionModule(
    private val plugin: Plugin,
    private val settings: ShyGuildSettings,
    private val language: ShyGuildLanguage,
    private val worldGuardService: WorldGuardService,
    private val placeHolderService: PlaceHolderService
) {
    fun build(): DependencyInjectionModule {
        val module = DependencyInjectionModule()

        // Params
        module.addService<Plugin>(plugin)
        module.addService<CoroutineHandler>(plugin)
        module.addService<ShyGuildLanguage>(language)
        module.addService<ShyGuildSettings>(settings)
        module.addService<PlaceHolderService>(placeHolderService)
        module.addService<WorldGuardService>(worldGuardService)

        // Repositories
        val templateRepositoryImpl = YamlFileRepositoryImpl<ShyGuildTemplate>(
            plugin,
            "guild",
            plugin.dataFolder.toPath().resolve("guild"),
            settings.defaultScoreboards,
            emptyList(),
            object : TypeReference<ShyGuildTemplate>() {})
        val cacheTemplateRepository = CachedRepositoryImpl(templateRepositoryImpl)
        module.addService<Repository<ShyGuildTemplate>>(cacheTemplateRepository)
        module.addService<CacheRepository<ShyGuildTemplate>>(cacheTemplateRepository)

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
                module.getService()
            )
        }
        module.addService<ShyGuildListener> {
            ShyGuildListener(module.getService(), module.getService(), module.getService())
        }
        module.addService<ScoreboardFactory> {
            ScoreboardFactoryImpl(module.getService(), module.getService(), module.getService())
        }
        module.addService<ScoreboardService> {
            ScoreboardServiceImpl(
                module.getService(), module.getService(), module.getService(), module.getService(), module.getService()
            )
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
            ScoreboardService::class.java, module.getService<ScoreboardService>(), plugin, ServicePriority.Normal
        )
        Bukkit.getServicesManager().register(
            ScoreboardFactory::class.java, module.getService<ScoreboardFactory>(), plugin, ServicePriority.Normal
        )

        return module
    }
}
