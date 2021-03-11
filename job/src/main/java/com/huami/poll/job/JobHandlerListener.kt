package com.huami.poll.job

/**
 * @author wangbin@huami.com <br>
 * @since 2021/3/5
 */
interface JobHandlerListener<T : Job<T>> {
    fun onPrepare(job: T)
    fun onCancel(job: T)
    fun onFinish(job: T)
}

abstract class JobHandlerListenerAdapter<T : Job<T>> : JobHandlerListener<T> {
    override fun onPrepare(job: T) {}
    override fun onCancel(job: T) {}
    override fun onFinish(job: T) {}
}