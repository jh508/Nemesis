# Nemesis

A GUI-driven moderation plugin for Paper servers. Punish players — mute, ban, or warn — through an inventory menu instead of memorizing command syntax, with full punishment history and one-click revocation.

> **Status:** actively in development, not yet at a stable release.


<img width="346" height="430" alt="image" src="https://github.com/user-attachments/assets/374cd387-22c5-4d4b-a469-60591ef72d19" />



## Features

- **GUI punishment menu** — `/punish <player> <reason>` opens an inventory of punishment options (mutes and bans at 1/7/30-day and permanent lengths, plus warnings), categorized by offence type.
- **Punishment history** — every past punishment for a player is shown directly in the menu, with reason, issue date, expiry, and issuer. Active punishments glow and can be revoked with a click.
- **Custom mute/ban enforcement** — mutes are enforced on chat, bans on login — both driven by the plugin's own storage rather than Bukkit's ban list, so revoking a punishment actually lifts it.
- **File-based storage** — punishment records are stored in `data/punishments.yml`, no database required.
- **SQL support** — punishment records are stored in MySQL database, perfect for servers who wants punishments to persist Network-wide.

<img width="346" height="436" alt="image" src="https://github.com/user-attachments/assets/57301b16-5174-4712-b694-c6d6ad65244a" />



## Requirements

- Paper (or a Paper fork) `1.21.x` / API version `26.1.2`+
- Java 25

## Installation

1. Download the plugin jar (or build it yourself, see below) and drop it into your server's `plugins/` folder.
2. Start the server once to generate `plugins/Nemesis/config.yml`.
3. Grant the `moderation.punish` permission to your staff.
4. Restart or reload.

## Building from source

```bash
mvn clean package
```

The shaded jar will be output to `target/nemesis-1.0-SNAPSHOT.jar`.

## Usage

```
/punish <player> <reason...>
/p <player> <reason...>
```

Opens the punishment menu for `<player>` with the given reason. Select a punishment from the menu to apply it; the target's punishment history is shown in the bottom rows and can be clicked to revoke an active punishment.

## Permissions

| Permission | Description |
|---|---|
| `moderation.punish` | Access to `/punish` and the punishment menu. |

## Configuration

`config.yml`:

```yaml
storage-method: file

appeals-url: "www.example.com/appeals"
```

- `storage-method` — how punishment data is stored. Only `file` is currently implemented; other values fall back to file storage with a warning.
- `appeals-url` — shown to banned players on the kick screen.

## Roadmap

- SQL storage backend (MySQL/MariaDB), for servers running a network of multiple instances.
- Tiered permissions (e.g. separate permission for permanent punishments and revocations).
- Pagination for players with a long punishment history.
