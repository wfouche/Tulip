/*-------------------------------------------------------------------------*/

package io.github.wfouche.tulip.core

/*-------------------------------------------------------------------------*/

// import io.micrometer.core.instrument.Clock
// import io.micrometer.core.instrument.Tag
// import io.micrometer.jmx.JmxConfig
// import io.micrometer.jmx.JmxMeterRegistry
import com.google.gson.JsonParser
import com.sun.management.OperatingSystemMXBean
import io.github.wfouche.tulip.api.TulipApi
import io.github.wfouche.tulip.api.TulipUser
import io.github.wfouche.tulip.api.TulipUserFactory
import io.github.wfouche.tulip.pfsm.Edge
import io.github.wfouche.tulip.pfsm.MarkovChain
import io.github.wfouche.tulip.report.createConfigReport
import java.io.BufferedWriter
import java.io.FileWriter
import java.lang.management.ManagementFactory
import java.nio.ByteBuffer
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.LinkedBlockingQueue as BlockingQueue
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import kotlin.collections.mutableListOf
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.abs
import kotlin.system.exitProcess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import org.HdrHistogram.Histogram
import org.HdrHistogram.IntCountsHistogram

/*-------------------------------------------------------------------------*/

const val VERSION = TulipApi.VERSION

const val NUM_ACTIONS = TulipApi.NUM_ACTIONS

private const val histogramNumberOfSignificantValueDigits = 3

private val osBean: OperatingSystemMXBean =
    ManagementFactory.getPlatformMXBean(OperatingSystemMXBean::class.java)

// private val isWindows: Boolean =
// System.getProperty("os.name").lowercase().contains("windows")

/*-------------------------------------------------------------------------*/

class InformativeBlockingQueue<E>(val capacity: Int) : BlockingQueue<E>(capacity) {}

typealias Java_Queue<E> = InformativeBlockingQueue<E>

typealias MPSC_Queue<E> = InformativeBlockingQueue<E>

typealias SPSC_Queue<E> = InformativeBlockingQueue<E>

/*-------------------------------------------------------------------------*/

// https://github.com/oshai/kotlin-logging
// private val logger = KotlinLogging.logger {}

/*-------------------------------------------------------------------------*/

private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss")

//
// Arrays of user objects and user actions.
//

private var TULIP_CONTEXT_NAME: String = ""
private var TULIP_CONTEXT_ID: Int = 0

private var MAX_NUM_USERS = 0
private var MAX_NUM_THREADS = 0

private var userObjects: Array<TulipUser?>? = null // arrayOfNulls<User>(NUM_USERS)
private var userActions: Array<Iterator<Int>?>? = null // arrayOfNulls<Iterator<Int>>(NUM_USERS)

//
// Array of Worker thread objects
//
private var userThreads: Array<UserThread?>? = null // arrayOfNulls<UserThread>(NUM_THREADS)

// ...
private var testSuite: List<TestProfile>? = null

// Markov chain object
var g_workflow: MarkovChain? = null

// ...
private var newUser: TulipUserFactory? = null

var actionNames: Map<Int, String> = emptyMap()
var workflows: HashMap<String, MarkovChain> = HashMap<String, MarkovChain>()

// private val registry = JmxMeterRegistry(JmxConfig.DEFAULT, Clock.SYSTEM)
//
// private val mg_num_actions    = registry.gauge("Tulip", listOf(Tag.of("num",
//  "actions")),   AtomicInteger(0))
// private val mg_num_failed    = registry.gauge("Tulip", listOf(Tag.of("num",
//  "failed")),   AtomicInteger(0))
//
// private val mg_context_id   = registry.gauge("Tulip",
// listOf(Tag.of("context", "id")),      AtomicInteger(0))
// private val mg_num_threads  = registry.gauge("Tulip",
// listOf(Tag.of("context", "id"), Tag.of("num", "threads")), AtomicInteger(0))
// private val mg_num_users    = registry.gauge("Tulip",
// listOf(Tag.of("context", "id"), Tag.of("num", "users")),   AtomicInteger(0))
//
// private val mg_rt_avg = registry.gauge("Tulip", listOf(Tag.of("rt",
// "avg")), AtomicInteger(0))
// private val mg_rt_max = registry.gauge("Tulip", listOf(Tag.of("rt",
// "max")), AtomicInteger(0))
// private val mg_rt_min = registry.gauge("Tulip", listOf(Tag.of("rt",
// "min")), AtomicInteger(0))
//
// private val mg_benchmark_id  = registry.gauge("Tulip",
// listOf(Tag.of("benchmark",   "id")),       AtomicInteger(0))
// private val mg_benchmark_aps = registry.gauge("Tulip",
// listOf(Tag.of("benchmark",   "aps")),      AtomicInteger(0))
// private val mg_benchmark_dur = registry.gauge("Tulip",
// listOf(Tag.of("benchmark",   "duration")), AtomicInteger(0))
// private val mg_benchmark_phs = registry.gauge("Tulip",
// listOf(Tag.of("benchmark",   "phase")), AtomicInteger(0))
// private val mg_benchmark_run = registry.gauge("Tulip",
// listOf(Tag.of("benchmark",   "run")), AtomicInteger(0))

// internal val mg_cpu_tulip = registry.gauge("Tulip", listOf(Tag.of("cpu",
// "tulip")), AtomicInteger(0))
// internal val mg_cpu_system = registry.gauge("Tulip", listOf(Tag.of("cpu",
// "system")), AtomicInteger(0))

/*-------------------------------------------------------------------------*/

private fun runtimeInit(
    contextId: Int,
    context: RuntimeContext,
    tests: List<TestProfile>,
    actionDesc: Map<Int, String>,
    userFactory: TulipUserFactory
) {
    TULIP_CONTEXT_ID = contextId
    TULIP_CONTEXT_NAME = context.name

    MAX_NUM_USERS = context.numUsers
    MAX_NUM_THREADS = context.numThreads
    testSuite = tests
    newUser = userFactory

    userObjects = arrayOfNulls(MAX_NUM_USERS)
    userActions = arrayOfNulls(MAX_NUM_USERS)
    userThreads = arrayOfNulls(MAX_NUM_THREADS)
    actionNames = actionDesc

    //    mg_num_threads?.set(MAX_NUM_THREADS)
    //    mg_num_users?.set(MAX_NUM_USERS)
    //    mg_context_id?.set(contextId)
}

private fun runtimeDone() {
    // Terminate all user threads.
    userThreads!!.forEach { userThread -> userThread!!.tq.put(Task(status = 999)) }

    // Wait for all user threads to exit.
    while (userThreads!!.map { if (it == null) 0 else 1 }.sum() > 0) {
        Thread.sleep(500)
    }
}

/*-------------------------------------------------------------------------*/

private data class RuntimeContext(
    val name: String = "",
    val numUsers: Int = 0,
    val numThreads: Int = 0
)

/*-------------------------------------------------------------------------*/

private data class Action(
    //
    // Numeric action ID.
    //
    val id: Int,

    //
    // Number of occurrences of this action relative to other actions.
    // Set weight to 0 (or use default value) when a workflow should be
    // specified.
    val weight: Int = 0
)

/*-------------------------------------------------------------------------*/

private data class Duration(

    // Start-up only executed once per TestCase.
    //
    val startupDurationUnits: Long = 0,

    // Warm-up phase executed once per TestCase.
    //
    val warmupDurationUnits: Long = 0,

    // Main duration in minutes.
    // The results from this period are reported.
    //
    // The main phase executed 'repeatCount' every iteration of TestCase.
    //
    val mainDurationUnits: Long = 0,
    val mainDurationRepeatCount: Int = 1,
    val timeUnit: TimeUnit = TimeUnit.SECONDS,

    // ------------------------------

    val startupDurationMillis: Long = timeUnit.toMillis(startupDurationUnits),
    val warmupDurationMillis: Long = timeUnit.toMillis(warmupDurationUnits),
    val mainDurationMillis: Long = timeUnit.toMillis(mainDurationUnits)
)

/*-------------------------------------------------------------------------*/

private data class TestProfile(
    val enabled: Boolean = true,
    val saveStats: Boolean = true,

    //
    // Name of the benchmark test.
    //
    val name: String = "",
    val duration: Duration = Duration(0, 0, 0, 1),

    // List of actions to be performed.
    // If the weights of all the actions are zero (0), then treat the action
    // list as a workflow to be executed per user object.
    val actions: List<Action>,

    // https://en.wikipedia.org/wiki/Queueing_theory
    //
    // The average arrival rate (arrivals per second) to be maintained.
    //
    val arrivalRate: Double = 0.0,

    // https://en.wikipedia.org/wiki/Little%27s_Law
    //
    // https://www.process.st/littles-law/
    //
    // This value represents the "L" in Little's Law (equation)
    //
    val queueLengths: List<Int> = listOf(0),

    // List of percentile values to report on.
    val percentiles: List<Double> = listOf(50.0, 90.0, 95.0, 99.0, 99.9),

    // Json results filename.
    val filename: String = "",

    // (Optional) Name of the workflow to execute
    val workflow: String = ""
)

/*-------------------------------------------------------------------------*/

//
// Task data class. Tasks are created be the main thread and send to User
// objects to perform known actions.
//
data class Task(

    // The user ID of the user object to which an operation should be applied.
    var userId: Int = -1,

    // Total number of user objects.
    var numUsers: Int = -1,

    // Total number of work threads servicing user objects.
    var numThreads: Int = -1,

    // Numeric id of the action (operation) to be invoked on a user object.
    var actionId: Int = -1,

    // Duration (elapsed time) in microseconds.
    var serviceTimeNanos: Long = 0,
    var waitTimeNanos: Long = 0,
    var rspQueue: MPSC_Queue<Task>? = null,
    var status: Int = -1,
    var beginQueueTimeNanos: Long = 0
)

/*-------------------------------------------------------------------------*/

inline fun elapsedTimeNanos(block: () -> Unit): Long {
    val start = System.nanoTime()
    block()
    return System.nanoTime() - start
}

fun delayMillisRandom(delayFrom: Long, delayTo: Long) {
    require(delayFrom >= 0) { "delayFrom must be non-negative, is $delayFrom" }
    require(delayTo >= 0) { "delayTo must be non-negative, is $delayTo" }
    require(delayFrom < delayTo) {
        "delayFrom must be smaller than delayTo, but $delayFrom >= $delayTo"
    }
    val delayMillis = ThreadLocalRandom.current().nextLong(delayTo - delayFrom + 1) + delayFrom
    Thread.sleep(delayMillis)
}

/*-------------------------------------------------------------------------*/

var g_config = TulipConfig()

private val g_contexts = mutableListOf<RuntimeContext>()

private val g_tests = mutableListOf<TestProfile>()

/*-------------------------------------------------------------------------*/

@Serializable
data class ConfigContext(
    val enabled: Boolean = false,
    @SerialName("num_users") val numUsers: Int = 0,
    @SerialName("num_threads") val numThreads: Int = 0
)

@Serializable
data class ConfigDuration(
    @SerialName("pre_warmup_duration") val startupDuration: Long = 0,
    @SerialName("warmup_duration") val warmupDuration: Long = 0,
    @SerialName("benchmark_duration") val mainDuration: Long = 0,
    @SerialName("benchmark_iterations") val mainDurationRepeatCount: Int = 1
)

@Serializable data class ConfigAction(val id: Int, val weight: Int = 0)

@Serializable
data class ConfigTest(
    val enabled: Boolean = true,
    @SerialName("save_stats") val logStats: Boolean = true,
    val time: ConfigDuration = ConfigDuration(),
    @SerialName("aps_rate") val throughputRate: Double = 0.0,
    @SerialName("worker_thread_queue_size") val workInProgress: Int = 0,
    @SerialName("scenario_actions") val actions: List<ConfigAction> = listOf(),
    @SerialName("scenario_workflow") val workflow: String = ""
)

@Serializable
data class ActionsConfig(
    @SerialName("description") val description: String = "",
    @SerialName("output_filename") val jsonFilename: String = "",
    @SerialName("report_filename") val htmlFilename: String = "",
    @SerialName("user_class") val userClass: String = "",
    @SerialName("user_params") val userParams: Map<String, JsonPrimitive> = mapOf(),
    @SerialName("user_actions") val userActions: Map<Int, String> = mapOf()
)

@Serializable
data class TulipConfig(
    val actions: ActionsConfig = ActionsConfig(),
    val contexts: Map<String, ConfigContext> = mapOf(),
    val benchmarks: Map<String, ConfigTest> = mapOf(),
    val workflows: Map<String, Map<String, Map<String, Double>>> = mapOf()
)

fun initConfig(text: String): String {
    val textIsJsonString: Boolean = text.trim().startsWith("{")
    initTulip()
    Console.put("")
    val wd = System.getProperty("user.dir")
    Console.put("  working directory = $wd")
    Console.put("")
    if (!textIsJsonString) {
        Console.put("  config filename = $text")
    }

    // Read JSON file contents into memory
    val sf: String =
        if (textIsJsonString) {
            text
        } else {
            java.io.File(text).readText()
        }

    // Remove all JSONC comments from the JSON
    val gsonJsonTree = JsonParser.parseString(sf)
    val jsonWithoutComments = gsonJsonTree.toString()

    // Parse the JSON config using the Kotlin JSON parser
    val json = Json { ignoreUnknownKeys = false }
    g_config = json.decodeFromString<TulipConfig>(jsonWithoutComments)
    g_config.contexts.forEach { entry ->
        val k = entry.key
        // println("${k}")
        val e = entry.value
        if (e.enabled) {
            val v = RuntimeContext(k, e.numUsers, e.numThreads)
            g_contexts.add(v)
        }
    }
    g_config.benchmarks.forEach { (key, e) ->
        // println("${e.name}")
        val v =
            TestProfile(
                enabled = e.enabled,
                saveStats = e.logStats,
                name = key,
                duration =
                    if (e.time == null) Duration(0, 0, 0, 1, TimeUnit.SECONDS)
                    else
                        Duration(
                            e.time.startupDuration,
                            e.time.warmupDuration,
                            e.time.mainDuration,
                            e.time.mainDurationRepeatCount,
                            TimeUnit.SECONDS),
                arrivalRate = e.throughputRate,
                queueLengths = listOf(e.workInProgress),
                actions =
                    mutableListOf<Action>().apply {
                        if (e.workflow.isEmpty()) {
                            // List of actions to execute
                            for (a: ConfigAction in e.actions) {
                                this.add(Action(a.id, a.weight))
                            }
                        } else {
                            // actionId -1 --> execute workflow
                            this.add(Action(-1, 0))
                        }
                    },
                filename = g_config.actions.jsonFilename,
                workflow = e.workflow)
        g_tests.add(v)
    }
    for (wn in g_config.workflows.keys) {
        // Console.put("workflow = $wn")
        val mc = MarkovChain(wn)
        for (an in g_config.workflows[wn]!!.keys) {
            // Console.put("  aid = $an")
            val an_id = if (an == "-") 0 else an.toInt()
            var list = mutableListOf<Edge>()
            for (da in g_config.workflows[wn]!![an]!!.keys) {
                val da_id = if (da == "-") 0 else da.toInt()
                val weight = (g_config.workflows[wn]!![an]!![da]!!.toDouble() * 1000).toInt()
                // Console.put("    did = $da, weight = $weight")
                list.add(Edge(da_id, weight))
            }
            mc.add(an_id, list)
        }
        // register workflow
        workflows[wn] = mc
    }
    Console.put("  output filename = ${g_config.actions.jsonFilename}")
    Console.put("  report filename = ${g_config.actions.htmlFilename}")
    if (!textIsJsonString) {
        createConfigReport(text)
    }

    return g_config.actions.jsonFilename
}

/*-------------------------------------------------------------------------*/

private data class ActionSummary(
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
    var histogram: Histogram = Histogram(histogramNumberOfSignificantValueDigits),
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

    // var avgCpuSystem: Double = 0.0,
    // var avgCpuProcess: Double = 0.0
    var processCpuTime: Long = 0,
    var processCpuCores: Double = 0.0,
    var processCpuUtilization: Double = 0.0
)

/*-------------------------------------------------------------------------*/

private var waitTimeMicrosHistogram = Histogram(histogramNumberOfSignificantValueDigits)

private class ActionStats {
    // <numberOfSignificantValueDigits>
    // private val NUM_DIGITS=1  // Tested - inaccurate results, don't use
    // private val NUM_DIGITS=2  // Tested - good results, small results file
    // (default, optimal)
    // private val NUM_DIGITS=3  // Tested - great results, large results file,
    // histogram_rt
    private val histogram: Histogram = Histogram(histogramNumberOfSignificantValueDigits)
    private var histogramMinRt: Long = Long.MAX_VALUE
    private var histogramMaxRt: Long = Long.MIN_VALUE
    private var histogramMaxRtTs = ""

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
        runId: Int,
        cpuTime: Long
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
        r.art = histogram.mean / 1000.0

        // standard deviation
        r.sdev = histogram.stdDeviation / 1000.0

        // min rt
        r.minRt = histogramMinRt / 1000.0

        // max rt
        r.maxRt = histogramMaxRt / 1000.0

        // max rt timestamp
        r.maxRtTs = histogramMaxRtTs

        // percentiles
        r.pk = testCase.percentiles
        r.pv =
            mutableListOf<Double>().apply {
                r.pk.forEach {
                    var px = histogram.getValueAtPercentile(it) / 1000.0
                    if (px > r.maxRt) px = r.maxRt
                    this.add(px)
                }
            }

        r.histogram = histogram

        r.awt = waitTimeMicrosHistogram.mean / 1000.0
        r.maxWt = waitTimeMicrosHistogram.maxValueAsDouble / 1000.0

        // Summarize CPU usage for global stats only.
        // if (actionId == NUM_ACTIONS) {
        //    // average process CPU load.
        //    // r.avgCpuProcess = 0.0
        //
        //    // average system CPU load.
        //    // r.avgCpuSystem = 0.0
        // }
        if (actionId == NUM_ACTIONS) {
            r.processCpuTime = cpuTime
        }
    }

    fun printStats(actionId: Int) {

        val output = mutableListOf("")

        if (r.processCpuTime == 0L) {
            // Init, or Shutdown -> TPS inaccurate, so set to 0.0
            // r.aps = 0.0
            // This is not a complete solution, breaks the report
            // Disabled for now.
        }

        if (actionId != NUM_ACTIONS) {
            output.add("  action_id = ${r.actionId}")
        }
        output.add("  num_actions = ${r.numActions}")
        output.add("  num_failed  = ${r.numActions - r.numSuccess}")
        output.add("")
        output.add(
            "  average number of actions completed per second = ${"%.3f".format(Locale.US, r.aps)}")
        if (r.processCpuTime == 0L) {
            // Init, or Shutdown
            output.add(
                "  duration of actions (in seconds)               = ${r.durationSeconds} seconds")
        } else {
            // Benchmark
            output.add(
                "  duration of benchmark (in seconds)             = ${r.durationSeconds} seconds")
        }
        output.add("")
        output.add(
            "  average latency     (response time)  (millis)  = ${"%.3f".format(Locale.US, r.art)} ms")
        output.add(
            "  standard deviation  (response time)  (millis)  = ${"%.3f".format(Locale.US, r.sdev)} ms")
        output.add("")
        r.pk.forEachIndexed { index, percentile ->
            val px = r.pv.elementAt(index)
            output.add(
                "  ${percentile}th percentile (response time) (millis) = ${"%.3f".format(Locale.US, px)} ms")
        }

        output.add("")
        output.add("  minimum response time (millis) = ${"%.3f".format(Locale.US, r.minRt)} ms")
        output.add(
            "  maximum response time (millis) = ${
                "%.3f".format(
                    Locale.US,
                    r.maxRt
                )
            } ms at $histogramMaxRtTs")

        if (actionId == NUM_ACTIONS) {
            val rt = Runtime.getRuntime()
            val fm = rt.freeMemory()
            val tm = rt.totalMemory()
            val mm = rt.maxMemory()

            // output.add("")
            // output.add("  average cpu load (process) =
            // ${"%.3f".format(Locale.US, r.avgCpuProcess)}")
            // output.add("  average cpu load (system ) =
            // ${"%.3f".format(Locale.US, r.avgCpuSystem)}")

            // mg_cpu_tulip?.set(r.avgCpuProcess.toInt())
            // mg_cpu_system?.set(r.avgCpuSystem.toInt())

            output.add("")
            val gb1 = 1073741824.0
            output.add("  memory used (jvm)    = ${"%.3f".format(Locale.US, (tm - fm)/gb1)} GB")
            output.add("  free memory (jvm)    = ${"%.3f".format(Locale.US, fm/gb1)} GB")
            output.add("  total memory (jvm)   = ${"%.3f".format(Locale.US, tm/gb1)} GB")
            output.add("  maximum memory (jvm) = ${"%.3f".format(Locale.US, mm/gb1)} GB")
            output.add("")
            val cpu_time_secs: Double = r.processCpuTime / 1000000000.0
            output.add(
                "  cpu time (process)   = ${"%.3f".format(Locale.US, cpu_time_secs)} seconds")
            r.processCpuCores = cpu_time_secs / r.durationSeconds
            output.add(
                "  num cores used       = ${"%.3f".format(Locale.US, r.processCpuCores)} cores")
            r.processCpuUtilization = 100.0 * r.processCpuCores
            // if (r.processCpuUtilization > 100.0) r.processCpuUtilization =
            // 100.0
            // support > 100.0 utilization due to hyper-threading
            output.add(
                "  avg cpu utilization  = ${"%.1f".format(Locale.US, r.processCpuUtilization)}%")

            //            output.add("")
            //            val awqs: Double = wthread_queue_stats.mean
            //            val mwqs: Long = wthread_queue_stats.maxValue
            //            output.add("  avg wkr thrd qsize =
            // ${"%.3f".format(Locale.US, awqs)}")
            //            output.add("  max wkr thrd qsize =
            // ${"%,d".format(Locale.US, mwqs)}")
            //            output.add("  average wait time  =
            // ${"%.3f".format(Locale.US, r.awt)} ms")
            //            output.add("  maximum wait time  =
            // ${"%.3f".format(Locale.US, r.maxWt)} ms")

            //            mg_rt_avg?.set(r.art.toInt())
            //            mg_rt_max?.set(r.maxRt.toInt())
            //            mg_rt_min?.set(r.minRt.toInt())
            //
            //            mg_num_actions?.set(r.numActions)
            //            mg_num_failed?.set(r.numActions - r.numSuccess)
            //
            //            mg_benchmark_aps?.set(r.aps.toInt())
            //            mg_benchmark_dur?.set(r.durationSeconds.toInt())
            var phaseId = 0
            if (r.testPhase == "PreWarmup") phaseId = 0
            if (r.testPhase == "Warmup") phaseId = 1
            if (r.testPhase == "Benchmark") phaseId = 2

            //            mg_benchmark_phs?.set(phaseId)
            //            mg_benchmark_run?.set(r.rowId)
        }

        Console.put(output)
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun toJson(actionId: Int): String {
        var results = ""

        // Skip actionId = -1
        if (actionId >= 0) {
            val name: String =
                if (actionNames.containsKey(actionId)) actionNames[actionId]!!
                else "action${actionId}"
            results += "\"name\": \"${name}\""
        }

        results += ", \"num_actions\": ${numActions}, \"num_failed\": ${numActions - numSuccess}"
        results +=
            ", \"avg_aps\": ${r.aps}, \"avg_rt\": ${r.art}, \"sdev_rt\": ${r.sdev}, \"min_rt\": ${r.minRt}, \"max_rt\": ${r.maxRt}, \"max_rt_ts\": \"${r.maxRtTs}\""

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

        val b = ByteBuffer.allocate(histogram.neededByteBufferCapacity)
        val numBytes = histogram.encodeIntoCompressedByteBuffer(b)
        val b64s = Base64.encode(b.array(), 0, numBytes)
        results += '\"' + b64s + '\"'

        //        if (actionId == -1) {
        //            val b =
        // ByteBuffer.allocate(histogram.neededByteBufferCapacity)
        //            val numBytes = histogram.encodeIntoCompressedByteBuffer(b)
        //            val b64s = Base64.encode(b.array(), 0, numBytes)
        //            results += '\"' + b64s + '\"'
        //        } else {
        //            results += "\"\""
        //        }

        return results
    }

    fun updateStats(task: Task) {
        val durationMicros = (task.serviceTimeNanos) / 1000
        histogram.recordValue(durationMicros)

        if (durationMicros < histogramMinRt) {
            histogramMinRt = durationMicros
        }
        if (durationMicros > histogramMaxRt) {
            histogramMaxRt = durationMicros
            histogramMaxRtTs = java.time.LocalDateTime.now().format(formatter)
        }
        numActions += 1
        if (task.status == 1) {
            numSuccess += 1
        }
        waitTimeMicrosHistogram.recordValue(task.waitTimeNanos / 1000)
    }

    fun clearStats() {
        histogram.reset()
        histogramMinRt = Long.MAX_VALUE
        histogramMaxRt = Long.MIN_VALUE
        histogramMaxRtTs = ""

        numActions = 0
        numSuccess = 0
    }
}

/*-------------------------------------------------------------------------*/

private object DataCollector {
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
        runId: Int,
        cpuTime: Long
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
            runId,
            cpuTime)
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
                        -1,
                        0L)
                }
            }
        }
    }

    fun printStats() {
        actionStats[NUM_ACTIONS].printStats(NUM_ACTIONS)
        //        if (printDetails) {
        //            actionStats.forEachIndexed { index, data ->
        //                if (data.numActions > 0) {
        //                    if (index != NUM_ACTIONS) {
        //                        data.printStats(index)
        //                    }
        //                }
        //            }
        //        }
    }

    fun saveStatsJson(filename: String) {
        if (filename != "") {
            val rt = Runtime.getRuntime()
            val fm = rt.freeMemory()
            val tm = rt.totalMemory()
            val mm = rt.maxMemory()

            val r = actionStats[NUM_ACTIONS].r

            var json = "{"

            val workflowName: String
            if (g_workflow == null) {
                workflowName = ""
            } else {
                workflowName = g_workflow!!.name
            }

            json += "\"context_name\": \"$TULIP_CONTEXT_NAME\", "
            json += "\"context_id\": $TULIP_CONTEXT_ID, "
            json += "\"bm_name\": \"${r.testName}\", "
            json += "\"bm_id\": ${r.testId}, "
            json += "\"row_id\": ${r.rowId}, "

            json += "\"num_users\": $MAX_NUM_USERS, "
            json += "\"num_threads\": $MAX_NUM_THREADS, "
            json += "\"queue_length\": ${r.queueLength}, "

            json += "\"workflow_name\": \"$workflowName\", "

            json += "\"test_begin\": \"${r.testBegin}\", "
            json += "\"test_end\": \"${r.testEnd}\", "

            json += "\"java\": { "
            json += "\"java.vendor\": \"${System.getProperty("java.vendor")}\", "
            json += "\"java.runtime.version\": \"${System.getProperty("java.runtime.version")}\", "
            json += "\"kotlin.version\": \"${KotlinVersion.CURRENT}\""

            json += "}, "

            json += "\"duration\": ${r.durationSeconds}, "

            // json += "\"avg_cpu_process\": ${r.avgCpuProcess},
            // \"avg_cpu_system\": ${r.avgCpuSystem}, "

            json += "\"jvm_memory_used\": ${tm-fm}, "
            json += "\"jvm_memory_free\": $fm, "
            json += "\"jvm_memory_total\": $tm, "
            json += "\"jvm_memory_maximum\": $mm, "

            json += "\"process_cpu_utilization\": ${r.processCpuUtilization}, "
            json += "\"process_cpu_cores\": ${r.processCpuCores}, "
            json += "\"process_cpu_time_ns\": ${r.processCpuTime}"

            val awqs: Double = wthread_queue_stats.mean
            val mwqs: Long = wthread_queue_stats.maxValue
            json += ", \"avg_wthread_qsize\": ${awqs}"
            json += ", \"max_wthread_qsize\": ${mwqs}"

            json += ", \"avg_wt\": ${r.awt}, \"max_wt\": ${r.maxWt}"

            json += actionStats[NUM_ACTIONS].toJson(-1)

            json += ", \"user_actions\": {"

            var t = ""
            actionStats.forEachIndexed { index, data ->
                if (data.numActions > 0) {
                    if (index != NUM_ACTIONS) {
                        if (t != "") {
                            t += ","
                        }
                        t += "\"${index}\": {" + data.toJson(index) + "}"
                    }
                }
            }
            json += t
            json += "}"

            json += "}"

            val fw = FileWriter(filename, true)
            val bw =
                BufferedWriter(fw).apply {
                    when (fileWriteId) {
                        0 -> {
                            // val gson =
                            // GsonBuilder().setPrettyPrinting().create()
                            // val jsonString = gson.toJson(g_config)
                            val jsonString = Json.encodeToString(g_config)
                            // val jsonString = "${g_config}"
                            write("{  ")
                            newLine()
                            write("  \"version\": \"${VERSION}\"")
                            newLine()
                            write(
                                ", \"timestamp\": \"${java.time.LocalDateTime.now().format(formatter)}\"")
                            newLine()
                            write(", \"config\": ${jsonString}")
                            newLine()
                            write(", \"results\":[")
                            newLine()
                            write(" ${json}")
                        }
                        else -> {
                            write(",${json}")
                        }
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
        val bw =
            BufferedWriter(fw).apply {
                write("]")
                newLine()
                write("}")
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
        //            .tags("action", "aps")
        //            .register(registry)
        //            .increment()
    }

    fun clearStats() {
        actionStats.forEach { it.clearStats() }
        waitTimeMicrosHistogram.reset()
        wthread_queue_stats.reset()
    }
}

/*-------------------------------------------------------------------------*/

private class RateGovernor(
    private val averageRate: Double,
    private val timeMillisStart: Long = TimeUnit.NANOSECONDS.toMillis(System.nanoTime())
) {

    private var count: Long = 0

    fun pace() {
        count += 1
        val deltaMs: Long =
            (timeMillisStart + count * (1000 / averageRate) -
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime()))
                .toLong()
        if (deltaMs > 0) Thread.sleep(deltaMs)
    }
}

/*-------------------------------------------------------------------------*/

private const val USER_THREAD_QSIZE = 11

private class UserThread(private val threadId: Int) : Thread() {

    init {
        name = "w$threadId"
    }

    //
    // Task Queue - input queue with tasks for this thread to complete.
    //
    val tq = SPSC_Queue<Task>(USER_THREAD_QSIZE)
    private var running = true

    override fun run() {
        while (running) {
            //
            // Wait for a new task to be assigned to this thread.
            //
            val task: Task = tq.take()

            /// ....
            if (task.status == 999) {
                // Console.put("Thread ${name} is stopping.")
                running = false
            } else {

                //
                // Locate the user object to which the task should be applied.
                // Dynamically create a new user object, if required.
                //
                var u = userObjects!![task.userId]
                if (u == null) {
                    u = newUser!!.getUser(task.userId, g_config.actions.userClass, threadId)
                    userObjects!![task.userId] = u
                }

                //
                // Apply the task to the user object. The return value is either
                // True or False, indicating that the task succeeded or failed.
                // Also calculate the elapsed time in microseconds.
                //
                task.waitTimeNanos = System.nanoTime() - task.beginQueueTimeNanos
                if (task.actionId < 0) {
                    // Use Markov Chain to determine next action to perform.
                    task.actionId = u!!.nextAction(task.actionId)
                }
                task.serviceTimeNanos = elapsedTimeNanos {
                    if (u!!.processAction(task.actionId)) task.status = 1 else task.status = 0
                }

                task.rspQueue!!.put(task)
            }
        }
        userThreads!![threadId] = null
    }
}

/*-------------------------------------------------------------------------*/

object Console : Thread() {

    init {
        // priority = Thread.MAX_PRIORITY
        isDaemon = true
        name = "console-thread"
        start()
    }

    private var q = Java_Queue<MutableList<String>>(100)

    override fun run() {
        while (true) {
            val list: MutableList<String> = q.take()
            for (s in list) println(s)
        }
    }

    @JvmStatic
    fun put(s: String) {
        put(mutableListOf(s))
    }

    @JvmStatic
    fun put(list: MutableList<String>) {
        q.put(list)
    }
}

/*-------------------------------------------------------------------------*/

private fun getQueueLengths(context: RuntimeContext, test: TestProfile): List<Int> {
    val list: MutableList<Int> = mutableListOf()
    test.queueLengths.forEach { queueLength ->
        list.add(
            when {
                queueLength == 0 -> context.numThreads * USER_THREAD_QSIZE // 11
                queueLength > 0 -> context.numThreads * queueLength // Actions per Thread
                else -> abs(queueLength) // Actions across all Threads
            })
    }
    return list
}

/*-------------------------------------------------------------------------*/

private fun getTest(context: RuntimeContext, test: TestProfile): TestProfile {
    return test.copy(queueLengths = getQueueLengths(context, test))
}

/*-------------------------------------------------------------------------*/

private val wthread_queue_stats = IntCountsHistogram(histogramNumberOfSignificantValueDigits)

private fun assignTask(task: Task) {
    val threadId = task.userId / (task.numUsers / task.numThreads)
    var w = userThreads!![threadId]
    if (w == null) {
        w =
            UserThread(threadId).apply {
                isDaemon = true
                start()
            }
        userThreads!![threadId] = w
    }
    task.beginQueueTimeNanos = System.nanoTime()
    if (!w.tq.offer(task)) {
        // We know the queue is full, so queue size = queue capacity
        w.tq.put(task)
        // No locking required, just reading of property capacity.
        wthread_queue_stats.recordValue(w.tq.capacity.toLong())
    } else {
        // Grab a reentrant lock and read the size property.
        wthread_queue_stats.recordValue(w.tq.size.toLong())
    }
}

/*-------------------------------------------------------------------------*/

private fun createActionGenerator(list: List<Int>): Iterator<Int> {
    val actions = iterator {
        while (true) {
            for (e in list) {
                yield(e)
            }
        }
    }
    return actions
}

/*-------------------------------------------------------------------------*/

private fun runTest(
    testCase: TestProfile,
    contextId: Int,
    indexTestCase: Int,
    indexUserProfile: Int,
    queueLength: Int
) {
    var cpuTime: Long = 0
    var tsBegin = java.time.LocalDateTime.now().format(formatter)
    val output = mutableListOf("")
    output.add("======================================================================")
    output.add(
        "= [${contextId}][${indexTestCase}][${indexUserProfile}][${queueLength}] ${testCase.name} - $tsBegin")
    output.add("======================================================================")
    Console.put(output)

    //    mg_benchmark_id?.set(indexTestCase)

    val rnd = ThreadLocalRandom.current()

    // create a list of randomized user IDs
    val userList = mutableListOf<Int>()
    repeat(MAX_NUM_USERS) { userList.add(it) }
    userList.shuffle()

    // Create a list of actions (per user).
    // If all the weights sum to zero, we should
    // treat the list of actions as a workflow.
    val actionList = mutableListOf<Int>()
    var wSum = 0
    for (action: Action in testCase.actions) {
        wSum += action.weight
    }
    if (wSum == 0) {
        for (action in testCase.actions) {
            actionList.add(action.id)
        }
    } else {
        for (action in testCase.actions) {
            repeat(action.weight) { actionList.add(action.id) }
        }
        actionList.shuffle(rnd)
    }
    repeat(MAX_NUM_USERS) { idx ->
        if ((testCase.duration.warmupDurationUnits == 0L) &&
            (testCase.duration.mainDurationUnits == 0L)) {
            userActions!![idx] = null
        } else {
            userActions!![idx] = createActionGenerator(actionList)
        }
    }

    //
    // Create a queue containing a total of queueLength tokens.
    //
    val rspQueue = MPSC_Queue<Task>(queueLength)
    var rspQueueInitialized = false

    fun initRspQueue() {
        if (rspQueueInitialized) return
        repeat(queueLength) { rspQueue.put(Task()) }
        rspQueueInitialized = true
    }

    fun drainRspQueue() {
        if (!rspQueueInitialized) return
        repeat(queueLength) {
            val task: Task = rspQueue.take()
            DataCollector.updateStats(task)
        }
        rspQueueInitialized = false
    }

    fun getProcessCpuTime(): Long {
        return osBean.processCpuTime
    }

    fun startTask(uid: Int, aid: Int, rateGovernor: RateGovernor?) {
        // Limit the number of active users.
        val task: Task = rspQueue.take()

        // ...
        DataCollector.updateStats(task)

        // Assign the task to the user object.
        task.apply {
            userId = uid
            numUsers = MAX_NUM_USERS
            numThreads = MAX_NUM_THREADS
            actionId = aid
            this.rspQueue = rspQueue
        }
        assignTask(task)

        // Limit the throughput rate, if required.
        rateGovernor?.pace()
    }

    if ((testCase.duration.warmupDurationUnits == 0L) &&
        (testCase.duration.mainDurationUnits == 0L)) {

        DataCollector.clearStats()

        initRspQueue()

        val timeMillisStart: Long = TimeUnit.NANOSECONDS.toMillis(System.nanoTime())

        // Special bootstrap test case to initialize terminals, and other
        // objects.
        // Typically only found at the start and end of a test suite.
        var rateGovernor: RateGovernor? = null
        if (testCase.arrivalRate > 0.0) {
            rateGovernor = RateGovernor(testCase.arrivalRate, timeMillisStart)
        }

        cpuTime = getProcessCpuTime()
        for (aid in actionList) {
            for (uid in userList) {
                startTask(uid, aid, rateGovernor)
            }
        }
        drainRspQueue()
        val timeMillisEnd: Long = TimeUnit.NANOSECONDS.toMillis(System.nanoTime())
        var durationMillis: Int = (timeMillisEnd - timeMillisStart).toInt()
        val tsEnd = java.time.LocalDateTime.now().format(formatter)

        if (durationMillis == 0) {
            durationMillis = 1
        }
        cpuTime = getProcessCpuTime() - cpuTime

        elapsedTimeNanos {
            DataCollector.createSummary(
                durationMillis,
                testCase,
                indexTestCase,
                indexUserProfile,
                queueLength,
                tsBegin,
                tsEnd,
                "Benchmark",
                0,
                cpuTime)
            DataCollector.printStats()
            if (testCase.saveStats) DataCollector.saveStatsJson(testCase.filename)
        }
        // Console.put("Init: Duration spend in stats processing =
        // ${durationNanos2}")
        return
    }

    // Normal test case.
    var timeMillisStart: Long
    var timeMillisEnd: Long = TimeUnit.NANOSECONDS.toMillis(System.nanoTime())

    fun assignTasks(
        durationMillis: Long,
        testPhase: String,
        runId: Int,
        runIdMax: Int,
        arrivalRate: Double = -1.0
    ) {

        if (durationMillis == 0L) {
            return
        }
        if (runId == 0) {
            // Console.put("initRspQueue: runId == 0")
            initRspQueue()
        }

        DataCollector.clearStats()
        tsBegin = java.time.LocalDateTime.now().format(formatter)
        val tsEndPredicted =
            java.time.LocalDateTime.now().plusSeconds(durationMillis / 1000).format(formatter)
        Console.put(
            "\n${testPhase} (${testCase.name}), run ${runId+1} of ${runIdMax+1}: begin (${tsBegin})")
        Console.put(
            "${testPhase} (${testCase.name}), run ${runId+1} of ${runIdMax+1}:       (${tsEndPredicted})")

        timeMillisStart = timeMillisEnd
        timeMillisEnd = timeMillisStart + durationMillis

        val rateGovernor: RateGovernor? = null
        // New rate control logic - begin
        val nanosPerAction: Double
        val numActionsMax: Long
        var numActions: Long = 0
        if (arrivalRate > -1.0) {
            // Warm-up duration at max speed, ungoverned.
            nanosPerAction = 0.0
            numActionsMax = 0
        } else {
            // Ramp-up or Main duration.
            if (testCase.arrivalRate > 0.0) {
                // rate limited, calculate time ns per action
                nanosPerAction = 1000000000.0 / testCase.arrivalRate
                numActionsMax = (testCase.arrivalRate * durationMillis / 1000.0).toLong()
            } else {
                // Not rate limited
                nanosPerAction = 0.0
                numActionsMax = 0
            }
        }

        val durationNanos: Double = durationMillis * 1000000.0
        val startTimeNanos = timeMillisStart * 1000000L
        val endTimeNanos: Double = startTimeNanos + durationNanos
        var rTime: Double = startTimeNanos.toDouble()
        var vTime: Double = rTime
        // New rate control logic - end

        cpuTime = getProcessCpuTime()
        while (rTime < endTimeNanos) {
            // Pick a random user object to assign a task to.
            val uid = userList.random()

            // Pick the next task for the user object to execute.
            val aid: Int = userActions!![uid]!!.next()

            startTask(uid, aid, rateGovernor)

            vTime += nanosPerAction
            if (vTime > rTime) {
                val delayMillis: Long = ((vTime - rTime) / 1000000.0).toLong()
                Thread.sleep(delayMillis)
            }
            rTime = System.nanoTime().toDouble()
            if (numActionsMax != 0L) {
                numActions += 1L
                if (!(numActions < numActionsMax)) {
                    break
                }
            }
        }
        cpuTime = getProcessCpuTime() - cpuTime
        val tsEnd = java.time.LocalDateTime.now().format(formatter)

        Console.put(
            "$testPhase (${testCase.name}), run ${runId+1} of ${runIdMax+1}: end   (${tsEnd})")

        elapsedTimeNanos {
            DataCollector.createSummary(
                durationMillis.toInt(),
                testCase,
                indexTestCase,
                indexUserProfile,
                queueLength,
                tsBegin,
                tsEnd,
                testPhase,
                runId,
                cpuTime)
            DataCollector.printStats()
            if (testPhase == "Benchmark") {
                if (testCase.saveStats) DataCollector.saveStatsJson(testCase.filename)
            }
        }
        // Console.put("Main: Duration spend in stats processing =
        // ${durationNanos2}")
        if (runId == runIdMax) {
            // Console.put("drainRspQueue: runId == runIdMax")
            if (testPhase == "Benchmark") drainRspQueue()
        }
    }

    // Start-up
    //
    // Since we could have 1 or more population set sizes, only perform the
    // start-up phase
    // on the first set, i.e., with index 0.
    //
    if (indexUserProfile == 0) {
        assignTasks(testCase.duration.startupDurationMillis, "PreWarmup", 0, 0, 0.0)
    }

    // Ramp-up
    assignTasks(testCase.duration.warmupDurationMillis, "Warmup", 0, 0)

    // Main run(s)
    for (runId in 0 until testCase.duration.mainDurationRepeatCount) {
        assignTasks(
            testCase.duration.mainDurationMillis,
            "Benchmark",
            runId,
            testCase.duration.mainDurationRepeatCount - 1)
    }
}

/*-------------------------------------------------------------------------*/

private fun initTulip() {
    Console.put(TulipApi.getVersionBanner())
    Console.put(
        "Tulip $VERSION (Java: ${System.getProperty("java.vendor")} ${System.getProperty("java.runtime.version")}, Kotlin: ${KotlinVersion.CURRENT})")
}

/*-------------------------------------------------------------------------*/

private fun runTulip(
    contextId: Int,
    context: RuntimeContext,
    tests: List<TestProfile>,
    actionNames: Map<Int, String>,
    userFactory: TulipUserFactory,
    getTest: (RuntimeContext, TestProfile) -> TestProfile
) {
    Console.put("")

    runtimeInit(contextId, context, tests, actionNames, userFactory)

    Console.put("======================================================================")
    Console.put("Context: ${context.name}")
    Console.put("======================================================================")
    Console.put("")
    Console.put("  NUM_USERS = $MAX_NUM_USERS")
    Console.put("  NUM_THREADS = $MAX_NUM_THREADS")
    Console.put("  NUM_USERS_PER_THREAD = ${MAX_NUM_USERS / MAX_NUM_THREADS}")
    if ((MAX_NUM_USERS / MAX_NUM_THREADS) * MAX_NUM_THREADS != MAX_NUM_USERS) {
        Console.put("")
        Console.put("NUM_USERS should equal n*NUM_THREADS, where n >= 1")
        exitProcess(0)
    }
    testSuite!!.forEachIndexed { indexTestCase, testCase ->
        if (testCase.enabled) {
            val x: TestProfile = getTest(context, testCase)
            if (x.workflow.isEmpty()) {
                g_workflow = null
            } else {
                g_workflow = workflows[x.workflow]
            }
            x.queueLengths.forEachIndexed { indexUserProfile, queueLength ->
                Thread.sleep(5000)
                runTest(x, contextId, indexTestCase, indexUserProfile, queueLength)
            }
            g_workflow = null
        }
    }

    runtimeDone()
}

/*-------------------------------------------------------------------------*/

private fun runBenchmarks(
    userFactory: TulipUserFactory,
    getTest: (RuntimeContext, TestProfile) -> TestProfile
) {
    val contexts = g_contexts
    val tests = g_tests
    val actionNames = g_config.actions.userActions
    // Remove the previous JSON results file (if it exists)
    val filename = g_tests[0].filename
    val file = java.io.File(filename)
    file.delete()
    // if (result) {
    //    //println("File deleted successfully - ${filename}")
    // } else {
    //    //throw Exception("Exiting, could not delete file - ${filename}")
    // }

    // run all benchmarks
    contexts.forEachIndexed { contextId, context ->
        runTulip(contextId, context, tests, actionNames, userFactory, getTest)
    }

    // write ']' to JSON results file
    DataCollector.closeStatsJson(filename)
}

fun runBenchmarks(userFactory: TulipUserFactory) {
    runBenchmarks(userFactory, ::getTest)
    // logger.info { "Done" }
}

/*-------------------------------------------------------------------------*/
