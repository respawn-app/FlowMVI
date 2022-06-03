package com.nek12.flowMVI.sample.repo

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

class CounterRepo {
    fun getCounter() = flow {
        var counter = 0
        while(true) {
            delay(1000)
            emit(counter++)
        }
    }
}
