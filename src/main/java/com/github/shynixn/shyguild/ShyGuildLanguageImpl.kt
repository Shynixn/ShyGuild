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

 override var shyGuildTemplateListCommandHint = LanguageItem("Displays all loaded guild templates.")

 override var shyGuildTemplateListMessage = LanguageItem("[&9ShyGuild&f] Templates:")

 override var shyGuildWordNotAllowedMessage = LanguageItem("[&9ShyGuild&f]&c The text contains invalid words, is too short or too long. Please make sure to follow the rules and try again.")

 override var shyGuildCreateCommandHint = LanguageItem("Creates a new guild with the given name based on the given template.")

 override var shyGuildCreateSuccessMessage = LanguageItem("[&9ShyGuild&f] Successfully created guild %shyguild_param_1%.")

 override var shyGuildAlreadyExistsMessage = LanguageItem("[&9ShyGuild&f]&c A guild with the name %shyguild_param_1% already exists.")

 override var shyGuildTemplateNotFoundMessage = LanguageItem("[&9ShyGuild&f]&c Guild template %shyguild_param_1% not found.")

 override var shyGuildNoPermissionTemplateMessage = LanguageItem("[&9ShyGuild&f]&c You do not have permission to use guild template %shyguild_param_1%.")

 override var shyGuildGuildNotFoundMessage = LanguageItem("[&9ShyGuild&f]&c Guild %shyguild_param_1% not found.")

 override var shyGuildRoleNotFoundMessage = LanguageItem("[&9ShyGuild&f]&c Role %shyguild_param_1% not found in guild %shyguild_param_2%.")

 override var shyGuildNoPermissionRoleMessage = LanguageItem("[&9ShyGuild&f]&c You do not have permission to assign role %shyguild_param_1%.")

 override var shyGuildAddRoleCommandHint = LanguageItem("Assigns the given role to the given player in the given guild.")

 override var shyGuildRemoveRoleCommandHint = LanguageItem("Removes the given role from the given player in the given guild.")

 override var shyGuildListRolesCommandHint = LanguageItem("Lists all roles of the given guild or the roles of a single player in the given guild.")

 override var shyGuildPlayerNotAMemberMessage = LanguageItem("[&9ShyGuild&f]&c Player %shyguild_param_1% is not a member of this guild.")

 override var shyGuildAssignRoleSuccessMessage = LanguageItem("[&9ShyGuild&f] Successfully assigned role %shyguild_param_1% to player %shyguild_param_2%.")

 override var shyGuildRemoveRoleSuccessMessage = LanguageItem("[&9ShyGuild&f] Successfully removed role %shyguild_param_1% from player %shyguild_param_2%.")

 override var shyGuildRoleListAllMessage = LanguageItem("[&9ShyGuild&f] All roles of this guild:")

 override var shyGuildRoleListPlayerMessage = LanguageItem("[&9ShyGuild&f] All roles of player %shyguild_param_1%:")
}
