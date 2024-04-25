package pro.respawn.flowmvi.util

import java.util.Collections

internal actual fun <E> concurrentLinkedSet(): MutableSet<E> = Collections.synchronizedSet(mutableSetOf())
