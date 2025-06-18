package pro.respawn.flowmvi.savedstate.dsl

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class NoOpSaverTest : FreeSpec({

    "Given a NoOpSaver" - {

        "when creating instances for different types" - {
            "then each instance should be independent" {
                val stringSaver = NoOpSaver<String>()
                val intSaver = NoOpSaver<Int>()
                val listSaver = NoOpSaver<List<String>>()

                // Verify they are different instances (not the same singleton)
                stringSaver shouldNotBe intSaver
                stringSaver shouldNotBe listSaver
                intSaver shouldNotBe listSaver
            }
        }

        "when saving state" - {
            "then save should complete without error" {
                val saver = NoOpSaver<String>()
                val testState = "test-state"

                // Should not throw any exception
                saver.save(testState)
                saver.save(null)
            }
        }

        "when restoring state" - {
            "then restore should always return null" {
                val saver = NoOpSaver<String>()

                val result = saver.restore()
                result shouldBe null
            }
        }

        "when using with different data types" - {
            "then it should work with any type" {
                val stringSaver = NoOpSaver<String>()
                val intSaver = NoOpSaver<Int>()
                val listSaver = NoOpSaver<List<String>>()
                val customSaver = NoOpSaver<CustomData>()

                // Test string type
                stringSaver.save("test")
                stringSaver.restore() shouldBe null

                // Test int type
                intSaver.save(42)
                intSaver.restore() shouldBe null

                // Test list type
                listSaver.save(listOf("a", "b", "c"))
                listSaver.restore() shouldBe null

                // Test custom type
                customSaver.save(CustomData("test", 123))
                customSaver.restore() shouldBe null
            }
        }

        "when saving null state" - {
            "then save should handle null gracefully" {
                val saver = NoOpSaver<String>()

                // Should not throw any exception
                saver.save(null)
                saver.restore() shouldBe null
            }
        }

        "when used multiple times" - {
            "then behavior should be consistent" {
                val saver = NoOpSaver<String>()

                // Multiple saves
                saver.save("first")
                saver.save("second")
                saver.save(null)
                saver.save("third")

                // Multiple restores should always return null
                saver.restore() shouldBe null
                saver.restore() shouldBe null
                saver.restore() shouldBe null
            }
        }
    }
})

private data class CustomData(val name: String, val value: Int)
