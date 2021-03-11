package com.huami.poll.job

/**
 * @author wangbin@huami.com <br>
 * @since 2021/3/5
 */
fun interface Interceptor<T : Job<T>> {
    fun onIntercept(job: T): Boolean
}