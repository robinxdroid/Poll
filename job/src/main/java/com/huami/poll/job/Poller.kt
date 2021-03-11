package com.huami.poll.job

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * 轮询装置
 * @author wangbin@huami.com <br>
 * @since 2021/3/5
 */
class Poller {
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private var future: ScheduledFuture<*>? = null

    var initialDelay: Long = 0L
    var period: Long = 0L
    var timeUnit: TimeUnit = TimeUnit.MILLISECONDS
    var action: () -> Unit = {}

    fun loop() {
        future = executor.scheduleWithFixedDelay({ action() }, initialDelay, period, timeUnit)
    }

    fun stop() {
        future?.run {
            if (!isCancelled) cancel(true)
        }
    }
}

fun Poll(
    initialDelay: Long = 0L,
    period: Long,
    timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
    action: () -> Unit
): Poller =
    Poller().apply {
        this.initialDelay = initialDelay
        this.period = period
        this.timeUnit = timeUnit
        this.action = action
        loop()
    }
