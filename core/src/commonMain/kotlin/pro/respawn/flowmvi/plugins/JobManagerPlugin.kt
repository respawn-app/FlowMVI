package pro.respawn.flowmvi.plugins

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.supervisorScope
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder

/**
 * An entity that manages running jobs. Used with  [jobManagerPlugin] and [manageJobs].
 * Will cancel and remove all jobs if the parent [pro.respawn.flowmvi.api.Store] is closed
 */
public class JobManager {

    internal companion object {

        internal const val Name = "JobManagerPlugin"
    }

    private val jobs by atomic(mutableMapOf<String, Job>())

    /**
     * Cancels all jobs without suspending
     */
    public fun cancelAll(): Unit = with(jobs) {
        forEach { (_, job) -> job.cancel() }
        clear()
    }

    /**
     * Joins all jobs **sequentially**
     */
    public suspend fun joinAll(): Unit = try {
        jobs.values.joinAll()
    } finally {
        jobs.clear()
    }

    /**
     * Cancels all jobs **in parallel** and then joins them all **sequentially**
     */
    public suspend fun cancelAndJoinAll(): Unit = try {
        supervisorScope {
            jobs.values
                .map { async { it.cancelAndJoin() } }
                .awaitAll()
        }
        Unit
    } finally {
        jobs.clear()
    }

    /**
     * Put a job into the container and call [Job.cancelAndJoin] if one already exists there.
     *
     * Does **not** start the job that was put.
     */
    public suspend fun putOrReplace(name: String, job: Job): Job? {
        // do not put the job until we join it to not trigger completion
        // handler after we put another job at the same spot
        val previous = jobs[name]
        previous?.cancelAndJoin()

        jobs[name] = job.apply {
            invokeOnCompletion {
                jobs.remove(name)
            }
        }
        return previous
    }

    /**
     * Put a job into the container and throw [IllegalArgumentException] if the job with the same name is already running.
     *
     * Does **not** start the job that was put.
     */
    public fun put(name: String, job: Job) {
        require(jobs.put(name, job)?.takeIf { it.isActive } == null) { "Job with the same name is already running!" }
        job.invokeOnCompletion {
            jobs.remove(name)
        }
    }

    /**
     * Get a job with the specified [name].
     */
    public operator fun get(name: String): Job? = jobs[name]

    /**
     * Alias for [put].
     * Will throw if a job with the same name already exists.
     */
    public operator fun set(name: String, value: Job): Unit = put(name, value)

    /**
     * Alias for [set] and [put].
     */
    public operator fun invoke(name: String, job: Job): Unit = put(name, job)

    /**
     * Put all [jobs] into the storage.
     */
    public fun putAll(vararg jobs: Pair<String, Job>) {
        jobs.forEach { put(it.first, it.second) }
    }

    /**
     * Cancel a job with the specified [name]
     *
     * @return the job that was cancelled, or null if not found.
     */
    public fun cancel(name: String): Job? = jobs[name]?.apply { cancel() }
}

/**
 * Same as [JobManager.put].
 */
@FlowMVIDSL
public fun Job.register(manager: JobManager, name: String) {
    manager[name] = this
}

/**
 * Same as [JobManager.putOrReplace].
 */
@FlowMVIDSL
public suspend fun Job.registerOrReplace(
    manager: JobManager,
    name: String
): Job? = manager.putOrReplace(name, this)

/**
 * Create a new plugin that uses [manager] to manage jobs.
 * Will cancel and remove all jobs if the parent [pro.respawn.flowmvi.api.Store] is closed
 *
 * By default, job managers can't be reused without overriding [name]
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> jobManagerPlugin(
    manager: JobManager,
    name: String? = JobManager.Name, // by default, do not allow duplicates
): StorePlugin<S, I, A> = genericPlugin {
    this.name = name
    onStop {
        manager.cancelAll()
    }
}

/**
 * Create and install a new plugin that uses [JobManager] to manage running jobs.
 * The plugin will cancel all running jobs if the store is closed.
 *
 * By default, job managers can't be reused without overriding [name]
 *
 * @return the [JobManager] instance that was created for this plugin.
 */
@FlowMVIDSL
public fun <A : MVIAction, I : MVIIntent, S : MVIState> StoreBuilder<S, I, A>.manageJobs(
    name: String = JobManager.Name
): JobManager = JobManager().also {
    install(jobManagerPlugin(it, name))
}
