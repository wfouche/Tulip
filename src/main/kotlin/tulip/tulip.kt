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
    val percentiles: List<Double> = listOf(50.0, 90.0, 95.0, 99.0)
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
                if (u.processAction(task.actionId)) task.status=1 else task.status=0
            }

            task.rspQueue!!.put(task)
        }
    }
}

/*-------------------------------------------------------------------------*/

object Console : Thread() {

    init {
        //priority = Thread.MAX_PRIORITY
        isDaemon = true
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

fun runTest(testCase: TestCase, indexTestCase: Int, indexUserProfile: Int, activeUsers: Int) {
    val output = mutableListOf("")
    output.add("======================================================================")
    output.add("= [${indexTestCase}][${indexUserProfile}][${activeUsers}] ${testCase.name} - ${java.time.LocalDateTime.now()}")
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
    for (action: Action in testCase.actions) {
        wSum += action.weight
        aCount += 1
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
    repeat(NUM_USERS) {
        if ((testCase.rampDurationMinutes == 0) && (testCase.mainDurationMinutes == 0)) {
            userActions[it] = null
        } else {
            userActions[it] = createActionsGenerator(actionList)
        }
    }

    //
    // Create a queue containing a total of NUM_ACTIVE_USERS tokens.
    //
    val NUM_ACTIVE_USERS: Int = if (activeUsers == 0) 10 * NUM_THREADS else activeUsers

    val rspQueue = Queue<Task>(NUM_ACTIVE_USERS)

    fun initRspQueue() {
        repeat(NUM_ACTIVE_USERS) {
            rspQueue.put(Task())
        }
    }

    fun drainRspQueue() {
        repeat(NUM_ACTIVE_USERS) {
            val task: Task = rspQueue.take()
            DataCollector.updateStats(task)
        }
    }

    if ((testCase.rampDurationMinutes == 0) && (testCase.mainDurationMinutes == 0)) {
        DataCollector.clearStats()

        // Special bootstrap test case to initialize terminals, and other objects.
        // Typically only found at the start and end of a test suite.
        var rateGoverner: RateGovernor? = null
        if (testCase.arrivalRate > 0.0) {
            rateGoverner = RateGovernor(timeMillis(), testCase.arrivalRate)
        }

        initRspQueue()

        val duration_millis: Int
        val timeMillis_start: Long = timeMillis()
        for (aid in actionList) {
            for (uid in userList) {
                // Limit the number of active users.
                val task: Task = rspQueue.take()
                DataCollector.updateStats(task)

                // Assign the task to the user object.
                task.apply {
                    userId = uid; numUsers = NUM_USERS; numThreads = NUM_THREADS; actionId = aid; this.rspQueue = rspQueue
                }
                assignTask(task)

                // Limit the throughput rate , if required.
                rateGoverner?.pace()
            }
        }
        drainRspQueue()
        duration_millis = (timeMillis() - timeMillis_start).toInt()
        DataCollector.createSummary(duration_millis, testCase)
        DataCollector.printStats(false)
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
                if (testCase.arrivalRate > 0.0) {
                    rateGoverner = RateGovernor(timeMillis_start, testCase.arrivalRate)
                }
            }

            while (timeMillis() < timeMillis_end) {
                // Pick a random user object to assign a task to.
                //val uid = rnd.nextInt(NUM_USERS)  // 0 until NUM_USERS
                val uid = userList.random()

                // Pick the next task for the user object to execute.
                val aid: Int = userActions[uid]!!.next()

                // Limit the number of active users.
                val task: Task = rspQueue.take()
                DataCollector.updateStats(task)

                // Assign the task to the user object.
                task.apply {
                    userId = uid; numUsers = NUM_USERS; numThreads = NUM_THREADS; actionId = aid; this.rspQueue = rspQueue
                }
                assignTask(task)

                // Limit the throughput rate , if required.
                rateGoverner?.pace()
            }
            drainRspQueue()
            val duration_millis: Int = (timeMillis() - timeMillis_start).toInt()
            Console.put("${name} run ${runId}: end   (${java.time.LocalDateTime.now()})")

            DataCollector.createSummary(duration_millis, testCase)
            DataCollector.printStats(false)
        }

        if (indexUserProfile == 0) {
            assignTasks(testCase.warmDurationMinutes, "Warm-up", 0, 0.0)
        }

        assignTasks(testCase.rampDurationMinutes, "Ramp-up", 0)

        for (runId in 0 until testCase.repeatCount) {
            assignTasks(testCase.mainDurationMinutes, "Main", runId)
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