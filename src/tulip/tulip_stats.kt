package tulip

//import io.micrometer.core.instrument.Counter
import com.google.gson.Gson
import java.util.*
import java.io.BufferedWriter

import java.io.FileWriter
import kotlin.math.pow
import kotlin.math.sqrt

import org.HdrHistogram.Histogram
import java.nio.ByteBuffer
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private const val histogramNumberOfSignificantValueDigits=2

data class ActionSummary(
    var actionId: Int = 0,

    var rowId: Int = 0,

    var testBegin: String = "",
    var testEnd: String = "",
    var testName: String = "",
    var testPhase: String = "",

    var testId: Int = 0,
    var indexUserProfile: Int = 0,
    var queueLength: Int = 0,

    var numActions: Int = 0,
    var numSuccess: Int = 0,

    var latencyMap2: Histogram = Histogram(histogramNumberOfSignificantValueDigits),

    var durationSeconds: Double = 0.0,

    var aps: Double = 0.0,
    var art: Double = 0.0,
    var sdev: Double = 0.0,
    var minRt: Double = 0.0,
    var maxRt: Double = 0.0,
    var maxRtTs: String = "",

    var awt: Double = 0.0,
    var maxWt: Double = 0.0,

    var pk: List<Double> = mutableListOf(),
    var pv: List<Double> = mutableListOf(),

    //var avgCpuSystem: Double = 0.0,
    //var avgCpuProcess: Double = 0.0
)

class ActionStats {
    // <numberOfSignificantValueDigits>
    //private val NUM_DIGITS=1  // Tested - inaccurate results, don't use
    //private val NUM_DIGITS=2  // Tested - good results, small results file (default, optimal)
    //private val NUM_DIGITS=3  // Tested - great results, large results file, histogram_rt
    private val latencyMap2: Histogram = Histogram(histogramNumberOfSignificantValueDigits)
    private var latencyMapMinRt: Long = Long.MAX_VALUE
    private var latencyMapMaxRt: Long = Long.MIN_VALUE
    private var latencyMapMaxRtTs = ""
    private var waitTimeMicrosHistogram = Histogram(histogramNumberOfSignificantValueDigits)

    var numActions: Int = 0
    private var numSuccess: Int = 0

    val r = ActionSummary()

    fun createSummary(
        actionId: Int,
        durationMillis: Int,
        testCase: TestProfile,
        indexTestCase: Int,
        indexUserProfile: Int,
        queueLength: Int,
        tsBegin: String,
        tsEnd: String,
        testPhase: String,
        runId: Int
    ) {
        r.actionId = actionId

        r.rowId = runId

        r.testName = testCase.name
        r.testBegin = tsBegin
        r.testEnd = tsEnd
        r.testPhase = testPhase

        r.testId = indexTestCase
        r.indexUserProfile = indexUserProfile
        r.queueLength = queueLength

        r.numActions = numActions
        r.numSuccess = numSuccess

        r.durationSeconds = durationMillis.toDouble() / 1000.0

        // actions per second (aps)
        r.aps = numActions / r.durationSeconds

        // average response time (art) in milliseconds
        //r.art = latencyMap.map { it.value * it.key }.sum() / 1000.0 / numActions
        r.art = latencyMap2.mean / 1000.0

        // standard deviation
        // HOWTO: https://www.statcan.gc.ca/edu/power-pouvoir/ch12/5214891-eng.htm#a2
        //r.sdev = sqrt(latencyMap.map { it.value * (it.key / 1000.0 - r.art).pow(2.0) }.sum()
        r.sdev = latencyMap2.stdDeviation / 1000.0

        // min rt
        r.minRt = latencyMapMinRt / 1000.0

        // max rt
        r.maxRt = latencyMapMaxRt / 1000.0

        // max rt timestamp
        r.maxRtTs = latencyMapMaxRtTs

        // percentiles
        r.pk = testCase.percentiles
        r.pv = mutableListOf<Double>().apply {
            r.pk.forEach {
                var px = latencyMap2.getValueAtPercentile(it) / 1000.0
                if (px > r.maxRt) px = r.maxRt
                this.add(px)
            }
        }

        r.latencyMap2 = latencyMap2

        r.awt = waitTimeMicrosHistogram.mean / 1000.0
        r.maxWt = waitTimeMicrosHistogram.maxValueAsDouble / 1000.0

        // Summarize CPU usage for global stats only.
        if (actionId == NUM_ACTIONS) {
            // average process CPU load.
            // r.avgCpuProcess = 0.0

            // average system CPU load.
            // r.avgCpuSystem = 0.0
        }

    }

    fun printStats(actionId: Int, printMap: Boolean = false) {

        val output = mutableListOf("")

        if (printMap) {
            val gson = Gson()
            val latencyMap: MutableMap<Long, Long> = mutableMapOf()
            output.add("latencyMap = " + gson.toJson(latencyMap).toString())
            output.add("")
        }
        if (actionId != NUM_ACTIONS) {
            output.add("  action_id = ${r.actionId}")
        }
        output.add("  num_actions = ${r.numActions}")
        output.add("  num_success = ${r.numSuccess}")
        output.add("  num_failed  = ${r.numActions - r.numSuccess}")
        output.add("")
        output.add("  average number of actions completed per second = ${"%.3f".format(Locale.US, r.aps)}")
        output.add("  duration of benchmark (in seconds)             = ${r.durationSeconds} seconds")
        output.add("")
        output.add("  average latency     (response time)  (millis)  = ${"%.3f".format(Locale.US, r.art)} ms")
        output.add("  standard deviation  (response time)  (millis)  = ${"%.3f".format(Locale.US, r.sdev)} ms")
        output.add("")
        r.pk.forEachIndexed { index, percentile ->
            val px = r.pv.elementAt(index)
            output.add("  ${percentile}th percentile (response time) (millis) = ${"%.3f".format(Locale.US, px)} ms")
        }

        output.add("")
        output.add("  minimum response time (millis) = ${"%.3f".format(Locale.US, r.minRt)} ms")
        output.add(
            "  maximum response time (millis) = ${
                "%.3f".format(
                    Locale.US,
                    r.maxRt
                )
            } ms at $latencyMapMaxRtTs"
        )

        if (actionId == NUM_ACTIONS) {
            val fm = Runtime.getRuntime().freeMemory()
            val tm = Runtime.getRuntime().totalMemory()
            val mm = Runtime.getRuntime().maxMemory()

            //output.add("")
            //output.add("  average cpu load (process) = ${"%.3f".format(Locale.US, r.avgCpuProcess)}")
            //output.add("  average cpu load (system ) = ${"%.3f".format(Locale.US, r.avgCpuSystem)}")

            //mg_cpu_tulip?.set(r.avgCpuProcess.toInt())
            //mg_cpu_system?.set(r.avgCpuSystem.toInt())

            output.add("")
            output.add("  memory used (jvm)    = ${"%,d".format(Locale.US, tm - fm)}")
            output.add("  free memory (jvm)    = ${"%,d".format(Locale.US, fm)}")
            output.add("  total memory (jvm)   = ${"%,d".format(Locale.US, tm)}")
            output.add("  maximum memory (jvm) = ${"%,d".format(Locale.US, mm)}")
            output.add("")
            output.add("  average wait time    = ${"%.3f".format(Locale.US, r.awt)} ms")
            output.add("  maximum wait time    = ${"%.3f".format(Locale.US, r.maxWt)} ms")

            mg_rt_avg?.set(r.art.toInt())
            mg_rt_max?.set(r.maxRt.toInt())
            mg_rt_min?.set(r.minRt.toInt())

            mg_num_actions?.set(r.numActions)
            mg_num_success?.set(r.numSuccess)
            mg_num_failed?.set(r.numActions - r.numSuccess)

            mg_benchmark_tps?.set(r.aps.toInt())
            mg_benchmark_dur?.set(r.durationSeconds.toInt())
            var phaseId = 0
            if (r.testPhase == "Prewarmup") phaseId = 0
            if (r.testPhase == "Warmup") phaseId = 1
            if (r.testPhase == "Benchmark") phaseId = 2

            mg_benchmark_phs?.set(phaseId)
            mg_benchmark_run?.set(r.rowId)
        }

        Console.put(output)
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun saveStatsJson(actionId: Int): String {
        var results = ""

        // Skip actionId = -1
        if (actionId >= 0) {
            val name: String = if (actionNames.containsKey(actionId)) actionNames[actionId]!! else "action${actionId}"
            results += "\"name\": \"${name}\""
        }

        results += ", \"num_actions\": ${numActions}, \"num_success\": ${numSuccess}, \"num_failed\": ${numActions - numSuccess}"
        results += ", \"avg_tps\": ${r.aps}, \"avg_rt\": ${r.art}, \"sdev_rt\": ${r.sdev}, \"min_rt\": ${r.minRt}, \"max_rt\": ${r.maxRt}, \"max_rt_ts\": \"${r.maxRtTs}\""

        results += ", \"percentiles_rt\": {"
        var t = ""
        r.pk.forEachIndexed { index, _ ->
            val k = r.pk[index].toString()
            val v = r.pv[index]
            if (t != "") t += ", "
            t += "\"${k}\": $v"
        }
        results += t
        results += "}"

        results += ", \"histogram_rt\": "
        val b = ByteBuffer.allocate(latencyMap2.neededByteBufferCapacity)
        latencyMap2.encodeIntoCompressedByteBuffer(b)
        val b64s = Base64.encode(b.array())
        results += '\"' + b64s + '\"'

        return results
    }

    fun updateStats(task: Task) {
        val durationMicros = (task.serviceTimeNanos)/1000
        latencyMap2.recordValue(durationMicros)

        if (durationMicros < latencyMapMinRt) {
            latencyMapMinRt = durationMicros
        }
        if (durationMicros > latencyMapMaxRt) {
            latencyMapMaxRt = durationMicros
            latencyMapMaxRtTs = java.time.LocalDateTime.now().format(formatter)
        }
        numActions += 1
        if (task.status == 1) {
            numSuccess += 1
        }
        waitTimeMicrosHistogram.recordValue(task.waitTimeNanos/1000)
    }

    fun clearStats() {
        latencyMap2.reset()
        latencyMapMinRt = Long.MAX_VALUE
        latencyMapMaxRt = Long.MIN_VALUE
        latencyMapMaxRtTs = ""
        waitTimeMicrosHistogram.reset()

        numActions = 0
        numSuccess = 0
    }
}

object DataCollector {
    private var fileWriteId: Int = 0
    private val actionStats = Array(NUM_ACTIONS + 1) { ActionStats() }

    // val a = arrayOf<Array<ActionStats>>()
    // init {
    //     for (i in 0..3) {
    //         a += actionStats
    //     }
    // }

    fun createSummary(
        durationMillis: Int,
        testCase: TestProfile,
        indexTestCase: Int,
        indexUserProfile: Int,
        queueLength: Int,
        tsBegin: String,
        tsEnd: String,
        testPhase: String,
        runId: Int
    ) {
        actionStats[NUM_ACTIONS].createSummary(
            NUM_ACTIONS,
            durationMillis,
            testCase,
            indexTestCase,
            indexUserProfile,
            queueLength,
            tsBegin,
            tsEnd,
            testPhase,
            runId
        )
        actionStats.forEachIndexed { index, data ->
            if (data.numActions > 0) {
                if (index != NUM_ACTIONS) {
                    data.createSummary(
                        index,
                        durationMillis,
                        testCase,
                        indexTestCase,
                        indexUserProfile,
                        queueLength,
                        tsBegin,
                        tsEnd,
                        testPhase,
                        -1
                    )
                }
            }
        }
    }

    fun printStats(printMap: Boolean = false ,printDetails: Boolean = false) {
        actionStats[NUM_ACTIONS].printStats(NUM_ACTIONS, printMap)
        if (printDetails) {
            actionStats.forEachIndexed { index, data ->
                if (data.numActions > 0) {
                    if (index != NUM_ACTIONS) {
                        data.printStats(index, false)
                    }
                }
            }
        }
    }

    fun saveStatsJson(filename: String) {
        if (filename != "") {
            val fm = Runtime.getRuntime().freeMemory()
            val tm = Runtime.getRuntime().totalMemory()
            val mm = Runtime.getRuntime().maxMemory()

            val r = actionStats[NUM_ACTIONS].r

            var json = "{" + "\"attributes\": {\"type\": \"data\"},"

            json += "\"scenario_name\": \"${TULIP_SCENARIO_NAME}\", "
            json += "\"scenario_id\": ${TULIP_SCENARIO_ID}, "
            json += "\"num_users\": ${MAX_NUM_USERS}, "
            json += "\"num_threads\": ${MAX_NUM_THREADS}, "
            json += "\"queue_length\": ${r.queueLength}, "
            json += "\"total_time_blocked_ns\": ${g_queueTimeBlocked}, "

            json += "\"test_name\": \"${r.testName}\", "
            json += "\"test_id\": ${r.testId}, "
            json += "\"row_id\": ${r.rowId}, "

            json += "\"test_begin\": \"${r.testBegin}\", "
            json += "\"test_end\": \"${r.testEnd}\", "

            json += "\"java\": { "
            json += "\"java.vendor\": \"${System.getProperty("java.vendor")}\", "
            json += "\"java.runtime.version\": \"${System.getProperty("java.runtime.version")}\""
            json += "}, "

            json += "\"duration\": ${r.durationSeconds}, "

            // json += "\"avg_cpu_process\": ${r.avgCpuProcess}, \"avg_cpu_system\": ${r.avgCpuSystem}, "

            json += "\"jvm_memory_used\": ${tm-fm}, "
            json += "\"jvm_memory_free\": $fm, "
            json += "\"jvm_memory_total\": $tm, "
            json += "\"jvm_memory_maximum\": $mm"

            json += actionStats[NUM_ACTIONS].saveStatsJson(-1)

            json += ", \"user_actions\": {"

            var t = ""
            actionStats.forEachIndexed { index, data ->
                if (data.numActions > 0) {
                    if (index != NUM_ACTIONS) {
                        if (t != "") {
                            t += ","
                        }
                        t += "\"${index}\": {" + data.saveStatsJson(index) + "}"
                    }
                }
            }
            json += t
            json += "}"

            json += "}"

            val fw = FileWriter(filename, true)
            val bw = BufferedWriter(fw).apply {
                when (fileWriteId) {
                    0 -> write("[${json}")
                    else -> write(",${json}")
                }
                newLine()
            }
            fileWriteId += 1
            bw.close()
            fw.close()
        }
    }

    fun closeStatsJson(filename: String) {
        val fw = FileWriter(filename, true)
        val bw = BufferedWriter(fw).apply {
            write("]")
            newLine()
        }
        bw.close()
        fw.close()
    }

    fun updateStats(task: Task) {
        require(task.actionId < NUM_ACTIONS)
        if (task.actionId < 0) {
            // Unused task drained from the rsp queue.
            return
        }
        actionStats[NUM_ACTIONS].updateStats(task)
        actionStats[task.actionId].updateStats(task)

//        Counter.builder("Tulip")
//            .tags("action", task.actionId.toString())
//            .register(registry)
//            .increment()
//
//        Counter.builder("Tulip")
//            .tags("action", "tps")
//            .register(registry)
//            .increment()
    }

    fun clearStats() {
        actionStats.forEach {
            it.clearStats()
        }
    }
}

/*-------------------------------------------------------------------------*/
