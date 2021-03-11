package com.huami.poll.job

import android.os.Process
import java.util.concurrent.BlockingQueue

/**
 * @author wangbin@huami.com <br>
 * @since 2021/3/5
 */
class JobDispatcher<T : Job<T>>(
    private val jobQueue: BlockingQueue<T>,
    private val interceptor: Interceptor<T>?,
    private val delivery: Delivery<T>,
    private val jobHandlerListeners: List<JobHandlerListener<T>>
) : Thread() {
    private var action: Action<T>? = null

    @Volatile
    private var quit = false
    override fun run() {
        log("start new dispatcher:$name")
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
        while (true) {
            try {
                val element = jobQueue.take()
                log("job-queue-take,current thread:$name")

                if (element == null) {
                    log("element is null,current thread:$name")
                    continue
                }

                if (element.isCancel) {
                    log("job is cancel when run")
                    log("element is canceled,current thread:$name")
                    delivery.cancel(jobHandlerListeners, element)
                    continue
                }

                val shouldIntercept = interceptor?.onIntercept(element) ?: false
                if (shouldIntercept) {
                    continue
                }

                delivery.prepare(jobHandlerListeners, element)

                if (action == null) {
                    log("action is null,current thread:$name")
                    continue
                }
                log("job run start,current thread:$name")
                action?.call(element)

                log("job run finish,current thread:$name")
                element.finish()

                log("element is finished,current thread:$name")
                delivery.finish(jobHandlerListeners, element)
            } catch (e: InterruptedException) {
                if (quit) {
                    return
                }
                continue
            }
        }
    }

    fun setAction(action: Action<T>?) {
        this.action = action
    }

    fun quit() {
        quit = true
        interrupt()
    }
}