package com.github.shynixn.shyguild.entity

import com.github.shynixn.mcutils.common.repository.Element

class ShyGuildMeta : Element {
    /**
     * Unique Identifier of the element.
     */
    override var name: String = ""

    /**
     * Name of the template.
     */
    var template: String = ""

    /**
     * All guids members of the guild.
     */
    var members : List<String> = ArrayList()
}
