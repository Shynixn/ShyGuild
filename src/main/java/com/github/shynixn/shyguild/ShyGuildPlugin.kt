package com.github.shynixn.shyguild

import com.github.shynixn.mccoroutine.folia.*
import com.github.shynixn.mcutils.common.ChatColor
import com.github.shynixn.mcutils.common.CoroutineHandler
import com.github.shynixn.mcutils.common.Version
import com.github.shynixn.mcutils.common.checkIfFoliaIsLoadable
import com.github.shynixn.mcutils.common.commonServer
import com.github.shynixn.mcutils.common.di.DependencyInjectionModule
import com.github.shynixn.mcutils.common.language.reloadTranslation
import com.github.shynixn.mcutils.common.placeholder.PlaceHolderService
import com.github.shynixn.mcutils.common.placeholder.PlaceHolderServiceImpl
import com.github.shynixn.mcutils.common.repository.CacheRepository
import com.github.shynixn.mcutils.database.api.PlayerDataRepository
import com.github.shynixn.mcutils.database.impl.SqliteConnectionServiceImpl
import com.github.shynixn.shyguild.contract.GuildMetaSqlRepository
import com.github.shynixn.shyguild.contract.GuildService
import com.github.shynixn.shyguild.entity.ShyGuildSettings
import com.github.shynixn.shyguild.entity.GuildTemplate
import com.github.shynixn.shyguild.entity.PlayerInformation
import com.github.shynixn.shyguild.enumeration.PlaceHolder
import com.github.shynixn.shyguild.impl.commandexecutor.ShyGuildCommandExecutor
import com.github.shynixn.shyguild.impl.listener.ShyGuildListener
import kotlinx.coroutines.Job
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level
import kotlin.coroutines.CoroutineContext

class ShyGuildPlugin : JavaPlugin(), CoroutineHandler {
    private val prefix: String = ChatColor.BLUE.toString() + "[ShyGuild] " + ChatColor.WHITE
    private var module: DependencyInjectionModule? = null

    companion object {
        val guildNameKey = "guildName"
        private val areLegacyVersionsIncluded: Boolean by lazy {
            try {
                Class.forName("com.github.shynixn.shyguild.lib.com.github.shynixn.mcutils.packet.nms.v1_8_R3.PacketSendServiceImpl")
                true
            } catch (e: ClassNotFoundException) {
                false
            }
        }
    }

    override fun onEnable() {
        Bukkit.getServer().consoleSender.sendMessage(prefix + ChatColor.GREEN + "Loading ShyGuild ...")
        commonServer = Bukkit.getServer()
        this.saveDefaultConfig()
        this.reloadConfig()
        val versions = if (areLegacyVersionsIncluded) {
            listOf(
                Version.VERSION_1_8_R3,
                Version.VERSION_1_9_R2,
                Version.VERSION_1_10_R1,
                Version.VERSION_1_11_R1,
                Version.VERSION_1_12_R1,
                Version.VERSION_1_13_R1,
                Version.VERSION_1_13_R2,
                Version.VERSION_1_14_R1,
                Version.VERSION_1_15_R1,
                Version.VERSION_1_16_R1,
                Version.VERSION_1_16_R2,
                Version.VERSION_1_16_R3,
                Version.VERSION_1_17_R1,
                Version.VERSION_1_18_R1,
                Version.VERSION_1_18_R2,
                Version.VERSION_1_19_R1,
                Version.VERSION_1_19_R2,
                Version.VERSION_1_19_R3,
                Version.VERSION_1_20_R1,
                Version.VERSION_1_20_R2,
                Version.VERSION_1_20_R3,
                Version.VERSION_1_20_R4,
                Version.VERSION_1_21_R1,
                Version.VERSION_1_21_R2,
                Version.VERSION_1_21_R3,
                Version.VERSION_1_21_R4,
                Version.VERSION_1_21_R5,
                Version.VERSION_1_21_R6,
                Version.VERSION_1_21_R7
            )
        } else {
            listOf(Version.VERSION_1_21_R7)
        }

        if (!Version.serverVersion.isCompatible(*versions.toTypedArray())) {
            logger.log(Level.SEVERE, "================================================")
            logger.log(Level.SEVERE, "ShyGuild does not support your server version")
            logger.log(Level.SEVERE, "Install v" + versions[0].from + " - v" + versions[versions.size - 1].to)
            logger.log(Level.SEVERE, "Need support for a particular version? Go to https://www.patreon.com/Shynixn")
            logger.log(Level.SEVERE, "Plugin gets now disabled!")
            logger.log(Level.SEVERE, "================================================")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }

        logger.log(Level.INFO, "Loaded NMS version ${Version.serverVersion}.")
        if (mcCoroutineConfiguration.isFoliaLoaded && !checkIfFoliaIsLoadable()) {
            logger.log(Level.SEVERE, "================================================")
            logger.log(Level.SEVERE, "ShyGuild for Folia requires ShyGuild-Premium-Folia.jar")
            logger.log(Level.SEVERE, "Go to https://www.patreon.com/Shynixn to download it.")
            logger.log(Level.SEVERE, "Plugin gets now disabled!")
            logger.log(Level.SEVERE, "================================================")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }

        // Register Language
        val language = ShyGuildLanguageImpl()
        reloadTranslation(language)
        logger.log(Level.INFO, "Loaded language file.")

        // Module
        val plugin = this
        val settings = ShyGuildSettings { settings ->
            settings.commandAliases = plugin.config.getStringList("commands.shyguild.aliases")
            settings.joinDelaySeconds = plugin.config.getInt("global.joinDelaySeconds")
            settings.maxJoinGuildsPerPlayer = plugin.config.getInt("global.maxJoinGuildsPerPlayer")
            settings.maxCreateGuildsPerPlayer = plugin.config.getInt("global.maxCreateGuildsPerPlayer")
            settings.synchronizeGuildsSeconds = plugin.config.getInt("global.synchronizeGuildsSeconds")
            settings.blackList = plugin.config.getStringList("global.blackList")
            settings.guildNameMinLength = plugin.config.getInt("global.guildNameMinLength")
            settings.guildNameMaxLength = plugin.config.getInt("global.guildNameMaxLength")
            settings.guildDisplayNameMinLength = plugin.config.getInt("global.guildDisplayNameMinLength")
            settings.guildDisplayNameMaxLength = plugin.config.getInt("global.guildDisplayNameMaxLength")
            settings.guildMaxInvites = plugin.config.getInt("global.guildMaxInvites")
        }
        settings.reload()
        val placeHolderService = PlaceHolderServiceImpl(this, Bukkit.getPluginManager())
        val sqlConnectionService =
            SqliteConnectionServiceImpl(plugin.dataFolder.toPath().resolve("ShyGuild.sqlite"), plugin.logger)
        this.module = ShyGuildDependencyInjectionModule(
            this,
            this,
            settings,
            language,
            placeHolderService,
            sqlConnectionService
        ).build()
        val guildService = module!!.getService<GuildService>()

        // Register PlaceHolders
        PlaceHolder.registerAll(
            this,
            this.module!!.getService<PlaceHolderService>(),
            module!!.getService<GuildService>()
        )

        // Register Listeners
        Bukkit.getPluginManager().registerEvents(module!!.getService<ShyGuildListener>(), this)

        // Register CommandExecutor
        module!!.getService<ShyGuildCommandExecutor>()
        val templateService = module!!.getService<CacheRepository<GuildTemplate>>()
        val playerDataRepository = module!!.getService<PlayerDataRepository<PlayerInformation>>()
        val guildMetaRepository = module!!.getService<GuildMetaSqlRepository>()
        plugin.launch {
            templateService.getAll()
            playerDataRepository.createIfNotExist()
            guildMetaRepository.createIfNotExist()
            for (player in Bukkit.getOnlinePlayers()) {
                guildService.getGuilds(player)
            }
            Bukkit.getServer().consoleSender.sendMessage(prefix + ChatColor.GREEN + "Enabled ShyGuild " + plugin.description.version + " by Shynixn")
        }
    }

    override fun execute(
        coroutineContext: CoroutineContext,
        f: suspend () -> Unit
    ): Job {
        return launch(coroutineContext) {
            f.invoke()
        }
    }

    override fun execute(f: suspend () -> Unit): Job {
        return launch {
            f.invoke()
        }
    }

    override fun fetchEntityDispatcher(entity: Entity): CoroutineContext {
        return entityDispatcher(entity)
    }

    override fun fetchGlobalRegionDispatcher(): CoroutineContext {
        return globalRegionDispatcher
    }

    override fun fetchLocationDispatcher(location: Location): CoroutineContext {
        return regionDispatcher(location)
    }

    override fun onDisable() {
        if (module == null) {
            return
        }

        module!!.close()
        module = null
    }
}
