package com.github.shynixn.shyguild

import com.github.shynixn.mcutils.common.language.LanguageItem
import com.github.shynixn.shyguild.contract.ShyGuildLanguage

class ShyGuildLanguageImpl : ShyGuildLanguage {
 override val names: List<String>
  get() = listOf("en_us")
 override var shyGuildPlayerNotFoundMessage = LanguageItem("[&9ShyGuild&f] &cPlayer %shyguild_param_1% not found.")

 override var shyGuildNoPermissionCommand = LanguageItem("[&9ShyGuild&f] &cYou do not have permission to execute this command.")

 override var shyGuildReloadCommandHint = LanguageItem("Reloads all guilds and configuration.")

 override var shyGuildReloadMessage = LanguageItem("[&9ShyGuild&f] Reloaded all guilds and configuration.")

 override var shyGuildCommonErrorMessage = LanguageItem("[&9ShyGuild&f]&c A problem occurred. Check the console log for details.")

 override var shyGuildCommandSenderHasToBePlayer = LanguageItem("[&9ShyGuild&f] The command sender has to be a player if you do not specify the optional player argument.")

 override var shyGuildCommandUsage = LanguageItem("[&9ShyGuild&f] Use /shyguild help to see more info about the plugin.")

 override var shyGuildCommandDescription = LanguageItem("[&9ShyGuild&f] All commands for the ShyGuild plugin.")
}
