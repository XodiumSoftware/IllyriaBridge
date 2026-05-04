package org.xodium.illyriabridge

import org.bukkit.plugin.java.JavaPlugin
import org.xodium.illyriabridge.managers.RecipeManager

/** Main class of the plugin. */
class IllyriaBridge : JavaPlugin() {
    companion object {
        lateinit var instance: IllyriaBridge
            private set
    }

    override fun onEnable() {
        instance = this

        val unsupportedVersionMsg =
            "This plugin requires a supported server version. Supported versions: ${pluginMeta.version}."

        if (!server.version.contains(pluginMeta.version.substringBefore("+"))) disablePlugin(unsupportedVersionMsg)

        server.pluginManager.registerEvents(RecipeManager, this)
        server.messenger.registerOutgoingPluginChannel(this, "fabric:recipe_sync")
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
