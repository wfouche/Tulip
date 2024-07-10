/*-------------------------------------------------------------------------*/

package tulip

/*-------------------------------------------------------------------------*/

import java.lang.management.ManagementFactory
import java.util.concurrent.ThreadLocalRandom
import javax.management.Attribute
import javax.management.ObjectName

/*-------------------------------------------------------------------------*/

//
// Use this function to calculate elapsed time in nanoseconds.
//
inline fun elapsedTimeNanos(block: () -> Unit): Long {
    val start = System.nanoTime()
    block()
    return System.nanoTime() - start
}

/*-------------------------------------------------------------------------*/

fun delayMillisRandom(delayFrom: Long, delayTo: Long) {
    require(delayFrom >= 0) { "delayFrom must be non-negative, is $delayFrom" }
    require(delayTo >= 0) { "delayTo must be non-negative, is $delayTo" }
    require(delayFrom < delayTo) { "delayFrom must be smaller than delayTo, but $delayFrom >= $delayTo"}
    val delayMillis = ThreadLocalRandom.current().nextLong(delayTo - delayFrom + 1) + delayFrom
    Thread.sleep(delayMillis)
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