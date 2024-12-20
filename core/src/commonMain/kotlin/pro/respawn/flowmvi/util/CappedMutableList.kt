package pro.respawn.flowmvi.util

internal class CappedMutableList<T>(
    private val maxSize: Int,
    private val backing: MutableList<T> = mutableListOf(),
) : MutableList<T> by backing {

    init {
        require(maxSize > 0) { "The max size of the collection must be >0, got $maxSize" }
    }

    override fun add(element: T): Boolean {
        backing.add(element)
        removeOverflowing()
        return true
    }

    override fun add(index: Int, element: T) {
        require(index <= maxSize)
        backing.add(index, element)
        removeOverflowing()
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        require(index <= maxSize)
        val added = backing.addAll(index, elements)
        removeOverflowing()
        return added
    }

    override fun addAll(elements: Collection<T>): Boolean {
        val added = backing.addAll(elements)
        removeOverflowing()
        return added
    }

    private fun removeOverflowing() {
        // do not use removeFirst because of https://jakewharton.com/kotlins-jdk-release-compatibility-flag/
        while (size > maxSize) removeAt(0)
    }
}
