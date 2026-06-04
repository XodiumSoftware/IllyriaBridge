package org.xodium.illyriabridge

import org.bukkit.plugin.java.JavaPlugin
import org.xodium.illyriabridge.bridges.FabricRecipeBridge
import org.xodium.illyriabridge.bridges.XaeroMapBridge

/** Main class of the plugin. */
internal class IllyriaBridge : JavaPlugin() {
    companion object {
        lateinit var instance: IllyriaBridge
            private set
    }

    override fun onEnable() {
        instance = this

        val unsupportedVersionMsg =
            "This plugin requires a supported server version. Supported versions: ${pluginMeta.version}."

        if (!server.version.contains(pluginMeta.version.substringBefore("+"))) disablePlugin(unsupportedVersionMsg)

        val bridges =
            listOf(
                FabricRecipeBridge,
                XaeroMapBridge,
            )

        logger.info(
            "Registered: ${bridges.size} bridge(s) | Took ${bridges.sumOf { it.register() }}ms",
        )
    }

    override fun onDisable() {
        server.messenger.unregisterOutgoingPluginChannel(this)
    }

    /**
     * Disable the plugin and log the message.
     *
     * @param msg The message to log.
     */
    private fun disablePlugin(msg: String): Nothing {
        logger.severe(msg)
        server.pluginManager.disablePlugin(instance)
        throw IllegalStateException(msg)
    }
}
