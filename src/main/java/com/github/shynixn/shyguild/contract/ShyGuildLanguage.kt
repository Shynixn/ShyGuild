package com.github.shynixn.shyguild.contract

import com.github.shynixn.mcutils.common.language.LanguageItem
import com.github.shynixn.mcutils.common.language.LanguageProvider

interface ShyGuildLanguage : LanguageProvider {
  var shyGuildPlayerNotFoundMessage: LanguageItem

  var shyGuildNoPermissionCommand: LanguageItem

  var shyGuildReloadCommandHint: LanguageItem

  var shyGuildReloadMessage: LanguageItem

  var shyGuildCommonErrorMessage: LanguageItem

  var shyGuildCommandSenderHasToBePlayer: LanguageItem

  var shyGuildCommandUsage: LanguageItem

  var shyGuildCommandDescription: LanguageItem

  var shyGuildTemplateListCommandHint: LanguageItem

  var shyGuildTemplateListMessage: LanguageItem

  var shyGuildWordNotAllowedMessage: LanguageItem

  var shyGuildCreateCommandHint: LanguageItem

  var shyGuildCreateSuccessMessage: LanguageItem

  var shyGuildAlreadyExistsMessage: LanguageItem

  var shyGuildTemplateNotFoundMessage: LanguageItem

  var shyGuildNoPermissionTemplateMessage: LanguageItem

  var shyGuildGuildNotFoundMessage: LanguageItem

  var shyGuildRoleNotFoundMessage: LanguageItem

  var shyGuildNoPermissionRoleMessage: LanguageItem

  var shyGuildAddRoleCommandHint: LanguageItem

  var shyGuildRemoveRoleCommandHint: LanguageItem

  var shyGuildListRolesCommandHint: LanguageItem

  var shyGuildPlayerNotAMemberMessage: LanguageItem

  var shyGuildAssignRoleSuccessMessage: LanguageItem

  var shyGuildRemoveRoleSuccessMessage: LanguageItem

  var shyGuildRoleListAllMessage: LanguageItem

  var shyGuildRoleListPlayerMessage: LanguageItem

  var shyGuildDeleteCommandHint: LanguageItem

  var shyGuildDeleteSuccessMessage: LanguageItem

  var shyGuildMemberAddCommandHint: LanguageItem

  var shyGuildMemberAddSuccessMessage: LanguageItem

  var shyGuildMemberInviteCommandHint: LanguageItem

  var shyGuildMemberInviteSuccessMessage: LanguageItem

  var shyGuildMemberInviteReceivedMessage: LanguageItem

  var shyGuildMemberRemoveCommandHint: LanguageItem

  var shyGuildMemberRemoveSuccessMessage: LanguageItem

  var shyGuildMemberListCommandHint: LanguageItem

  var shyGuildMemberListMessage: LanguageItem

  var shyGuildMemberAlreadyInGuildMessage: LanguageItem

  var shyGuildMemberMaxGuildsReachedMessage: LanguageItem

  var shyGuildMemberInviteFailedMessage: LanguageItem

  var shyGuildMemberAcceptCommandHint: LanguageItem

  var shyGuildMemberAcceptSuccessMessage: LanguageItem

  var shyGuildMemberAcceptNoInviteMessage: LanguageItem

  var shyGuildMemberGuildFullMessage: LanguageItem

  var shyGuildCreateMaxGuildsReachedMessage: LanguageItem

  var shyGuildLeaveCommandHint: LanguageItem

  var shyGuildLeaveSuccessMessage: LanguageItem

  var shyGuildCannotLeaveOwnerGuildMessage: LanguageItem

  var shyGuildThereCannotBeNoOwnerMessage: LanguageItem
}
