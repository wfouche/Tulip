package org.example.user

/*-------------------------------------------------------------------------*/

import io.github.wfouche.tulip.api.TulipUtils
import io.github.wfouche.tulip.user.HttpUser
import java.util.concurrent.ThreadLocalRandom
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/*-------------------------------------------------------------------------*/

class TestHttpUser(userId: Int, threadId: Int) : HttpUser(userId, threadId) {

    // ----------------------------------------------------------------- //

    override fun onStart(): Boolean {
        if (userId == 0) {
            logger.info("Kotlin")
            super.onStart()
            // not required, just for testing
            val s: String = getActionName(0);
        }
        return true
    }

    // ----------------------------------------------------------------- //

    // 0.25*6 + 0.75*14 = 12.0 ms

    override fun action1(): Boolean {
        // 6 ms delay (average)
        TulipUtils.delayMillisRandom(0, 12)
        return true
    }

    override fun action2(): Boolean {
        // 14 ms delay (average)
        TulipUtils.delayMillisRandom(0, 28)
        return true
    }

    // ----------------------------------------------------------------- //

    override fun action3(): Boolean {
        val id: Int = ThreadLocalRandom.current().nextInt(100)+1
        return !http_GET("/posts/{id}", id).isEmpty()
    }

    override fun action4(): Boolean {
        val id: Int = ThreadLocalRandom.current().nextInt(500)+1
        return !http_GET("/comments/{id}", id).isEmpty()
    }

    override fun action5(): Boolean {
        val id: Int = ThreadLocalRandom.current().nextInt(100)+1
        return !http_GET("/albums/{id}", id).isEmpty()
    }

    override fun action6(): Boolean {
        val id: Int = ThreadLocalRandom.current().nextInt(5000)+1
        return !http_GET("/photos/{id}", id).isEmpty()
    }

    override fun action7(): Boolean {
        val id: Int = ThreadLocalRandom.current().nextInt(200)+1
        return !http_GET("/todos/{id}", id).isEmpty()
    }

    // ----------------------------------------------------------------- //

    override fun action8(): Boolean {
        return true
    }

    // ----------------------------------------------------------------- //

    override fun action9(): Boolean {
        return true
    }

    // ----------------------------------------------------------------- //

    override fun action10(): Boolean {
        TulipUtils.delayMillisFixed(10)
        return true
    }

    // ----------------------------------------------------------------- //

    override fun onStop(): Boolean {
        logger.info("  Terminate: UserId = $userId")
        Thread.sleep(100)
        return true
    }

    // ----------------------------------------------------------------- //

    override fun logger(): Logger {
        return logger
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(TestHttpUser::class.java)
    }

}

/*-------------------------------------------------------------------------*/
