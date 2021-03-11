package com.huami.poll.job

/**
 * @author wangbin@huami.com <br>
 * @since 2021/3/5
 */
abstract class Job<T : Job<T>> : Comparable<T> {
    var tag: Any? = null
        protected set
    var isCancel = false
        protected set
    var isFinish = false
        protected set
    var sequence: Int = 0
        protected set
    var priority = Priority.NORMAL
        protected set

    fun tag(tag: Any?): Job<*> {
        this.tag = tag
        return this
    }

    fun priority(priority: Priority): Job<*> {
        this.priority = priority
        return this
    }

    fun cancel(): Job<*> {
        isCancel = true
        return this
    }

    fun finish(): Job<*> {
        isFinish = true
        return this
    }

    fun sequence(sequence: Int): Job<*> {
        this.sequence = sequence
        return this
    }

    override fun compareTo(other: T): Int {
        val left = priority
        val right = other.priority
        return if (left === right) sequence - other.sequence else right.ordinal - left.ordinal
    }

    abstract val repeatFilterKey: Any?
}