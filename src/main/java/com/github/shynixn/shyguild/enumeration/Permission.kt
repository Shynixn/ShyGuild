package com.github.shynixn.shyguild.enumeration

enum class Permission(val text: String) {
    COMMAND("shyguild.command"),

    CMD_CREATE("shyguild.cmd.create"),
    TEMPLATE_USE("shyguild.template.<template>"),

    CMD_DELETE("shyguild.cmd.delete"),
    DELETE_GUILD("shyguild.guild.<guild>.delete"),

    CMD_ROLE_ADD("shyguild.cmd.role.add"),
    GUILD_ADD_ROLE("shyguild.guild.<guild>.role.add.<role>"),

    CMD_ROLE_REMOVE("shyguild.cmd.role.remove"),
    GUILD_REMOVE_ROLE("shyguild.guild.<guild>.role.remove.<role>"),

    CMD_LIST_ROLE("shyguild.cmd.role.list"),
    GUILD_LIST_ROLE("shyguild.guild.<guild>.role.list"),

    CMD_MEMBER_ADD("shyguild.cmd.member.add"),
    GUILD_MEMBER_ADD("shyguild.guild.<guild>.member.add"),

    CMD_MEMBER_REMOVE("shyguild.cmd.member.remove"),
    GUILD_MEMBER_REMOVE("shyguild.guild.<guild>.member.remove"),

    CMD_MEMBER_LIST("shyguild.cmd.member.list"),
    GUILD_MEMBER_LIST("shyguild.guild.<guild>.member.list"),

    CMD_MEMBER_INVITE("shyguild.cmd.member.invite"),
    GUILD_MEMBER_INVITE("shyguild.guild.<guild>.member.invite"),

    CND_MEMBER_LEAVE("shyguild.cmd.member.leave"),
    GUILD_MEMBER_LEAVE("shyguild.guild.<guild>.member.leave"),

    CMD_MEMBER_ACCEPT("shyguild.cmd.member.accept"),

    CMD_RELOAD("shyguild.cmd.reload"),

    CMD_TEMPLATE_LIST("shyguild.cmd.template.list"),
}
