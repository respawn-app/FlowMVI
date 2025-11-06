package pro.respawn.flowmvi.savedstate.dsl

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

@Suppress("RETURN_VALUE_NOT_USED")
class NoOpSaverTest : FreeSpec({

    "Given a NoOpSaver" - {

        "when restoring state" - {
            "then restore should always return null" {
                val saver = NoOpSaver<String>()

                val result = saver.restore()
                result shouldBe null
            }
        }

        "when saving various values" - {
            "then restore should always return null regardless of input" {
                val saver = NoOpSaver<String>()

                saver.save("value")
                saver.restore() shouldBe null

                saver.save(null)
                saver.restore() shouldBe null
            }
        }

        "when used with different generic types" - {
            "then restore should return null for each type" {
                NoOpSaver<Int>().apply {
                    save(42)
                    restore() shouldBe null
                }

                NoOpSaver<List<String>>().apply {
                    save(listOf("a", "b"))
                    restore() shouldBe null
                }

                NoOpSaver<CustomData>().apply {
                    save(CustomData("test", 123))
                    restore() shouldBe null
                }
            }
        }
    }
})

private data class CustomData(val name: String, val value: Int)
