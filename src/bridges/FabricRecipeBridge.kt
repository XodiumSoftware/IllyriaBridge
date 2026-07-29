package org.xodium.illyriabridge.bridges

import io.netty.buffer.Unpooled
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
import net.minecraft.network.protocol.common.custom.DiscardedPayload
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.xodium.illyriabridge.IllyriaBridge.Companion.instance
import org.xodium.illyriabridge.payloads.FabricRecipeSyncPayload
import kotlin.time.measureTime

/**
 * Manages recipe synchronization for Fabric clients connecting to the server.
 * Listens for player join events and sends appropriate recipe data to Fabric clients.
 */
internal object FabricRecipeBridge : BridgeInterface {
    private const val RECIPE_CHANNEL = "fabric:recipe_sync"

    private val recipePayloadId = Identifier.fromNamespaceAndPath("fabric", "recipe_sync")
    private var cachedRecipePayload: ByteArray? = null

    override fun register(): Long =
        super.register() +
            measureTime {
                instance.server.messenger.registerOutgoingPluginChannel(instance, RECIPE_CHANNEL)
            }.inWholeMilliseconds

    /**
     * Handles player join events by checking the client brand and sending
     * Fabric-compatible recipe sync payloads if the client is running Fabric.
     *
     * @param event The player join event
     */
    @EventHandler
    fun on(event: PlayerJoinEvent) {
        val originalPlayer = event.player
        val brand = originalPlayer.clientBrandName ?: return

        if (!brand.equals("fabric", ignoreCase = true)) return

        val player = (originalPlayer as CraftPlayer).handle
        val bytes = cachedRecipePayload ?: encodeAndCachePayload(player)

        sendPayload(player, recipePayloadId, bytes)
    }

    /**
     * Encodes the server's current recipes into a Fabric recipe sync payload
     * and caches the resulting bytes so subsequent joins do not re-encode everything.
     *
     * @param player A server player used to access the recipe manager and registry
     * @return The encoded payload bytes
     */
    private fun encodeAndCachePayload(player: ServerPlayer): ByteArray {
        val server = player.level().server
        val buffer = RegistryFriendlyByteBuf(Unpooled.buffer(), server.registryAccess())

        return try {
            val entries =
                server.recipeManager
                    .recipes
                    .values()
                    .groupBy { it.value().serializer }
                    .map { (serializer, recipes) -> FabricRecipeSyncPayload.Entry(serializer, recipes) }
            val payload = FabricRecipeSyncPayload(entries)

            FabricRecipeSyncPayload.CODEC.encode(buffer, payload)

            val bytes = ByteArray(buffer.writerIndex())
            buffer.getBytes(0, bytes)
            cachedRecipePayload = bytes
            bytes
        } finally {
            buffer.release()
        }
    }

    /**
     * Sends a custom payload packet to the specified player.
     *
     * @param player The server player to send the packet to
     * @param id The identifier for the payload type
     * @param bytes The raw payload bytes
     */
    private fun sendPayload(
        player: ServerPlayer,
        id: Identifier,
        bytes: ByteArray,
    ) {
        player.connection.send(ClientboundCustomPayloadPacket(DiscardedPayload(id, bytes)))
    }
}
