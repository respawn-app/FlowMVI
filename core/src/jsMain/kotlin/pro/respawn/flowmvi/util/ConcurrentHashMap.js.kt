package pro.respawn.flowmvi.util

internal actual fun <K, V> concurrentMutableMap(): MutableMap<K, V> = SynchronizedHashMap()
