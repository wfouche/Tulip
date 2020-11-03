/*-------------------------------------------------------------------------*/

import java.util.concurrent.TimeUnit
import java.lang.System

/*-------------------------------------------------------------------------*/

// 
// Program output:
// 
// 1 1 1 1 1 1 1 1 1 1
// Timing accuracy: timeMillis()               : milliseconds = 1
// 
// 1 1 1 1 1 1 1 1 2 42
// Timing accuracy: timeMicros()               : milliseconds = 1
// 
// 100 100 100 100 100 100 100 100 4000 42100
// Timing accuracy: timeNanos()                : milliseconds = 100
// 
// 15 15 15 15 16 16 16 16 16 16
// Timing accuracy: System.currentTimeMillis() : milliseconds = 15
// 

/*-------------------------------------------------------------------------*/

//
// It is worth reading the article on Accuracy vs Precision
// at https://en.wikipedia.org/wiki/Accuracy_and_precision
//
private fun measureTimeAccuracy(time: () -> Long, arraySize: Int = 1000, arrayPrint: Boolean = false): Long {
    val a = LongArray(arraySize)
    var z: Long
    var y: Long = 0

    // warm-up
    repeat(10_000_000) {
        y = time()
    }

    // timing
    repeat(a.size) {
        z = y
        while (z == y) {
            y = time()
        }
        a[it] = y - z
    }

    // sort in increasing order.
    a.sort()
	
	if (arrayPrint) {
	    repeat(a.size) {
		    print("${a[it]} ")
		}
		println("")
	}

    // return smallest element.
    return a[0]
}

/*-------------------------------------------------------------------------*/

fun timeMillis():Long {
   return TimeUnit.NANOSECONDS.toMillis(timeNanos())
}

fun timeMicros():Long {
    return TimeUnit.NANOSECONDS.toMicros(timeNanos())
}

fun timeNanos():Long {
    return System.nanoTime()
}

fun System_currentTimeMillis(): Long {
    return System.currentTimeMillis()
}

/*-------------------------------------------------------------------------*/

fun accuracyTimeMillis(): Long {
    return measureTimeAccuracy(::timeMillis, 10, true)
}

fun accuracyTimeMicros(): Long {
    return measureTimeAccuracy(::timeMicros, 10, true)
}

fun accuracyTimeNanos(): Long {
    return measureTimeAccuracy(::timeNanos, 10, true)
}

fun accuracySystemCurrentTimeMillis(): Long {
    return measureTimeAccuracy(::System_currentTimeMillis, 10, true)
}

/*-------------------------------------------------------------------------*/

fun main(args:Array<String>) {
    println("Timing accuracy: timeMillis()               : milliseconds = ${accuracyTimeMillis()}\n")
    println("Timing accuracy: timeMicros()               : milliseconds = ${accuracyTimeMicros()}\n")
    println("Timing accuracy: timeNanos()                : milliseconds = ${accuracyTimeNanos()}\n")
    println("Timing accuracy: System.currentTimeMillis() : milliseconds = ${accuracySystemCurrentTimeMillis()}\n")
}