package org.tulip.pfsm

import org.tulip.core.Console
import java.util.concurrent.ThreadLocalRandom
import kotlin.system.exitProcess

data class Edge (
    val actionId: Int,
    val weight: Int
)

class PFSM() {

    private val matrix = Array(100, { i -> Array(1000, { j -> 0 }) })
    private var cid: Int = 0

    fun add (actionId: Int, eList: List<Edge>) {
        val pList = mutableListOf<Int>()
        for (e in eList) {
            for (i in 1..e.weight) {
                pList.add(e.actionId)
            }
        }
        pList.shuffle()
        if (pList.size != 1000) {
            Console.put("error: PFSM:add - probability list size != 1000")
            Console.put("actionId = ${actionId}")
            Console.put("eList = ${eList.toString()}")
            exitProcess(1)
        }
        for (i in 1..1000) {
            matrix[actionId][i-1] = pList[i-1]
        }
    }

    fun next(): Int {
        val idx = ThreadLocalRandom.current().nextInt(1000)
        cid = matrix[cid][idx]
        if (cid == 0) {
            cid = matrix[cid][idx]
        }
        return cid
    }

    fun reset() {
        cid = 0
    }

}
