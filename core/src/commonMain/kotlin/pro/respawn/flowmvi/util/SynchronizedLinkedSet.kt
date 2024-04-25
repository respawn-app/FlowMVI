package pro.respawn.flowmvi.util

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

internal expect fun <E> concurrentLinkedSet(): MutableSet<E>

internal class SynchronizedLinkedSet<E> : MutableSet<E>, SynchronizedObject() {

    private val inner = LinkedHashSet<E>()

    // getters need to be synchronized because
    // When a thread reads a variable without synchronization, it may see a stale value.

    private inline fun <R> sync(block: LinkedHashSet<E>.() -> R) = synchronized(this) { inner.run(block) }

    override val size: Int get() = sync { size }

    override fun add(element: E) = sync { add(element) }
    override fun clear() = sync { clear() }
    override fun isEmpty() = sync { inner.isEmpty() }
    override fun retainAll(elements: Collection<E>) = sync { retainAll(elements) }
    override fun removeAll(elements: Collection<E>) = sync { removeAll(elements) }
    override fun remove(element: E) = sync { remove(element) }
    override fun containsAll(elements: Collection<E>) = sync { inner.containsAll(elements) }
    override fun contains(element: E) = sync { inner.contains(element) }
    override fun addAll(elements: Collection<E>) = sync { inner.addAll(elements) }
    override fun iterator(): MutableIterator<E> = object : MutableIterator<E>, SynchronizedObject() {
        val delegate = inner.iterator()
        override fun hasNext(): Boolean = synchronized(this) { delegate.hasNext() }
        override fun next(): E = synchronized(this) { delegate.next() }
        override fun remove() = synchronized(this) { delegate.remove() }
    }
}
