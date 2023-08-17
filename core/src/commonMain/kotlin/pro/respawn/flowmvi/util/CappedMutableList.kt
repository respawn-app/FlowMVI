package pro.respawn.flowmvi.util

internal class CappedMutableList<T>(
    private val maxSize: Int,
    private val backing: MutableList<T> = mutableListOf(),
) : MutableList<T> by backing {

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
        require(elements.size < maxSize)
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
