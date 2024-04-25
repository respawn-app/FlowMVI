@file:Suppress("UnnecessaryVariable")

package pro.respawn.flowmvi.test.store

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.equals.shouldNotBeEqual
import pro.respawn.flowmvi.util.testStore

class StoreComparisonTest : FreeSpec({
    "Given two stores without names" - {
        val a = testStore { configure { name = null } }
        val b = testStore { configure { name = null } }
        "then stores are distinct" {
            a shouldNotBeEqual b
        }
    }
    "given two stores with different names" - {
        val a = testStore { configure { name = "a" } }
        val b = testStore { configure { name = "b" } }
        "then stores are distinct" {
            a shouldNotBeEqual b
        }
    }
    "given same store without a name" - {
        val a = testStore { configure { name = null } }
        val b = a
        "then store is equal to itself" {
            a shouldBeEqual b
        }
    }
    "given different stores with the same name" - {
        val a = testStore { configure { name = "a" } }
        val b = testStore { configure { name = "a" } }
        "then stores are equal" {
            a shouldBeEqual b
        }
        "then stores are equal to themselves" {
            a shouldBeEqual a
        }
    }
})
