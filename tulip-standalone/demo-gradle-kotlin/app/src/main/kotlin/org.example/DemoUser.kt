package org.example

import io.github.wfouche.tulip.api.TulipUser
import io.github.wfouche.tulip.api.TulipConsole

class DemoUser(userId: Int, threadId: Int) : TulipUser(userId, threadId) {

    override fun onStart(): Boolean {
        TulipConsole.put("KotlinDemoUser " + userId)
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

    override fun action3(): Boolean = true

    override fun onStop(): Boolean = true

}
