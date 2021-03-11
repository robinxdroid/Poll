package com.huami.poll.sample

import com.huami.poll.job.Job
import kotlin.math.max

/**
 * 实时数据接收者
 * @author wangbin@huami.com <br>
 * @since 2021/3/8
 */
object RealTimeReceiver {
    private var currentStep: Step? = null
    private var currentHeartRate: HeartRate? = null


    fun onStepReceived(step: Step) {
        currentStep = step
    }

    fun onHeartRateReceived(heartRate: HeartRate) {
        currentHeartRate = heartRate
    }

    fun generateJob(): TimelyDataJob? {
        var steps = 0L
        var calories = 0L
        var heartRate = 0

        var stepTime = 0L
        var heartRateTime = 0L

        var trigger = false

        currentStep?.let { step ->
            if (dataValid(step.time)) {
                steps = step.steps
                calories = step.calories
                stepTime = step.time
                trigger = true
            }
        }

        currentHeartRate?.let { hr ->
            if (dataValid(hr.time)) {
                heartRate = hr.heartRate
                heartRateTime = hr.time
                trigger = true
            }
        }

        if (trigger) {
            return TimelyDataJob(
                max(stepTime, heartRateTime),
                heartRate = heartRate,
                steps = steps,
                calories = calories
            ).also { it.tag(TimelyDataJob.TAG) }
        }
        return null

    }

    private fun dataValid(time: Long): Boolean =
        System.currentTimeMillis() - time <= TimelyConfig.PERIOD
}

data class Step(val time: Long, val steps: Long, val calories: Long)

data class HeartRate(val time: Long, val heartRate: Int)

data class TimelyDataJob(
    val time: Long,
    val heartRate: Int,
    val steps: Long,
    val calories: Long,
    override val repeatFilterKey: Any? = time
) : Job<TimelyDataJob>() {
    companion object {
        const val TAG = "RealTimeDataJob"
    }
}