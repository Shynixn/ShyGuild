package com.github.shynixn.shyguild.entity

import com.github.shynixn.shyguild.enumeration.Permission


class ShyGuildSettings(private val reloadFun: (ShyGuildSettings) -> Unit) {
    var baseCommand: String = "shyguild"

    var commandAliases: List<String> = ArrayList()

    var guildArgument = "guild"

    var maxCreateGuildsPerPlayer: Int = 1

    var maxJoinGuildsPerPlayer: Int = 3

    var joinDelaySeconds = 3

    var synchronizeGuildsSeconds = 300

    var blackList: List<String> = ArrayList()

    var guildMaxInvites: Int = 5

    var guildNameMinLength: Int = 3

    var guildNameMaxLength: Int = 16

    var guildDisplayNameMinLength: Int = 3

    var guildDisplayNameMaxLength: Int = 32

    var defaultTemplates: List<Pair<String, String>> = listOf(
        "guild/sample_guild.yml" to "sample_guild.yml"
    )

    var commandPermission: String = Permission.COMMAND.text

    var createCmdPermission: String = Permission.CMD_CREATE.text
    var templateUsePermission: String = Permission.TEMPLATE_USE.text

    var deleteCmdPermission: String = Permission.CMD_DELETE.text
    var guildDeletePermission: String = Permission.DELETE_GUILD.text

    var reloadCmdPermission: String = Permission.CMD_RELOAD.text

    var templateListCmdPermission: String = Permission.CMD_TEMPLATE_LIST.text

    var addRoleCmdPermission: String = Permission.CMD_ROLE_ADD.text
    var guildAddRolePermission: String = Permission.GUILD_ADD_ROLE.text

    var removeRoleCmdPermission: String = Permission.CMD_ROLE_REMOVE.text
    var guildRemoveRolePermission: String = Permission.GUILD_REMOVE_ROLE.text

    var listRoleCmdPermission: String = Permission.CMD_LIST_ROLE.text
    var guildListRolePermission: String = Permission.GUILD_LIST_ROLE.text

    var addMemberPermission: String = Permission.CMD_MEMBER_ADD.text
    var guildMemberAddPermission: String = Permission.GUILD_MEMBER_ADD.text

    var removeMemberPermission: String = Permission.CMD_MEMBER_REMOVE.text
    var guildMemberRemovePermission: String = Permission.GUILD_MEMBER_REMOVE.text

    var listMembersPermission: String = Permission.CMD_MEMBER_LIST.text
    var guildMemberListPermission: String = Permission.GUILD_MEMBER_LIST.text

    var inviteMemberPermission: String = Permission.CMD_MEMBER_INVITE.text
    var guildMemberInvitePermission: String = Permission.GUILD_MEMBER_INVITE.text

    var acceptMemberPermission = Permission.CMD_MEMBER_ACCEPT.text

    var leaveMemberPermission = Permission.CND_MEMBER_LEAVE.text
    var guildMemberLeavePermission = Permission.GUILD_MEMBER_LEAVE.text

    var guildListPermission = Permission.CMD_GUILD_LIST.text

    /**
     * Reloads the config.
     */
    fun reload() {
        reloadFun.invoke(this)
    }
}
