package org.xodium.illyriabridge.managers

import org.bukkit.event.Listener
import org.xodium.illyriabridge.IllyriaBridge.Companion.instance
import kotlin.time.measureTime

/** Represents a contract for a manager within the system. */
internal interface ManagerInterface : Listener {
    /**
     * Registers this feature with the server.
     *
     * @return The time taken to register the feature in milliseconds.
     */
    fun register(): Long =
        measureTime {
            instance.server.pluginManager.registerEvents(this, instance)
        }.inWholeMilliseconds
}
