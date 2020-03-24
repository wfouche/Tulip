package tulip

import java.util.*
import java.util.concurrent.ArrayBlockingQueue as Queue
import java.util.concurrent.LinkedBlockingQueue

import com.google.gson.Gson
import com.google.gson.GsonBuilder

data class ActionStatsSummary(
    val duration_seconds: Double = 0.0
)

class ActionStats {

    val latencyMap = mutableMapOf<Long, Long>()
    var latencyMap_min_rt: Long = Long.MAX_VALUE
    var latencyMap_max_rt: Long = Long.MIN_VALUE
    var latencyMap_max_ts = ""

    var num_actions: Int = 0
    var num_success: Int = 0

    fun printStats(duration_millis: Int, printMap: Boolean = false, test: TestCase) {
        val duration_seconds: Double = duration_millis.toDouble() / 1000.0

        // actions per second (aps)
        val aps: Double = num_actions / duration_seconds

        // average response time (art) in milliseconds
        val art: Double = latencyMap.map { it.value * it.key }.sum() / 1000.0 / num_actions

        // standard deviation
        // HOWTO: https://www.statcan.gc.ca/edu/power-pouvoir/ch12/5214891-eng.htm#a2
        val sdev: Double = Math.sqrt(latencyMap.map { it.value * Math.pow((it.key / 1000.0 - art), 2.0) }.sum() / num_actions)

        // 95th percentile value
        // HOWTO 1: https://www.youtube.com/watch?v=9QhU2grGU_E
        // HOWTO 2: https://www.youtube.com/watch?v=8U__c22VOVA
        //
        // https://harding.edu/sbreezeel/460%20files/statbook/chapter5.pdf
        //
        // Formula for finding Percentiles (Grouped Frequency Distribution)
        ///
        fun percentile(k: Double, min_value: Double = 0.0, max_value: Double = 0.0): Double {
            val P: Double = (k / 100.0) * num_actions

            var CFb: Double = 0.0
            var F: Double = 0.0

            val keys = latencyMap.keys.sorted()
            var tk: Long = 0
            for (key in keys) {
                tk = key
                F = latencyMap[key]!!.toDouble()
                if (100.0 * (CFb + F) / (1.0 * num_actions) >= k) {
                    break
                }
                CFb += F
            }
            var L = tk
            var U = tk

            while (llq(L) == tk) {
                L -= 1
            }
            L += 1

            while (llq(U) == tk) {
                U += 1
            }
            U -= 1

            var p: Double = (L + ((P - CFb) / F) * (U - L)) / 1000.0
            if (p < 0.0) {
                p = 0.0
            }

            if (p < min_value) {
                p = min_value
            }
            if (p > max_value) {
                p = max_value
            }

            return p
        }

        // min rt
        val min_rt = latencyMap_min_rt / 1000.0

        // max rt
        val max_rt = latencyMap_max_rt / 1000.0

        val output = mutableListOf("")

        if (printMap) {
            output.add("latencyMap = " + latencyMap.toString())
            output.add("")
        }
        output.add("  num_actions = ${num_actions}")
        output.add("  num_success = ${num_success}")
        output.add("  num_failed  = ${num_actions - num_success}")
        output.add("")

        output.add("  average number of actions completed per second = ${"%.3f".format(Locale.US, aps)}")
        output.add("  average duration/response time in milliseconds = ${"%.3f".format(Locale.US, art)}")
        output.add("  standard deviation  (response time)  (millis)  = ${"%.3f".format(Locale.US, sdev)}")
        output.add("")
        output.add("  duration of benchmark (in seconds) = ${duration_seconds}")
        output.add("  number of actions completed = ${num_actions}")

        output.add("")

        val percentiles = test.percentiles
        for (kk in percentiles) {
            val px = percentile(kk, min_rt, max_rt)
            output.add("  ${kk}th percentile (response time) (millis) = ${"%.3f".format(Locale.US, px)}")
        }

        output.add("")
        output.add("  minimum response time (millis) = ${"%.3f".format(Locale.US, min_rt)}")
        output.add("  maximum response time (millis) = ${"%.3f".format(Locale.US, max_rt)} at ${latencyMap_max_ts}")

        output.add("")
        var cpu_load: Double = 0.0
        var i = 0.0
        while (!CpuLoadMetrics.processCpuStats.isEmpty())
        {
            cpu_load += CpuLoadMetrics.processCpuStats.take()
            i += 1.0
        }
        output.add("  average cpu load (process) = ${"%.3f".format(Locale.US, if (i == 0.0) 0.0 else cpu_load / i)}")

        cpu_load = 0.0
        i = 0.0
        while (!CpuLoadMetrics.systemCpuStats.isEmpty())
        {
            cpu_load += CpuLoadMetrics.systemCpuStats.take()
            i += 1.0
        }
        output.add("  average cpu load (system)  = ${"%.3f".format(Locale.US, if (i == 0.0) 0.0 else cpu_load / i)}")

        Console.put(output)
    }

    fun updateStats(task: Task) {
        // frequency map: count the number of times a given (key) response time occurred.
        // key = response time in microseconds (NOT milliseconds).
        // value = the number of times a given (key) response time occurred.

        val durationMicros = task.durationMicros
        val key = llq(durationMicros)

        // Attempt 1:
        //
        // if (key in latencyMap.keys) {
        //    latencyMap[key] = (latencyMap[key])!! + 1
        //} else {
        //    latencyMap[key] = 1
        //}

        // Attempt 2:
        //
        //latencyMap[key] = latencyMap.getOrDefault(key, 0) + 1

        // Attempt 3:
        //
        // Java 8:  map.merge(key, 1, (a, b) -> a + b);
        //
        // Kotlin: map.merge(key, 1, {a, b -> a+b})
        //
        latencyMap.merge(key, 1, { a, b -> a + b })

        if (durationMicros < latencyMap_min_rt) {
            latencyMap_min_rt = durationMicros
        }
        if (durationMicros > latencyMap_max_rt) {
            latencyMap_max_rt = durationMicros
            latencyMap_max_ts = java.time.LocalDateTime.now().toString()
        }
        num_actions += 1
        if (task.status == 1) {
            num_success += 1
        }
    }

    fun clearStats() {
        latencyMap.clear()
        latencyMap_min_rt = Long.MAX_VALUE
        latencyMap_max_rt = Long.MIN_VALUE
        latencyMap_max_ts = ""

        num_actions = 0
        num_success = 0
    }
}

object DataCollector {
    val actionStats = Array(NUM_ACTIONS+1) {ActionStats()}

    fun printStats(duration_millis: Int, printMap: Boolean = false, test: TestCase) {
        actionStats[NUM_ACTIONS].printStats(duration_millis, printMap, test)
    }

    fun updateStats(task: Task) {
        require(task.actionId < NUM_ACTIONS)
        if (task.actionId < 0) {
            // Unused task drained from the rsp queue.
            return
        }
        actionStats[NUM_ACTIONS].updateStats(task)
        actionStats[task.actionId].updateStats(task)
    }

    fun clearStats() {
        actionStats.forEach {
            it.clearStats()
        }
    }
}

/*-------------------------------------------------------------------------*/
