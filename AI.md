# IllyriaBridge — Claude Code Context

## Project at a Glance

- **Name:** IllyriaBridge
- **Type:** Single-module Minecraft Paper plugin project (server-side only)
- **MC Version:** 1.21.11 (Paper 1.21.11)
- **Language:** Java (JVM 25)
- **Build Tool:** Gradle with Kotlin DSL

## APIs & Tools

| Category           | Technology                               | Purpose                       |
|--------------------|------------------------------------------|-------------------------------|
| **Core API**       | [Paper API](https://papermc.io/) 1.21.11 | Minecraft server plugin API   |
| **Language**       | Java 25                                  | JVM language                  |
| **Build Tool**     | Gradle (Kotlin DSL)                      | Build automation              |
| **Gradle Plugins** | run-paper 3.0.2                          | Local test server             |
|                    | resource-factory 1.3.1                   | `paper-plugin.yml` generation |
|                    | foojay-resolver 1.0.0                    | Auto-download JVM toolchains  |
| **Code Style**     | .editorconfig                            | IDE-agnostic formatting rules |

### Paper API Resources

- **Documentation**: https://docs.papermc.io/paper/dev/
- **JavaDoc**: https://jd.papermc.io/paper/1.21.11/ (matches project version)

## Quick Commands

```bash
# Build the plugin
./gradlew shadowJar

# Run local test server (auto-downloads Paper 1.21.11)
./gradlew runServer

# Build only (no shadow)
./gradlew build
```

## Project Structure

```
IllyriaBridge/
├── build.gradle.kts          # Build configuration
├── settings.gradle.kts         # Project settings
├── src/                        # Source directory
│   ├── IllyriaBridge.java              # Main plugin class (no package)
│   ├── managers/
│   │   └── RecipeManager.java          # Recipe sync handler
│   └── payloads/                         # Payload classes
│       └── FabricRecipeSyncPayload.java
└── docs/                       # Generated documentation
```

## Architecture

### Entry Point

**IllyriaBridge** (`JavaPlugin`) — Main class. On enable:

- Registers plugin channel for Fabric (`fabric:recipe_sync`)
- Registers the RecipeHandler event listener

### Recipe Sync System

**RecipeManager** (in `managers` package) implements `Listener` and handles `PlayerJoinEvent`:

1. Detects Fabric client via `Player.getClientBrandName()`
2. For Fabric clients: calls `sendFabricPayload()`

**Fabric Sync:**

- Creates `FabricRecipeSyncPayload` (from `payloads` package) with recipe entries grouped by `RecipeSerializer`
- Encodes to `RegistryFriendlyByteBuf` using `Entry.CODEC`
- Sends via `ClientboundCustomPayloadPacket` with ID `fabric:recipe_sync`

### Package Structure

| Package     | Contents                                       |
|-------------|------------------------------------------------|
| (default)   | `IllyriaBridge` — Main plugin class            |
| `managers/` | `RecipeManager` — Recipe sync handler          |
| `payloads/` | `FabricRecipeSyncPayload` — Fabric recipe sync |

### Key Conventions

- All internal classes use appropriate visibility (`public`/`private`)
- Plugin uses Paper's plugin channel API for cross-platform mod compatibility
- Recipe data is encoded using Minecraft's `RegistryFriendlyByteBuf`

## CI/CD

GitHub Actions workflows in `.github/workflows/`:

- **kotlin.yml** — Builds shadow JAR on push/PR, uploads artifacts, creates nightly release
- **enforce_pr_title.yml** — Validates PR titles follow conventional commits

## Claude Code Workflow

### Task Management

**When creating tasks:**

- Number tasks in the name (e.g., "1. Add Verdance enchantment", "2. Update mana system")
- This makes it easy to reference specific tasks in conversation

**After completing each task:**

- Ask the user if they want to git commit the changes or adjust before committing

**When all tasks in a worktree are complete:**

- Ask the user if they want to git publish (push) the changes or adjust before publishing

## Memory System

This project uses Claude Code's persistent memory in `.claude/memory/`. These files persist across sessions and different PCs. Review `MEMORY.md` for existing context about the user and project.
