package com.github.shynixn.shyguild.entity

import java.util.Date
import java.util.UUID

class GuildInvite {
    var creationDate = Date()
    var guildName: String = ""
    var senderUUID: UUID = UUID.randomUUID()
    var receiverUUID: UUID = UUID.randomUUID()
}