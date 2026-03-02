@file:pro.respawn.flowmvi.annotation.MustUseReturnValues

package pro.respawn.flowmvi.plugins

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
import pro.respawn.flowmvi.dsl.plugin
import pro.respawn.flowmvi.util.concurrentMutableMap

/**
 * An entity that manages running jobs. Used with  [jobManagerPlugin] and [manageJobs].
 * Will cancel and remove all jobs if the parent [pro.respawn.flowmvi.api.Store] is closed
 */
@Suppress("TooManyFunctions") // jobs have these functions!
public class JobManager<K : Any> {

    internal companion object {

        internal const val Name = "JobManagerPlugin"
    }

    private val jobs = concurrentMutableMap<K, Job>()

    /** Returns true if this manager has any active jobs, false otherwise. */
    public val hasJobs: Boolean get() = jobs.isNotEmpty()

    /** Returns a read-only view of the currently active jobs. */
    public val activeJobs: Collection<Job> get() = jobs.values

    /**
     * Cancels all jobs without suspending
     */
    @IgnorableReturnValue
    public fun cancelAll(): Unit = with(jobs) {
        forEach { (_, job) -> job.cancel() }
        clear()
    }

    /**
     * Joins all jobs **sequentially**
     */
    @IgnorableReturnValue
    public suspend fun joinAll(): Unit = try {
        jobs.values.joinAll()
    } finally {
        jobs.clear()
    }

    /**
     * Cancels all jobs **in parallel** and then joins them all **sequentially**
     */
    @IgnorableReturnValue
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
    @IgnorableReturnValue
    public suspend fun putOrReplace(key: K, job: Job): Job? {
        // do not put the job until we join it to not trigger completion
        // handler after we put another job at the same spot
        val previous = jobs[key]
        previous?.cancelAndJoin()

        jobs[key] = job.apply {
            invokeOnCompletion {
                jobs.remove(key)
            }
        }
        return previous
    }

    /**
     * Put a job into the container and throw [IllegalArgumentException] if the job with the same key is already running.
     *
     * Does **not** start the job that was put.
     */
    @IgnorableReturnValue
    public fun put(key: K, job: Job) {
        require(jobs.put(key, job)?.takeIf { it.isActive } == null) { "Job with the same key $key is already running!" }
        job.invokeOnCompletion {
            jobs.remove(key)
        }
    }

    /**
     * Get a job for the specified [key].
     */
    public operator fun get(key: K): Job? = jobs[key]

    /**
     * Alias for [put].
     * Will throw if a job with the same key already exists.
     */
    @IgnorableReturnValue
    public operator fun set(key: K, value: Job): Unit = put(key, value)

    /**
     * Alias for [set] and [put].
     */
    @IgnorableReturnValue
    public operator fun invoke(key: K, job: Job): Unit = put(key, job)

    /**
     * Put all [jobs] into the storage.
     */
    @IgnorableReturnValue
    public fun putAll(vararg jobs: Pair<K, Job>) {
        jobs.forEach { put(it.first, it.second) }
    }

    /**
     * Cancel a job with the specified [key]
     *
     * @return the job that was cancelled, or null if not found.
     */
    @IgnorableReturnValue
    public fun cancel(key: K): Job? = jobs[key]?.apply { cancel() }

    /**
     * Cancel and join a job for [key] if it is present
     * @return the job that was cancelled, or null if not found.
     */
    @IgnorableReturnValue
    public suspend fun cancelAndJoin(key: K): Job? = jobs[key]?.apply { cancelAndJoin() }

    /**
     * Join the job for [key] if it is present.
     *
     * @return the completed job or null if not found
     */
    @IgnorableReturnValue
    public suspend fun join(key: K): Job? = jobs[key]?.apply { join() }

    /**
     * Joins all jobs specified in [keys] in the declaration order
     */
    @IgnorableReturnValue
    public suspend fun joinAll(vararg keys: K): Unit = keys.forEach { join(it) }

    /**
     * Start the job with [key] if it is present.
     * @return the job that was started or null if not found.
     */
    @IgnorableReturnValue
    public fun start(key: K): Job? = jobs[key]?.apply { start() }
}

/**
 * Same as [JobManager.put].
 */
@IgnorableReturnValue
@FlowMVIDSL
public fun <K : Any> Job.register(manager: JobManager<K>, key: K): Job = apply { manager[key] = this }

/**
 * Same as [JobManager.putOrReplace].
 */
@IgnorableReturnValue
@FlowMVIDSL
public suspend fun <K : Any> Job.registerOrReplace(
    manager: JobManager<K>,
    key: K,
): Job? = manager.putOrReplace(key, this)

/**
 * Create a new plugin that uses [manager] to manage jobs.
 * This will cancel and remove all jobs when the parent [pro.respawn.flowmvi.api.Store] is closed.
 *
 * By default, job managers can't be reused without overriding [name].
 */
@FlowMVIDSL
public fun <K : Any, S : MVIState, I : MVIIntent, A : MVIAction> jobManagerPlugin(
    manager: JobManager<K>,
    name: String? = JobManager.Name, // by default, do not allow duplicates
): StorePlugin<S, I, A> = plugin {
    this.name = name
    onStop { manager.cancelAll() }
}

/**
 * Create and install a new plugin that uses [JobManager] to manage running jobs.
 * The plugin will cancel all running jobs if the store is closed.
 *
 * By default, job managers can't be reused without overriding [name]
 *
 * This version creates a basic job manager of type [String]. If you want to assign other types to your keys,
 * please use [jobManagerPlugin] builder function.
 *
 * @return the [JobManager] instance that was created for this plugin.
 */
@FlowMVIDSL
public fun <K : Any, A : MVIAction, I : MVIIntent, S : MVIState> StoreBuilder<S, I, A>.manageJobs(
    jobs: JobManager<K> = JobManager(),
    name: String = JobManager.Name
): JobManager<K> = jobs.also { install(jobManagerPlugin(it, name)) }
