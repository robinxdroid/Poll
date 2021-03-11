package com.huami.poll.sample

import android.util.Log
import com.huami.poll.job.JobHandler
import com.huami.poll.job.Poll
import com.huami.poll.job.Poller
import com.huami.poll.sample.TimelyConfig.PERIOD

/**
 * 实时数据轮询器
 * @author wangbin@huami.com <br>
 * @since 2021/3/8
 */
object TimelyPoller {
    private var jobHandler: JobHandler<TimelyDataJob>? = null
    private var poller: Poller? = null

    /**
     * 开启实时数据轮询
     *
     */
    fun start() {
        stop()
        // 开启轮询
        poller = Poll(period = PERIOD) {
            if (jobHandler == null) {
                jobHandler = JobHandler(threadPoolSize = TimelyConfig.getThreadNum())
            }
            val job = RealTimeReceiver.generateJob() ?: return@Poll
            jobHandler?.enqueue(job) {
                action {
                    //TODO 上传操作
                    Log.d("RealTimePoller", "执行上传操作${it.heartRate} ${it.steps} ${it.calories} ")
                    Thread.sleep(5000)
                }

                finish {
                    //action操作执行完毕
                    Log.d(
                        "RealTimePoller",
                        "action操作执行完毕${it.heartRate} ${it.steps} ${it.calories} "
                    )
                }
            }

            Log.d(
                "RealTimePoller",
                "当前队列情况${jobHandler?.getQueue()} "
            )
        }
    }

    /**
     * 终止轮询线程，清除任务队列
     *
     */
    @JvmStatic
    fun stop() {
        poller?.stop()
        jobHandler?.cancelAll(TimelyDataJob.TAG)
    }
}