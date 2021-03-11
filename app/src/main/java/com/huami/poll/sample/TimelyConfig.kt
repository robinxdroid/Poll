package com.huami.poll.sample

/**
 * 实时数据全局配置
 * @author wangbin@huami.com <br>
 * @since 2021/3/8
 */
object TimelyConfig {
    /**
     * 轮询间隔（毫秒）
     */
    const val PERIOD = 5000L

    /**
     * 上传接口超时时间
     */
    const val UPLOAD_TIMEOUT = 30 * 1000L

    /**
     * 计算任务线程数量，防止任务队列在轮询速率快但是任务执行慢的情况下导致无限膨胀
     *
     * @return
     */
    fun getThreadNum(): Int {
        // 约定上传接口超时为30（秒）的情况下
        // 轮询间隔（秒）
        val period = PERIOD / 1000
        return (UPLOAD_TIMEOUT / period).toInt()
    }
}