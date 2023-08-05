package pro.respawn.flowmvi.sample.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

class CounterRepository {

    fun getTimer() = flow {
        var counter = 0
        while (true) {
            delay(1000)
            emit(counter++)
        }
    }
}
