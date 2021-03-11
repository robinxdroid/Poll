package com.huami.poll.job

/**
 * @author wangbin@huami.com <br>
 * @since 2021/3/3
 */
object Logging {
    internal var logger = Logger.DEFAULT
}

fun log(message: String) = Logging.logger.log(message)


fun logger(logger: Logger) {
    Logging.logger = logger
}