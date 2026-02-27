# Permissions Guide

This guide explains all permission nodes available in ShyGuild and how to properly configure them for your server. Understanding permissions is crucial for controlling who can use commands and manage guilds.

## 🔐 Permission Levels

ShyGuild uses two permission levels:

* **👤 User Level**: Permissions that regular players can have (typically assigned via guild role templates and LuckPerms)
* **🛡️ Admin Level**: Permissions that should only be given to trusted staff

---

## 📋 Command Permission Reference

These permissions control who can **execute** each command.

| Permission | Level | Description                      | Required For |
|------------|-------|----------------------------------|--------------|
| `shyguild.command` | 👤 User | Use the base `/shyguild` command | Running any command |
| `shyguild.cmd.create` | 👤 User | Create new guilds                | `/shyguild create` |
| `shyguild.cmd.delete` | 👤 User | Delete guilds                    | `/shyguild delete` |
| `shyguild.cmd.guild.list` | 👤 User | List all joined guilds           | `/shyguild guild list` |
| `shyguild.template.<template>` | 👤 User | Use a specific guild template    | Creating a guild with that template |
| `shyguild.cmd.role.add` | 👤 User | Assign roles to guild members    | `/shyguild role add` |
| `shyguild.cmd.role.remove` | 👤 User | Remove roles from guild members  | `/shyguild role remove` |
| `shyguild.cmd.role.list` | 👤 User | List roles of a guild or player  | `/shyguild role list` |
| `shyguild.cmd.member.remove` | 👤 User | Remove players from a guild      | `/shyguild member remove` |
| `shyguild.cmd.member.list` | 👤 User | List members of a guild          | `/shyguild member list` |
| `shyguild.cmd.member.invite` | 👤 User | Invite players to a guild        | `/shyguild member invite` |
| `shyguild.cmd.member.accept` | 👤 User | Accept a pending guild invite    | `/shyguild member accept` |
| `shyguild.cmd.member.leave` | 👤 User | Leave a guild                    | `/shyguild member leave` |
| `shyguild.cmd.member.add` | 🛡️ Admin | Add players directly to a guild  | `/shyguild member add` |
| `shyguild.cmd.template.list` | 🛡️ Admin | List all loaded guild templates  | `/shyguild template list` |
| `shyguild.cmd.reload` | 🛡️ Admin | Reload all configurations        | `/shyguild reload` |

---

## 🏰 Guild-Specific Permission Reference

These permissions control **per-guild** actions. They are typically assigned at guild template level. Do not assign these manually.

| Permission | Level | Description | Required For |
|------------|-------|-------------|--------------|
| `shyguild.guild.<guild>.delete` | 👤 User | Delete a specific guild | `/shyguild delete <guild>` |
| `shyguild.guild.<guild>.role.add.<role>` | 👤 User | Assign a specific role in a guild | `/shyguild role add <guild> <role>` |
| `shyguild.guild.<guild>.role.remove.<role>` | 👤 User | Remove a specific role in a guild | `/shyguild role remove <guild> <role>` |
| `shyguild.guild.<guild>.role.list` | 👤 User | List roles in a specific guild | `/shyguild role list <guild>` |
| `shyguild.guild.<guild>.member.add` | 🛡️ Admin | Add members to a specific guild | `/shyguild member add <guild>` |
| `shyguild.guild.<guild>.member.remove` | 👤 User | Remove members from a specific guild | `/shyguild member remove <guild>` |
| `shyguild.guild.<guild>.member.list` | 👤 User | List members of a specific guild | `/shyguild member list <guild>` |
| `shyguild.guild.<guild>.member.invite` | 👤 User | Invite players to a specific guild | `/shyguild member invite <guild>` |
| `shyguild.guild.<guild>.member.leave` | 👤 User | Leave a specific guild | `/shyguild member leave <guild>` |

---

## 💡 How Permissions Work Together

Each command requires **two** permission checks:

1. **Command permission** (e.g., `shyguild.cmd.delete`) — allows the player to use the command at all
2. **Guild-specific permission** (e.g., `shyguild.guild.my-guild.delete`) — allows the action on that particular guild

Both must be granted for the action to succeed.

### Example

For a player to delete a guild called `warriors`:
```
shyguild.command              # Base command access
shyguild.cmd.delete           # Permission to use /shyguild delete
shyguild.guild.warriors.delete # Permission to delete this specific guild
```

---

## 🔧 Guild Role Templates

Guild-specific permissions are typically **not assigned manually**. Instead, they are defined in guild templates and automatically managed by ShyGuild via LuckPerms.

For example, the default `sample_guild` template assigns these permissions to the `owner` role:

```yaml
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

The `%shyguild_guild_name%` placeholder is automatically replaced with the actual guild name when permissions are applied.

> **Note:** LuckPerms must be installed for automatic role permission management. Without it, guild-specific permissions need to be assigned manually.
