/*-------------------------------------------------------------------------*/

package tulip

/*-------------------------------------------------------------------------*/

import java.util.concurrent.ArrayBlockingQueue  as Queue
import java.util.concurrent.LinkedBlockingQueue

import java.lang.Thread
import java.util.concurrent.ThreadLocalRandom

import kotlin.sequences.iterator

import java.util.Locale

/*-------------------------------------------------------------------------*/

//
// A reference to the current TestCase being executed.
//
var mainTestCase = TestCase(actions = listOf(Action(0)))

//
// Arrays of user objects and user actions.
//

val userObjects = arrayOfNulls<User>(NUM_USERS)
val userActions = arrayOfNulls<Iterator<Int>>(NUM_USERS)

//
// Array of Worker thread objects of a concrete type.
//
var userThreads = arrayOfNulls<UserThread>(NUM_THREADS)

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

data class TestCase(
    //
    // Name of the benchmark test.
    //
    val name: String = "",

    // Warm-up period in minutes.
    // The results from this period are discarded.
    //
    // Warm-up only executed once per TestCase.
    //
    val warmDurationMinutes: Int = 0,

    // Ramp-up period in minutes.
    // The results from this period are discarded.
    //
    // Ramp-up executed once per every iteration of TestCase.
    //
    val rampDurationMinutes: Int = 0,

    // Main duration in minutes.
    // The results from this period are reported.
    //
    // Main executed once per every iteration of TestCase.
    //
    val mainDurationMinutes: Int = 0,

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
    // This value represents the "L" in Little's Law (equation)
    //
    val userProfile: List<Int> = listOf(0),

    // Repeat a benchmark test this number of times
    val repeatCount: Int = 1,

    // List of percentile values to report on.
    val percentiles: List<Double> = listOf(90.0, 95.0, 99.0)
)

/*-------------------------------------------------------------------------*/

//
// Task data class. Tasks are created be the main thread and send to User objects to perform known actions.
//
data class Task(

    // The user ID of the user object to which an operation should be applied.
    val userId: Int,

    // Total number of user objects.
    val numUsers: Int,

    // Total number of work threads servicing user objects.
    val numThreads: Int,

    // Numeric id of the action (operation) to be invoked on a user object.
    val actionId: Int,

    // Duration (elapsed time) in microseconds.
    var durationMicros: Long = 0,

    var success: Boolean = false,

    var rspQueue: Queue<Int>? = null
)

/*-------------------------------------------------------------------------*/

class RateGovernor(val timeMillis_start: Long, val averageRate: Double) {

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

class UserThread(val threadId: Int) : Thread() {

    //
    // Task Queue - input queue with tasks for this thread to complete.
    //
    var tq = Queue<Task>(10)

    override fun run() {
        while (true) {
            //
            // Wait for a new task to be assigned to this thread.
            //
            val task: Task = tq.take()

            //
            // Locate the user object to which the task should be applied.
            // Dynamically create a new user object, if required.
            //
            var u = userObjects[task.userId]
            if (u == null) {
                u = newUser(task.userId)
                userObjects[task.userId] = u
            }

            //
            // Apply the task to the user object. The return value is either
            // True or False indicating that the task succeeded or failed.
            // Also calculate the elapsed time in microseconds.
            //
            task.durationMicros = elapsedTimeMicros {
                task.success = u.processAction(task.actionId)
            }

            //
            // Forward response time data for this Action ID to the DataCollector
            // thread to keep track of response times.
            //
            DataCollector.put(task)
        }
    }
}

/*-------------------------------------------------------------------------*/

object Console : Thread() {

    init {
        isDaemon = true
        //priority = Thread.MAX_PRIORITY
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
        val list = listOf(s)
        put(list.toMutableList())
    }

    fun put(list: MutableList<String>) {
        q.put(list)
    }
}

/*-------------------------------------------------------------------------*/

object CpuLoadMetrics : Thread() {

    init {
        isDaemon = true
        //start()
    }

    val systemCpuStats = Queue<Double>(1000)
    val processCpuStats = Queue<Double>(1000)

    override fun run() {
        var timeMillis_next: Long = timeMillis()
        var i = 0
        var total_cpu_system: Double = 0.0
        var total_cpu_process: Double = 0.0
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

object DataCollector : Thread() {

    init {
        isDaemon = true
        start()
    }

    // Input queue for the Data Collector thread.
    private var dcq = Queue<Task>(10000)

    // Queue used to block the main thread on while the DataCollector thread
    // prints the results of a TestCase to the console.
    private var handshakeQueue = LinkedBlockingQueue<Int>()

    fun clearStats() {
        dcq.put(Task(userId = -1, numUsers = 0, numThreads = 0, actionId = 0))
    }

    fun printStats(num_actions: Int, duration_millis: Int, printMap: Boolean) {
        val numUsers = if (printMap) 1 else 0

        dcq.put(Task(userId = -2, numUsers = numUsers, numThreads = num_actions, actionId = duration_millis))

        handshakeQueue.take()
    }

    fun put(task: Task) {
        dcq.put(task)
    }

    override fun run() {
        // frequency map: count the number of times a given (key) response time occurred.
        // key = response time in microseconds (NOT milliseconds).
        // value = the number of times a given (key) response time occurred.
        val latencyMap = mutableMapOf<Long, Long>()
        var latencyMap_min_rt: Long = Long.MAX_VALUE
        var latencyMap_max_rt: Long = Long.MIN_VALUE
        var latencyMap_max_ts = ""

        fun saveStats(num_actions: Int, duration_millis: Int, printMap: Boolean) {
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

            output.add("  average number of actions completed per second = ${"%.3f".format(Locale.US, aps)}")
            output.add("  average duration/response time in milliseconds = ${"%.3f".format(Locale.US, art)}")
            output.add("  standard deviation  (response time)  (millis)  = ${"%.3f".format(Locale.US, sdev)}")
            output.add("")
            output.add("  duration of benchmark (in seconds) = ${duration_seconds}")
            output.add("  number of actions completed = ${num_actions}")

            output.add("")

            val percentiles = mainTestCase.percentiles
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

            handshakeQueue.put(0)
        }

        while (true) {

            var shouldPrintStats = false

            var num_actions: Int = 0
            var duration_millis: Int = 0
            var print_map: Boolean = false

            while (true) {

                val task: Task = dcq.take()
                if (task.userId < 0) {
                    if (task.userId == -2) {
                        shouldPrintStats = true
                        num_actions = task.numThreads
                        duration_millis = task.actionId
                        print_map = task.numUsers == 1
                    }
                    break
                }
                val durationMicros = task.durationMicros

                //
                // Limit the number of active users.
                //
                task.rspQueue?.put(if (task.success) 1 else 0)

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
            }
            if (shouldPrintStats) {
                saveStats(num_actions, duration_millis, print_map)
            }
            latencyMap.clear()
            latencyMap_min_rt = Long.MAX_VALUE
            latencyMap_max_rt = Long.MIN_VALUE
            latencyMap_max_ts = ""
        }
    }
}

/*-------------------------------------------------------------------------*/

fun assignTask(task: Task) {
    val threadId = task.userId / (task.numUsers / task.numThreads)
    var w = userThreads[threadId]
    if (w == null) {
        w = UserThread(threadId).apply {
            isDaemon = true
            start()
        }
        userThreads[threadId] = w
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

fun runTest(test: TestCase, indexTestCase: Int, indexUserProfile: Int, activeUsers: Int) {
    mainTestCase = test

    val output = mutableListOf("")
    output.add("======================================================================")
    output.add("= [${indexTestCase}][${indexUserProfile}][${activeUsers}] ${test.name} - ${java.time.LocalDateTime.now()}")
    output.add("======================================================================")
    Console.put(output)

    val rnd = ThreadLocalRandom.current()

    // create a list of randomized user IDs
    val userList = mutableListOf<Int>()
    repeat(NUM_USERS) {
        userList.add(it)
    }
    userList.shuffle()

    // Create a list of actions (per user).
    // If all the weights sum to zero, we should
    // treat the list of actions as a workflow.
    val actionList = mutableListOf<Int>()
    var wSum: Int = 0
    var aCount: Int = 0
    for (action: Action in test.actions) {
        wSum += action.weight
        aCount += 1
    }
    if (wSum == 0) {
        for (action in test.actions) {
            actionList.add(action.actionId)
        }
    } else {
        for (action in test.actions) {
            repeat(action.weight) {
                actionList.add(action.actionId)
            }
        }
        actionList.shuffle(rnd)
    }
    repeat(NUM_USERS) {
        if ((test.rampDurationMinutes == 0) && (test.mainDurationMinutes == 0)) {
            userActions[it] = null
        } else {
            userActions[it] = createActionsGenerator(actionList)
        }
    }

    //
    // Create a queue containing a total of NUM_ACTIVE_USERS tokens.
    //
    val NUM_ACTIVE_USERS: Int = if (activeUsers == 0) 10 * NUM_THREADS else activeUsers

    val rspQueue = Queue<Int>(NUM_ACTIVE_USERS)

    fun initRspQueue() {
        repeat(NUM_ACTIVE_USERS) {
            rspQueue.put(-1)
        }
    }

    fun drainRspQueue(): Int {
        var num_success: Int = 0
        repeat(NUM_ACTIVE_USERS) {
            val rc: Int = rspQueue.take()
            num_success += if (rc < 0) 0 else rc
        }
        return num_success
    }

    if ((test.rampDurationMinutes == 0) && (test.mainDurationMinutes == 0)) {
        DataCollector.clearStats()

        // Special bootstrap test case to initialize terminals, and other objects.
        // Typically only found at the start and end of a test suite.
        var rateGoverner: RateGovernor? = null
        if (test.arrivalRate > 0.0) {
            rateGoverner = RateGovernor(timeMillis(), test.arrivalRate)
        }

        initRspQueue()

        var num_actions: Int = 0
        var num_success: Int = 0
        val duration_millis: Int
        val timeMillis_start: Long = timeMillis()
        for (aid in actionList) {
            for (uid in userList) {
                // Limit the number of active users.
                val rc: Int = rspQueue.take()
                num_success += if (rc < 0) 0 else rc

                // Assign the task to the user object.
                assignTask(Task(userId = uid, numUsers = NUM_USERS, numThreads = NUM_THREADS, actionId = aid, rspQueue = rspQueue))

                // Limit the throughput rate , if required.
                rateGoverner?.pace()

                num_actions += 1
            }
        }
        num_success += drainRspQueue()
        duration_millis = (timeMillis() - timeMillis_start).toInt()
        DataCollector.printStats(num_actions, duration_millis, true)
    } else {
        // Normal test case.
        var timeMillis_start: Long
        var timeMillis_end: Long = timeMillis()

        fun assignTasks(durationMinutes: Int, name: String, runId:Int, arrivalRate: Double = -1.0) {
            if (durationMinutes == 0) {
                return
            }
            initRspQueue()

            DataCollector.clearStats()
            val ts1 = java.time.LocalDateTime.now()
            Console.put("\n${name} run ${runId}: begin (${ts1})")

            timeMillis_start = timeMillis_end
            timeMillis_end = timeMillis_start + durationMinutes * 60 * 1000

            var rateGoverner: RateGovernor? = null
            if (arrivalRate > -1.0) {
                // Warm-up duration at max speed, ungoverned.
            }
            else {
                // Ramp-up or Main duration.
                if (test.arrivalRate > 0.0) {
                    rateGoverner = RateGovernor(timeMillis_start, test.arrivalRate)
                }
            }

            var num_actions: Int = 0
            var num_success: Int = 0

            while (timeMillis() < timeMillis_end) {
                // Pick a random user object to assign a task to.
                //val uid = rnd.nextInt(NUM_USERS)  // 0 until NUM_USERS
                val uid = userList.random()

                // Pick the next task for the user object to execute.
                val aid: Int = userActions[uid]!!.next()

                // Limit the number of active users.
                val rc: Int = rspQueue.take()
                num_success += if (rc < 0) 0 else rc

                // Assign the task to the user object.
                assignTask(Task(userId = uid, numUsers = NUM_USERS, numThreads = NUM_THREADS, actionId = aid, rspQueue = rspQueue))

                // Limit the throughput rate , if required.
                rateGoverner?.pace()

                num_actions += 1
            }
            num_success += drainRspQueue()
            val duration_millis: Int = (timeMillis() - timeMillis_start).toInt()
            Console.put("${name} run ${runId}: end   (${java.time.LocalDateTime.now()})")

            Console.put("")
            Console.put("  num_actions = ${num_actions}")
            Console.put("  num_success = ${num_success}")
            Console.put("  num_failed  = ${num_actions - num_success}")

            DataCollector.printStats(num_actions, duration_millis, false)
        }
        if (indexUserProfile == 0) {
            assignTasks(test.warmDurationMinutes, "Warm-up", 0, 0.0)
        }
        assignTasks(test.rampDurationMinutes, "Ramp-up", 0)
        for (runId in 0 until test.repeatCount) {
            assignTasks(test.mainDurationMinutes, "Main", runId)
        }
    }
}

/*-------------------------------------------------------------------------*/

fun initTulip() {
    println("NUM_USERS = ${NUM_USERS}")
    println("NUM_THREADS = ${NUM_THREADS}")
    println("NUM_USERS_PER_THREAD = ${NUM_USERS / NUM_THREADS}")
    if ((NUM_USERS / NUM_THREADS) * NUM_THREADS != NUM_USERS) {
        println("")
        println("NUM_USERS should equal n*NUM_THREADS, where n >= 1")
        System.exit(0)
    }
    while (getProcessCpuLoad().isNaN()) {
        delay(250)
    }
    while (getSystemCpuLoad().isNaN()) {
        delay(250)
    }
    CpuLoadMetrics.start()
}

/*-------------------------------------------------------------------------*/

fun runTulip() {
    println("Tulip (${java.lang.System.getProperty("java.vendor")}, ${java.lang.System.getProperty("java.runtime.version")})\n")
    initTulip()
    initTestSuite()
    testSuite.forEachIndexed { indexTestCase, testCase ->
        testCase.userProfile.forEachIndexed { indexUserProfile, activeUsers ->
            delay(5000)
            runTest(testCase, indexTestCase, indexUserProfile, activeUsers)
        }
    }
}

/*-------------------------------------------------------------------------*/