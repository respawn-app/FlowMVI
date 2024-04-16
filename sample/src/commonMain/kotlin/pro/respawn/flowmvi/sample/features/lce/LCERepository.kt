package pro.respawn.flowmvi.sample.features.lce

import kotlinx.coroutines.delay
import kotlin.random.Random

class LCERepository {

    suspend fun loadItems(canThrow: Boolean): List<LCEItem> {
        delay(Delay)
        require(Random.nextBoolean() || !canThrow) { "LoadItems request failed" }
        return List(Random.nextInt(10, MaxItems)) { LCEItem(it) }.shuffled()
    }

    companion object {

        private const val Delay = 2000L
        private const val MaxItems = 20
    }
}
