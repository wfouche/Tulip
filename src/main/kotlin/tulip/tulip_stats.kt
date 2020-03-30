package tulip

import java.util.*
import java.util.concurrent.ArrayBlockingQueue as Queue
import java.util.concurrent.LinkedBlockingQueue

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.BufferedWriter

import java.io.File
import java.io.FileWriter

data class ActionSummary(
    var action_id: Int = 0,
    var num_actions: Int = 0,
    var num_success: Int = 0,

    var latencyMap: MutableMap<Long,Long> = mutableMapOf<Long, Long>(),

    var duration_seconds: Double = 0.0,

    var aps: Double = 0.0,
    var art: Double = 0.0,
    var sdev: Double = 0.0,
    var min_rt: Double = 0.0,
    var max_rt: Double = 0.0,
    var max_rt_ts: String = "",

    var pk: List<Double> = mutableListOf<Double>(),
    var pv: List<Double> = mutableListOf<Double>()
)

class ActionStats {

    val latencyMap = mutableMapOf<Long, Long>()
    var latencyMap_min_rt: Long = Long.MAX_VALUE
    var latencyMap_max_rt: Long = Long.MIN_VALUE
    var latencyMap_max_rt_ts = ""

    var num_actions: Int = 0
    var num_success: Int = 0

    private val r = ActionSummary()

    fun createSummary(action_id: Int, duration_millis: Int, testCase: TestCase) {
        r.action_id = action_id
        r.num_actions = num_actions
        r.num_success = num_success

        r.duration_seconds = duration_millis.toDouble() / 1000.0

        // actions per second (aps)
        r.aps = num_actions / r.duration_seconds

        // average response time (art) in milliseconds
        r.art = latencyMap.map { it.value * it.key }.sum() / 1000.0 / num_actions

        // standard deviation
        // HOWTO: https://www.statcan.gc.ca/edu/power-pouvoir/ch12/5214891-eng.htm#a2
        r.sdev = Math.sqrt(latencyMap.map { it.value * Math.pow((it.key / 1000.0 - r.art), 2.0) }.sum() / num_actions)

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
        r.min_rt = latencyMap_min_rt / 1000.0

        // max rt
        r.max_rt = latencyMap_max_rt / 1000.0

        // max rt timestamp
        r.max_rt_ts = latencyMap_max_rt_ts

        // percentiles
        r.pk = testCase.percentiles
        r.pv = mutableListOf<Double>().apply {
            r.pk.forEach {
                val px = percentile(it, r.min_rt, r.max_rt)
                this.add(px)
            }
        }

        r.latencyMap = latencyMap
    }

    fun printStats(printMap: Boolean = false) {

        val output = mutableListOf("")

        if (printMap) {
            output.add("latencyMap = " + r.latencyMap.toString())
            output.add("")
        }
        output.add("  num_actions = ${r.num_actions}")
        output.add("  num_success = ${r.num_success}")
        output.add("  num_failed  = ${r.num_actions - r.num_success}")
        output.add("")

        output.add("  average number of actions completed per second = ${"%.3f".format(Locale.US, r.aps)}")
        output.add("  average duration/response time in milliseconds = ${"%.3f".format(Locale.US, r.art)}")
        output.add("  standard deviation  (response time)  (millis)  = ${"%.3f".format(Locale.US, r.sdev)}")
        output.add("")
        output.add("  duration of benchmark (in seconds) = ${r.duration_seconds}")
        output.add("  number of actions completed = ${r.num_actions}")

        output.add("")

        r.pk.forEachIndexed { index, percentile ->
            val px = r.pv.elementAt(index)
            output.add("  ${percentile}th percentile (response time) (millis) = ${"%.3f".format(Locale.US, px)}")
        }

        output.add("")
        output.add("  minimum response time (millis) = ${"%.3f".format(Locale.US, r.min_rt)}")
        output.add("  maximum response time (millis) = ${"%.3f".format(Locale.US, r.max_rt)} at ${latencyMap_max_rt_ts}")

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

    fun saveStatsJson(filename: String) {
        val results = "{'duration': ${r.duration_seconds}, 'num_actions': ${num_actions}, 'num_success': ${num_success}, 'num_failed': ${num_actions - num_success}, 'avg_tps': ${r.aps}, 'avg_rt': ${r.art}, 'sdev_rt': ${r.sdev}, 'min_rt': ${r.min_rt}, 'max_rt': ${r.max_rt}, 'max_rt_ts': '${r.max_rt_ts}'}"
        val fw = FileWriter(filename, true)
        val bw = BufferedWriter(fw).apply {
            write(results)
            newLine()
        }
        bw.close()
        fw.close()
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
            latencyMap_max_rt_ts = java.time.LocalDateTime.now().toString()
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
        latencyMap_max_rt_ts = ""

        num_actions = 0
        num_success = 0
    }
}

object DataCollector {
    val actionStats = Array(NUM_ACTIONS+1) {ActionStats()}

    fun createSummary(duration_millis: Int, testCase: TestCase) {
        actionStats[NUM_ACTIONS].createSummary(NUM_ACTIONS, duration_millis, testCase)
    }

    fun printStats(printMap: Boolean = false) {
        actionStats[NUM_ACTIONS].printStats(printMap)
    }

    fun saveStatsJson(filename: String) {
        if (filename != "") {
            actionStats[NUM_ACTIONS].saveStatsJson(filename)
        }
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
