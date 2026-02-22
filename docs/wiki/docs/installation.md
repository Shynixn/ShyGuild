# Configuration Guide

This guide will walk you through installing and configuring ShyGuild. You'll learn how to use the built-in sample guild template and then create a fully custom guild template from scratch.

## 📋 Prerequisites

* A Bukkit or Folia based Minecraft server (1.8 – 1.21)
* **LuckPerms** (recommended) — for automatic role-based permission management
* **PlaceholderAPI** (optional) — for placeholder support in other plugins

---

## 🔧 Installation

1. Download the ShyGuild `.jar` file
2. Place it in your server's `plugins/` folder
3. Start (or restart) the server
4. ShyGuild generates its default files:

```
plugins/ShyGuild/
├── config.yml                  # Main configuration
├── lang/
│   └── en_us.yml               # Language file
└── guild/
    └── sample_guild.yml        # Sample guild template
```

---

## 📂 Understanding the config.yml

The main configuration file controls global settings:

```yaml
# Language file to use
language: "en_us"

# Command aliases
commands:
  shyguild:
    aliases:
      - "sguild"

# Database settings (sqlite or mysql)
database:
  type: "sqlite"

# Global guild settings
global:
  joinDelaySeconds: 3             # Delay before loading guilds on player join
  synchronizeGuildsSeconds: 300   # Cross-server sync interval (MySQL only)
  maxJoinGuildsPerPlayer: 3       # Max guilds a player can be in at once
  maxCreateGuildsPerPlayer: 1     # Max guilds a player can create
  guildNameMinLength: 3           # Min characters for guild names
  guildNameMaxLength: 16          # Max characters for guild names
  guildDisplayNameMinLength: 3    # Min characters for display names
  guildDisplayNameMaxLength: 32   # Max characters for display names
  guildMaxInvites: 5              # Max pending invites per player
  blackList:                      # Blocked words for guild names
    - "badword"
```

---

## 🏰 Using the Sample Guild Template

ShyGuild ships with a `sample_guild.yml` template in the `plugins/ShyGuild/guild/` folder. This section walks through using it from both an admin and a player perspective.

### Understanding the Sample Template

```yaml
name: "sample_guild"
maxPlayers: 10
defaultRole: "member"
roles:
  - name: "owner"
    allowPermissions:
      - "shyguild.guild.%shyguild_guild_name%.delete"
      - "shyguild.guild.%shyguild_guild_name%.role.add.owner"
      - "shyguild.guild.%shyguild_guild_name%.role.remove.owner"
      - "shyguild.guild.%shyguild_guild_name%.role.add.member"
      - "shyguild.guild.%shyguild_guild_name%.role.remove.member"
      - "shyguild.guild.%shyguild_guild_name%.role.list"
      - "shyguild.guild.%shyguild_guild_name%.member.remove"
      - "shyguild.guild.%shyguild_guild_name%.member.list"
      - "shyguild.guild.%shyguild_guild_name%.invite"
      - "shyguild.guild.%shyguild_guild_name%.leave"
    denyPermissions: []
  - name: "member"
    allowPermissions:
      - "shyguild.guild.%shyguild_guild_name%.role.list"
      - "shyguild.guild.%shyguild_guild_name%.member.list"
      - "shyguild.guild.%shyguild_guild_name%.leave"
    denyPermissions: []
```

**Key points:**

* `%shyguild_guild_name%` is automatically replaced with the actual guild name when permissions are applied
* The `owner` role can manage roles, kick members, send invites, and delete the guild
* The `member` role can only view roles, view members, and leave
* LuckPerms groups are automatically created and managed — do not edit them manually

---

### 🛡️ Admin Perspective

As an admin, you set up the server so that players can create and manage guilds on their own.

#### Step 1: Assign Base Permissions

Give your default player group the permissions they need to interact with guilds. 
See the [permissions](permission.md) page.

> **Note:** `shyguild.cmd.member.add` is an admin-only command that bypasses the invite system. Do not give it to regular players.

#### Step 2: Verify the Template is Loaded

```bash
/shyguild template list
```

You should see `sample_guild` in the output. If not, check your `plugins/ShyGuild/guild/` folder and run `/shyguild reload`.

---

### 👤 Player Perspective

Once an admin has set up the permissions, players can create and manage guilds entirely on their own.

#### Step 1: Create a Guild

```bash
/shyguild create sample_guild dragons The_Dragons
```

* `sample_guild` — the template to use
* `dragons` — the internal guild name (lowercase, alphanumeric and hyphens only)
* `The_Dragons` — the display name (underscores become spaces: "The Dragons")

The player who creates the guild is automatically assigned the `owner` role.

#### Step 2: Invite Members

The guild owner invites other online players:

```bash
/shyguild member invite dragons Alex
/shyguild member invite dragons Steve
```

Invited players see a message in chat and can accept:

```bash
/shyguild member accept dragons
```

Once accepted, they join the guild and receive the default role specified in the template.

#### Step 3: Manage Roles

The owner can assign the `member` role to organize their guild:

```bash
/shyguild role add dragons member Alex
/shyguild role add dragons member Steve
```

#### Step 4: View Guild Info

Any member with the right permissions can view the roster and roles:

```bash
# List all members and their roles
/shyguild member list dragons

# List all available roles in the guild
/shyguild role list dragons

# List roles of a specific player
/shyguild role list dragons Alex
```

#### Step 5: Remove Members or Leave

The owner can kick a member:

```bash
/shyguild member remove dragons Steve
```

A member can leave voluntarily:

```bash
/shyguild member leave dragons
```

> **⚠️ Owner restriction:** The last remaining owner cannot leave. Transfer ownership first by assigning the `owner` role to another player.

#### Step 6: Delete the Guild

When the guild is no longer needed, the owner can delete it:

```bash
/shyguild delete dragons
```

---

## ⚽ Full Example: Soccer Club Template

This example shows how to create a custom guild template with four roles — `owner`, `coach`, `captain`, and `player` — to manage a soccer club on your server.

### Step 1: Create the Template File

Create a new file at `plugins/ShyGuild/guild/soccer_club.yml`:

```yaml
name: "soccer_club"
maxPlayers: 30
defaultRole: "player"
roles:
  # The club owner has full control over the guild
  - name: "owner"
    allowPermissions:
      - "shyguild.guild.%shyguild_guild_name%.delete"
      - "shyguild.guild.%shyguild_guild_name%.role.add.owner"
      - "shyguild.guild.%shyguild_guild_name%.role.remove.owner"
      - "shyguild.guild.%shyguild_guild_name%.role.add.coach"
      - "shyguild.guild.%shyguild_guild_name%.role.remove.coach"
      - "shyguild.guild.%shyguild_guild_name%.role.add.captain"
      - "shyguild.guild.%shyguild_guild_name%.role.remove.captain"
      - "shyguild.guild.%shyguild_guild_name%.role.add.player"
      - "shyguild.guild.%shyguild_guild_name%.role.remove.player"
      - "shyguild.guild.%shyguild_guild_name%.role.list"
      - "shyguild.guild.%shyguild_guild_name%.member.remove"
      - "shyguild.guild.%shyguild_guild_name%.member.list"
      - "shyguild.guild.%shyguild_guild_name%.invite"
      - "shyguild.guild.%shyguild_guild_name%.leave"
    denyPermissions: []

  # Coaches can manage captains and players, but cannot delete the club or assign owners
  - name: "coach"
    allowPermissions:
      - "shyguild.guild.%shyguild_guild_name%.role.add.captain"
      - "shyguild.guild.%shyguild_guild_name%.role.remove.captain"
      - "shyguild.guild.%shyguild_guild_name%.role.add.player"
      - "shyguild.guild.%shyguild_guild_name%.role.remove.player"
      - "shyguild.guild.%shyguild_guild_name%.role.list"
      - "shyguild.guild.%shyguild_guild_name%.member.remove"
      - "shyguild.guild.%shyguild_guild_name%.member.list"
      - "shyguild.guild.%shyguild_guild_name%.invite"
      - "shyguild.guild.%shyguild_guild_name%.leave"
    denyPermissions: []

  # Captains can invite new players and view the roster, but cannot manage roles
  - name: "captain"
    allowPermissions:
      - "shyguild.guild.%shyguild_guild_name%.role.list"
      - "shyguild.guild.%shyguild_guild_name%.member.list"
      - "shyguild.guild.%shyguild_guild_name%.invite"
      - "shyguild.guild.%shyguild_guild_name%.leave"
    denyPermissions: []

  # Regular players can only view the roster and leave
  - name: "player"
    allowPermissions:
      - "shyguild.guild.%shyguild_guild_name%.role.list"
      - "shyguild.guild.%shyguild_guild_name%.member.list"
      - "shyguild.guild.%shyguild_guild_name%.leave"
    denyPermissions: []
```

### Step 2: Set Up Permissions

Grant your default player group access to the new template:

```bash
# Allow using the soccer_club template (in addition to existing permissions)
/lp group default permission set shyguild.template.soccer_club true
```

### Step 3: Reload the Plugin

```bash
/shyguild reload
```

Verify the template is loaded:

```bash
/shyguild template list
```

You should see both `sample_guild` and `soccer_club`.

### Step 4: Create a Soccer Club

A player creates their club:

```bash
/shyguild create soccer_club united-fc United_FC
```

The creator is automatically the `owner`.

### Step 5: Build the Roster

The owner invites members and assigns roles:

```bash
# Invite players
/shyguild member invite united-fc Alex
/shyguild member invite united-fc Steve
/shyguild member invite united-fc Bob
/shyguild member invite united-fc Charlie
```

After they accept:

```bash
# Assign a coach
/shyguild role add united-fc coach Alex

# Assign a captain
/shyguild role add united-fc captain Steve
```

### Step 6: Day-to-Day Management

**The coach** (Alex) can now independently manage the team:

```bash
# Coach promotes Bob to captain
/shyguild role add united-fc captain Bob

# Coach invites a new player
/shyguild member invite united-fc Dave

# Coach removes a role from a player
/shyguild role remove united-fc captain Bob

# Coach kicks a player from the club
/shyguild member remove united-fc Charlie
```

**The captain** (Steve) can recruit but not manage roles:

```bash
# Captain invites a new player
/shyguild member invite united-fc Eve

# Captain views the roster
/shyguild member list united-fc
```

**A regular player** (Bob) can only view and leave:

```bash
# Player views the roster
/shyguild member list united-fc

# Player views available roles
/shyguild role list united-fc

# Player leaves the club
/shyguild member leave united-fc
```

### Role Hierarchy Summary

| Role | Invite | Kick | Manage Roles | Delete Club |
|------|--------|------|--------------|-------------|
| 👑 Owner | ✅ | ✅ | All roles | ✅ |
| 🎓 Coach | ✅ | ✅ | Captain & Player | ❌ |
| ⚓ Captain | ✅ | ❌ | None | ❌ |
| ⚽ Player | ❌ | ❌ | None | ❌ |

---

## ❓ Common Issues

**Q: My guild template isn't showing in `/shyguild template list`**

* Ensure the `.yml` file is in `plugins/ShyGuild/guild/`
* Check the YAML syntax is valid (indentation matters!)
* Run `/shyguild reload` after adding new template files

**Q: Role permissions aren't being applied**

* Verify that **LuckPerms** is installed — role permissions require it
* Do not manually edit the LuckPerms groups created by ShyGuild
* Check that the `allowPermissions` in your template use the correct permission nodes

**Q: A player can't create a guild**

* Ensure they have `shyguild.command`, `shyguild.cmd.create`, and `shyguild.template.<template>` permissions
* Check `maxCreateGuildsPerPlayer` in `config.yml` — they may have hit the limit

**Q: A player can't accept an invite**

* Ensure they have `shyguild.command` and `shyguild.cmd.member.accept` permissions
* Check `maxJoinGuildsPerPlayer` in `config.yml` — they may have hit the limit
* Check `maxPlayers` in the guild template — the guild may be full

**Q: Cross-server guild data isn't syncing**

* Set `database.type` to `mysql` in `config.yml`
* Configure the JDBC connection settings with your MySQL server details
* Adjust `synchronizeGuildsSeconds` to a lower value for faster sync
