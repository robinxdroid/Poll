package com.huami.poll.job

/**
 * @author wangbin@huami.com <br>
 * @since 2021/3/5
 */
fun interface Action<T : Job<T>> {
    fun call(element: T)
}