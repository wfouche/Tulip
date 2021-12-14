/*-------------------------------------------------------------------------*/

package tulip

/*-------------------------------------------------------------------------*/

import java.lang.management.ManagementFactory
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import javax.management.Attribute
import javax.management.ObjectName

import tulip.RuntimeContext
import tulip.TestProfile

/*-------------------------------------------------------------------------*/

// Round value x to the nearest multiple of n.
fun roundXN(x: Long, n: Long): Long {
    return if (x >= 0)
        ((x + n / 2.0) / n).toLong() * n
    else
        ((x - n / 2.0) / n).toLong() * n
}

/*-------------------------------------------------------------------------*/

// Log Linear Quantization function
fun llq(x: Long): Long {
    // Do not quantize the values from 0 to 9.
    if (x < 10) return x
    val a: Double = Math.floor(Math.log10(x.toDouble())) - 1.0
    val n: Long = (Math.pow(10.0, a)).toLong() * 5
    return roundXN(x, n)
}

/*-------------------------------------------------------------------------*/

//
// Use this monotonically increasing function to get a millisecond accurate timestamp.
//
fun timeMillis(): Long {
    return TimeUnit.NANOSECONDS.toMillis(timeNanos())
}

//
// Use this monotonically increasing function to get a microsecond accurate timestamp.
//
fun timeMicros(): Long {
    return TimeUnit.NANOSECONDS.toMicros(timeNanos())
}

//
// Use this monotonically increasing function to get a nanosecond accurate timestamp.
//
inline fun timeNanos(): Long {
    return System.nanoTime()
}

/*-------------------------------------------------------------------------*/

//
// Use this function to calculate elapsed time in milliseconds.
//
inline fun elapsedTimeMillis(block: () -> Unit): Long {
    val start = timeMillis()
    block()
    return timeMillis() - start
}

//
// Use this function to calculate elapsed time in microseconds.
//
inline fun elapsedTimeMicros(block: () -> Unit): Long {
    val start = timeMicros()
    block()
    return timeMicros() - start
}

//
// Use this function to calculate elapsed time in nanoseconds.
//
inline fun elapsedTimeNanos(block: () -> Unit): Long {
    val start = timeNanos()
    block()
    return timeNanos() - start
}

/*-------------------------------------------------------------------------*/

//
// It is worth reading the article on Accuracy vs Precision
// at https://en.wikipedia.org/wiki/Accuracy_and_precision
//
private fun measureTimeAccuracy(time: () -> Long): Long {
    val a = LongArray(1000)
    var z: Long
    var y: Long

    // warm-up
    repeat(10_000_000) {
        time()
    }

    // timing
    y = time()
    repeat(a.size) {
        z = y
        while (z == y) {
            y = time()
        }
        a[it] = y - z
    }

    // sort in increasing order.
    a.sort()

    // return smallest element.
    return a[0]
}

/*-------------------------------------------------------------------------*/

fun accuracyTimeMillis(): Long {
    return measureTimeAccuracy(::timeMillis)
}

fun accuracyTimeMicros(): Long {
    return measureTimeAccuracy(::timeMicros)
}

fun accuracyTimeNanos(): Long {
    return measureTimeAccuracy(::timeNanos)
}

/*-------------------------------------------------------------------------*/

fun accuracySystemCurrentTimeMillis(): Long {
    fun System_currentTimeMillis(): Long {
        return System.currentTimeMillis()
    }
    return measureTimeAccuracy(::System_currentTimeMillis)
}

fun accuracySystemNanoTime(): Long {
    fun System_nanoTime(): Long {
        return System.nanoTime()
    }
    return measureTimeAccuracy(::System_nanoTime)
}

/*-------------------------------------------------------------------------*/

//
// Delay function to workaround Thread.sleep issue on Windows.
// Only ever delay for a value that is a multiple of 10 milliseconds (ms).
//
fun delay(delayMillis: Long) {
    // round delay to the nearest value of 10.
    // 11 -> 10, ..., 14 -> 10,  15 -> 20, etc.
    Thread.sleep(roundXN(delayMillis, 10))
}

/*-------------------------------------------------------------------------*/

// Burn CPU cycles for 'delay' milliseconds
fun delayMillis(delay: Long) {
    val end = timeMicros() + delay * 1000
    while (timeMicros() < end) {
        // pass
    }
}

// Burn CPU cycles for 'delay' microseconds
fun delayMicros(delay: Long) {
    val end = timeNanos() + delay * 1000
    while (timeNanos() < end) {
        // pass
    }
}

/*-------------------------------------------------------------------------*/

fun delayMillisRandom(delayFrom: Long, delayTo: Long) {
    val delay = ThreadLocalRandom.current().nextLong(delayTo - delayFrom + 1) + delayFrom
    delayMillis(delay)
}

fun delayMicrosRandom(delayFrom: Long, delayTo: Long) {
    val delay = ThreadLocalRandom.current().nextLong(delayTo - delayFrom + 1) + delayFrom
    delayMicros(delay)
}

/*-------------------------------------------------------------------------*/

fun getLoadValue(counter: String): Double {
    val mbs = ManagementFactory.getPlatformMBeanServer()
    val name = ObjectName.getInstance("java.lang:type=OperatingSystem")
    val list = mbs.getAttributes(name, arrayOf(counter.toString()))

    if (list.isEmpty()) return java.lang.Double.NaN

    val att = list[0] as Attribute
    val value = att.getValue() as Double

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
    test.queueLenghts.forEach { queueLength ->
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

fun getTest(context: RuntimeContext, test: TestProfile): TestProfile {
    return test.copy(queueLenghts = getQueueLengths(context, test))
}

/*-------------------------------------------------------------------------*/

fun durationMillisToString(durationInMillis: Long): String {
    val seconds = durationInMillis / 1000
    val s = seconds % 60
    val m = (seconds / 60) % 60
    val h = (seconds / (60 * 60)) % 24
    val q = durationInMillis % 1000
    return String.format("%d:%02d:%02d.%03d",h,m,s,q)
}

/*-------------------------------------------------------------------------*/
