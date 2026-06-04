# ARCHITECTURE.md

This file provides guidance when working with code in this repository.

## Project Overview

IllyriaBridge is a single-module Paper Minecraft plugin (1.21.11) that syncs server-side recipes to JEI (Just Enough Items) on Fabric and NeoForge clients.

Built with Java + Gradle, targeting Java 25. Uses the Paper API's plugin messaging channels to send recipe data to modded clients.

## Project Structure

```
IllyriaBridge/
├── settings.gradle.kts      # Project settings
├── build.gradle.kts         # Build configuration
├── src/                     # Source directory
│   ├── IllyriaBridge.java              # Main plugin class (no package)
│   ├── bridges/
│   │   ├── FabricBridge.kt           # Fabric sync handler
│   │   └── XaeroMapBridge.kt         # Xaero map sync handler
│   └── payloads/                       # Payload classes
│       └── FabricRecipeSyncPayload.java
└── docs/                    # Generated documentation
```

## Build & Run Commands

```bash
# Build the plugin
./gradlew shadowJar

# Run test server
./gradlew runServer

# Build only (no shadow)
./gradlew build
```

Output JAR:

- `build/libs/IllyriaBridge-*.jar`

There are no automated tests in this project.

## Architecture

### Entry Point

- **`IllyriaBridge`** — `JavaPlugin` implementation (default package). On enable:
    - Stores plugin instance in static `Plugin` field
    - Registers `FabricBridge` as event listener
        - Registers outgoing plugin channel for Fabric (`fabric:recipe_sync`)

### Recipe Sync System

**FabricBridge** (in `bridges` package) implements `Listener` and handles `PlayerJoinEvent`:

1. Detects Fabric client via `Player.getClientBrandName()`
2. For Fabric clients: calls `sendFabricPayload()`

**Fabric Sync:**

- Creates `FabricRecipeSyncPayload` (from `payloads` package) with recipe entries grouped by `RecipeSerializer`
- Encodes to `RegistryFriendlyByteBuf` using `Entry.CODEC`
- Sends via `ClientboundCustomPayloadPacket` with ID `fabric:recipe_sync`

### Payload Classes

**FabricRecipeSyncPayload:**

- Record with `List<Entry>`
- `Entry` record contains `RecipeSerializer<?>` and `List<RecipeHolder<?>>`
- Implements `CustomPacketPayload` with `TYPE` and `CODEC`

### Package Structure

| Package     | Contents                                       |
|-------------|------------------------------------------------|
| (default)   | `IllyriaBridge` — Main plugin class            |
| `bridges/`  | `FabricBridge` — Fabric sync handler           |
| `bridges/`  | `XaeroMapBridge` — Xaero map sync handler      |
| `payloads/` | `FabricRecipeSyncPayload` — Fabric recipe sync |

### Key Conventions

- Main plugin class extends `JavaPlugin`
- Event handlers implement `Listener` interface
- Payload records implement `CustomPacketPayload`
- Use `RegistryFriendlyByteBuf` for Minecraft registry-aware serialization
- Plugin channels registered via `Messenger.registerOutgoingPluginChannel()`
