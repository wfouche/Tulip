package io.github.wfouche.tulip.core

import io.github.wfouche.tulip.api.TulipUser
import org.HdrHistogram.IntCountsHistogram

var userPlatformThreads: Array<UserThread?>? = null // arrayOfNulls<UserThread>(NUM_THREADS)
var userObjects: Array<TulipUser?>? = null // arrayOfNulls<User>(NUM_USERS)

const val USER_THREAD_QSIZE = 11

class UserThread(private val threadId: Int) : Thread() {

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
                    u = newUser!!.getUser(g_config.actions.userClass, task.userId, threadId)
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
                    if (u.processAction(task.actionId)) {
                        task.status = 1
                    } else {
                        task.status = 0
                    }
                }
                task.rspQueue!!.put(task)
            }
        }
        userPlatformThreads!![threadId] = null
    }
}

val wthread_queue_stats = IntCountsHistogram(histogramNumberOfSignificantValueDigits)

fun assignTaskToUser(task: Task) {
    val threadId = task.userId / (task.numUsers / task.numThreads)
    var w = userPlatformThreads!![threadId]
    if (w == null) {
        w =
            UserThread(threadId).apply {
                isDaemon = true
                start()
            }
        userPlatformThreads!![threadId] = w
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

fun runtimeDone() {
    // Terminate all user threads.
    userPlatformThreads!!.forEach { userThread -> userThread!!.tq.put(Task(status = 999)) }

    // Wait for all user threads to exit.
    while (userPlatformThreads!!.map { if (it == null) 0 else 1 }.sum() > 0) {
        Thread.sleep(500)
    }
}

// https://ericnormand.me/guide/clojure-virtual-threads
// - Don't access synchronized blocks or methods