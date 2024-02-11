package pro.respawn.flowmvi.test.plugin

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import pro.respawn.flowmvi.plugins.UndoRedo
import pro.respawn.flowmvi.util.asUnconfined
import pro.respawn.flowmvi.util.idle

class UndoRedoPluginTest : FreeSpec({
    asUnconfined()
    "Given undo/redo" - {
        val plugin = UndoRedo(10)
        var counter = 0
        suspend fun redo() = plugin.invoke(true, redo = { ++counter }, undo = { --counter })
        "and when redo is invoked" - {
            "then block is executed" {
                with(plugin) {
                    redo()
                    counter shouldBe 1
                    isQueueEmpty shouldBe false
                    index.value shouldBe 0
                    canRedo shouldBe false
                    canUndo shouldBe true
                }
            }
        }

        "and when undo is called" - {
            "then state is restored" {
                with(plugin) {
                    plugin.undo(true)
                    counter shouldBe 0
                    isQueueEmpty shouldBe false
                    canRedo shouldBe true
                    canUndo shouldBe false
                    index.value shouldBe -1
                }
            }
        }
        "and when multiple operations are executed" - {
            plugin.reset()
            counter = 0
            val reps = 5
            repeat(5) { redo() }
            "then queue size matches intent count" {
                plugin.index.value shouldBe reps - 1
                counter shouldBe 5
            }
            "then multiple actions can be undone" {
                plugin.undo(true)
                plugin.undo(true)
                idle()
                plugin.index.value shouldBe reps - 1 - 2 // 2
                counter shouldBe 3
                plugin.queueSize shouldBe 5
            }
            "then making another action replaces the redo queue" {
                redo()
                idle()
                assertSoftly {
                    plugin.queueSize shouldBe 5 - 2 + 1
                    plugin.index.value shouldBe 4 - 1
                }
            }
        }
    }
})
