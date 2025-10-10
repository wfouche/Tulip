package org.example.user

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import io.github.wfouche.tulip.api.TulipUser
import java.util.concurrent.ThreadLocalRandom

import io.github.wfouche.tulip.stats.LlqHistogram
import org.HdrHistogram.Histogram as HdrHistogram

class DemoUser(userId: Int, threadId: Int) : TulipUser(userId, threadId) {

    val llqh = LlqHistogram()
    val hdrh = HdrHistogram(3)
    val rnd = ThreadLocalRandom.current()
    var num = 1450312123L

    override fun onStart(): Boolean {
        return true
    }

    override fun action1(): Boolean {
        Thread.sleep(10)
        return true
    }

    override fun action2(): Boolean {
        Thread.sleep(20)
        return true
    }

    override fun action3(): Boolean {
        return true
    }

    override fun action7(): Boolean {
        num = rnd.nextLong(1, 1_000_000_000L)
        return true
    }

    override fun action8(): Boolean {
        for (i in 1..1000) {
            hdrh.recordValue(num)
        }
        return true
    }

    override fun action9(): Boolean {
        for (i in 1..1000) {
            llqh.update(num)
        }
        return true
    }

    override fun onStop(): Boolean {
        return true
    }

    override fun logger(): Logger {
        return logger
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(DemoUser::class.java)
    }
}
