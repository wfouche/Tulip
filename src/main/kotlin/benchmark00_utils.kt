/*-------------------------------------------------------------------------*/

import tulip.RuntimeContext
import tulip.TestProfile

/*-------------------------------------------------------------------------*/

fun getQueueLengths(context: RuntimeContext, test: TestProfile): List<Int> {
    val list: MutableList<Int> = mutableListOf()
    test.queueLenghts.forEach { queueLength ->
        list.add (when(queueLength) {
            0 -> context.numThreads
            -1 -> context.numThreads*10
            else -> queueLength
        })
    }
    return list
}

/*-------------------------------------------------------------------------*/
