package com.huami.poll.job

import android.os.Handler
import android.os.Looper
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author wangbin@huami.com <br>
 * @since 2021/3/5
 */
class JobQueue<T : Job<T>>(
    threadPoolSize: Int = DEFAULT_THREAD_POOL_SIZE,
    private val interceptor: Interceptor<T> = Interceptor { false }
) {
    private val jobQueue = PriorityBlockingQueue<T>()
    private val jobDispatchers: Array<JobDispatcher<T>?> = arrayOfNulls(threadPoolSize)
    private val delivery: Delivery<T> = DefaultDelivery(Handler(Looper.getMainLooper()))
    private val waitingJobQueues: MutableMap<Any?, Queue<T>?> = HashMap()
    private val currentJobs: MutableSet<T> = HashSet()
    private val jobHandlerListener: MutableList<JobHandlerListener<T>> = ArrayList()
    private val sequenceGenerator = AtomicInteger()
    private var jobHandlerCallback: JobHandlerListener<T>? = null

    fun start() {
        stop()
        val threadPoolExecutor =
            Executors.newFixedThreadPool(jobDispatchers.size) as ThreadPoolExecutor
        for (i in jobDispatchers.indices) {
            val savedDispatcher =
                JobDispatcher(jobQueue, interceptor, delivery, jobHandlerListener)
            jobDispatchers[i] = savedDispatcher
            threadPoolExecutor.submit(savedDispatcher)
        }
    }

    fun stop() = jobDispatchers.forEach { it?.quit() }

    fun add(element: T): JobQueue<T> {
        log("add to queue")
        element.sequence(sequenceNumber)
        synchronized(currentJobs) { currentJobs.add(element) }
        if (jobHandlerCallback == null) {
            jobHandlerCallback = object : JobHandlerListenerAdapter<T>() {
                override fun onCancel(job: T) {
                    finish(job)
                }

                override fun onFinish(job: T) {
                    finish(job)
                }
            }
            addListener(jobHandlerCallback!!)
        }
        val repeatTag = element.repeatFilterKey
        if (repeatTag != null) {
            synchronized(waitingJobQueues) {
                if (waitingJobQueues.containsKey(repeatTag)) {
                    var stagedRequests = waitingJobQueues[repeatTag]
                    if (stagedRequests == null) {
                        stagedRequests = LinkedList()
                    }
                    stagedRequests.add(element)
                    waitingJobQueues[repeatTag] = stagedRequests
                    log("Job for filterTag =$repeatTag is in flight, putting on hold.")
                } else {
                    waitingJobQueues[repeatTag] = null
                    jobQueue.add(element)
                }
                return this
            }
        }
        jobQueue.add(element)
        return this
    }

    fun finish(element: T) {
        synchronized(currentJobs) { currentJobs.remove(element) }
        var repeatTag = element.repeatFilterKey
        if (repeatTag != null) {
            synchronized(waitingJobQueues) {
                val waitingJobQueue = waitingJobQueues.remove(repeatTag)
                if (waitingJobQueue != null) {
                    log("Releasing " + waitingJobQueue.size + " waiting jobs for filterTag=" + repeatTag)
                    jobQueue.addAll(waitingJobQueue)
                }
            }
        } else if (waitingJobQueues.isNotEmpty()) {  //Releasing all
            synchronized(waitingJobQueues) {
                val queueIterator: Iterator<Map.Entry<Any?, Queue<T>?>> =
                    waitingJobQueues.entries.iterator()
                while (queueIterator.hasNext()) {
                    val entry = queueIterator.next()
                    repeatTag = entry.key
                    val waitingJobQueue = waitingJobQueues.remove(repeatTag)
                    if (waitingJobQueue != null) {
                        log("Releasing " + waitingJobQueue.size + " waiting jobs for filterTag=" + repeatTag)
                        jobQueue.addAll(waitingJobQueue)
                    }
                }
            }
        }
    }

    fun action(action: Action<T>?): JobQueue<T> {
        for (i in jobDispatchers.indices) {
            jobDispatchers[i]!!.setAction(action)
        }
        return this
    }

    fun addListener(listener: JobHandlerListener<T>): JobQueue<T> {
        if (jobHandlerListener.contains(listener)) {
            return this
        }
        jobHandlerListener.add(listener)
        return this
    }

    private var prepareListener:JobHandlerListenerAdapter<T>?=null
    fun prepare(block: (job: T) -> Unit): JobQueue<T> {
        if (prepareListener == null) {
            prepareListener = object : JobHandlerListenerAdapter<T>() {
                override fun onPrepare(job: T) {
                    block(job)
                }
            }
        }
        prepareListener?.let {
            if (!jobHandlerListener.contains(it)) {
                jobHandlerListener.add(it)
            }
        }
        return this
    }


    private var finishListener:JobHandlerListenerAdapter<T>?=null
    fun finish(block: (job: T) -> Unit): JobQueue<T> {
        if (finishListener == null) {
            finishListener = object : JobHandlerListenerAdapter<T>() {
                override fun onFinish(job: T) {
                    block(job)
                }
            }
        }
        finishListener?.let {
            if (!jobHandlerListener.contains(it)) {
                jobHandlerListener.add(it)
            }
        }
        return this
    }


    private var cancelListener:JobHandlerListenerAdapter<T>?=null
    fun cancel(block: (job: T) -> Unit): JobQueue<T> {
        if (cancelListener == null) {
            cancelListener = object : JobHandlerListenerAdapter<T>() {
                override fun onCancel(job: T) {
                    block(job)
                }
            }
        }
        cancelListener?.let {
            if (!jobHandlerListener.contains(it)) {
                jobHandlerListener.add(it)
            }
        }
        return this
    }

    fun getQueue() = jobQueue


    private val sequenceNumber: Int
        get() = sequenceGenerator.incrementAndGet()

    interface JobFilter<T> {
        fun apply(job: T): Boolean
    }

    fun cancelAll(filter: JobFilter<T>) {
        synchronized(currentJobs) {
            for (job in currentJobs) {
                if (filter.apply(job)) {
                    job.cancel()
                }
            }
        }
    }

    fun cancelAll(tag: Any?) {
        requireNotNull(tag) { "Cannot cancelAll with a null tag" }
        cancelAll(object : JobFilter<T> {
            override fun apply(job: T): Boolean {
                return job.tag === tag
            }
        })
    }

    companion object {
        const val DEFAULT_THREAD_POOL_SIZE = 4
    }
}