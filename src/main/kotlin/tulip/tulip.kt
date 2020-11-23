/*-------------------------------------------------------------------------*/

package tulip

/*-------------------------------------------------------------------------*/

import java.util.concurrent.ArrayBlockingQueue  as Queue
//import java.util.concurrent.LinkedBlockingQueue

import java.lang.Thread
import java.util.concurrent.ThreadLocalRandom

import kotlin.sequences.iterator

/*-------------------------------------------------------------------------*/

//
// Arrays of user objects and user actions.
//

var TULIP_SCENARIO_NAME: String = ""
var TULIP_SCENARIO_ID: Int = 0

var MAX_NUM_USERS = 0
var MAX_NUM_THREADS = 0

private var userObjects: Array<User?>? = null // arrayOfNulls<User>(NUM_USERS)
private var userActions: Array<Iterator<Int>?>? = null // arrayOfNulls<Iterator<Int>>(NUM_USERS)

//
// Array of Worker thread objects of a concrete type.
//
private var userThreads: Array<UserThread?>? = null //arrayOfNulls<UserThread>(NUM_THREADS)

// ...
private var testSuite: List<TestProfile>? = null

private var newUser: ((Int) -> User)? = null

/*-------------------------------------------------------------------------*/

fun runtimeInit(contextId: Int, context: RuntimeContext, tests: List<TestProfile>, func: (Int) -> User) {
    TULIP_SCENARIO_ID = contextId
    TULIP_SCENARIO_NAME = context.name

    MAX_NUM_USERS = context.numUsers
    MAX_NUM_THREADS = context.numThreads
    testSuite = tests
    newUser = func

    userObjects = arrayOfNulls<User>(MAX_NUM_USERS)
    userActions = arrayOfNulls<Iterator<Int>>(MAX_NUM_USERS)
    userThreads = arrayOfNulls<UserThread>(MAX_NUM_THREADS)
}

fun runtimeDone() {
    // Terminate all user threads.
    userThreads!!.forEach { userThread ->
        userThread!!.tq.put(Task(status=999))
    }

    // Wait for all user threads to exit.
    while (userThreads!!.map {if (it == null) 0 else 1}.sum() > 0) {
        delay(500)
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
    val actionId: Int,

    //
    // Number of occurrences of this action relative to other actions.
    // Set weight to 0 (or use default value) when a workflow should be specified.
    val weight: Int = 0
)

/*-------------------------------------------------------------------------*/

data class Duration (
    // Start-up period in minutes.
    // The results from this period are discarded.
    //
    // Start-up only executed once per TestCase.
    //
    val startupDurationMinutes: Int = 0,

    // Warm-up period in minutes.
    // The results from this period are discarded.
    //
    // Warm-up phase executed once per TestCase.
    //
    val warmupDurationMinutes: Int = 0,

    // Main duration in minutes.
    // The results from this period are reported.
    //
    // Main phase executed 'repeatCount' every iteration of TestCase.
    //
    val mainDurationMinutes: Int = 0,

    val mainDurationRepeatCount: Int = 1,
)

data class TestProfile(
    val isEnabled: Boolean = true,

    //
    // Name of the benchmark test.
    //
    val name: String = "",

    val duration: Duration = Duration(0,0,0,1),

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
    val queueLenghts: List<Int> = listOf(0),


    // List of percentile values to report on.
    val percentiles: List<Double> = listOf(50.0, 90.0, 95.0, 99.0),

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
    var durationMicros: Long = 0,

    var rspQueue: Queue<Task>? = null,

    var status: Int = -1
)

/*-------------------------------------------------------------------------*/

class RateGovernor(private val timeMillis_start: Long, private val averageRate: Double) {

    private var count: Long = 0

    fun pace() {
        count += 1
        var deltaMs: Long = (timeMillis_start + ((count * 1000) / averageRate) - timeMillis()).toLong()
        deltaMs = roundXN(deltaMs, 10)
        if (deltaMs < 0) {
            return
        }
        if (averageRate < 100) {
            // At low throughput rates, we just sleep for the required number of millis.
            delay(deltaMs)
        } else {
            // Soften the impact of large delays at very high throughput rates.
            if (deltaMs > 100) Thread.sleep(10)
        }
    }
}

/*-------------------------------------------------------------------------*/

class UserThread (private val threadId: Int): Thread() {

    init {
        name = "user-thread-$threadId"
    }

    //
    // Task Queue - input queue with tasks for this thread to complete.
    //
    var tq = Queue<Task>(10)
    var running = true

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
            }
            else {

                //
                // Locate the user object to which the task should be applied.
                // Dynamically create a new user object, if required.
                //
                var u = userObjects!![task.userId]
                if (u == null) {
                    u = newUser!!(task.userId)
                    userObjects!![task.userId] = u
                }

                //
                // Apply the task to the user object. The return value is either
                // True or False indicating that the task succeeded or failed.
                // Also calculate the elapsed time in microseconds.
                //
                task.durationMicros = elapsedTimeMicros {
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

    private var q = Queue<MutableList<String>>(10)

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

object CpuLoadMetrics : Thread() {

    init {
        isDaemon = true
        name = "cpu-load-metrics"
        start()
    }

    val systemCpuStats = Queue<Double>(1000)
    val processCpuStats = Queue<Double>(1000)

    override fun run() {
        while (getProcessCpuLoad().isNaN()) {
            delay(250)
        }
        while (getSystemCpuLoad().isNaN()) {
            delay(250)
        }

        var timeMillis_next: Long = timeMillis()
        var i = 0
        var total_cpu_system = 0.0
        var total_cpu_process = 0.0
        while (true) {
            timeMillis_next += 1000
            while (timeMillis() < timeMillis_next) {
                delay(250)
            }
            total_cpu_system += getSystemCpuLoad()
            total_cpu_process += getProcessCpuLoad()
            i += 1
            if (i % 10 == 0) {
                val avg_cpu_system = total_cpu_system / 10.0
                val avg_cpu_process = total_cpu_process / 10.0
                systemCpuStats.put(avg_cpu_system)
                processCpuStats.put(avg_cpu_process)
                //println("${i}, ${avg_cpu_system}, ${avg_cpu_process}")
                total_cpu_system = 0.0
                total_cpu_process = 0.0
                i = 0
            }
        }
    }
}

/*-------------------------------------------------------------------------*/

fun assignTask(task: Task) {
    val threadId = task.userId / (task.numUsers / task.numThreads)
    var w = userThreads!!.get(threadId)
    if (w == null) {
        w = UserThread(threadId).apply {
            isDaemon = true
            start()
        }
        userThreads!!.set(threadId, w)
    }
    w.tq.put(task)
}

/*-------------------------------------------------------------------------*/

fun createActionsGenerator(list: List<Int>): Iterator<Int> {
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
    val ts_begin = java.time.LocalDateTime.now().toString()
    val output = mutableListOf("")
    output.add("======================================================================")
    output.add("= [${contextId}][${indexTestCase}][${indexUserProfile}][${queueLength}] ${testCase.name} - ${ts_begin}")
    output.add("======================================================================")
    Console.put(output)

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
            actionList.add(action.actionId)
        }
    } else {
        for (action in testCase.actions) {
            repeat(action.weight) {
                actionList.add(action.actionId)
            }
        }
        actionList.shuffle(rnd)
    }
    repeat(MAX_NUM_USERS) { idx ->
        if ((testCase.duration.warmupDurationMinutes == 0) && (testCase.duration.mainDurationMinutes == 0)) {
            userActions!![idx] = null
        } else {
            userActions!![idx] = createActionsGenerator(actionList)
        }
    }

    //
    // Create a queue containing a total of queueLength tokens.
    //
    val rspQueue = Queue<Task>(queueLength)

    fun initRspQueue() {
        repeat(queueLength) {
            rspQueue.put(Task())
        }
    }

    fun drainRspQueue() {
        repeat(queueLength) {
            val task: Task = rspQueue.take()
            DataCollector.updateStats(task)
        }
    }

    if ((testCase.duration.warmupDurationMinutes == 0) && (testCase.duration.mainDurationMinutes == 0)) {

        DataCollector.clearStats()

        initRspQueue()

        val timeMillis_start: Long = timeMillis()

        // Special bootstrap test case to initialize terminals, and other objects.
        // Typically only found at the start and end of a test suite.
        var rateGoverner: RateGovernor? = null
        if (testCase.arrivalRate > 0.0) {
            rateGoverner = RateGovernor(timeMillis_start, testCase.arrivalRate)
        }

        for (aid in actionList) {
            for (uid in userList) {
                // Limit the number of active users.
                val task: Task = rspQueue.take()

                // ...
                DataCollector.updateStats(task)

                // Assign the task to the user object.
                task.apply {
                    userId = uid; numUsers = MAX_NUM_USERS; numThreads = MAX_NUM_THREADS; actionId = aid; this.rspQueue = rspQueue
                }
                assignTask(task)

                // Limit the throughput rate , if required.
                rateGoverner?.pace()
            }
        }
        drainRspQueue()
        val timeMillis_end: Long = timeMillis()
        val duration_millis: Int = (timeMillis_end - timeMillis_start).toInt()
        val ts_end = java.time.LocalDateTime.now().toString()

        DataCollector.createSummary(duration_millis, testCase, indexTestCase, indexUserProfile, queueLength, ts_begin, ts_end, "Main", 0)
        DataCollector.printStats(true)
        DataCollector.saveStatsJson(testCase.filename)
    } else {
        // Normal test case.
        var timeMillis_start: Long
        var timeMillis_end: Long = timeMillis()

        fun assignTasks(durationMinutes: Int, test_phase: String, runId:Int, runIdMax: Int, arrivalRate: Double = -1.0) {
            if (durationMinutes == 0) {
                return
            }
            if (runId == 0) {
                //Console.put("initRspQueue: runId == 0")
                initRspQueue()
            }

            DataCollector.clearStats()
            val ts1 = java.time.LocalDateTime.now()
            Console.put("\n${test_phase} run ${runId}: begin (${ts1})")

            timeMillis_start = timeMillis_end
            timeMillis_end = timeMillis_start + durationMinutes * 60 * 1000

            var rateGoverner: RateGovernor? = null
            if (arrivalRate > -1.0) {
                // Warm-up duration at max speed, ungoverned.
            }
            else {
                // Ramp-up or Main duration.
                if (testCase.arrivalRate > 0.0) {
                    rateGoverner = RateGovernor(timeMillis_start, testCase.arrivalRate)
                }
            }

            while (timeMillis() < timeMillis_end) {
                // Pick a random user object to assign a task to.
                //val uid = rnd.nextInt(NUM_USERS)  // 0 until NUM_USERS
                val uid = userList.random()

                // Pick the next task for the user object to execute.
                val aid: Int = userActions!![uid]!!.next()

                // Limit the number of active users.
                val task: Task = rspQueue.take()

                // ...
                DataCollector.updateStats(task)

                // Assign the task to the user object.
                task.apply {
                    userId = uid; numUsers = MAX_NUM_USERS; numThreads = MAX_NUM_THREADS; actionId = aid; this.rspQueue = rspQueue
                }
                assignTask(task)

                // Limit the throughput rate , if required.
                rateGoverner?.pace()
            }
            if (runId == runIdMax) {
                //Console.put("drainRspQueue: runId == runIdMax")
                drainRspQueue()
            }
            val duration_millis: Int = (timeMillis() - timeMillis_start).toInt()
            val ts_end = java.time.LocalDateTime.now().toString()

            Console.put("${test_phase} run ${runId}: end   (${ts_end})")

            DataCollector.createSummary(duration_millis, testCase, indexTestCase, indexUserProfile, queueLength, ts_begin, ts_end, test_phase, runId)
            DataCollector.printStats(false)
            if (test_phase == "Main") {
                DataCollector.saveStatsJson(testCase.filename)
            }
        }

        // Start-up
        //
        // Since we could have 1 or more population set sizes, only perform the start-up phase
        // on the first set, i.e. with index 0.
        //
        if (indexUserProfile == 0) {
            assignTasks(testCase.duration.startupDurationMinutes, "Start-up", 0, 0, 0.0)
        }

        // Ramp-up
        assignTasks(testCase.duration.warmupDurationMinutes, "Ramp-up", 0, 0)

        // Main run(s)
        for (runId in 0 until testCase.duration.mainDurationRepeatCount) {
            assignTasks(testCase.duration.mainDurationMinutes, "Main", runId, testCase.duration.mainDurationRepeatCount-1)
        }
    }
}

/*-------------------------------------------------------------------------*/

fun initTulip() {
    println("Tulip (${System.getProperty("java.vendor")} ${System.getProperty("java.runtime.version")}, Kotlin ${KotlinVersion.CURRENT})")
}

/*-------------------------------------------------------------------------*/

fun runTulip(contextId: Int, context: RuntimeContext, tests: List<TestProfile>, getUser: (Int) -> User, getTest: (RuntimeContext, TestProfile) -> TestProfile) {
    println("")

    runtimeInit(contextId, context, tests, getUser)

    println("======================================================================")
    println("Scenario: ${context.name}")
    println("======================================================================")
    println("")
    println("NUM_USERS = ${MAX_NUM_USERS}")
    println("NUM_THREADS = ${MAX_NUM_THREADS}")
    println("NUM_USERS_PER_THREAD = ${MAX_NUM_USERS / MAX_NUM_THREADS}")
    if ((MAX_NUM_USERS / MAX_NUM_THREADS) * MAX_NUM_THREADS != MAX_NUM_USERS) {
        println("")
        println("NUM_USERS should equal n*NUM_THREADS, where n >= 1")
        System.exit(0)
    }
    testSuite!!.forEachIndexed { indexTestCase, testCase ->
        if (testCase.isEnabled) {
            val x: TestProfile = getTest(context, testCase)
            x.queueLenghts.forEachIndexed { indexUserProfile, queueLength ->
                delay(5000)
                runTest(x, contextId, indexTestCase, indexUserProfile, queueLength)
            }
        }
    }

    runtimeDone()
}

/*-------------------------------------------------------------------------*/

fun runTests(contexts: List<RuntimeContext>, tests: List<TestProfile>, getUser: (Int) -> User) {
    runTests(contexts, tests, getUser, ::getTest)
}

fun runTests(contexts: List<RuntimeContext>, tests: List<TestProfile>, getUser: (Int) -> User, getTest: (RuntimeContext, TestProfile) -> TestProfile) {
    initTulip()
    contexts.forEachIndexed { contextId, context ->
        runTulip(contextId, context, tests, getUser, getTest)
    }
}

/*-------------------------------------------------------------------------*/
