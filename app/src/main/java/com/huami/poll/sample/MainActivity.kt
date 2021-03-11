package com.huami.poll.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.huami.poll.job.Poll
import com.huami.poll.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var activityMainBinding: ActivityMainBinding? = null
    var heartRate = 0
    var steps = 0L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding?.root)

        Poll(period = 500) {
            RealTimeReceiver.onHeartRateReceived(
                HeartRate(
                    System.currentTimeMillis(),
                    heartRate = heartRate
                )
            )
            heartRate++
        }
        Poll(period = 1000) {
            RealTimeReceiver.onStepReceived(Step(System.currentTimeMillis(), steps, steps))
            steps++
        }
        activityMainBinding?.start?.setOnClickListener {
            TimelyPoller.start()
        }
        activityMainBinding?.stop?.setOnClickListener {
            TimelyPoller.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        TimelyPoller.stop()
    }
}