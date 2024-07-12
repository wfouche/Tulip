/*-------------------------------------------------------------------------*/

package tulip

/*-------------------------------------------------------------------------*/

//https://github.com/conversant/disruptor
//import com.conversantmedia.util.concurrent.DisruptorBlockingQueue as Queue
//import java.util.concurrent.ArrayBlockingQueue  as Queue

import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.Tag
import io.micrometer.jmx.JmxConfig
import io.micrometer.jmx.JmxMeterRegistry
import java.time.format.DateTimeFormatter
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.exitProcess
import java.util.concurrent.LinkedBlockingQueue as Java_Queue
import java.util.concurrent.LinkedBlockingQueue as SPSC_Queue
import java.util.concurrent.LinkedBlockingQueue as MPSC_Queue
import io.github.oshai.kotlinlogging.KotlinLogging
import java.lang.management.ManagementFactory
import javax.management.Attribute
import javax.management.ObjectName

/*-------------------------------------------------------------------------*/
// https://github.com/oshai/kotlin-logging
val logger = KotlinLogging.logger {}

var g_queueTimeBlocked: Long = 0

/*-------------------------------------------------------------------------*/

val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss.SSS")

//
// Arrays of user objects and user actions.
//

var TULIP_SCENARIO_NAME: String = ""
var TULIP_SCENARIO_ID: Int = 0

var MAX_NUM_USERS = 0
var MAX_NUM_THREADS = 0

private var userObjects: Array<VirtualUser?>? = null // arrayOfNulls<User>(NUM_USERS)
private var userActions: Array<Iterator<Int>?>? = null // arrayOfNulls<Iterator<Int>>(NUM_USERS)

//
// Array of Worker thread objects
//
private var userThreads: Array<UserThread?>? = null //arrayOfNulls<UserThread>(NUM_THREADS)

// ...
private var testSuite: List<TestProfile>? = null

private var newUser: ((Int,String) -> VirtualUser)? = null

var actionNames: Map<Int, String> = emptyMap()

internal val registry = JmxMeterRegistry(JmxConfig.DEFAULT, Clock.SYSTEM)

internal val mg_num_success    = registry.gauge("Tulip", listOf(Tag.of("num",   "success")),   AtomicInteger(0))
internal val mg_num_failed    = registry.gauge("Tulip", listOf(Tag.of("num",    "failed")),   AtomicInteger(0))
internal val mg_num_actions    = registry.gauge("Tulip", listOf(Tag.of("num",   "actions")),   AtomicInteger(0))

internal val mg_context_id   = registry.gauge("Tulip", listOf(Tag.of("context", "id")),      AtomicInteger(0))
internal val mg_num_threads  = registry.gauge("Tulip", listOf(Tag.of("context", "id"), Tag.of("num", "threads")), AtomicInteger(0))
internal val mg_num_users    = registry.gauge("Tulip", listOf(Tag.of("context", "id"), Tag.of("num", "users")),   AtomicInteger(0))

internal val mg_rt_avg = registry.gauge("Tulip", listOf(Tag.of("rt",   "avg")), AtomicInteger(0))
internal val mg_rt_max = registry.gauge("Tulip", listOf(Tag.of("rt",   "max")), AtomicInteger(0))
internal val mg_rt_min = registry.gauge("Tulip", listOf(Tag.of("rt",   "min")), AtomicInteger(0))

internal val mg_benchmark_id  = registry.gauge("Tulip", listOf(Tag.of("benchmark",   "id")),       AtomicInteger(0))
internal val mg_benchmark_tps = registry.gauge("Tulip", listOf(Tag.of("benchmark",   "tps")),      AtomicInteger(0))
internal val mg_benchmark_dur = registry.gauge("Tulip", listOf(Tag.of("benchmark",   "duration")), AtomicInteger(0))
internal val mg_benchmark_phs = registry.gauge("Tulip", listOf(Tag.of("benchmark",   "phase")), AtomicInteger(0))
internal val mg_benchmark_run = registry.gauge("Tulip", listOf(Tag.of("benchmark",   "run")), AtomicInteger(0))

internal val mg_cpu_tulip = registry.gauge("Tulip", listOf(Tag.of("cpu",   "tulip")), AtomicInteger(0))
internal val mg_cpu_system = registry.gauge("Tulip", listOf(Tag.of("cpu",   "system")), AtomicInteger(0))

/*-------------------------------------------------------------------------*/

fun runtimeInit(contextId: Int, context: RuntimeContext, tests: List<TestProfile>, actionDesc: Map<Int, String>, func: (Int,String) -> VirtualUser) {
    TULIP_SCENARIO_ID = contextId
    TULIP_SCENARIO_NAME = context.name

    MAX_NUM_USERS = context.numUsers
    MAX_NUM_THREADS = context.numThreads
    testSuite = tests
    newUser = func

    userObjects = arrayOfNulls(MAX_NUM_USERS)
    userActions = arrayOfNulls(MAX_NUM_USERS)
    userThreads = arrayOfNulls(MAX_NUM_THREADS)
    actionNames = actionDesc

    mg_num_threads?.set(MAX_NUM_THREADS)
    mg_num_users?.set(MAX_NUM_USERS)
    mg_context_id?.set(contextId)
}

fun runtimeDone() {
    // Terminate all user threads.
    userThreads!!.forEach { userThread ->
        userThread!!.tq.put(Task(status = 999))
    }

    // Wait for all user threads to exit.
    while (userThreads!!.map { if (it == null) 0 else 1 }.sum() > 0) {
        Thread.sleep(500)
    }
}

/*-------------------------------------------------------------------------*/

data class RuntimeContext(
    val name: String = "",
    val numUsers: Int = 0,
    val numThreads: Int = 0
)

/*-------------------------------------------------------------------------*/

data class Action(
    //
    // Numeric action ID.
    //
    val id: Int,

    //
    // Number of occurrences of this action relative to other actions.
    // Set weight to 0 (or use default value) when a workflow should be specified.
    val weight: Int = 0
)

/*-------------------------------------------------------------------------*/

data class Duration(

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

    val timeUnit: TimeUnit = TimeUnit.MINUTES,

    // ------------------------------

    val startupDurationMillis: Long = timeUnit.toMillis(startupDurationUnits),
    val warmupDurationMillis: Long = timeUnit.toMillis(warmupDurationUnits),
    val mainDurationMillis: Long = timeUnit.toMillis(mainDurationUnits)
)

data class TestProfile(
    val enabled: Boolean = true,

    //
    // Name of the benchmark test.
    //
    val name: String = "",

    val duration: Duration = Duration(0, 0, 0, 1),

    // List of actions to be performed.
    // If the weights of all the actions are zero (0), then treat the action list
    // as a workflow to be executed per user object.
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
    val filename: String = ""
)

/*-------------------------------------------------------------------------*/

//
// Task data class. Tasks are created be the main thread and send to User objects to perform known actions.
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
    require(delayFrom < delayTo) { "delayFrom must be smaller than delayTo, but $delayFrom >= $delayTo"}
    val delayMillis = ThreadLocalRandom.current().nextLong(delayTo - delayFrom + 1) + delayFrom
    Thread.sleep(delayMillis)
}

/*-------------------------------------------------------------------------*/

class RateGovernor(private val averageRate: Double, private val timeMillisStart: Long = TimeUnit.NANOSECONDS.toMillis(System.nanoTime()))  {

    private var count: Long = 0

    fun pace() {
        count += 1
        val deltaMs: Long = (timeMillisStart + count * (1000 / averageRate) - TimeUnit.NANOSECONDS.toMillis(System.nanoTime())).toLong()
        if (deltaMs > 0) Thread.sleep(deltaMs)
    }
}

/*-------------------------------------------------------------------------*/

class UserThread(private val threadId: Int) : Thread() {

    init {
        name = "user-thread-$threadId"
    }

    //
    // Task Queue - input queue with tasks for this thread to complete.
    //
    val tq = SPSC_Queue<Task>(10)
    private var running = true

    override fun run() {
        while (running) {
            //
            // Wait for a new task to be assigned to this thread.
            //
            val task: Task = tq.take()

            /// ....
            if (task.status == 999) {
                //Console.put("Thread ${name} is stopping.")
                running = false
            } else {

                //
                // Locate the user object to which the task should be applied.
                // Dynamically create a new user object, if required.
                //
                var u = userObjects!![task.userId]
                if (u == null) {
                    u = newUser!!(task.userId, g_config.userClass)
                    userObjects!![task.userId] = u
                }

                //
                // Apply the task to the user object. The return value is either
                // True or False, indicating that the task succeeded or failed.
                // Also calculate the elapsed time in microseconds.
                //
                task.waitTimeNanos = System.nanoTime() - task.beginQueueTimeNanos
                task.serviceTimeNanos = elapsedTimeNanos {
                    if (u.processAction(task.actionId)) task.status = 1 else task.status = 0
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
        //priority = Thread.MAX_PRIORITY
        isDaemon = true
        name = "console-thread"
        start()
    }

    private var q = Java_Queue<MutableList<String>>(10)

    override fun run() {
        while (true) {
            val list: MutableList<String> = q.take()
            for (s in list) println(s)
        }
    }

    fun put(s: String) {
        put(mutableListOf(s))
    }

    fun put(list: MutableList<String>) {
        q.put(list)
    }
}

/*-------------------------------------------------------------------------*/

fun getLoadValue(counter: String): Double {
    val mbs = ManagementFactory.getPlatformMBeanServer()
    val name = ObjectName.getInstance("java.lang:type=OperatingSystem")
    val list = mbs.getAttributes(name, arrayOf(counter))

    if (list.isEmpty()) return java.lang.Double.NaN

    val att = list[0] as Attribute
    val value = att.value as Double

    // usually takes a couple of seconds before we get real values
    return if (value == -1.0) java.lang.Double.NaN else (value * 1000).toInt() / 10.0
    // returns a percentage value with 1 decimal point precision
}

fun getProcessCpuLoad(): Double {
    return getLoadValue("ProcessCpuLoad")
}

fun getSystemCpuLoad(): Double {
    return getLoadValue("SystemCpuLoad")
}

object CpuLoadMetrics : Thread() {

    init {
        isDaemon = true
        name = "cpu-load-metrics"
        start()
    }

    val systemCpuStats = Java_Queue<Double>(1000)
    val processCpuStats = Java_Queue<Double>(1000)

    override fun run() {
        while (getProcessCpuLoad().isNaN()) {
            Thread.sleep(250)
        }
        while (getSystemCpuLoad().isNaN()) {
            Thread.sleep(250)
        }

        var timeMillisNext: Long = TimeUnit.NANOSECONDS.toMillis(System.nanoTime())
        var i = 0
        var totalCpuSystem = 0.0
        var totalCpuProcess = 0.0
        while (true) {
            timeMillisNext += 1000
            while (TimeUnit.NANOSECONDS.toMillis(System.nanoTime()) < timeMillisNext) {
                Thread.sleep(250)
            }
            totalCpuSystem += getSystemCpuLoad()
            totalCpuProcess += getProcessCpuLoad()
            i += 1
            if (i % 10 == 0) {
                val avgCpuSystem = totalCpuSystem / 10.0
                val avgCpuProcess = totalCpuProcess / 10.0
                systemCpuStats.put(avgCpuSystem)
                processCpuStats.put(avgCpuProcess)
                //Console.put("${i}, ${avgCpuSystem}, ${avgCpuProcess}")
                totalCpuSystem = 0.0
                totalCpuProcess = 0.0
                i = 0
            }
        }
    }
}

/*-------------------------------------------------------------------------*/

fun getQueueLengths(context: RuntimeContext, test: TestProfile): List<Int> {
    val list: MutableList<Int> = mutableListOf()
    test.queueLengths.forEach { queueLength ->
        list.add(
            when (queueLength) {
                0 -> context.numThreads
                -1 -> context.numThreads * 10
                else -> queueLength
            }
        )
    }
    return list
}

/*-------------------------------------------------------------------------*/

fun getTest(context: RuntimeContext, test: TestProfile): TestProfile {
    return test.copy(queueLengths = getQueueLengths(context, test))
}

/*-------------------------------------------------------------------------*/

fun assignTask(task: Task) {
    val threadId = task.userId / (task.numUsers / task.numThreads)
    var w = userThreads!![threadId]
    if (w == null) {
        w = UserThread(threadId).apply {
            isDaemon = true
            start()
        }
        userThreads!![threadId] = w
    }
    task.beginQueueTimeNanos = System.nanoTime()
    if (!w.tq.offer(task)) {
        val qtw = elapsedTimeNanos {
            w.tq.put(task)
        }
        if (qtw > 0) g_queueTimeBlocked += qtw
    }
}

/*-------------------------------------------------------------------------*/

fun createActionGenerator(list: List<Int>): Iterator<Int> {
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

fun runTest(testCase: TestProfile, contextId: Int, indexTestCase: Int, indexUserProfile: Int, queueLength: Int) {
    var tsBegin = java.time.LocalDateTime.now().format(formatter)
    val output = mutableListOf("")
    output.add("======================================================================")
    output.add("= [${contextId}][${indexTestCase}][${indexUserProfile}][${queueLength}] ${testCase.name} - $tsBegin")
    output.add("======================================================================")
    Console.put(output)

    mg_benchmark_id?.set(indexTestCase)

    val rnd = ThreadLocalRandom.current()

    // create a list of randomized user IDs
    val userList = mutableListOf<Int>()
    repeat(MAX_NUM_USERS) {
        userList.add(it)
    }
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
            repeat(action.weight) {
                actionList.add(action.id)
            }
        }
        actionList.shuffle(rnd)
    }
    repeat(MAX_NUM_USERS) { idx ->
        if ((testCase.duration.warmupDurationUnits == 0L) && (testCase.duration.mainDurationUnits == 0L)) {
            userActions!![idx] = null
        } else {
            userActions!![idx] = createActionGenerator(actionList)
        }
    }

    //
    // Create a queue containing a total of queueLength tokens.
    //
    val rspQueue = MPSC_Queue<Task>(queueLength)
    var rspQueueInitialized: Boolean = false

    fun initRspQueue() {
        if (rspQueueInitialized) return
        repeat(queueLength) {
            rspQueue.put(Task())
        }
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

    fun startTask(uid: Int, aid: Int, rateGovernor: RateGovernor?) {
        // Limit the number of active users.
        val task: Task = rspQueue.take()

        // ...
        DataCollector.updateStats(task)

        // Assign the task to the user object.
        task.apply {
            userId = uid; numUsers = MAX_NUM_USERS; numThreads = MAX_NUM_THREADS; actionId =
            aid; this.rspQueue = rspQueue
        }
        assignTask(task)

        // Limit the throughput rate, if required.
        rateGovernor?.pace()
    }

    if ((testCase.duration.warmupDurationUnits == 0L) && (testCase.duration.mainDurationUnits == 0L)) {

        DataCollector.clearStats()

        initRspQueue()

        val timeMillisStart: Long = TimeUnit.NANOSECONDS.toMillis(System.nanoTime())

        // Special bootstrap test case to initialize terminals, and other objects.
        // Typically only found at the start and end of a test suite.
        var rateGovernor: RateGovernor? = null
        if (testCase.arrivalRate > 0.0) {
            rateGovernor = RateGovernor(testCase.arrivalRate, timeMillisStart)
        }

        for (aid in actionList) {
            for (uid in userList) {
                startTask(uid, aid, rateGovernor)
            }
        }
        drainRspQueue()
        val timeMillisEnd: Long = TimeUnit.NANOSECONDS.toMillis(System.nanoTime())
        val durationMillis: Int = (timeMillisEnd - timeMillisStart).toInt()
        val tsEnd = java.time.LocalDateTime.now().format(formatter)

        val durationNanos2 = elapsedTimeNanos {
            DataCollector.createSummary(
                durationMillis,
                testCase,
                indexTestCase,
                indexUserProfile,
                queueLength,
                tsBegin,
                tsEnd,
                "Benchmark",
                0)
            DataCollector.printStats(true)
            DataCollector.saveStatsJson(testCase.filename)
        }
        //Console.put("Init: Duration spend in stats processing = ${durationNanos2}")
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
        arrivalRate: Double = -1.0) {

        if (durationMillis == 0L) {
            return
        }
        if (runId == 0) {
            //Console.put("initRspQueue: runId == 0")
            initRspQueue()
        }

        DataCollector.clearStats()
        tsBegin = java.time.LocalDateTime.now().format(formatter)
        Console.put("\n${testPhase} run ${runId}: begin (${tsBegin})")

        timeMillisStart = timeMillisEnd
        timeMillisEnd = timeMillisStart + durationMillis

        val rateGovernor: RateGovernor? = null
        // New rate control logic - begin
        var nanosPerAction: Double = 0.0
        if (arrivalRate > -1.0) {
            // Warm-up duration at max speed, ungoverned.
        } else {
            // Ramp-up or Main duration.
            if (testCase.arrivalRate > 0.0) {
                //rateGovernor = RateGovernor(testCase.arrivalRate, timeMillisStart)
                nanosPerAction = 1000000000.0 /  testCase.arrivalRate
            }
        }

        val durationNanos: Double = durationMillis * 1000000.0
        val startTimeNanos = System.nanoTime()
        val endTimeNanos: Double = startTimeNanos + durationNanos
        var rTime: Double = startTimeNanos.toDouble()
        var vTime: Double = rTime
        // New rate control logic - end

        while (rTime < endTimeNanos) {
            // Pick a random user object to assign a task to.
            val uid = userList.random()

            // Pick the next task for the user object to execute.
            val aid: Int = userActions!![uid]!!.next()

            startTask(uid, aid, rateGovernor)

            vTime += nanosPerAction
            if (vTime > rTime) {
                val delayMillis: Long = ((vTime-rTime)/1000000.0).toLong()
                Thread.sleep(delayMillis)
            }
            rTime = System.nanoTime().toDouble()
        }
        if (runId == runIdMax) {
            //Console.put("drainRspQueue: runId == runIdMax")
            if (testPhase == "Benchmark") drainRspQueue()
        }
        val tsEnd = java.time.LocalDateTime.now().format(formatter)

        Console.put("$testPhase run ${runId}: end   (${tsEnd})")

        val durationNanos2 = elapsedTimeNanos {
            DataCollector.createSummary(
                durationMillis.toInt(),
                testCase,
                indexTestCase,
                indexUserProfile,
                queueLength,
                tsBegin,
                tsEnd,
                testPhase,
                runId
            )
            DataCollector.printStats(false)
            if (testPhase == "Benchmark") {
                DataCollector.saveStatsJson(testCase.filename)
            }
        }
        //Console.put("Main: Duration spend in stats processing = ${durationNanos2}")
    }

    // Start-up
    //
    // Since we could have 1 or more population set sizes, only perform the start-up phase
    // on the first set, i.e., with index 0.
    //
    g_queueTimeBlocked = 0
    if (indexUserProfile == 0) {
        assignTasks(testCase.duration.startupDurationMillis, "Prewarmup", 0, 0, 0.0)
    }

    // Ramp-up
    g_queueTimeBlocked = 0
    assignTasks(testCase.duration.warmupDurationMillis, "Warmup", 0, 0)
    Console.put("  total time blocked   = ${g_queueTimeBlocked/1000/1000} ms")

    // Main run(s)
    for (runId in 0 until testCase.duration.mainDurationRepeatCount) {
        g_queueTimeBlocked = 0
        assignTasks(
            testCase.duration.mainDurationMillis,
            "Benchmark",
            runId,
            testCase.duration.mainDurationRepeatCount - 1
        )
        Console.put("  total time blocked   = ${g_queueTimeBlocked/1000/1000} ms")
    }

}

/*-------------------------------------------------------------------------*/

fun initTulip() {
    Console.put("Tulip (${System.getProperty("java.vendor")} ${System.getProperty("java.runtime.version")}, Kotlin ${KotlinVersion.CURRENT})")
}

/*-------------------------------------------------------------------------*/

fun runTulip(
    contextId: Int,
    context: RuntimeContext,
    tests: List<TestProfile>,
    actionNames: Map<Int, String>,
    getUser: (Int,String) -> VirtualUser,
    getTest: (RuntimeContext, TestProfile) -> TestProfile
) {
    Console.put("")

    runtimeInit(contextId, context, tests, actionNames, getUser)

    Console.put("======================================================================")
    Console.put("Scenario: ${context.name}")
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
            x.queueLengths.forEachIndexed { indexUserProfile, queueLength ->
                Thread.sleep(5000)
                runTest(x, contextId, indexTestCase, indexUserProfile, queueLength)
            }
        }
    }

    runtimeDone()
}

/*-------------------------------------------------------------------------*/

fun runTests(getUser: (Int,String) -> VirtualUser) {
    val actionNames = tulip.g_config.userActions
    runTests(g_contexts, g_tests, actionNames, getUser, ::getTest)
}

private fun runTests(
    contexts: List<RuntimeContext>,
    tests: List<TestProfile>,
    actionNames: Map<Int, String>,
    getUser: (Int, String) -> VirtualUser,
    getTest: (RuntimeContext, TestProfile) -> TestProfile
) {
    // Remove the previous JSON results file (if it exists)
    val filename = g_tests[0].filename
    val file = java.io.File(filename)
    val result = file.delete()
    if (result) {
        //println("File deleted successfully - ${filename}")
    } else {
        //throw Exception("Exiting, could not delete file - ${filename}")
    }

    // run all benchmarks
    contexts.forEachIndexed { contextId, context ->
        runTulip(contextId, context, tests, actionNames, getUser, getTest)
    }

    // write ']' to JSON results file
    DataCollector.closeStatsJson(filename)
}

/*-------------------------------------------------------------------------*/
