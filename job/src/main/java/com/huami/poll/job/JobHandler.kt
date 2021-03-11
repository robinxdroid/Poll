package com.huami.poll.job

/**
 * @author wangbin@huami.com <br>
 * @since 2021/3/5
 */
class JobHandler<T : Job<T>> private constructor(builder: Builder<T>) {

    private var threadPoolSize: Int = builder.threadPoolSize
    private var interceptor: Interceptor<T> = builder.interceptor
    private val jobQueue: JobQueue<T> by lazy { JobQueue(threadPoolSize, interceptor) }

    fun enqueue(element: T,block:JobQueue<T>.()->Unit): JobQueue<T> {
        return jobQueue.add(element).apply(block)
    }

    fun <TAG> cancelAll(tag: TAG): JobQueue<T> {
        jobQueue.cancelAll(tag)
        return jobQueue
    }

    fun getQueue() = jobQueue.getQueue()

    class Builder<T : Job<T>> {
        internal var threadPoolSize = JobQueue.DEFAULT_THREAD_POOL_SIZE
        internal var interceptor: Interceptor<T> = Interceptor { false }

        fun threadPoolSize(threadPoolSize: Int): Builder<T> {
            this.threadPoolSize = threadPoolSize
            return this
        }

        fun interceptor(interceptor: Interceptor<T>): Builder<*> {
            this.interceptor = interceptor
            return this
        }

        fun build(): JobHandler<T> {
            return JobHandler(this)
        }
    }

    init {
        jobQueue.start()
    }
}


fun <T : Job<T>> Builder(block: JobHandler.Builder<T>.() -> Unit): JobHandler<T> =
    JobHandler.Builder<T>().apply(block).build()

fun <T : Job<T>> JobHandler(
    threadPoolSize: Int = JobQueue.DEFAULT_THREAD_POOL_SIZE,
    interceptor: Interceptor<T> = Interceptor { false },
    block: JobHandler<T>.() -> Unit = {}
): JobHandler<T> {
    val jobHandler = Builder<T> {
        this.threadPoolSize = threadPoolSize
        this.interceptor = interceptor
    }
    jobHandler.block()
    return jobHandler
}
