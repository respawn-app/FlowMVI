package pro.respawn.flowmvi.sample.features.transitions

import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

internal class AuthRepository {

    @Suppress("UnusedParameter")
    suspend fun authenticate(username: String, password: String): Result<String> {
        delay(2.seconds)
        @Suppress("MagicNumber")
        return if (Random.nextFloat() < 0.3f) {
            Result.failure(IllegalStateException("Invalid credentials"))
        } else {
            Result.success(username)
        }
    }
}
