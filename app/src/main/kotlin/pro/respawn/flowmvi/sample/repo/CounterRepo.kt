package pro.respawn.flowmvi.sample.repo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class CounterRepo {

    suspend fun getCounterSync() = withContext(Dispatchers.Default) {
        delay(1000)
        1
    }

    fun getTimer() = flow {
        var counter = 0
        while (true) {
            delay(1000)
            emit(counter++)
        }
    }
}
