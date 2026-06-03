package org.xodium.illyriabridge.managers

import com.google.common.io.ByteStreams
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerRegisterChannelEvent
import org.xodium.illyriabridge.IllyriaBridge.Companion.instance
import org.xodium.illyriabridge.managers.XaeroMapManager.configPath
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.random.Random

/**
 * Manages Xaero's WorldMap and Minimap plugin channel synchronization.
 * Ensures Fabric clients receive the correct server-level world ID for map consistency.
 */
internal object XaeroMapManager : Listener {
    /** The plugin channel identifier for Xaero's WorldMap. */
    private const val WORLDMAP_CHANNEL = "xaeroworldmap:main"

    /** The plugin channel identifier for Xaero's Minimap. */
    private const val MINIMAP_CHANNEL = "xaerominimap:main"

    /** JSON serializer instance with pretty printing for human-readable config files. */
    private val json = Json { prettyPrint = true }

    /** Persistent data class representing the Xaero map server configuration. */
    @Serializable
    private data class Config(
        val id: Int,
    )

    /** Path to the persistent Xaero map configuration file inside the plugin data folder. */
    private val configPath by lazy { instance.dataFolder.toPath().resolve("xaeromap.json") }

    /** The cached server-level map configuration. Loaded from disk or generated on first use. */
    private val config: Config by lazy { loadConfig() }

    init {
        instance.server.messenger.registerOutgoingPluginChannel(instance, WORLDMAP_CHANNEL)
        instance.server.messenger.registerOutgoingPluginChannel(instance, MINIMAP_CHANNEL)
    }

    /**
     * Handles player channel registration events by sending the server world ID
     * when a player registers a Xaero map channel.
     *
     * @param event The player register channel event
     */
    @EventHandler
    fun on(event: PlayerRegisterChannelEvent) {
        when (val channel = event.channel) {
            WORLDMAP_CHANNEL, MINIMAP_CHANNEL -> sendPlayerWorldId(event.player, channel)
            else -> return
        }
    }

    /**
     * Handles player world change events by re-sending the server world ID
     * for both WorldMap and Minimap channels.
     *
     * @param event The player changed world event
     */
    @EventHandler
    fun on(event: PlayerChangedWorldEvent) {
        event.player.apply {
            sendPlayerWorldId(this, WORLDMAP_CHANNEL)
            sendPlayerWorldId(this, MINIMAP_CHANNEL)
        }
    }

    /**
     * Sends the server world ID to the specified player via the given plugin channel.
     *
     * @param player The player to send the world ID to
     * @param channel The plugin channel to send the message on
     */
    private fun sendPlayerWorldId(
        player: Player,
        channel: String,
    ) {
        player.sendPluginMessage(
            instance,
            channel,
            ByteStreams
                .newDataOutput()
                .apply {
                    writeByte(0)
                    writeInt(config.id)
                }.toByteArray(),
        )
    }

    /**
     * Loads the server map configuration from disk, creating it if necessary.
     * If [configPath] exists, it is deserialized and returned. Otherwise,
     * a new random ID is generated, persisted, and returned.
     *
     * @return The loaded or created [Config], or a fallback with `id = 0` on failure
     */
    private fun loadConfig(): Config =
        runCatching {
            when {
                configPath.exists() -> json.decodeFromString<Config>(configPath.readText())
                else -> Config(Random.nextInt()).also { configPath.writeText(json.encodeToString(it)) }
            }
        }.getOrDefault(Config(0))
}
