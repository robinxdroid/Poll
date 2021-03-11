package com.huami.poll.job

import android.os.Handler
import java.util.concurrent.Executor

/**
 * @author wangbin@huami.com <br>
 * @since 2021/3/5
 */
interface Delivery<T : Job<T>> {
    fun prepare(listeners: List<JobHandlerListener<T>>, job: T)
    fun cancel(listeners: List<JobHandlerListener<T>>, job: T)
    fun finish(listeners: List<JobHandlerListener<T>>, job: T)
}

class DefaultDelivery<T : Job<T>>(handler: Handler) : Delivery<T> {
    private val responsePoster: Executor = Executor { command -> handler.post(command) }

    override fun prepare(listeners: List<JobHandlerListener<T>>, job: T) {
        responsePoster.execute(Runnable {
            if (job.isCancel) {
                log("job is cancel when deliveryPrepare")
                cancel(listeners, job)
                return@Runnable
            }
            log("deliveryPrepare,current thread:${Thread.currentThread().name}")
            listeners.forEach { it.onPrepare(job) }
        })
    }

    override fun cancel(listeners: List<JobHandlerListener<T>>, job: T) {
        responsePoster.execute {
            log("deliveryCancel,current thread:${Thread.currentThread().name}")
            listeners.forEach { it.onCancel(job) }
        }
    }

    override fun finish(listeners: List<JobHandlerListener<T>>, job: T) {
        if (job.isCancel) {
            log("job is cancel when deliveryFinish")
            cancel(listeners, job)
            return
        }
        responsePoster.execute {
            log("deliveryFinish,current thread:${Thread.currentThread().name}")
            listeners.forEach { it.onFinish(job) }
        }
    }
}