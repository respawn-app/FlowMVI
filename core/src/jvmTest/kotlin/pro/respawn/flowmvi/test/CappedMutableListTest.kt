package pro.respawn.flowmvi.test

import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import pro.respawn.flowmvi.util.CappedMutableList

class CappedMutableListTest : FreeSpec({
    "given a capped mutable list" - {
        "and the size is less than 1" - {
            "then an exception is thrown" {
                shouldThrowAny {
                    CappedMutableList<Int>(0)
                }
            }
        }

        "and for size 1" - {
            "only one element may be present" {
                val list = CappedMutableList<Int>(1)
                list.add(1)
                list.add(1)
                list shouldContainExactly listOf(1)
            }
        }

        val size = 3
        "and for size $size" - {

            "elements can be added up to capacity" {
                val list = CappedMutableList<Int>(size)
                val elems = listOf(1, 2, 3)
                list.addAll(elems)
                list shouldContainExactly elems
            }

            "elements that overflow capacity drop oldest" {
                val list = CappedMutableList<Int>(size)
                val elems = listOf(1, 2, 3)
                val additional = listOf(4, 5)

                list.addAll(elems)
                list.addAll(additional)
                list shouldContainExactly listOf(3, 4, 5)
            }

            "single added element that overflows will remove the oldest value" {
                val list = CappedMutableList<Int>(size)
                val elems = listOf(1, 2, 3)

                list.addAll(elems)
                list.add(4)
                list shouldContainExactly listOf(2, 3, 4)
            }

            "element can be removed and added" {
                val list = CappedMutableList<Int>(size)
                val element = 1

                list.add(element)
                list.remove(element)
                list.add(element)
                list shouldContainExactly listOf(1)
            }
        }
    }
})
