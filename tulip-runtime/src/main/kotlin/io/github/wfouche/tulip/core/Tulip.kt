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
import io.github.wfouche.tulip.report.convertAdocToHtml
import io.github.wfouche.tulip.report.createConfigReport
import java.lang.management.ManagementFactory
import java.time.format.DateTimeFormatter
import java.util.concurrent.LinkedBlockingQueue as BlockingQueue
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.system.exitProcess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import org.HdrHistogram.Histogram
import org.HdrHistogram.IntCountsHistogram

/*-------------------------------------------------------------------------*/

const val VERSION = TulipApi.VERSION

const val NUM_ACTIONS = TulipApi.NUM_ACTIONS

const val histogramNumberOfSignificantValueDigits = 3

private val osBean: OperatingSystemMXBean =
    ManagementFactory.getPlatformMXBean(OperatingSystemMXBean::class.java)

fun getProcessCpuTime(): Long {
    return osBean.processCpuTime
}

fun getCpuLoad(): Double {
    return osBean.cpuLoad
}

fun getProcessCpuLoad(): Double {
    return osBean.processCpuLoad
}

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

val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss")

//
// Arrays of user objects and user actions.
//

var TULIP_CONTEXT_NAME: String = ""
var TULIP_CONTEXT_ID: Int = 0

var MAX_NUM_USERS = 0
var MAX_NUM_THREADS = 0

var userObjects: Array<TulipUser?>? = null // arrayOfNulls<User>(NUM_USERS)
var userActions: Array<Iterator<Int>?>? = null // arrayOfNulls<Iterator<Int>>(NUM_USERS)

//
// Array of Worker thread objects
//
var userThreads: Array<UserThread?>? = null // arrayOfNulls<UserThread>(NUM_THREADS)

// ...
private var testSuite: List<TestProfile>? = null

// Markov chain object
var g_workflow: MarkovChain? = null

// ...
var newUser: TulipUserFactory? = null

var actionNames: Map<Int, String> = emptyMap()
var workflows: HashMap<String, MarkovChain> = HashMap<String, MarkovChain>()
var userRuntimeContext: RuntimeContext = RuntimeContext()

var g_outputDirname: String = ""

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
    userFactory: TulipUserFactory,
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
    userRuntimeContext = context

    //    mg_num_threads?.set(MAX_NUM_THREADS)
    //    mg_num_users?.set(MAX_NUM_USERS)
    //    mg_context_id?.set(contextId)
    for (i in 1..10) {
        val l0 = getCpuLoad()
        val l1 = getProcessCpuTime()
        val l2 = getProcessCpuLoad()
    }
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
    @SerialName("num_threads") val numThreads: Int = 0,
    @SerialName("user_params") val userParams: Map<String, JsonPrimitive> = mapOf(),
)

@Serializable
data class ConfigDuration(
    @SerialName("pre_warmup_duration") val startupDuration: Long = 0,
    @SerialName("warmup_duration") val warmupDuration: Long = 0,
    @SerialName("benchmark_duration") val mainDuration: Long = 0,
    @SerialName("benchmark_iterations") val mainDurationRepeatCount: Int = 1,
)

@Serializable data class ConfigAction(val id: Int, val weight: Int = 0)

@Serializable
data class ConfigTest(
    val enabled: Boolean = true,
    @SerialName("save_stats") val logStats: Boolean = true,
    val time: ConfigDuration = ConfigDuration(),
    @SerialName("aps_rate") val throughputRate: Double = 0.0,
    @SerialName("aps_rate_step_change") val throughputRateStepChange: Double = 0.0,
    @SerialName("aps_rate_step_count") val throughputRateStepCount: Int = 1,
    @SerialName("worker_thread_queue_size") val workInProgress: Int = 0,
    @SerialName("scenario_actions") val actions: List<ConfigAction> = listOf(),
    @SerialName("scenario_workflow") val workflow: String = "",
)

@Serializable
data class ActionsConfig(
    @SerialName("description") val description: String = "",
    @SerialName("output_filename") val jsonFilename: String = "",
    @SerialName("report_filename") val htmlFilename: String = "",
    @SerialName("user_class") val userClass: String = "",
    @SerialName("user_params") val userParams: Map<String, JsonPrimitive> = mapOf(),
    @SerialName("user_actions") val userActions: Map<Int, String> = mapOf(),
)

@Serializable
data class TulipConfig(
    val actions: ActionsConfig = ActionsConfig(),
    val contexts: Map<String, ConfigContext> = mapOf(),
    val benchmarks: Map<String, ConfigTest> = mapOf(),
    val workflows: Map<String, Map<String, Map<String, Double>>> = mapOf(),
)

fun initConfig(text: String): String {
    val textIsJsonString: Boolean = text.trim().startsWith("{")
    var configFilename: String = ""
    initTulip()
    Console.put("")
    val wd = System.getProperty("user.dir")
    Console.put("  --- Tulip Runtime Configuration ---")
    Console.put("  working directory = $wd")
    if (!textIsJsonString) {
        Console.put("  config filename   = $text")
    }

    // Read JSON file contents into memory
    val sf: String =
        if (textIsJsonString) {
            text
        } else {
            // read config from local folder (JBang) or from src/main/resources (Gradle or Maven)
            configFilename = text
            if (java.io.File(configFilename).exists()) {
                // JBang project
            } else {
                // Gradle or Maven project
                val file2: String = "src/main/resources/$configFilename"
                // Console.put("file2 = ${file2}")
                if (java.io.File(file2).isFile()) {
                    configFilename = file2
                    // Console.put("file2: is a file")
                    java.io.File("build/reports/tulip").mkdirs()
                    g_outputDirname = "build/reports/tulip"
                } else {
                    // Console.put("file2: is not a file")
                }
            }
            java.io.File(configFilename).readText()
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
            val v = RuntimeContext(k, e.numUsers, e.numThreads, e.userParams)
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
                    Duration(
                        e.time.startupDuration,
                        e.time.warmupDuration,
                        e.time.mainDuration,
                        e.time.mainDurationRepeatCount,
                        TimeUnit.SECONDS,
                    ),
                arrivalRate = e.throughputRate,
                arrivalRateStepChange = e.throughputRateStepChange,
                arrivalRateStepCount = e.throughputRateStepCount,
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
                workflow = e.workflow,
            )
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
    Console.put("  output filename   = ${g_config.actions.jsonFilename}")
    Console.put("  report filename   = ${g_config.actions.htmlFilename}")
    if (!textIsJsonString) {
        val adocFilename = createConfigReport(configFilename)
        convertAdocToHtml(adocFilename)
    }

    return g_config.actions.jsonFilename
}

/*-------------------------------------------------------------------------*/

val wthread_wait_stats = Histogram(histogramNumberOfSignificantValueDigits)

/*-------------------------------------------------------------------------*/

// object PlantUmlServer : Thread() {
//
//    var running = false
//
//    init {
//        isDaemon = true
//        name = "plantuml-server"
//    }
//
//    override fun run() {
//        running = true
//        net.sourceforge.plantuml.Run.main(arrayOf("-picoweb:8080"))
//    }
// }

/*-------------------------------------------------------------------------*/

private fun getQueueLengths(context: RuntimeContext, test: TestProfile): List<Int> {
    val list: MutableList<Int> = mutableListOf()
    test.queueLengths.forEach { queueLength ->
        list.add(
            when {
                queueLength == 0 -> context.numThreads * USER_THREAD_QSIZE // 11
                queueLength > 0 -> context.numThreads * queueLength // Actions per Thread
                else -> abs(queueLength) // Actions across all Threads
            }
        )
    }
    return list
}

/*-------------------------------------------------------------------------*/

private fun getTest(context: RuntimeContext, test: TestProfile): TestProfile {
    return test.copy(queueLengths = getQueueLengths(context, test))
}

/*-------------------------------------------------------------------------*/

val wthread_queue_stats = IntCountsHistogram(histogramNumberOfSignificantValueDigits)

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
    queueLength: Int,
) {
    var cpuTime: Long = 0
    var tsBegin = java.time.LocalDateTime.now().format(formatter)
    val output = mutableListOf("")
    output.add("======================================================================")
    output.add(
        "= [${contextId}][${indexTestCase}][${indexUserProfile}][${queueLength}] ${testCase.name} - $tsBegin"
    )
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
        if (
            (testCase.duration.warmupDurationUnits == 0L) &&
                (testCase.duration.mainDurationUnits == 0L)
        ) {
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
    val rstQueue = MPSC_Queue<Task>(queueLength)
    var statsThread: StatsThread? = null

    fun initRspQueue() {
        if (rspQueueInitialized) return
        repeat(queueLength) { rspQueue.put(Task()) }
        rspQueueInitialized = true
        statsThread = StatsThread(rstQueue, rspQueue)
        statsThread!!.setPriority(Thread.NORM_PRIORITY + 1)
        statsThread!!.start()
    }

    fun drainRspQueue() {
        if (!rspQueueInitialized) return
        repeat(queueLength) {
            val task: Task = rspQueue.take()
        }
        rspQueueInitialized = false
        statsThread!!.itq!!.put(Task(status = 999))
        while (statsThread!!.running) {
            Thread.sleep(10)
        }
        statsThread = null
    }

    fun startTask(uid: Int, aid: Int) {
        // Limit the number of active users.
        val task: Task = rspQueue.take()

        // ...
        // DataCollector.updateStats(task)

        // Assign the task to the user object.
        task.apply {
            userId = uid
            numUsers = MAX_NUM_USERS
            numThreads = MAX_NUM_THREADS
            actionId = aid
            this.rspQueue = rstQueue
        }
        assignTask(task)
    }

    if (
        (testCase.duration.warmupDurationUnits == 0L) && (testCase.duration.mainDurationUnits == 0L)
    ) {

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
                startTask(uid, aid)
                rateGovernor?.pace()
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
                cpuTime,
                0.0,
            )
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
        arrivalRate: Double = -1.0,
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
            "\n${testPhase} (${testCase.name}), run ${runId+1} of ${runIdMax+1}: begin (${tsBegin})"
        )
        Console.put(
            "${testPhase} (${testCase.name}), run ${runId+1} of ${runIdMax+1}:       (${tsEndPredicted})"
        )

        timeMillisStart = timeMillisEnd
        timeMillisEnd = timeMillisStart + durationMillis

        // New rate control logic - begin
        val nanosPerAction: Double
        val numActionsMax: Long
        var numActions: Long = 0
        var apsRate: Double = 0.0
        if (arrivalRate > -1.0) {
            // Warm-up duration at max speed, ungoverned.
            nanosPerAction = 0.0
            numActionsMax = 0
        } else {
            // Ramp-up or Main duration.
            if (testCase.arrivalRate > 0.0) {
                val sprintId: Int = runId / testCase.arrivalRateStepCount
                val _arrivalRate: Double =
                    testCase.arrivalRate + sprintId * testCase.arrivalRateStepChange
                // rate limited, calculate time ns per action
                nanosPerAction = 1000000000.0 / _arrivalRate
                numActionsMax = (_arrivalRate * durationMillis / 1000.0).toLong()
                apsRate = _arrivalRate
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

            startTask(uid, aid)

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
            "$testPhase (${testCase.name}), run ${runId+1} of ${runIdMax+1}: end   (${tsEnd})"
        )

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
                cpuTime,
                apsRate,
            )
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
    val runIdMax: Int =
        (testCase.duration.mainDurationRepeatCount * testCase.arrivalRateStepCount) - 1
    for (runId in 0..runIdMax) {
        assignTasks(testCase.duration.mainDurationMillis, "Benchmark", runId, runIdMax)
    }
}

/*-------------------------------------------------------------------------*/

private fun initTulip() {
    Console.put(TulipApi.getVersionBanner())
    var tulip = " " + String(Character.toChars(0x0001F337))
    //    if (SystemUtils.IS_OS_WINDOWS) {
    //        tulip = ""
    //    }
    Console.put(
        "Tulip $VERSION (Java: ${System.getProperty("java.vendor")} ${System.getProperty("java.runtime.version")}, Kotlin: ${KotlinVersion.CURRENT})" +
            tulip
    )

    Console.put("")
    Console.put("  --- JVM Runtime Options (VM Arguments) ---")

    val jvmArgs = ManagementFactory.getRuntimeMXBean().getInputArguments()

    if (jvmArgs.isEmpty()) {
        Console.put("  No explicit JVM Runtime Options found (default settings are in use).")
    } else {
        for (i in jvmArgs.indices) {
            Console.put("  Option " + (i + 1) + ": " + jvmArgs.get(i))
        }
    }
}

/*-------------------------------------------------------------------------*/

private fun runTulip(
    contextId: Int,
    context: RuntimeContext,
    tests: List<TestProfile>,
    actionNames: Map<Int, String>,
    userFactory: TulipUserFactory,
    getTest: (RuntimeContext, TestProfile) -> TestProfile,
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
                // Thread.sleep(5000)
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
    getTest: (RuntimeContext, TestProfile) -> TestProfile,
) {
    fun outputFilename(filename: String): String {
        return if (g_outputDirname == "") filename else "$g_outputDirname/$filename"
    }

    val contexts = g_contexts
    val tests = g_tests
    val actionNames = g_config.actions.userActions
    // Remove the previous JSON results file (if it exists)
    val filename = g_tests[0].filename
    val file = java.io.File(outputFilename(filename))
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
    // Save current thread priority
    val ctp: Int = Thread.currentThread().priority

    // main-thread and stats-thread have the same priority
    Thread.currentThread().setPriority(Thread.NORM_PRIORITY + 1)

    runBenchmarks(userFactory, ::getTest)
    // logger.info { "Done" }

    // Restore original thread priority
    Thread.currentThread().setPriority(ctp)
}

/*-------------------------------------------------------------------------*/
