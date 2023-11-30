package pro.respawn.flowmvi.util

import java.util.concurrent.ConcurrentHashMap

internal actual fun <K, V> concurrentMutableMap(): MutableMap<K, V> = ConcurrentHashMap()
