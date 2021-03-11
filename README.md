# Poll
**轮询装置 + 任务队列 + 多线程并发处理**  模型

轮询线程 ♻️ ---`推送任务（优先级，重复性）`---> 任务队列 <---> 多线程 ---> 执行任务 ----> callback

# 轮询
```kotlin
Poll(period = 1000) {
    //TODO 间隔1000毫秒重复执行一个动作
}
```

# 任务队列
```kotlin
// 定义job
data class TestJob(
    val time: Long,
    override val repeatFilterKey: Any? = time
) : Job<TestJob>() {
    companion object {
        const val TAG = "RealTimeDataJob"
    }
}

// 推送任务至任务队列等待执行
val job = TestJob(1000)
val jobHandler = JobHandler(threadPoolSize = 4)
jobHandler.enqueue(job) {
    action {
        //TODO 任务处理，比如上传数据至云端，或者大幅度耗时处理动作
    }

    finish {
        //action操作执行完毕回调
    }
}
```
