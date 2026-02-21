package com.github.shynixn.shyguild.enumeration

enum class Permission(val text: String) {
    COMMAND("shyguild.command"),
    RELOAD("shyguild.reload"),
    TEMPLATE_LIST("shyguild.template.list"),
    DYN_TEMPLATE("shyguild.template.use.<template>"),
    GUILD_ADD_ROLE("shyguild.guild.<guild>.role.add.<role>"),
    GUILD_REMOVE_ROLE("shyguild.guild.<guild>.role.remove.<role>"),
    GUILD_LIST_ROLE("shyguild.guild.<guild>.role.list"),
    DELETE("shyguild.guild.<guild>.delete")
}
