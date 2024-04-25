package pro.respawn.flowmvi.util

internal actual fun <E> concurrentLinkedSet(): MutableSet<E> = SynchronizedLinkedSet()
