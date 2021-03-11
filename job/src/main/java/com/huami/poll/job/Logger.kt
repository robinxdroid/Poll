package com.huami.poll.job

import android.util.Log

/**
 * [Job] Logger.
 * @author wangbin@huami.com <br>
 * @since 2021/3/3
 */
interface Logger {
    /**
     * Add [message] to log.
     */
    fun log(message: String)

    companion object
}

/**
 * Default logger to use.
 */
val Logger.Companion.DEFAULT: Logger get() = Android()

/**
 * [Logger] using [println].
 */
val Logger.Companion.SIMPLE: Logger get() = SimpleLogger()

/**
 * Empty [Logger] for test purpose.
 */
val Logger.Companion.EMPTY: Logger
    get() = object : Logger {
        override fun log(message: String) {}
    }

private class SimpleLogger : Logger {
    override fun log(message: String) {
        println("Job: $message")
    }
}

private class Android : Logger {
    companion object {
        private const val TAG = "Job"

    }

    override fun log(message: String) {
        Log.d(TAG, message)
    }
}