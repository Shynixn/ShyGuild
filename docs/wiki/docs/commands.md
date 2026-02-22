# Commands Reference

This page provides a complete reference for all ShyGuild commands. All commands require the `shyguild.command` permission unless otherwise specified.

## 🎮 Getting Started

To see all available commands in-game, use:
```
/shyguild help 1
```

---

## 📝 Command Overview

| Command | Purpose | Permission Required |
|---------|---------|-------------------|
| `/shyguild create` | Create a new guild from a template | `shyguild.cmd.create` |
| `/shyguild delete` | Delete an existing guild | `shyguild.cmd.delete` |
| `/shyguild role add` | Assign a role to a guild member | `shyguild.cmd.role.add` |
| `/shyguild role remove` | Remove a role from a guild member | `shyguild.cmd.role.remove` |
| `/shyguild role list` | List roles of a guild or player | `shyguild.cmd.role.list` |
| `/shyguild member add` | Add a player directly to a guild | `shyguild.cmd.member.add` |
| `/shyguild member remove` | Remove a player from a guild | `shyguild.cmd.member.remove` |
| `/shyguild member list` | List all members of a guild | `shyguild.cmd.member.list` |
| `/shyguild member invite` | Invite a player to a guild | `shyguild.cmd.member.invite` |
| `/shyguild member accept` | Accept a pending guild invite | `shyguild.cmd.member.accept` |
| `/shyguild member leave` | Leave a guild | `shyguild.cmd.member.leave` |
| `/shyguild template list` | List all loaded guild templates | `shyguild.cmd.template.list` |
| `/shyguild reload` | Reload all configurations | `shyguild.cmd.reload` |

---

## 🔧 Detailed Command Reference

### `/shyguild create`
**Purpose:** Create a new guild with the given name based on a template

```
/shyguild create <template> <name> <displayName>
```

**Parameters:**

* `<template>` - The guild template to use (required)
* `<name>` - The internal name of the guild (required, alphanumeric and hyphens only)
* `<displayName>` - The display name of the guild (required, underscores are replaced with spaces)

**Behavior:**

* 🏗️ **Creates:** A new guild based on the specified template
* 👑 **Owner:** If run by a player, that player becomes the guild owner
* 🔤 **Name rules:** Must be between the configured min/max length, alphanumeric and hyphens only
* 🚫 **Blacklist:** Names are checked against the configured word blacklist
* 🔒 **Requires:** `shyguild.template.<template>` permission to use the template

**Examples:**
```bash
# Create a guild using the sample_guild template
/shyguild create sample_guild my-guild My_Cool_Guild

# Create a PvP-focused guild
/shyguild create pvp_template warriors The_Warriors
```

**Common Use Cases:**

* Players creating their own guilds
* Admins setting up guilds for events or factions

---

### `/shyguild delete`
**Purpose:** Delete an existing guild

```
/shyguild delete <guild>
```

**Parameters:**

* `<guild>` - The name of the guild to delete (required)

**Behavior:**

* 🗑️ **Deletes:** The specified guild and all associated data
* 🔒 **Requires:** `shyguild.guild.<guild>.delete` permission

**Examples:**
```bash
# Delete a guild
/shyguild delete my-guild
```

**Common Use Cases:**

* Guild owners disbanding their guild
* Admins cleaning up inactive guilds

---

### `/shyguild role add`
**Purpose:** Assign a role to a guild member

```
/shyguild role add <guild> <role> [player]
```

**Parameters:**

* `<guild>` - The name of the guild (required)
* `<role>` - The name of the role to assign (required, must exist in the guild's template)
* `[player]` - Target player name (optional, defaults to command sender)

**Behavior:**

* ✅ **Assigns:** The specified role to the player in the guild
* 🔑 **Permissions:** Role permissions from the template are applied via LuckPerms (if installed)
* 👑 **Owner role:** If the role is `owner`, the player is also tracked as a guild creator
* 🔒 **Requires:** `shyguild.guild.<guild>.role.add.<role>` permission

**Examples:**
```bash
# Assign yourself the moderator role in a guild
/shyguild role add my-guild moderator

# Assign a role to a specific player
/shyguild role add my-guild officer Steve
```

**Common Use Cases:**

* Promoting guild members
* Assigning administrative roles within a guild

---

### `/shyguild role remove`
**Purpose:** Remove a role from a guild member

```
/shyguild role remove <guild> <role> [player]
```

**Parameters:**

* `<guild>` - The name of the guild (required)
* `<role>` - The name of the role to remove (required)
* `[player]` - Target player name (optional, defaults to command sender)

**Behavior:**

* ❌ **Removes:** The specified role from the player in the guild
* 🔑 **Permissions:** Role permissions are revoked via LuckPerms (if installed)
* 👑 **Owner role:** If the role is `owner`, the player is removed as a guild creator
* 🔒 **Requires:** `shyguild.guild.<guild>.role.remove.<role>` permission

**Examples:**
```bash
# Remove a role from yourself
/shyguild role remove my-guild moderator

# Remove a role from a specific player
/shyguild role remove my-guild officer Steve
```

**Common Use Cases:**

* Demoting guild members
* Revoking special permissions within a guild

---

### `/shyguild role list`
**Purpose:** List all roles of a guild or the roles of a specific player in a guild

```
/shyguild role list <guild> [player]
```

**Parameters:**

* `<guild>` - The name of the guild (required)
* `[player]` - Target player name (optional, lists all template roles if omitted)

**Behavior:**

* 📋 **Without player:** Lists all roles defined in the guild's template
* 👤 **With player:** Lists the roles assigned to that specific player in the guild
* 🔒 **Requires:** `shyguild.guild.<guild>.role.list` permission

**Examples:**
```bash
# List all roles in a guild
/shyguild role list my-guild

# List roles of a specific player
/shyguild role list my-guild Steve
```

**Common Use Cases:**

* Viewing available roles in a guild
* Checking what roles a member has

---

### `/shyguild member add`
**Purpose:** Add a player directly to a guild (administrative command)

```
/shyguild member add <guild> <player>
```

**Parameters:**

* `<guild>` - The name of the guild (required)
* `<player>` - The player name or UUID to add (required)

**Behavior:**

* ➕ **Adds:** The player directly to the guild without an invite
* 🔢 **Limits:** Respects max guild members and max guilds per player settings
* ⚠️ **Administrative:** Should only be used for administrative purposes
* 🔒 **Requires:** `shyguild.guild.<guild>.member.add` permission

**Examples:**
```bash
# Add a player to a guild
/shyguild member add my-guild Steve

# Add a player by UUID
/shyguild member add my-guild 550e8400-e29b-41d4-a716-446655440000
```

**Common Use Cases:**

* Admin-forced guild membership
* Restoring players to guilds after data issues

---

### `/shyguild member remove`
**Purpose:** Remove a player from a guild

```
/shyguild member remove <guild> <player>
```

**Parameters:**

* `<guild>` - The name of the guild (required)
* `<player>` - The player name or UUID to remove (required)

**Behavior:**

* ➖ **Removes:** The player from the guild
* 🔒 **Requires:** `shyguild.guild.<guild>.member.remove` permission

**Examples:**
```bash
# Remove a player from a guild
/shyguild member remove my-guild Steve
```

**Common Use Cases:**

* Kicking members from a guild
* Admin-forced removal of disruptive players

---

### `/shyguild member list`
**Purpose:** List all members of a guild

```
/shyguild member list <guild>
```

**Parameters:**

* `<guild>` - The name of the guild (required)

**Behavior:**

* 📋 **Lists:** All members along with their assigned roles
* 🔒 **Requires:** `shyguild.guild.<guild>.member.list` permission

**Examples:**
```bash
# List all members of a guild
/shyguild member list my-guild
```

**Common Use Cases:**

* Viewing guild roster
* Checking member roles and counts

---

### `/shyguild member invite`
**Purpose:** Invite an online player to join a guild

```
/shyguild member invite <guild> <player>
```

**Parameters:**

* `<guild>` - The name of the guild (required)
* `<player>` - The online player to invite (required)

**Behavior:**

* 📨 **Sends:** An invite to the target player
* 👤 **Player only:** The command sender must be a player
* 🔢 **Limits:** Respects the configured maximum number of pending invites
* 🔒 **Requires:** `shyguild.guild.<guild>.member.invite` permission

**Examples:**
```bash
# Invite a player to your guild
/shyguild member invite my-guild Alex
```

**Common Use Cases:**

* Guild owners/officers recruiting new members
* Growing guild membership through the invite system

---

### `/shyguild member accept`
**Purpose:** Accept a pending guild invite

```
/shyguild member accept <guild>
```

**Parameters:**

* `<guild>` - The name of the guild whose invite to accept (required)

**Behavior:**

* ✅ **Accepts:** A pending invite and joins the guild
* 👤 **Player only:** The command sender must be a player
* 🔢 **Limits:** Respects max guild members and max guilds per player settings
* 🔒 **Requires:** `shyguild.cmd.member.accept` permission

**Examples:**
```bash
# Accept an invite to a guild
/shyguild member accept my-guild
```

**Common Use Cases:**

* Players accepting guild invitations
* Responding to recruitment offers

---

### `/shyguild member leave`
**Purpose:** Leave a guild you are a member of

```
/shyguild member leave <guild>
```

**Parameters:**

* `<guild>` - The name of the guild to leave (required)

**Behavior:**

* 🚪 **Leaves:** Removes yourself from the guild
* 👤 **Player only:** The command sender must be a player
* 👑 **Owner restriction:** You cannot leave if you are the only owner — assign the owner role to another player first
* 🔒 **Requires:** `shyguild.guild.<guild>.member.leave` permission

**Examples:**
```bash
# Leave a guild
/shyguild member leave my-guild
```

**Common Use Cases:**

* Players voluntarily leaving a guild
* Switching to a different guild

---

### `/shyguild template list`
**Purpose:** List all loaded guild templates

```
/shyguild template list
```

**Parameters:** None

**Behavior:**

* 📋 **Lists:** All available guild templates by name
* 🔒 **Requires:** `shyguild.cmd.template.list` permission

**Examples:**
```bash
# View all available guild templates
/shyguild template list
```

**Common Use Cases:**

* Checking which templates are available before creating a guild
* Verifying template configurations loaded correctly

---

### `/shyguild reload`
**Purpose:** Reload all plugin configurations and guild data

```
/shyguild reload
```

**Parameters:** None

**Behavior:**

* 📁 **Reloads:** All configuration files and language files
* 🔄 **Refreshes:** Guild data is re-synchronized
* ⚡ **Updates:** Settings take effect immediately

**Examples:**
```bash
# Reload after editing configuration files
/shyguild reload
```

**Common Use Cases:**

* After editing configuration or language files
* Adding new guild templates without server restart
* Testing configuration changes

**⚠️ Important Notes:**

* Always run this command after editing `.yml` files
* Invalid configurations will show error messages in console

---

## 💡 Usage Tips

### Guild Creation Workflow
A typical workflow for setting up guilds:

```bash
# 1. Check available templates
/shyguild template list

# 2. Create a guild
/shyguild create sample_guild warriors The_Warriors

# 3. Invite members
/shyguild member invite warriors Alex
/shyguild member invite warriors Steve

# 4. Members accept invites
/shyguild member accept warriors

# 5. Assign roles to members
/shyguild role add warriors officer Alex
```

### Administrative Management
For server administrators managing guilds:

```bash
# Directly add a player to a guild (bypasses invite)
/shyguild member add warriors Steve

# Remove a problematic player
/shyguild member remove warriors Steve

# Check guild membership
/shyguild member list warriors

# Delete an inactive guild
/shyguild delete old-guild
```

### Role Management
When managing roles within guilds:

* Roles are defined in guild templates (e.g., `owner`, `member`, `officer`)
* Role permissions are applied via LuckPerms if installed
* The `owner` role has special significance — a guild must always have at least one owner

### Troubleshooting
If players report issues:
1. `/shyguild reload` — Refresh all configurations
2. Check permissions: `shyguild.command` for base access
3. Verify guild-specific permissions: `shyguild.guild.<guild>.*`
4. Ensure template permissions: `shyguild.template.<template>`
