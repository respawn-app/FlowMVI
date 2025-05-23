package pro.respawn.flowmvi.sample.features.progressive

import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.random.nextLong

internal class ProgressiveRepository {

    suspend fun getSuggestions(): List<Item> {
        delay(Random.nextLong(2000L..5000L))
        return List(Random.nextInt(10..20)) { Item(it, "Recommendation $it") }.shuffled()
    }

    suspend fun getFeed(): List<Item> {
        delay(Random.nextLong(2000L..5000L))
        return List(Random.nextInt(3..10)) { Item(it, "Feed item $it") }.shuffled()
    }
}
