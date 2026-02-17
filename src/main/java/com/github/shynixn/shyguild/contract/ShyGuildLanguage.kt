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
}
