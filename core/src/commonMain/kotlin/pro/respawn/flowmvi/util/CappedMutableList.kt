package pro.respawn.flowmvi.util

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

internal class CappedMutableList<T>(
    private val maxSize: Int,
    private val backing: MutableList<T> = mutableListOf(),
) : MutableList<T> by backing, SynchronizedObject() {

    init {
        require(maxSize > 0) { "The max size of the collection must be >0, got $maxSize" }
    }

    override fun add(element: T): Boolean = synchronized(this) {
        backing.add(element)
        removeOverflowing()
        return true
    }

    override fun add(index: Int, element: T) = synchronized(this) {
        require(index <= maxSize)
        backing.add(index, element)
        removeOverflowing()
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean = synchronized(this) {
        require(index <= maxSize)
        val added = backing.addAll(index, elements)
        removeOverflowing()
        return added
    }

    override fun addAll(elements: Collection<T>): Boolean = synchronized(this) {
        val added = backing.addAll(elements)
        removeOverflowing()
        return added
    }

    private fun removeOverflowing() {
        while (size > maxSize) {
            removeAt(0)
        }
    }
}
